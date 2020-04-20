package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.HistoryInfoBean;

public class HistoryView extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 60000;

    private static HistoryView instance;

    private int pageSize = 32;
    private RpcProxy<PagingLoadResult<HistoryInfoBean>> hProxy;
    private BasePagingLoader<PagingLoadResult<HistoryInfoBean>> hLoader;
    private ListStore<HistoryInfoBean> hStore;
    private Grid<HistoryInfoBean> grid;
    private DateTimeFormat formatter = DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss");
    
    private Timer updater = new Timer() {

        @Override
        public void run() {
            updateView();
        }

    };

    private boolean refreshPending = false;

    public void scheduleRefresh() {
        if (refreshPending) {
            updater.cancel();
        }

        updater.schedule(REFRESH_INTERVAL);
        refreshPending = true;
    }

    public void cancelRefresh() {
        updater.cancel();
        refreshPending = false;
    }

    private HistoryView() {
        hProxy = new RPCInvocation<PagingLoadResult<HistoryInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<PagingLoadResult<HistoryInfoBean>> callback) {
                remoteService.retrieveHistory((PagingLoadConfig) _loadConfig,
                        callback);
            }
        
            @Override
            public void  onComplete(PagingLoadResult<HistoryInfoBean> res){
                scheduleRefresh();
            }
        };
        hLoader = new BasePagingLoader<PagingLoadResult<HistoryInfoBean>>(
                hProxy);
        hStore = new ListStore<HistoryInfoBean>(hLoader);
    }

    public static HistoryView getInstance() {
        if (instance == null) {
            instance = new HistoryView();
        }
        return instance;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        PagingLinkPanel linkPanel = new PagingLinkPanel(pageSize);
        linkPanel.bind(hLoader);
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(new ColumnConfig("JobId", "JobId", 120));
        configs.add(new ColumnConfig("Feature", "Feature", 120));
        configs.add(new ColumnConfig("SubmitTime", "SumbmitTime", 120));
        configs.add(new ColumnConfig("EndTime", "EndTime", 120));
        configs.add(new ColumnConfig("Status", "Status", 40));
        configs.add(new ColumnConfig("Desc", "Description", 130));
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            configs.add(new ColumnConfig("User", "User", 60));
        }
        ColumnModel cm = new ColumnModel(configs);
        grid = new Grid<HistoryInfoBean>(hStore, cm);
        grid.setBorders(true);
        grid.setStyleAttribute("border", "3px inset #1d6689");
        grid.setAutoExpandColumn("Desc");
        grid.addStyleName("custom-grid-row-height");
        // grid.setStateful(true);
        grid.addListener(Events.Attach,
                new Listener<GridEvent<HistoryInfoBean>>() {

                    @Override
                    public void handleEvent(GridEvent<HistoryInfoBean> be) {
                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(pageSize);

                        Map<String, Object> state = grid.getState();
                        if (state.containsKey("offset")) {
                            int offset = (Integer) state.get("offset");
                            int limit = (Integer) state.get("limit");
                            config.setOffset(offset);
                            config.setLimit(limit);
                        }
                        if (state.containsKey("sortField")) {
                            config
                                    .setSortField((String) state
                                            .get("sortField"));
                            config.setSortDir(SortDir.valueOf((String) state
                                    .get("sortDir")));
                        }
                        hLoader.load(config);
                    }
                });
        
        grid.setModelProcessor(new ModelProcessor<HistoryInfoBean>() {

            @Override
            public HistoryInfoBean prepareData(HistoryInfoBean model) {
                Long startTime = model.get("SubmitTime");
                if (startTime != null) {
                    model.set("SubmitTime", formatter
                            .format(new Date(startTime)));
                }
                Long endTime = model.get("EndTime");
                if (endTime != null) {
                    model.set("EndTime", formatter
                            .format(new Date(endTime)));
                }

                return model;
            }

        });
        
        final GridSelectionModel<HistoryInfoBean> sm = new GridSelectionModel<HistoryInfoBean>();
        grid.setSelectionModel(sm);
        
        Menu menu = new Menu();
        menu.addListener(Events.BeforeShow, new Listener<MenuEvent>(){
            @Override
            public void handleEvent(MenuEvent be) {
                if(sm.getSelectedItem() instanceof HistoryInfoBean){
                     be.setCancelled(false);
                }
                else{
                    be.setCancelled(true);
                }
            }     
        });
        MenuItem openItem = new MenuItem("Open Job Log");
        openItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                String location = sm.getSelectedItem().getLogLocation();
                if(location != null)
                    Window.open(location + "/job.log", "_blank", "");
            }
        });
        menu.add(openItem);
        
        grid.setContextMenu(menu);
        
        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.setBottomComponent(linkPanel);
        _contentPanel.add(grid);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    @Override
    public void updateView() {
        hLoader.load();
    }

}
