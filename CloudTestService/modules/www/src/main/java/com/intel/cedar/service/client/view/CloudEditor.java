package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.GatewayInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.view.ViewCache.ViewType;

public class CloudEditor extends ComponentViewer {

    private static CloudEditor instance = null;

    private CloudInfoBean cloud = null;

    final FormPanel formPanel = new FormPanel();
    TextField<String> cloudName;
    TextField<String> cloudAddr;
    RadioGroup group1 = new RadioGroup();
    Radio radio1 = new Radio();
    Radio radio2 = new Radio();

    private RpcProxy<BaseListLoadResult<GatewayInfoBean>> gatewayProxy;
    private ListLoader<BaseListLoadResult<GatewayInfoBean>> gatewayLoader;
    private ListStore<GatewayInfoBean> gatewayStore;

    public static CloudEditor getInstance() {
        if (instance == null) {
            instance = new CloudEditor();
        }

        return instance;
    }

    private CloudEditor() {
        cloudName = new TextField<String>();
        cloudAddr = new TextField<String>();
        radio1.setBoxLabel("enabled");
        radio2.setBoxLabel("disabled");

        gatewayProxy = new RPCInvocation<BaseListLoadResult<GatewayInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<GatewayInfoBean>> callback) {
                remoteService.retrieveGatewayList(cloud, callback);
            }

        };
        gatewayLoader = new BaseListLoader<BaseListLoadResult<GatewayInfoBean>>(
                gatewayProxy);
        gatewayStore = new ListStore<GatewayInfoBean>(gatewayLoader);
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setLayout(new FlowLayout());
        cp.add(createEditorForm());

        ToolBar toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        toolBar.add(new Button("Return", AbstractImagePrototype
                .create(Resources.ICONS.back()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TabPanel tabPanel = MainPage.getInstance()
                                .getTabPanel();
                        TabItem cloudTab = tabPanel.getItemByItemId("Cloud");
                        cloudTab.removeAll();
                        cloudTab.add(ViewCache
                                .createViewer(ViewType.TYPE_CLOUD_VIEW));
                        cloudTab.layout();
                    }

                }));

        cp.setTopComponent(toolBar);

        add(cp);
    }

    public static class TestData extends BaseModel {
        /**
		 * 
		 */
        private static final long serialVersionUID = 6104437459889429971L;
        private String name;

        public TestData() {

        }

        public TestData(String name) {
            this.setName(name);
            set("Name", name);
        }

        public void setName(String name) {
            this.name = name;
            set("Name", name);
        }

        public String getName() {
            return name;
        }

    }

    public LayoutContainer createEditorForm() {
        FormData formData = new FormData("0");
        formData.setMargins(new Margins(0, 0, 10, 0));
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(125);
        formPanel.setLayout(formLayout);
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setPadding(50);
        formPanel.setWidth(600);

        cloudName.setFieldLabel("Cloud Name");
        cloudName.setValue(cloud.getCloudName());
        cloudName.setReadOnly(true);

        cloudAddr.setFieldLabel("Cloud Address");
        cloudAddr.setValue(cloud.getHost());
        cloudAddr.setReadOnly(true);

        group1.setFieldLabel("Ready for Service");
        group1.add(radio1);
        group1.add(radio2);
        if (cloud.isEnabled()) {
            group1.setValue(radio1);
        } else {
            group1.setValue(radio2);
        }

        // HorizontalPanel btnBar = new HorizontalPanel();
        // btnBar.setTableWidth("100%");

        formPanel.getButtonBar().setSpacing(30);
        formPanel.getButtonBar().setStyleAttribute("padding-right", "50px");
        formPanel.addButton(new Button("Save",
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (group1.getValue().getBoxLabel().equalsIgnoreCase(
                                "enabled")) {
                            cloud.setEnabled(true);
                        } else {
                            cloud.setEnabled(false);
                        }
                        new RPCInvocation<CloudInfoBean>(false, true, false,
                                false) {

                            @Override
                            public void execute(
                                    CloudRemoteServiceAsync remoteService,
                                    AsyncCallback<CloudInfoBean> callback) {
                                ArrayList<GatewayInfoBean> gateways = new ArrayList<GatewayInfoBean>();
                                for (int i = 0; i < gatewayStore.getCount(); i++) {
                                    gateways.add(gatewayStore.getAt(i));
                                }
                                remoteService.saveCloud(cloud, gateways,
                                        callback);
                            }

                            public void onComplete(CloudInfoBean bean) {
                                // return to cloudview page
                                backToViewer(ViewType.TYPE_CLOUD_VIEW);
                            }

                            public String getProgressTitle() {
                                return "Save Cloud";
                            }

                            public String getProgressText() {
                                return "Saving...";
                            }

                        }.invoke(false);
                    }

                }));
        formPanel.addButton(new Button("Cancel",
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        backToViewer(ViewType.TYPE_CLOUD_VIEW);
                    }

                }));

        formPanel.add(cloudName, formData);
        formPanel.add(cloudAddr, formData);
        formPanel.add(group1, formData);
        // if(cloud.isSeparated()){
        LayoutContainer main = createGatewayWidget();
        formPanel.add(main, formData);
        // }

        return formPanel;
    }

    private Grid<GatewayInfoBean> gatewayGrid;
    private GridSelectionModel<GatewayInfoBean> gatewaySelectionModel = new GridSelectionModel<GatewayInfoBean>();

    private Button btn1 = new Button("Edit");
    private Button btn2 = new Button("Add");
    private Button btn3 = new Button("Delete");

    public void backToViewer(ViewType type) {
        TabPanel tabPanel = MainPage.getInstance().getTabPanel();
        TabItem cloudTab = tabPanel.getItemByItemId("Cloud");
        cloudTab.removeAll();
        ComponentViewer viewer = ViewCache.createViewer(type);
        viewer.updateView();
        cloudTab.add(viewer);
        cloudTab.layout();
    }

    public Grid<GatewayInfoBean> createGrid() {
        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(new ColumnConfig("Host", "Host", 100));
        ColumnModel cm = new ColumnModel(columnConfigs);
        gatewayGrid = new Grid<GatewayInfoBean>(gatewayStore, cm);
        gatewayLoader.load();
        gatewayGrid.setAutoExpandColumn("Host");
        gatewayGrid.setBorders(true);
        gatewayGrid.setStyleAttribute("border-color", "#cfcfcf");
        gatewayGrid.setStyleAttribute("border-style", "groove");
        gatewayGrid.setStyleAttribute("margin-left", "10px");
        gatewayGrid.setHeight(100);
        gatewayGrid.setSelectionModel(gatewaySelectionModel);

        return gatewayGrid;
    }

    public ContentPanel createGridPanel() {
        ContentPanel left = new ContentPanel();
        left.setHeaderVisible(false);
        left.setBodyBorder(false);
        FormLayout inner = new FormLayout();
        FormData formData = new FormData("0");
        left.setLayout(inner);
        left.add(createGrid(), formData);

        return left;
    }

    public LayoutContainer createGatewayWidget() {
        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        ContentPanel left = createGridPanel();

        VerticalPanel right = new VerticalPanel();
        right.setStyleAttribute("padding", "0 10 0 10");
        TableData td = new TableData();
        td.setHorizontalAlign(HorizontalAlignment.RIGHT);
        td.setPadding(2);
        btn1.setItemId("Edit");
        btn1.setWidth(50);
        btn1.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<GatewayInfoBean> sl = gatewaySelectionModel
                        .getSelectedItems();
                GatewayInfoBean bean = sl.get(0);
                Dialog dialog = createGatewayEditor(bean, "Edit");
                dialog.show();
            }

        });
        btn2.setItemId("Add");
        btn2.setWidth(50);
        btn2.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dialog dialog = createGatewayEditor(null, "Add");
                dialog.show();
            }

        });
        btn3.setItemId("Delete");
        btn3.setWidth(50);
        btn3.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<GatewayInfoBean> sl = gatewaySelectionModel
                        .getSelection();
                for (GatewayInfoBean td : sl) {
                    gatewayStore.remove(td);
                }
            }

        });
        gatewaySelectionModel
                .addSelectionChangedListener(new SelectionChangedListener<GatewayInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<GatewayInfoBean> se) {
                        if (gatewaySelectionModel.getSelection().size() == 1) {
                            btn1.enable();
                        } else {
                            btn1.disable();
                        }
                        btn2.enable();
                        if (gatewaySelectionModel.getSelection().size() > 0) {
                            btn3.enable();
                        } else {
                            btn3.disable();
                        }
                    }

                });
        if (gatewaySelectionModel.getSelection().size() == 1) {
            btn1.enable();
        } else {
            btn1.disable();
        }
        btn2.enable();
        if (gatewaySelectionModel.getSelection().size() > 0) {
            btn3.enable();
        } else {
            btn3.disable();
        }

        // comment on 2010-11-03
        // temporary disable the edit and remove gateway functionality while
        // editing a cloud, as the gateway may be in use,
        // we have to do much dynamic check to fulfill the functionality more
        // perfectly
        // right.add(btn1, td);
        right.add(btn2, td);
        // right.add(btn3, td);

        LayoutContainer empty = new LayoutContainer();
        Text text = new Text("Gateway:");
        text.setStyleAttribute("font",
                "normal 12px tahoma, arial, helvetica, sans-serif");
        empty.add(text);
        main.add(empty, new ColumnData(.25));
        main.add(left, new ColumnData(.6));
        main.add(right, new ColumnData(.15));

        return main;
    }

    public Dialog createGatewayEditor(final GatewayInfoBean bean, String title) {
        Dialog dialog = new Dialog();
        dialog.setWidth(300);
        dialog.setHeading(title);
        dialog.setBodyBorder(false);
        dialog.setHideOnButtonClick(true);

        FormPanel fp = new FormPanel();
        FormLayout fl = new FormLayout();
        FormData fd = new FormData("0");
        fl.setLabelWidth(75);
        fp.setLayout(fl);
        fp.setHeaderVisible(false);
        fp.setBodyBorder(false);
        fp.setBorders(false);

        final TextField<String> gw = new TextField<String>();
        gw.setFieldLabel("Gateway");
        if (bean != null)
            gw.setValue(bean.getHost());
        fp.add(gw, fd);

        /*
         * final SimpleComboBox<String> intf = new SimpleComboBox<String>();
         * intf.setFieldLabel("Interface"); intf.add("eth1");
         * intf.setSimpleValue("eth1"); if(bean != null)
         * intf.setSimpleValue(bean.getIntf()); fp.add(intf, fd);
         * 
         * FormLayout inner = new FormLayout(); inner.setLabelWidth(64);
         * FieldSet fieldSet = new FieldSet();
         * fieldSet.setHeading("Port Range"); fieldSet.setLayout(inner);
         * 
         * final TextField<String> start = new TextField<String>();
         * start.setFieldLabel("Start"); start.setWidth(150); if(bean != null)
         * start.setValue(bean.getStartPort().toString()); else
         * start.setValue(CloudRegistration.DFT_START); fieldSet.add(start, fd);
         * 
         * final TextField<String> end = new TextField<String>();
         * end.setFieldLabel("End"); end.setWidth(150); if(bean != null)
         * end.setValue(bean.getEndPort().toString()); else
         * end.setValue(CloudRegistration.DFT_END); fieldSet.add(end, fd);
         * fp.add(fieldSet);
         */
        Button okbtn = dialog.getButtonById(Dialog.OK);
        okbtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                String gwstr = gw.getValue();
                if (gwstr == null || gwstr.length() == 0)
                    return;
                GatewayInfoBean m = new GatewayInfoBean();
                /*
                 * String sport = start.getValue(); String eport =
                 * end.getValue(); Integer sNum = Integer.valueOf(sport);
                 * Integer eNum = Integer.valueOf(eport);
                 * 
                 * m.setIntf(intf.getSimpleValue()); m.setStartPort(sNum);
                 * m.setEndPort(eNum);
                 */
                m.setHost(gwstr);
                m.refresh();
                ArrayList<GatewayInfoBean> added = new ArrayList<GatewayInfoBean>();
                added.add(m);
                if (bean != null)
                    gatewayStore.remove(bean);
                gatewayStore.add(added);
                gatewaySelectionModel.setSelection(added);
            }

        });

        dialog.add(fp);
        return dialog;
    }

    @Override
    public void updateView() {
        this.cloudName.setValue(cloud.getCloudName());
        this.cloudAddr.setValue(cloud.getHost());
        if (cloud.isEnabled()) {
            group1.setValue(radio1);
        } else {
            group1.setValue(radio2);
        }
        gatewayLoader.load();
    }

    public void setCloud(CloudInfoBean cloud) {
        this.cloud = cloud;
    }

    public CloudInfoBean getCloud() {
        return cloud;
    }

}
