package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.KeyPairBean;

public class KeyPairViewer extends ComponentViewer {

    @Override
    public void updateView() {
        mLoader.load();
    }

    private static KeyPairViewer instance;

    private RpcProxy<BaseListLoadResult<KeyPairBean>> mProxy;

    private BaseListLoader<BaseListLoadResult<KeyPairBean>> mLoader;

    private GroupingStore<KeyPairBean> mStore;

    private KeyPairViewer() {
        mProxy = new RPCInvocation<BaseListLoadResult<KeyPairBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<KeyPairBean>> callback) {
                remoteService.retrieveKeyPairList(new ArrayList<String>(),
                        callback);
            }

        };
        mLoader = new BaseListLoader<BaseListLoadResult<KeyPairBean>>(mProxy);
        mStore = new GroupingStore<KeyPairBean>(mLoader);
    }

    public static KeyPairViewer getInstance() {
        if (instance == null) {
            instance = new KeyPairViewer();
        }

        return instance;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        mLoader.setRemoteSort(true);
        mStore.groupBy("CloudName");
        mLoader.load();

        List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(new ColumnConfig("CloudName", "CloudName", 200));
        columnConfigs.add(new ColumnConfig("KeyName", "KeyName", 200));
        columnConfigs.add(new ColumnConfig("KeyFingerPrint", "KeyFingerPrint",
                500));

        final ColumnModel columnModel = new ColumnModel(columnConfigs);
        GroupingView groupView = new GroupingView();
        groupView.setShowGroupedColumn(true);
        groupView.setForceFit(true);
        groupView.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = columnModel.getColumnById(data.field)
                        .getHeader();
                String u = data.models.size() == 1 ? "Key" : "Keys";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });
        final Grid<KeyPairBean> grid = new Grid<KeyPairBean>(mStore,
                columnModel);
        grid.setBorders(false);
        grid.setView(groupView);
        grid.setStateful(true);
        grid.setAutoExpandColumn("KeyFingerPrint");

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.add(grid);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

}
