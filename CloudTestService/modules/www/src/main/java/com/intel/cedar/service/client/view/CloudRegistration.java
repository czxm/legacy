package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
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
import com.intel.cedar.service.client.widget.CedarIconButton;

public class CloudRegistration extends ComponentViewer {

    private static CloudRegistration instance;

    private LayoutContainer gatewayAll;

    public static CloudRegistration getInstance() {
        instance = new CloudRegistration();

        return instance;
    }

    private CloudRegistration() {
        gatewayAll = createGatewayWidget();
    }

    @Override
    public void updateView() {

    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setBorders(false);
        cp.setLayout(new FlowLayout());
        cp.add(createRegistrationForm());

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
                        ComponentViewer viewer = ViewCache
                                .createViewer(ViewType.TYPE_CLOUD_VIEW);
                        viewer.updateView();
                        cloudTab.add(viewer);
                        cloudTab.layout();
                    }

                }));
        cp.setTopComponent(toolBar);

        add(cp);
    }

    public LayoutContainer createRegistrationForm() {
        FormData formData = new FormData("0");
        final FormPanel formPanel = new FormPanel();
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(140);
        formPanel.setLayout(formLayout);
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setStyleAttribute("padding", "10px 50px 10px 50px");

        formPanel.setWidth(650);

        final TextField<String> accessKey = new TextField<String>();
        final TextField<String> secretKey = new TextField<String>();

        final Text note = new Text();
        FormData notefd = new FormData();
        notefd.setMargins(new Margins(0, 0, 10, 0));
        formPanel.add(note, notefd);
        note.setTagName("p");
        note.setStyleName("warning");

        final TextField<String> cloudName = new TextField<String>();
        cloudName.setFieldLabel("Cloud Name");
        // cloudName.setAllowBlank(false);
        cloudName.setEmptyText("e.g. Cloud");
        formPanel.add(cloudName, formData);

        final TextField<String> ccHost = new TextField<String>();
        ccHost.setFieldLabel("Cloud Address");
        // ccHost.setAllowBlank(false);
        ccHost.setEmptyText("e.g. clc-host");
        formPanel.add(ccHost, formData);

        final TextField<String> ccResourcePrefix = new TextField<String>();
        ccResourcePrefix.setFieldLabel("Resource Prefix");
        ccResourcePrefix.setEmptyText("e.g. /services/Eucalyptus");
        formPanel.add(ccResourcePrefix, formData);

        final TextField<String> ccPort = new TextField<String>();
        ccPort.setFieldLabel("Service Port");
        // ccPort.setAllowBlank(false);
        ccPort.setValue("8773");
        formPanel.add(ccPort, formData);

        final CheckBox cb1 = new CheckBox();
        cb1.setFieldLabel("Use HTTPS");
        cb1.setWidth(100);
        cb1.setValue(true);
        formPanel.add(cb1, formData);

        final SimpleComboBox<String> ccProtocols = new SimpleComboBox<String>();
        ccProtocols.setFieldLabel("Protocol");
        ccProtocols.setEditable(false);
        ccProtocols.add("EC2");
        ccProtocols.add("Eucalyptus");
        ccProtocols.add("OpenStack");
        ccProtocols.setFireChangeEventOnSetValue(true);
        ccProtocols.setEmptyText("select a protocol");
        ccProtocols.setTriggerAction(TriggerAction.ALL);
        ccProtocols.addListener(Events.Select, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                String proto = ccProtocols.getSimpleValue();
                boolean isEuca = proto.equalsIgnoreCase("Eucalyptus");
                boolean isEC2 = proto.equalsIgnoreCase("EC2");
                boolean isOpenStack = proto.equalsIgnoreCase("OpenStack");
                if (isEuca || isEC2 || isOpenStack) {
                    int index = formPanel.indexOf(ccProtocols);
                    accessKey.setFieldLabel("Access Key");
                    // accessKey.setEmptyText("access key for EC2 cloud");
                    // FIXME: test only
                    accessKey.setValue("WKy3rMzOWPouVOxK1p3Ar1C2uRBwa2FBXnCw");
                    formPanel.insert(accessKey, index + 1);

                    secretKey.setFieldLabel("Secret Key");
                    // secretKey.setEmptyText("secret key for EC2 cloud");
                    // FIXME: test only
                    secretKey.setValue("A7eu3WQSJu0BEl49fl8i2DlhLGmCLqxpWTtfQ");
                    formPanel.insert(secretKey, index + 2);
                    formPanel.layout();
                } else {
                    formPanel.remove(accessKey);
                    formPanel.remove(secretKey);
                    formPanel.layout();
                }
                if (isEuca) {
                    ccResourcePrefix.setValue("/services/Eucalyptus");
                    ccPort.setValue("8773");
                    cb1.setValue(false);
                } else if (isEC2) {
                    ccResourcePrefix.setValue("/");
                    ccPort.setValue("443");
                    cb1.setValue(true);
                } else if (isOpenStack) {
                    ccResourcePrefix.setValue("/services/Cloud");
                    ccPort.setValue("8773");
                    cb1.setValue(false);
                }
            }

        });
        formPanel.add(ccProtocols, formData);

        Radio radio3 = new Radio();
        radio3.setWidth(120);
        radio3.setBoxLabel("Managed by NAT");
        radio3.setValue(true);
        Radio radio4 = new Radio();
        radio4.setWidth(120);
        radio4.setBoxLabel("Public Addresses");
        final RadioGroup radioGroup2 = new RadioGroup();
        radioGroup2.setFieldLabel("Network Configuration");
        radioGroup2.add(radio3);
        radioGroup2.add(radio4);
        formPanel.add(radioGroup2, formData);
        formPanel.add(gatewayAll, formData);

        radioGroup2.addListener(Events.Valid, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio radio = radioGroup2.getValue();
                if (radio.getBoxLabel().equalsIgnoreCase("Managed by NAT")) {
                    int index = formPanel.indexOf(radioGroup2);
                    formPanel.insert(gatewayAll, index + 1);
                    formPanel.layout();
                    refreshGateway();
                } else {
                    formPanel.remove(gatewayAll);
                }
            }
        });

        formPanel.add(gatewayAll, formData);

        final LayoutContainer proxy = new LayoutContainer();
        proxy.setLayout(new ColumnLayout());
        proxy.disable();

        Radio radio5 = new Radio();
        radio5.setWidth(120);
        radio5.setBoxLabel("Direct Connection");
        radio5.setValue(true);
        Radio radio6 = new Radio();
        radio6.setWidth(120);
        radio6.setBoxLabel("Connect via HTTP Proxy");
        final RadioGroup radioGroup3 = new RadioGroup();
        radioGroup3.setFieldLabel("Network Connectivity");
        radioGroup3.add(radio5);
        radioGroup3.add(radio6);
        radioGroup3.addListener(Events.Valid, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                Radio radio = radioGroup3.getValue();
                if (radio.getBoxLabel().equalsIgnoreCase(
                        "Connect via HTTP Proxy")) {
                    proxy.enable();
                } else {
                    proxy.disable();
                }
            }

        });
        formPanel.add(radioGroup3, formData);

        LayoutContainer empty = new LayoutContainer();
        empty.add(new Label());

        LayoutContainer left = new LayoutContainer();
        left.setStyleAttribute("padding-right", "10px");
        FormLayout leftLayout = new FormLayout();
        leftLayout.setLabelAlign(LabelAlign.TOP);
        left.setLayout(leftLayout);
        final TextField<String> proxyURL = new TextField<String>();
        proxyURL.setFieldLabel("Proxy URL");
        left.add(proxyURL, formData);
        final TextField<String> proxyUser = new TextField<String>();
        proxyUser.setFieldLabel("User Name");
        left.add(proxyUser, formData);

        LayoutContainer right = new LayoutContainer();
        right.setStyleAttribute("padding-left", "10px");
        FormLayout rightLayout = new FormLayout();
        rightLayout.setLabelAlign(LabelAlign.TOP);
        right.setLayout(rightLayout);
        final TextField<String> proxyPort = new TextField<String>();
        proxyPort.setFieldLabel("Port");
        right.add(proxyPort, formData);
        final TextField<String> proxyPassword = new TextField<String>();
        proxyPassword.setFieldLabel("Password");
        right.add(proxyPassword, formData);

        proxy.add(empty, new ColumnData(0.28));
        proxy.add(left, new ColumnData(0.36));
        proxy.add(right, new ColumnData(.36));
        formPanel.add(proxy, formData);

        // formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
        // formPanel.getButtonBar().setStyleAttribute("padding",
        // "10px 5px 0px 0px");
        // formPanel.getButtonBar().setSpacing(30);

        HorizontalPanel btnBar = new HorizontalPanel();
        btnBar.setTableWidth("100%");
        IconButton smtBtn = new CedarIconButton("cedar_submit");
        smtBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                if (isEmpty(cloudName.getValue())) {
                    note.setText("cloud name field is empty");
                    return;
                }
                if (isEmpty(ccHost.getValue())) {
                    note.setText("the host entry for cloud is empty");
                }
                if (isEmpty(ccPort.getValue())) {
                    note.setText("the port entry for cloud service is empty");
                }
                if (isEmpty(ccProtocols.getSimpleValue())) {
                    note.setText("undetermined protocol for cloud service");
                }
                CloudInfoBean cloud = new CloudInfoBean();
                cloud.setCloudName(cloudName.getValue().toUpperCase());
                cloud.setHost(ccHost.getValue().toLowerCase());
                cloud.setPort(Integer.parseInt(ccPort.getValue()));
                cloud.setProtocol(ccProtocols.getSimpleValue());
                if (ccProtocols.getSimpleValue().equalsIgnoreCase("EC2")
                        || ccProtocols.getSimpleValue().equalsIgnoreCase(
                                "Eucalyptus")
                        || ccProtocols.getSimpleValue().equalsIgnoreCase(
                                "OpenStack")) {
                    cloud.setAccessKey(accessKey.getValue());
                    cloud.setSecretKey(secretKey.getValue());
                }
                cloud.setResourcePrefix(ccResourcePrefix.getValue());
                cloud.setSecured(cb1.getValue());

                ArrayList<GatewayInfoBean> gateways = new ArrayList<GatewayInfoBean>();
                if (gatewayStore.getCount() > 0) {
                    cloud.setSeparated(true);
                    for (int i = 0; i < gatewayStore.getCount(); i++) {
                        gateways.add(gatewayStore.getAt(i));
                    }
                } else {
                    cloud.setSeparated(false);
                }

                if (proxy.isEnabled()) {
                    cloud.setProxyHost(proxyURL.getValue());
                    cloud.setProxyPort(proxyPort.getValue());
                    cloud.setProxyAuth(proxyUser.getValue());
                    cloud.setProxyPass(proxyPassword.getValue());
                }
                cloud.setEnabled(true);
                new CloudRegister(cloud, gateways).invoke(false);
            }

        });
        smtBtn.setSize(80, 24);
        TableData smtfd = new TableData();
        smtfd.setWidth("80%");
        smtfd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(smtBtn, smtfd);
        IconButton celBtn = new CedarIconButton("cedar_cancel");
        celBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                TabPanel tabPanel = MainPage.getInstance().getTabPanel();
                TabItem cloudTab = tabPanel.getItemByItemId("Cloud");
                cloudTab.removeAll();
                ComponentViewer viewer = ViewCache
                        .createViewer(ViewType.TYPE_CLOUD_VIEW);
                viewer.updateView();
                cloudTab.add(viewer);
                cloudTab.layout();
            }

        });
        celBtn.setSize(80, 24);
        TableData celfd = new TableData();
        celfd.setWidth("20%");
        celfd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(celBtn, celfd);
        FormData btnBarfd = new FormData();
        btnBarfd.setMargins(new Margins(20, 0, 0, 0));
        formPanel.add(btnBar, btnBarfd);

        Listener<FieldEvent> clearListener = new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                note.setText("");
            }

        };
        cloudName.addListener(Events.OnClick, clearListener);
        ccHost.addListener(Events.OnClick, clearListener);
        ccPort.addListener(Events.OnClick, clearListener);
        ccProtocols.addListener(Events.OnClick, clearListener);

        return formPanel;
    }

    public void refreshGateway() {
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

    private final ListStore<GatewayInfoBean> gatewayStore = new ListStore<GatewayInfoBean>();
    private Grid<GatewayInfoBean> gatewayGrid;
    private GridSelectionModel<GatewayInfoBean> gatewaySelectionModel = new GridSelectionModel<GatewayInfoBean>();
    private final Button btn1 = new Button("Edit");
    private final Button btn2 = new Button("Add");
    private final Button btn3 = new Button("Delete");

    public LayoutContainer createGatewayWidget() {
        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        FieldSet gwfs = new FieldSet();
        FormLayout gwfl = new FormLayout();
        FormData gwfd = new FormData();
        gwfs.setHeading("Gateways");
        gwfs.setLayout(gwfl);

        LayoutContainer innerContainer = new LayoutContainer();
        innerContainer.setLayout(new ColumnLayout());

        ContentPanel left = new ContentPanel();
        left.setHeaderVisible(false);
        left.setBodyBorder(false);
        FormLayout inner = new FormLayout();
        FormData formData = new FormData("0");
        left.setLayout(inner);
        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(new ColumnConfig("Host", "Host", 150));
        ColumnModel cm = new ColumnModel(columnConfigs);
        gatewayGrid = new Grid<GatewayInfoBean>(gatewayStore, cm);
        /*
         * gatewayGrid.setModelProcessor(new ModelProcessor<GatewayInfoBean>(){
         * 
         * @Override public PhysicalNodeInfoBean prepareData(GatewayInfoBean
         * model) { model.set("PortRange", model.getStartPort() + "-" +
         * model.getEndPort()); return model; }
         * 
         * });
         */
        gatewayGrid.setAutoExpandColumn("Host");
        gatewayGrid.setBorders(true);
        gatewayGrid.setStyleAttribute("border-color", "#cfcfcf");
        gatewayGrid.setStyleAttribute("border-style", "groove");
        gatewayGrid.setStyleAttribute("margin-left", "10px");
        if (!GXT.isIE) {
            gatewayGrid.setStyleAttribute("margin-bottom", "6px");
        }
        gatewayGrid.setHeight(80);
        gatewayGrid.setSelectionModel(gatewaySelectionModel);
        left.add(gatewayGrid, formData);

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
                Dialog dialog = createDialog(bean, "Edit");
                dialog.show();
            }

        });
        btn2.setItemId("Add");
        btn2.setWidth(50);
        btn2.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dialog dialog = createDialog(null, "Add");
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

        right.add(btn1, td);
        right.add(btn2, td);
        right.add(btn3, td);

        innerContainer.add(left, new ColumnData(0.8));
        innerContainer.add(right, new ColumnData(0.2));
        gwfs.add(innerContainer, gwfd);

        LayoutContainer empty = new LayoutContainer();
        empty.add(new Label(""));
        main.add(empty, new ColumnData(.28));
        main.add(gwfs, new ColumnData(.72));

        return main;
    }

    protected Dialog createDialog(final GatewayInfoBean bean, String title) {
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

        // substitute with FormBinding
        final TextField<String> gw = new TextField<String>();
        gw.setAllowBlank(false);
        gw.setFieldLabel("Gateway");
        if (bean != null)
            gw.setValue(bean.getHost());
        fp.add(gw, fd);

        /*
         * final SimpleComboBox<String> intf = new SimpleComboBox<String>();
         * intf.setFieldLabel("Interface"); intf.add("eth1"); if(bean != null)
         * intf.setSimpleValue(bean.getIntf()); fp.add(intf, fd);
         * 
         * FormLayout inner = new FormLayout(); inner.setLabelWidth(64);
         * FieldSet fieldSet = new FieldSet();
         * fieldSet.setHeading("Port Range"); fieldSet.setLayout(inner);
         * 
         * //use LayoutContainer to shrink the field length final
         * TextField<String> start = new TextField<String>();
         * start.setAllowBlank(false); start.setFieldLabel("Start"); if(bean !=
         * null) start.setValue(bean.getStartPort().toString()); else
         * start.setValue(DFT_START); fieldSet.add(start, fd);
         * 
         * final TextField<String> end = new TextField<String>();
         * end.setAllowBlank(false); end.setFieldLabel("End"); if(bean != null)
         * end.setValue(bean.getEndPort().toString()); else
         * end.setValue(DFT_END); fieldSet.add(end, fd); fp.add(fieldSet);
         */

        Button okbtn = dialog.getButtonById(Dialog.OK);
        okbtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                String gwstr = gw.getValue();
                if (gwstr == null || gwstr.length() == 0) {

                }
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

    private boolean isEmpty(String str) {
        if (str == null || str.equals(""))
            return true;
        return false;
    }

    public class CloudRegister extends RPCInvocation<Boolean> {
        private CloudInfoBean cloud;
        private ArrayList<GatewayInfoBean> gateways;

        public CloudRegister() {

        }

        public CloudRegister(CloudInfoBean bean,
                ArrayList<GatewayInfoBean> gateways) {
            super(false, true, false, false);
            cloud = bean;
            this.gateways = gateways;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<Boolean> callback) {
            remoteService.registerCloud(cloud, gateways, callback);

        }

        public String getProgressTitle() {
            return "Cloud Registration";
        }

        public String getProgressText() {
            return "Registering...";
        }

        public void onComplete(Boolean res) {
            TabPanel tabPanel = MainPage.getInstance().getTabPanel();
            TabItem cloudTab = tabPanel.getItemByItemId("Cloud");
            cloudTab.removeAll();
            ComponentViewer viewer = ViewCache
                    .createViewer(ViewType.TYPE_CLOUD_VIEW);
            viewer.updateView();
            cloudTab.add(viewer);
            cloudTab.layout();
        }
    }
}
