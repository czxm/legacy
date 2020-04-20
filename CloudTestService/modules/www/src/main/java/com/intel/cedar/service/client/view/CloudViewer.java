package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.view.ViewCache.ViewType;

public class CloudViewer extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 10000;
    private boolean refreshPending = false;
    private static CloudViewer instance;
    private Grid<CloudInfoBean> grid;
    private RpcProxy<BaseListLoadResult<CloudInfoBean>> cloudProxy;
    private ListLoader<BaseListLoadResult<CloudInfoBean>> cloudLoader;
    private ListStore<CloudInfoBean> cloudStore;

    public static CloudViewer getInstance() {
        if (instance == null) {
            instance = new CloudViewer();
        }

        return instance;
    }

    private Timer updater = new Timer() {

        @Override
        public void run() {
            if (cloudLoader != null) {
                cloudLoader.load();
            }
        }
    };

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

    private CloudViewer() {
        cloudProxy = new RPCInvocation<BaseListLoadResult<CloudInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<CloudInfoBean>> callback) {
                remoteService.retrieveCloudList(new ArrayList<String>(),
                        callback);

            }

            public void onComplete(BaseListLoadResult<CloudInfoBean> obj) {
                scheduleRefresh();
            }
        };
    }

    @Override
    public void updateView() {
        if (cloudLoader != null) {
            cloudLoader.load();
        }
        scheduleRefresh();
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        final CheckBoxSelectionModel<CloudInfoBean> sm = new CheckBoxSelectionModel<CloudInfoBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        cloudLoader = new BaseListLoader<BaseListLoadResult<CloudInfoBean>>(
                cloudProxy);
        cloudStore = new ListStore<CloudInfoBean>(cloudLoader);
        cloudLoader.load();

        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(sm.getColumn());
        columnConfigs.add(new ColumnConfig("CloudName", "CloudName", 150));
        columnConfigs.add(new ColumnConfig("Protocol", "Protocol", 100));
        columnConfigs.add(new ColumnConfig("Host", "Host", 150));
        columnConfigs.add(new ColumnConfig("Port", "Port", 100));
        columnConfigs.add(new ColumnConfig("Separated", "Separated", 100));
        columnConfigs.add(new ColumnConfig("Enabled", "Enabled", 100));
        ColumnModel cm = new ColumnModel(columnConfigs);

        grid = new Grid<CloudInfoBean>(cloudStore, cm);
        grid.setBorders(false);
        grid.setAutoExpandColumn("CloudName");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        ToolBar toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);

        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            toolBar.add(new Button("Register", AbstractImagePrototype
                    .create(Resources.ICONS.register()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            TabPanel tabPanel = MainPage.getInstance()
                                    .getTabPanel();
                            TabItem cloudTab = tabPanel
                                    .getItemByItemId("Cloud");
                            cloudTab.removeAll();
                            cloudTab
                                    .add(ViewCache
                                            .createViewer(ViewType.TYPE_CLOUD_REGISTRATION));
                            cloudTab.layout();
                        }

                    }));
            toolBar.add(new SeparatorToolItem());
            toolBar.add(new Button("Unregister", AbstractImagePrototype
                    .create(Resources.ICONS.deRegister()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            // TODO Auto-generated method stub

                            List<CloudInfoBean> list = sm.getSelectedItems();
                            if (list == null || list.size() == 0) {
                                MessageBox
                                        .info(
                                                "Info",
                                                "not any cloud selected to de-register",
                                                null).show();
                            } else
                                new CloudDeregister(list).invoke(false);
                        }

                    }));
            toolBar.add(new SeparatorToolItem());
            toolBar.add(new Button("Edit", AbstractImagePrototype
                    .create(Resources.ICONS.editCloud()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            List<CloudInfoBean> list = sm.getSelectedItems();
                            if (list == null || list.size() == 0) {
                                MessageBox.info("Info",
                                        "not any cloud selected to edit", null)
                                        .show();
                            } else {
                                TabPanel tabPanel = MainPage.getInstance()
                                        .getTabPanel();
                                TabItem cloudTab = tabPanel
                                        .getItemByItemId("Cloud");
                                cloudTab.removeAll();
                                CloudEditor ceditor = (CloudEditor) (ViewCache
                                        .createViewer(ViewType.TYPE_CLOUD_EDITOR));
                                ceditor.setCloud(list.get(0));
                                ceditor.updateView();
                                cloudTab.add(ceditor);
                                cloudTab.layout();
                            }
                        }

                    }));
        }

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setBodyBorder(false);
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.add(grid);
        _contentPanel.setTopComponent(toolBar);

        setLayout(new FitLayout());
        add(_contentPanel);

    }

    public class CloudDeregister extends RPCInvocation<List<CloudInfoBean>> {
        private List<CloudInfoBean> list;

        public CloudDeregister() {
            super(false, false, true, true);
            list = null;
        }

        public CloudDeregister(List<CloudInfoBean> list) {
            super(false, false, true, true);
            this.list = list;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<List<CloudInfoBean>> callback) {
            remoteService.deregisterCloud(list, callback);
        }

        public String getSuccessMsg() {
            return "De-register the cloud successfully.";
        }

        public String getConfirmMsg() {
            return "Really want to deregister the cloud?";
        }

        public void onComplete(List<CloudInfoBean> obj) {
            for (CloudInfoBean cloud : obj) {
                grid.getStore().remove(cloud);
            }
        }
    }

}
