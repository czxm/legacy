package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.InstanceInfoBean;

public class InstanceViewer2 extends ComponentViewer {

    private static int ITERM_PER_PAGE = 20;

    public InstanceViewer2() {

    }

    public void updateView() {
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);
        RpcProxy<PagingLoadResult<InstanceInfoBean>> proxy = new RPCInvocation<PagingLoadResult<InstanceInfoBean>>(
                true, false, false, false) {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<PagingLoadResult<InstanceInfoBean>> callback) {
                remoteService.retrieveInstanceList(
                        (PagingLoadConfig) _loadConfig, callback);
            }
        };

        final BasePagingLoader<PagingLoadResult<ModelData>> loader = new BasePagingLoader<PagingLoadResult<ModelData>>(
                proxy);
        loader.setRemoteSort(true);
        ListStore<InstanceInfoBean> store = new ListStore<InstanceInfoBean>(
                loader);
        List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(new ColumnConfig("Cloud", "Cloud", 120));
        columnConfigs.add(new ColumnConfig("Instance", "Instance", 100));
        columnConfigs.add(new ColumnConfig("IP", "IP", 120));
        columnConfigs.add(new ColumnConfig("OS", "OS", 60));
        columnConfigs.add(new ColumnConfig("Arch", "Arch", 60));
        columnConfigs.add(new ColumnConfig("Type", "Type", 80));
        columnConfigs.add(new ColumnConfig("State", "State", 80));
        columnConfigs.add(new ColumnConfig("Gateway", "Gateway", 120));

        ColumnModel columnModel = new ColumnModel(columnConfigs);
        PagingToolBar toolBar = new PagingToolBar(ITERM_PER_PAGE);
        toolBar.bind(loader);

        final Grid<InstanceInfoBean> grid = new Grid<InstanceInfoBean>(store,
                columnModel);
        grid.setBorders(false);
        grid.setStateful(true);
        grid.addListener(Events.Attach,
                new Listener<GridEvent<InstanceInfoBean>>() {
                    public void handleEvent(GridEvent<InstanceInfoBean> be) {
                        PagingLoadConfig config = new BasePagingLoadConfig();
                        config.setOffset(0);
                        config.setLimit(ITERM_PER_PAGE);

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
                        loader.load(config);
                    }
                });
        grid.setAutoExpandColumn("Instance");

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.setBottomComponent(toolBar);
        _contentPanel.add(grid);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

}
