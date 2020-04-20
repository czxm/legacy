package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.data.RpcProxy;
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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.GatewayInfoBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.model.KeyPairBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.model.MachineTypeInfoBean;
import com.intel.cedar.service.client.model.NATInfoBean;
import com.intel.cedar.service.client.widget.CedarIconButton;

public class InstanceLauncher extends ComponentViewer {

    private FormPanel formPanel = null;

    final private TabPanel bottomPanel = new TabPanel();

    private TabItem parentItem;

    final private BorderLayout borderLayout = new BorderLayout();

    private static NATInfoBean[] WINDEFPORT = new NATInfoBean[] {
            new NATInfoBean("RDP", 3389), new NATInfoBean("HTTP", 80),
            new NATInfoBean("HTTPS", 8443), };

    private static NATInfoBean[] LNXDEFPORT = new NATInfoBean[] {
            new NATInfoBean("VNC", 5901), new NATInfoBean("SSH", 22),
            new NATInfoBean("HTTP", 80), new NATInfoBean("HTTPS", 8443), };

    private InstanceInfoBean launchedIns = null;
    private MachineInfoBean machineBean = null;
    private ComboBox<MachineInfoBean> comboImage;
    private LayoutContainer natWidget;
    private static final int REFRESH_INTERVAL = 5000;

    public InstanceLauncher() {
        progressDlg = new Dialog();
        progressDlg.setHeading("Launch");
    }

    public Widget createBottomPanel() {
        bottomPanel.setBorders(false);
        bottomPanel.setBodyBorder(false);
        bottomPanel.setBorderStyle(true);
        TabItem logItem = new TabItem("log");
        logItem.setScrollMode(Scroll.AUTOY);
        logItem.setItemId("log");
        bottomPanel.add(logItem);

        TabItem errorItem = new TabItem("error");
        errorItem.setScrollMode(Scroll.AUTOY);
        errorItem.setItemId("error");
        bottomPanel.add(errorItem);

        return bottomPanel;
    }

