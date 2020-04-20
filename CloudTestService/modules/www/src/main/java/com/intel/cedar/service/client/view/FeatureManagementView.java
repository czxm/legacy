package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.view.ViewCache.ViewType;

public class FeatureManagementView extends ComponentViewer {
    private static FeatureManagementView instance;
    public static String ID = "FeatureMangement";
    private ToolBar toolBar;
    private Grid<FeatureBean> grid;
    private RpcProxy<BaseListLoadResult<FeatureBean>> fProxy;
    private BaseListLoader<BaseListLoadResult<FeatureBean>> fLoader;
    private ListStore<FeatureBean> fStore;

    private FeatureManagementView() {
        fProxy = new RPCInvocation<BaseListLoadResult<FeatureBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<FeatureBean>> callback) {
                remoteService.retrieveFeatureBeanList(new ArrayList<String>(),
                        callback);
            }

        };
    }

    public static FeatureManagementView getInstance() {
        if (instance == null) {
            instance = new FeatureManagementView();
        }
        return instance;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setBodyBorder(false);
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setLayout(new FitLayout());

        final CheckBoxSelectionModel<FeatureBean> sm = new CheckBoxSelectionModel<FeatureBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        fLoader = new BaseListLoader<BaseListLoadResult<FeatureBean>>(fProxy);
        fStore = new ListStore<FeatureBean>(fLoader);
        fLoader.load();

        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(sm.getColumn());
        columnConfigs.add(new ColumnConfig("Name", "FeatureName", 200));
        columnConfigs.add(new ColumnConfig("Contributor", "Contributor", 300));
        columnConfigs.add(new ColumnConfig("Version", "Version", 100));
        ColumnModel cm = new ColumnModel(columnConfigs);

        grid = new Grid<FeatureBean>(fStore, cm);
        grid.setBorders(false);
        grid.setAutoExpandColumn("Name");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        _contentPanel.add(grid);

        toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        toolBar.add(new Button("Upload", AbstractImagePrototype
                .create(Resources.ICONS.register()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TabPanel tabPanel = MainPage.getInstance()
                                .getTabPanel();
                        TabItem cloudTab = tabPanel.getItemByItemId("Features");
                        cloudTab.removeAll();
                        cloudTab.add(ViewCache
                                .createViewer(ViewType.TYPE_FEATUREMGR_UPLOAD));
                        cloudTab.layout();
                    }

                }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Remove", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final List<FeatureBean> list = sm.getSelectedItems();
                        if (list == null || list.size() == 0) {
                            MessageBox.info("Info",
                                    "No features to remove",
                                    null).show();
                        } else {
                            new RPCInvocation<List<FeatureBean>>(true, true, true, false) {

                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<List<FeatureBean>> callback) {
                                    remoteService
                                            .deleteFeatures(list, callback);
                                }

                                public String getConfirmMsg() {
                                    StringBuilder str = new StringBuilder("Really want to remove the feature(s): ");
                                    for (FeatureBean f : list) {
                                        str.append(f.getName());
                                        str.append(" ");
                                    }
                                    str.append("?");
                                    return str.toString();
                                }

                                public void onComplete(List<FeatureBean> obj) {
                                    for (FeatureBean f : list) {
                                        fStore.remove(f);
                                    }
                                    // also update feature panel
                                    MainPage.getInstance().updateFeaturePanel();
                                }

                            }.invoke(false);
                        }
                    }

                }));
        toolBar.add(new SeparatorToolItem());
        toolBar.add(new Button("Customize", AbstractImagePrototype
                .create(Resources.ICONS.editCloud()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TodoViewer.show();
                    }

                }));
        _contentPanel.setTopComponent(toolBar);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    // public void onRender(Element target, int index){
    // super.onRender(target, index);
    //		
    // ContentPanel _contentPanel = new ContentPanel();
    // _contentPanel.setBodyBorder(false);
    // _contentPanel.setHeaderVisible(false);
    // _contentPanel.setLayout(new FitLayout());
    //		
    // toolBar = new ToolBar();
    // toolBar.setAlignment(HorizontalAlignment.RIGHT);
    // toolBar.add(new Button("Upload",
    // AbstractImagePrototype.create(Resources.ICONS.register()),
    // new SelectionListener<ButtonEvent>(){
    //
    // @Override
    // public void componentSelected(ButtonEvent ce) {
    // TabPanel tabPanel = MainPage.getInstance().getTabPanel();
    // TabItem cloudTab = tabPanel.getItemByItemId("Features");
    // cloudTab.removeAll();
    // cloudTab.add(ViewCache.createViewer(ViewType.TYPE_FEATUREMGR_UPLOAD));
    // cloudTab.layout();
    // }
    //			
    // }));
    // toolBar.add(new SeparatorToolItem());
    // toolBar.add(new Button("Delete",
    // AbstractImagePrototype.create(Resources.ICONS.deRegister()),
    // new SelectionListener<ButtonEvent>(){
    //
    // @Override
    // public void componentSelected(ButtonEvent ce) {
    //						
    // }
    //			
    // }));
    // toolBar.add(new SeparatorToolItem());
    // toolBar.add(new Button("Edit",
    // AbstractImagePrototype.create(Resources.ICONS.editCloud()),
    // new SelectionListener<ButtonEvent>(){
    //
    // @Override
    // public void componentSelected(ButtonEvent ce) {
    //						
    // }
    //
    // }));
    // _contentPanel.setTopComponent(toolBar);
    //		
    // fLoader = new BaseListLoader<BaseListLoadResult<FeatureInfoBean>>(fProxy,
    // new BeanModelReader());
    // fStore = new ListStore<BeanModel>(fLoader);
    // fLoader.load();
    //		
    // view = new ListView<BeanModel>(){
    // @Override
    // protected BeanModel prepareData(BeanModel model){
    // FeatureInfoBean bean = model.getBean();
    // model.set("showname", bean.getName());
    // model.set("eniconpath", GWT.getHostPageBaseURL() + bean.getContextPath()
    // + bean.getEnIcon());
    // model.set("shortname", "CPP.Conformance");
    // return model;
    // }
    // };
    // view.setId("img-chooser-view");
    // view.setTemplate(getTemplate());
    // view.setBorders(false);
    // view.setStore(fStore);
    // view.setItemSelector("div.thumb-wrap");
    // view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    // view.getSelectionModel().addListener(Events.SelectionChange,
    // new Listener<SelectionChangedEvent<BeanModel>>() {
    // public void handleEvent(SelectionChangedEvent<BeanModel> be) {
    // //onSelectionChange(be);
    // }
    // });
    // _contentPanel.add(view);
    //		
    // setLayout(new FitLayout());
    // add(_contentPanel);
    // }

    private native String getTemplate() /*-{
                                        return ['<tpl for=".">', 
                                        '<div class="thumb-wrap" id="{showname}" style="border: 1px solid white">', 
                                        '<div class="thumb"><img src="{eniconpath}" title="{showname}"></div>', 
                                        '<span class="x-editable">{shortname}</span></div>', 
                                        '</tpl>', 
                                        '<div class="x-clear"></div>'].join("");
                                        }-*/;

    @Override
    public void updateView() {
        if (fLoader != null) {
            fLoader.load();
        }
    }

}
