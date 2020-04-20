package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.data.SortInfo;
import com.extjs.gxt.ui.client.data.TreeLoader;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.AgentInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobInfoBean;
import com.intel.cedar.service.client.feature.model.ProgressInfoBean;
import com.intel.cedar.service.client.feature.model.TaskletInfoBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.util.Util;

public class FeatureJobView extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 15000;
    private static FeatureJobView instance;
    private RpcProxy<List<ProgressInfoBean>> fProxy;
    private TreeLoader<ProgressInfoBean> fLoader;
    private TreeStore<ProgressInfoBean> fStore;
    private TreeGrid<ProgressInfoBean> fGrid;
    private DateTimeFormat formatter = DateTimeFormat.getFormat("HH:mm:ss");

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

    private FeatureJobView() {
        fProxy = new RPCInvocation<List<ProgressInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<List<ProgressInfoBean>> callback) {
                remoteService.retrieveJobList((ProgressInfoBean) _loadConfig,
                        callback);
            }
            
            public void onComplete(List<ProgressInfoBean> res) {
                scheduleRefresh();
            }
        };
        fLoader = new BaseTreeLoader<ProgressInfoBean>(fProxy) {
            public boolean hasChildren(ProgressInfoBean bean) {
                return !(bean instanceof AgentInfoBean);
            }
        };
        fStore = new TreeStore<ProgressInfoBean>(fLoader);
        fStore.setStoreSorter(new StoreSorter<ProgressInfoBean>() {
            public int compare(Store<ProgressInfoBean> store,
                    ProgressInfoBean m1, ProgressInfoBean m2, String property) {
                boolean f1 = m1 instanceof FeatureJobInfoBean;
                boolean f2 = m2 instanceof FeatureJobInfoBean;

                if (f1 && !f2) {
                    return -1;
                } else if (!f1 && f2) {
                    return 1;
                }

                return super.compare(store, m1, m2, property);
            }
        });
    }

    public static FeatureJobView getInstance() {
        if (instance == null) {
            instance = new FeatureJobView();
        }
        return instance;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        GridCellRenderer<ProgressInfoBean> pgBarRenderer = new GridCellRenderer<ProgressInfoBean>() {
            private boolean init;

            @Override
            public Object render(ProgressInfoBean model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<ProgressInfoBean> store,
                    Grid<ProgressInfoBean> grid) {
                if (!init) {
                    init = true;
                    grid.addListener(Events.ColumnResize,
                            new Listener<GridEvent<ProgressInfoBean>>() {

                                @Override
                                public void handleEvent(
                                        GridEvent<ProgressInfoBean> be) {
                                    for (int i = 0; i < be.getGrid().getStore()
                                            .getCount(); i++) {
                                        if (be.getGrid().getView().getWidget(i,
                                                be.getColIndex()) != null
                                                && be
                                                        .getGrid()
                                                        .getView()
                                                        .getWidget(
                                                                i,
                                                                be
                                                                        .getColIndex()) instanceof BoxComponent) {
                                            ((BoxComponent) be.getGrid()
                                                    .getView().getWidget(i,
                                                            be.getColIndex()))
                                                    .setWidth(be.getWidth() - 10);
                                        }
                                    }
                                }

                            });
                }
                Object m = model.get(property);
                if (m instanceof Double) {
                    Double d = (Double)m * 100;
                    String text = d.intValue() + "%";
                    ProgressBar pgBar = new ProgressBar();
                    pgBar.setWidth(grid.getColumnModel().getColumnWidth(
                            colIndex) - 10);
                    pgBar.updateProgress((Double)m, text);
                    return pgBar;
                } else {
                    Label lbl = new Label(m.toString());
                    lbl.setToolTip(m.toString());
                    return lbl;
                }
            }
        };
        
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig name = new ColumnConfig("Name", "Name", 240);
        name.setRenderer(new TreeGridCellRenderer<ProgressInfoBean>());
        configs.add(name);
        configs.add(new ColumnConfig("StartTime", "StartTime", 80));
        configs.add(new ColumnConfig("ElapsedTime", "ElapsedTime", 80));
        ColumnConfig pg = new ColumnConfig("Progress", "Progress", 110);
        pg.setRenderer(pgBarRenderer);
        configs.add(pg);
        configs.add(new ColumnConfig("Des", "Description", 120));
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            configs.add(new ColumnConfig("User", "User", 50));
        }
        for(ColumnConfig c : configs){
            c.setSortable(false);
        }
        
        ColumnModel cm = new ColumnModel(configs);

        fGrid = new TreeGrid<ProgressInfoBean>(fStore, cm);
        fStore.setKeyProvider(new ModelKeyProvider<ProgressInfoBean>() {

            @Override
            public String getKey(ProgressInfoBean model) {
                return model.<String> get("Id");
            }
        });
        fStore.setSortInfo(new SortInfo("StartTime", SortDir.ASC));
        fGrid.setModelProcessor(new ModelProcessor<ProgressInfoBean>() {

            @Override
            public ProgressInfoBean prepareData(ProgressInfoBean model) {
                Long startTime = model.get("StartTime");
                if (startTime != null) {
                    model.set("StartTime", formatter
                            .format(new Date(startTime)));
                    long now = ServerClock.getInstance().now();
                    model.set("ElapsedTime", Util.diffData(now, startTime));
                }

                return model;
            }

        });
        fGrid.setBorders(false);
        fGrid.setAutoExpandColumn("Progress");
        // fGrid.getStyle().setLeafIcon(
        // AbstractImagePrototype.create(Resources.ICONS.tasklet()));
        // fGrid.getStyle().setNodeCloseIcon(
        // AbstractImagePrototype.create(Resources.ICONS.features()));
        // fGrid.getStyle().setNodeOpenIcon(
        // AbstractImagePrototype.create(Resources.ICONS.features()));
        fGrid.getTreeView().setRowHeight(29);
        fGrid.setIconProvider(new ModelIconProvider<ProgressInfoBean>() {
            @Override
            public AbstractImagePrototype getIcon(ProgressInfoBean model) {
                if (model instanceof FeatureJobInfoBean) {
                    return AbstractImagePrototype.create(Resources.ICONS
                            .features());
                } else if (model instanceof TaskletInfoBean) {
                    return AbstractImagePrototype.create(Resources.ICONS
                            .tasklet());
                } else if (model instanceof AgentInfoBean) {
                    return AbstractImagePrototype.create(Resources.ICONS
                            .agent());
                } else {
                    return AbstractImagePrototype.create(Resources.ICONS
                            .tasklet());
                }

            }
        });
        fGrid.setAutoExpand(true);
        final GridSelectionModel<ProgressInfoBean> sm = new GridSelectionModel<ProgressInfoBean>();
        fGrid.setSelectionModel(sm);
        
        Menu menu = new Menu();
        menu.addListener(Events.BeforeShow, new Listener<MenuEvent>(){
            @Override
            public void handleEvent(MenuEvent be) {
                if(sm.getSelectedItem() instanceof FeatureJobInfoBean){
                     be.setCancelled(false);
                }
                else{
                    be.setCancelled(true);
                }
            }     
        });
        MenuItem openItem = new MenuItem("Open Job Location");
        openItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                String location = ((FeatureJobInfoBean)sm.getSelectedItem()).getLogLocation();
                if(location != null)
                    Window.open(location, "_blank", "");
            }
        });
        menu.add(openItem);
        
        MenuItem killItem = new MenuItem("kill this job");
        killItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                final FeatureJobInfoBean bean = (FeatureJobInfoBean)sm.getSelectedItem();
                new RPCInvocation<Boolean>(true, true, true, false) {
                    @Override
                    public String getConfirmMsg() {
                        return "Really want to kill the " + bean.getJobId() + (bean.getDes() != null ? (" (" + bean.getDes() + ")") : "");
                    }
                    @Override
                    public void onComplete(Boolean result) {
                        if (result) {
                            MessageBox.info("Info", bean.getJobId() + " was killed successfully", null).show();
                        } else {
                            MessageBox.info("Info", "Failed to kill " + bean.getJobId(), null).show();
                        }
                        updateView();
                    }
                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Boolean> callback) {
                        remoteService.killJob(bean.getJobId(), callback);
                    }
                }.invoke(false);
            }
        });
        menu.add(killItem);
        
        fGrid.setContextMenu(menu);
        
        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.add(fGrid);

        setLayout(new FitLayout());
        add(_contentPanel);

    }

    @Override
    public void updateView() {
        fLoader.load();
    }

}