    public LayoutContainer createForm() {
        FormData formData = new FormData("0");
        FormLayout formLayout = new FormLayout();
        formPanel = new FormPanel();
        formLayout.setLabelWidth(60);
        formPanel.setFieldWidth(300);
        formPanel.setLayout(formLayout);
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setPadding(75);
        formPanel.setWidth(700);

        final ComboBox<CloudInfoBean> comboCloud = new ComboBox<CloudInfoBean>();
        comboCloud.setFieldLabel("Cloud");
        comboCloud.setDisplayField("CloudName");
        RpcProxy<BaseListLoadResult<CloudInfoBean>> cloudProxy = new RPCInvocation<BaseListLoadResult<CloudInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<CloudInfoBean>> callback) {
                remoteService.retrieveCloudList(new ArrayList<String>(),
                        callback);
            }

        };
        ListLoader<BaseListLoadResult<CloudInfoBean>> cloudLoader = new BaseListLoader<BaseListLoadResult<CloudInfoBean>>(
                cloudProxy);
        ListStore<CloudInfoBean> cloudStore = new ListStore<CloudInfoBean>(
                cloudLoader);
        comboCloud.setStore(cloudStore);
        comboCloud.setEditable(false);
        comboCloud.setEmptyText("Select a Cloud...");
        final TextField<String> tfCloud = new TextField<String>();
        tfCloud.setFieldLabel("Cloud");
        if (machineBean != null) {
            tfCloud.setValue(machineBean.getCloudName());
            tfCloud.setReadOnly(true);
            formPanel.add(tfCloud, formData);
        } else {
            formPanel.add(comboCloud, formData);
        }

        comboImage = new ComboBox<MachineInfoBean>();
        comboImage.setFieldLabel("Image");
        comboImage.setDisplayField("Verbose");
        comboImage.getView().setModelProcessor(
                new ModelProcessor<MachineInfoBean>() {
                    public MachineInfoBean prepareData(MachineInfoBean model) {
                        CloudInfoBean bean = comboCloud.getValue();
                        if (bean != null) {
                            model.set("Verbose", model.getVerbose());
                        } else {
                            model.set("Verbose", model.getVerbose() + " -- "
                                    + model.getCloudName());
                        }
                        return model;
                    }
                });
        RpcProxy<BaseListLoadResult<MachineInfoBean>> imageProxy = new RPCInvocation<BaseListLoadResult<MachineInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback) {
                ArrayList<CloudInfoBean> clouds = new ArrayList<CloudInfoBean>();
                CloudInfoBean bean = comboCloud.getValue();
                if (bean != null) {
                    clouds.add(bean);
                }
                remoteService.retrieveMachineList(clouds, callback);
            }

        };
        final ListLoader<BaseListLoadResult<MachineInfoBean>> imageLoader = new BaseListLoader<BaseListLoadResult<MachineInfoBean>>(
                imageProxy);
        ListStore<MachineInfoBean> imageStore = new ListStore<MachineInfoBean>(
                imageLoader);
        comboImage.setStore(imageStore);
        comboImage.setEditable(false);
        comboImage.setEmptyText("Select an image...");
        final TextField<String> tfImage = new TextField<String>();
        tfImage.setFieldLabel("Image");
        if (machineBean != null) {
            tfImage.setValue(machineBean.getImageId());
            tfImage.setReadOnly(true);
            formPanel.add(tfImage, formData);
        } else {
            formPanel.add(comboImage, formData);
        }

        final ComboBox<MachineTypeInfoBean> comboType = new ComboBox<MachineTypeInfoBean>();
        comboType.setFieldLabel("Type");
        comboType.setDisplayField("Verbose");
        comboType.getView().setModelProcessor(
                new ModelProcessor<MachineTypeInfoBean>() {
                    public MachineTypeInfoBean prepareData(
                            MachineTypeInfoBean model) {
                        CloudInfoBean bean = comboCloud.getValue();
                        if (bean != null || machineBean != null) {
                            model.set("Verbose", model.getType() + "("
                                    + model.getVerbose() + ")");
                        } else {
                            model.set("Verbose", model.getType() + "("
                                    + model.getVerbose() + ")" + " -- "
                                    + model.getCloudName());
                        }
                        return model;
                    }
                });

        RpcProxy<BaseListLoadResult<MachineTypeInfoBean>> typeProxy = new RPCInvocation<BaseListLoadResult<MachineTypeInfoBean>>() {

            @Override
            public void execute(
                    CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<MachineTypeInfoBean>> callback) {
                ArrayList<CloudInfoBean> clouds = new ArrayList<CloudInfoBean>();
                CloudInfoBean bean;
                if (machineBean == null) {
                    bean = comboCloud.getValue();
                } else {
                    bean = new CloudInfoBean();
                    bean.setId(machineBean.getCloudId());
                }
                if (bean != null) {
                    clouds.add(bean);
                }
                remoteService.retrieveMachineTypeList(clouds, callback);
            }

        };
        final ListLoader<BaseListLoadResult<MachineTypeInfoBean>> typeLoader = new BaseListLoader<BaseListLoadResult<MachineTypeInfoBean>>(
                typeProxy);
        ListStore<MachineTypeInfoBean> typeStore = new ListStore<MachineTypeInfoBean>(
                typeLoader);
        comboType.setStore(typeStore);
        comboType.setEmptyText("Select a machine type...");
        formPanel.add(comboType, formData);

        // final ComboBox<KeyPairBean> comboKey = new ComboBox<KeyPairBean>();
        // comboKey.setFieldLabel("KeyPair");
        // comboKey.setDisplayField("KeyName");
        // RpcProxy<BaseListLoadResult<KeyPairBean>> keyPairProxy =
        // new RPCInvocation<BaseListLoadResult<KeyPairBean>>(){
        //
        // @Override
        // public void execute(CloudRemoteServiceAsync remoteService,
        // AsyncCallback<BaseListLoadResult<KeyPairBean>> callback) {
        // remoteService.retrieveKeyPairList(new ArrayList<String>(), callback);
        // }
        //			
        // };
        // final ListLoader<BaseListLoadResult<KeyPairBean>> keyPairLoader =
        // new BaseListLoader<BaseListLoadResult<KeyPairBean>>(keyPairProxy);
        // ListStore<KeyPairBean> keyStore = new
        // ListStore<KeyPairBean>(keyPairLoader);
        // comboKey.setStore(keyStore);
        // comboKey.setEmptyText("Select a keypair...");
        // formPanel.add(comboKey, formData);

        final NumberField maxNum = new NumberField();
        maxNum.setPropertyEditorType(Integer.class);
        maxNum.setAllowDecimals(false);
        maxNum.setAllowNegative(false);
        maxNum.setFieldLabel("Instances");
        maxNum.setValue(1);
        formPanel.add(maxNum, formData);

        final ComboBox<GatewayInfoBean> comboGateway = new ComboBox<GatewayInfoBean>();
        natWidget = createNATWidget();
        comboGateway.setFieldLabel("Gateway");
        comboGateway.setDisplayField("Host");
        RpcProxy<BaseListLoadResult<GatewayInfoBean>> gatewayProxy = new RPCInvocation<BaseListLoadResult<GatewayInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<GatewayInfoBean>> callback) {
                CloudInfoBean bean;
                if (machineBean == null) {
                    bean = comboCloud.getValue();
                } else {
                    bean = new CloudInfoBean();
                    bean.setId(machineBean.getCloudId());
                }
                if (bean != null) {
                    remoteService.retrieveGatewayList(bean, callback);
                }
            }

        };
        final ListLoader<BaseListLoadResult<GatewayInfoBean>> gatewayLoader = new BaseListLoader<BaseListLoadResult<GatewayInfoBean>>(
                gatewayProxy);
        ListStore<GatewayInfoBean> gatewayStore = new ListStore<GatewayInfoBean>(
                gatewayLoader);
        comboGateway.setStore(gatewayStore);
        comboGateway.setEditable(false);
        comboGateway.setEmptyText("Select a gateway...");
        if (machineBean != null) // machineBean indicates where Launching Viewer
                                 // originates
            comboGateway.setEnabled(true);
        else
            comboGateway.setEnabled(false);
        comboGateway.addListener(Events.Select, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                int redo = 2;
                while (--redo >= 0) {
                    GatewayInfoBean gwBean = comboGateway.getValue();
                    if (gwBean != null) {
                        int index = formPanel.indexOf(comboGateway);
                        refreshNatList();
                        formPanel.insert(natWidget, index + 1);
                        formPanel.layout();
                    } else {
                        formPanel.remove(natWidget);
                    }
                }
            }

        });
        formPanel.add(comboGateway, formData);
        comboImage.addListener(Events.Select, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                CloudInfoBean cbean = comboCloud.getValue();
                MachineInfoBean mbean = comboImage.getValue();
                if (cbean != null && mbean != null) {
                    if (formPanel.indexOf(natWidget) >= 0)
                        formPanel.remove(natWidget);
                    comboGateway.setEnabled(true);
                    comboGateway.clear();
                    gatewayLoader.load();
                } else {
                    formPanel.remove(natWidget);
                    comboGateway.clear();
                    comboGateway.setEnabled(false);
                    formPanel.layout();
                }
            }

        });
        comboCloud.addListener(Events.Select, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                CloudInfoBean cbean = comboCloud.getValue();
                // if(cbean == null){
                if (formPanel.indexOf(natWidget) >= 0)
                    formPanel.remove(natWidget);
                comboGateway.clear();
                comboGateway.setEnabled(false);
                formPanel.layout();
                // }
                comboImage.clear();
                comboType.clear();
                // comboKey.clear();

                imageLoader.load();
                typeLoader.load();
                // keyPairLoader.load();
            }

        });

        HorizontalPanel btnBar = new HorizontalPanel();
        btnBar.setTableWidth("100%");
        IconButton smtBtn = new CedarIconButton("cedar_submit");
        smtBtn.setSize(80, 24);
        smtBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                CloudInfoBean cloud = null;
                MachineInfoBean machine = null;
                if (machineBean != null) {
                    cloud = new CloudInfoBean();
                    cloud.setId(machineBean.getCloudId());
                    cloud.setCloudName(machineBean.getCloudName());
                    machine = machineBean;
                } else {
                    cloud = comboCloud.getValue();
                    machine = comboImage.getValue();
                }
                if(!machine.getEnabled()){
                    MessageBox.info("Info", "This image is disabled!", null);
                    return;
                }                
                MachineTypeInfoBean type = comboType.getValue();
                // KeyPairBean key = comboKey.getValue();
                GatewayInfoBean gateway = comboGateway.getValue();

                TabItem item = bottomPanel.getItemByItemId("error");
                if (cloud == null) {
                    bottomPanel.setSelection(item);
                    item.addText("cloud field is not selected");
                    item.layout();
                    return;
                }

                if (machine == null) {
                    bottomPanel.setSelection(item);
                    item.addText("machine field is not selected");
                    item.layout();
                    return;
                }

                if (type == null) {
                    bottomPanel.setSelection(item);
                    item.addText("type field is not selected");
                    item.layout();
                    return;
                }

                // if(key == null){
                // bottomPanel.setSelection(item);
                // item.addText("key field is not selected");
                // item.layout();
                // return;
                // }

                int num = 1;
                if (maxNum.getValue() != null) {
                    num = maxNum.getValue().intValue();
                }

                if (num <= 0 || num >= 5) {
                    bottomPanel.setSelection(item);
                    item.addText("Invalid request for " + num + " instances!");
                    item.layout();
                    return;
                }

                KeyPairBean key = new KeyPairBean();
                new InstanceRunner(cloud, machine, type, key, num, gateway)
                        .invoke(false);

            }

        });
        TableData smttd = new TableData();
        smttd.setWidth("75%");
        smttd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(smtBtn, smttd);
        IconButton celBtn = new CedarIconButton("cedar_close");
        celBtn.setSize(80, 24);
        celBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                InstanceLauncher.this.getParentItem().close();
            }

        });
        TableData celtd = new TableData();
        celtd.setWidth("25%");
        celtd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(celBtn, celtd);
        FormData btnBarfd = new FormData();
        btnBarfd.setMargins(new Margins(40, 0, 0, 0));
        formPanel.add(btnBar, btnBarfd);

        // formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
        // formPanel.getButtonBar().setSpacing(30);
        // formPanel.getButtonBar().setStyleAttribute("padding-right", "75px");
        // formPanel.addButton(new Button("Submit", new
        // SelectionListener<ButtonEvent>(){
        //
        // @Override
        // public void componentSelected(ButtonEvent ce) {
        // CloudInfoBean cloud = null;
        // MachineInfoBean machine = null;
        // if(machineBean != null){
        // cloud = new CloudInfoBean();
        // cloud.setId(machineBean.getCloudId());
        // cloud.setCloudName(machineBean.getCloudName());
        // machine = machineBean;
        // }else{
        // cloud = comboCloud.getValue();
        // machine = comboImage.getValue();
        // }
        // MachineTypeInfoBean type = comboType.getValue();
        // KeyPairBean key = comboKey.getValue();
        //				
        // TabItem item = bottomPanel.getItemByItemId("error");
        // if(cloud == null){
        // bottomPanel.setSelection(item);
        // item.addText("cloud field is not selected");
        // item.layout();
        // return;
        // }
        //				
        // if(machine == null){
        // bottomPanel.setSelection(item);
        // item.addText("machine field is not selected");
        // item.layout();
        // return;
        // }
        //				
        // if(type == null){
        // bottomPanel.setSelection(item);
        // item.addText("type field is not selected");
        // item.layout();
        // return;
        // }
        //				
        // if(key == null){
        // bottomPanel.setSelection(item);
        // item.addText("key field is not selected");
        // item.layout();
        // return;
        // }
        //				
        // new InstanceRunner(cloud, machine, type, key).invoke(false);
        //				
        // }
        // }));
        // formPanel.addButton(new Button("Close", new
        // SelectionListener<ButtonEvent>(){
        //
        // @Override
        // public void componentSelected(ButtonEvent ce) {
        // InstanceLauncher.this.getParentItem().close();
        // }
        //			
        // }));

        return formPanel;
    }

    private Grid<NATInfoBean> natGrid;
    private GridSelectionModel<NATInfoBean> natSelectionModel = new GridSelectionModel<NATInfoBean>();
    private final ListStore<NATInfoBean> natStore = new ListStore<NATInfoBean>();
    private final Button btn1 = new Button("Edit");
    private final Button btn2 = new Button("Add");
    private final Button btn3 = new Button("Delete");

    public void refreshNatList() {
        // TODO: change according to the image selection
        ArrayList<NATInfoBean> natList = new ArrayList<NATInfoBean>();
        natStore.removeAll();
        if (machineBean != null) {
            String osName = machineBean.getOs();
            for (NATInfoBean bean : getDefaultPort(osName)) {
                bean.refresh();
                natList.add(bean);
            }
        } else {
            MachineInfoBean mbean = comboImage.getValue();
            if (mbean != null) {
                String osName = mbean.getOs();
                for (NATInfoBean bean : getDefaultPort(osName)) {
                    bean.refresh();
                    natList.add(bean);
                }
            }
        }
        natStore.add(natList);
    }

    public LayoutContainer createNATWidget() {
        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());

        FieldSet natfs = new FieldSet();
        FormLayout natfl = new FormLayout();
        FormData natfd = new FormData("0");
        natfs.setHeading("NAT Information");
        natfs.setLayout(natfl);

        LayoutContainer innerContainer = new LayoutContainer();
        innerContainer.setLayout(new ColumnLayout());

        ContentPanel left = new ContentPanel();
        left.setHeaderVisible(false);
        left.setBodyBorder(false);
        FormLayout inner = new FormLayout();
        FormData formData = new FormData("0");
        left.setLayout(new FitLayout());
        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(new ColumnConfig("Name", "Name", 100));
        columnConfigs.add(new ColumnConfig("Port", "Port", 100));
        ColumnModel cm = new ColumnModel(columnConfigs);
        natGrid = new Grid<NATInfoBean>(natStore, cm);
        natGrid.setAutoExpandColumn("Name");
        natGrid.setBorders(true);
        natGrid.setStyleAttribute("border-color", "#cfcfcf");
        natGrid.setStyleAttribute("border-style", "groove");
        natGrid.setStyleAttribute("margin-left", "10px");
        natGrid.setHeight(80);
        natGrid.setSelectionModel(natSelectionModel);
        left.add(natGrid, formData);

        VerticalPanel right = new VerticalPanel();
        right.setStyleAttribute("padding", "0 7 0 7");
        TableData td = new TableData();
        td.setHorizontalAlign(HorizontalAlignment.RIGHT);
        td.setPadding(2);

        btn1.setItemId("Edit");
        btn1.setWidth(50);
        btn1.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<NATInfoBean> sl = natSelectionModel.getSelection();
                NATInfoBean bean = sl.get(0);
                Dialog dialog = createDialog(bean);
                dialog.show();
            }

        });

        btn2.setItemId("Add");
        btn2.setWidth(50);
        btn2.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                Dialog dialog = createDialog(null);
                dialog.show();
            }

        });

        btn3.setItemId("Delete");
        btn3.setWidth(50);
        btn3.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<NATInfoBean> sl = natSelectionModel.getSelection();
                for (NATInfoBean td : sl) {
                    natStore.remove(td);
                }
            }

        });

        natSelectionModel
                .addSelectionChangedListener(new SelectionChangedListener<NATInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<NATInfoBean> se) {
                        if (natSelectionModel.getSelection().size() == 1) {
                            btn1.enable();
                        } else {
                            btn1.disable();
                        }
                        btn2.enable();
                        if (natSelectionModel.getSelection().size() > 0) {
                            btn3.enable();
                        } else {
                            btn3.disable();
                        }
                    }

                });
        if (natSelectionModel.getSelection().size() == 1) {
            btn1.enable();
        } else {
            btn1.disable();
        }
        btn2.enable();
        if (natSelectionModel.getSelection().size() > 0) {
            btn3.enable();
        } else {
            btn3.disable();
        }

        right.add(btn1, td);
        right.add(btn2, td);
        right.add(btn3, td);

        innerContainer.add(left, new ColumnData(0.8));
        innerContainer.add(right, new ColumnData(0.2));
        natfs.add(innerContainer, natfd);

        LayoutContainer placeHolder = new LayoutContainer();
        main.add(placeHolder, new ColumnData(.16));
        main.add(natfs, new ColumnData(.84));

        return main;
    }

    public Dialog createDialog(final NATInfoBean bean) {
        Dialog dialog = new Dialog();
        dialog.setWidth(300);
        dialog.setHeading("Edit");
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
        final TextField<String> pname = new TextField<String>();
        // pname.setAllowBlank(false);
        pname.setFieldLabel("Name");
        if (bean != null)
            pname.setValue(bean.getName());
        fp.add(pname, fd);

        final TextField<String> port = new TextField<String>();
        // port.setAllowBlank(false);
        port.setFieldLabel("Port");
        if (bean != null)
            port.setValue(bean.getPort().toString());
        fp.add(port, fd);

        Button okbtn = dialog.getButtonById(Dialog.OK);
        okbtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                String pstr = pname.getValue();
                if (pstr == null || pstr.length() == 0) {

                }
                NATInfoBean m = new NATInfoBean();
                String sport = port.getValue();
                Integer sNum = Integer.valueOf(sport);
                m.setName(pstr);
                m.setPort(sNum);
                m.refresh();
                ArrayList<NATInfoBean> added = new ArrayList<NATInfoBean>();
                added.add(m);
                if (bean != null)
                    natStore.remove(bean);
                natStore.add(added);
                natSelectionModel.setSelection(added);
            }

        });

        dialog.add(fp);
        return dialog;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);
        setLayout(borderLayout);

        ContentPanel north = new ContentPanel();
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
                450);
        northData.setMargins(new Margins(0, 0, 3, 0));
        northData.setSplit(true);
        northData.setFloatable(true);
        northData.setMinSize(350);
        north.setHeaderVisible(false);
        north.setBodyBorder(true);
        north.setLayout(new FlowLayout());
        north.add(createForm());

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);

        add(north, northData);
        add(createBottomPanel(), centerData);
    }

    @Override
    public void updateView() {
        // TODO Auto-generated method stub

    }

    private Dialog progressDlg;

    public class InstanceRunner extends RPCInvocation<Boolean> {
        CloudInfoBean cloud;
        MachineInfoBean machine;
        MachineTypeInfoBean type;
        KeyPairBean key;
        int num;
        GatewayInfoBean gateway;

        public InstanceRunner(CloudInfoBean cloud, MachineInfoBean machine,
                MachineTypeInfoBean type, KeyPairBean key, int num,
                GatewayInfoBean gateway) {
            super(false, true, true, false);
            this.setHideProgressAfterSuccess(true);

            this.cloud = cloud;
            this.machine = machine;
            this.type = type;
            this.key = key;
            this.num = num;
            this.gateway = gateway;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<Boolean> callback) {
            List<NATInfoBean> natList = natStore.getModels();
            for (NATInfoBean bean : natList) {
                bean.setGatewayId(gateway.getId());
            }
            remoteService.applyInstance(cloud, machine, type, key, num,
                    natList, callback);
        }

        public String getConfirmMsg() {
            return "Do you really want to start " + num + " new instances in "
                    + cloud.getCloudName() + "."
                    + " It may take you 5 minutes around to be ready for use.";
        }

        public void onComplete(Boolean obj) {
            /*
             * InstanceViewer view =
             * (InstanceViewer)ViewCache.createViewer(ViewType
             * .TYPE_INSTANCE_VIEW); view.addInstanceInfoBean(obj);
             */
            InstanceLauncher.this.getParentItem().close();
            MainPage.getInstance().addTabItem("Instance");
        }

        public String getProgressTitle() {
            return "Launching";
        }

        public String getProgressMsg() {
            return "Reserving instance from cloud...";
        }
    }

    public void setMachineBean(MachineInfoBean machineBean) {
        this.machineBean = machineBean;
    }

    public MachineInfoBean getMachineBean() {
        return machineBean;
    }

    public void setParentItem(TabItem parentItem) {
        this.parentItem = parentItem;
    }

    public TabItem getParentItem() {
        return parentItem;
    }

    public static enum OSPattern {
        WIN, REDHAT, CENTOS, UBUNTU, SUSE;
    }

    private static HashMap<OSPattern, NATInfoBean[]> NATMAP = new HashMap<OSPattern, NATInfoBean[]>();
    static {
        NATMAP.put(OSPattern.WIN, WINDEFPORT);
        NATMAP.put(OSPattern.REDHAT, LNXDEFPORT);
        NATMAP.put(OSPattern.CENTOS, LNXDEFPORT);
        NATMAP.put(OSPattern.UBUNTU, LNXDEFPORT);
        NATMAP.put(OSPattern.SUSE, LNXDEFPORT);
    }

    public static NATInfoBean[] getDefaultPort(String osName) {
        if (osName == null)
            return null;
        for (OSPattern pat : OSPattern.values()) {
            String value = pat.name().toLowerCase();
            if (osName.toLowerCase().indexOf(value) != -1)
                return NATMAP.get(pat);
        }
        // by default, use the Linux ports
        return LNXDEFPORT;
    }
}
