package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.FeatureJobInfoBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.model.NATInfoBean;
import com.intel.cedar.service.client.model.VolumeInfoBean;
import com.intel.cedar.service.client.resources.Resources;

public class InstanceViewer extends ComponentViewer {

    private static int REFRESH_INTERVAL = 5000;

    private RpcProxy<BaseListLoadResult<InstanceInfoBean>> proxy;
    private BaseListLoader<BaseListLoadResult<InstanceInfoBean>> loader;
    private GroupingStore<InstanceInfoBean> groupStore;
    private CheckBoxSelectionModel<InstanceInfoBean> sm;
    private Grid<InstanceInfoBean> grid;
    private Timer instanceTimer = new Timer() {

        @Override
        public void run() {
            scheduleLoader();
        }

    };
    private boolean refreshPending = false;
    private static InstanceViewer instance = null;

    public static InstanceViewer getInstance() {
        if (instance == null) {
            instance = new InstanceViewer();
        }

        return instance;
    }

    private InstanceViewer() {
        proxy = new RPCInvocation<BaseListLoadResult<InstanceInfoBean>>(false,
                false, false, false) {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<InstanceInfoBean>> callback) {
                remoteService.retrieveInstanceList(new ArrayList<String>(),
                        callback);
            }

            public void onComplete(BaseListLoadResult<InstanceInfoBean> res) {
                instanceTimer.schedule(REFRESH_INTERVAL);
            }
        };
        loader = new BaseListLoader<BaseListLoadResult<InstanceInfoBean>>(proxy);
        groupStore = new GroupingStore<InstanceInfoBean>(loader);
    }

    public void updateView() {
        scheduleLoader();
    }

    public void scheduleLoader() {
        loader.load();
    }

    public void scheduleRefresh() {
        if (refreshPending) {
            instanceTimer.cancel();
        }
        loader.load();
        instanceTimer.schedule(REFRESH_INTERVAL);
        refreshPending = true;
    }

    public void cancelRefresh() {
        instanceTimer.cancel();
        refreshPending = false;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        sm = new CheckBoxSelectionModel<InstanceInfoBean>();
        sm.setSelectionMode(SelectionMode.MULTI);

        loader.setRemoteSort(true);
        groupStore.groupBy("Cloud");
        loader.load();

        GridCellRenderer<InstanceInfoBean> statusBarRenderer = new GridCellRenderer<InstanceInfoBean>() {

            @Override
            public Object render(InstanceInfoBean model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<InstanceInfoBean> store,
                    Grid<InstanceInfoBean> grid) {
                if (!((String) model.get(property)).equalsIgnoreCase("running")) {
                    Image img = new Image(Resources.ICONS.progressBar());
                    return img;
                } else
                    return "<span style=color:green;font-size:14px;font-weight:bold>"
                            + model.get(property) + "</span>";
            }

        };

        // XTemplate eTemp = XTemplate.create("<p><b>{Instance}</b><p>");
        // RowExpander expander = new RowExpander();
        // expander.setTemplate(eTemp);

        List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        // columnConfigs.add(expander);
        columnConfigs.add(sm.getColumn());
        columnConfigs.add(new ColumnConfig("Cloud", "Cloud", 30));
        columnConfigs.add(new ColumnConfig("Instance", "Instance", 70));
        columnConfigs.add(new ColumnConfig("IP", "IP", 90));
        columnConfigs.add(new ColumnConfig("PrivateIP", "PrivateIP", 90));
        columnConfigs.add(new ColumnConfig("PrivateDNS", "PrivateDNS", 60));
        columnConfigs.add(new ColumnConfig("Arch", "Arch", 50));
        columnConfigs.add(new ColumnConfig("OS", "OS", 160));
        columnConfigs.add(new ColumnConfig("Comment", "Comment", 140));
        ColumnConfig ccState = new ColumnConfig("State", "State", 90);
        ccState.setRenderer(statusBarRenderer);
        ccState.setFixed(true);
        columnConfigs.add(ccState);
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            columnConfigs.add(new ColumnConfig("User", "User", 40));
        }

        final ColumnModel columnModel = new ColumnModel(columnConfigs);
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        // view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = columnModel.getColumnById(data.field)
                        .getHeader();
                String u = data.models.size() == 1 ? "Instance" : "Instances";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });

        grid = new Grid<InstanceInfoBean>(groupStore, columnModel);
        grid.setBorders(false);
        grid.setStateful(true);
        grid.setView(view);
        grid.setAutoExpandColumn("Comment");
        // grid.addPlugin(expander);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        grid.setContextMenu(createContextMenu());

        ToolBar topBar = new ToolBar();
        topBar.setAlignment(HorizontalAlignment.RIGHT);
        final Button tmtBtn = new Button("terminate", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()));
        tmtBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final List<InstanceInfoBean> list = sm.getSelectedItems();
                new RPCInvocation<List<InstanceInfoBean>>(false, true, true,
                        true) {

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<List<InstanceInfoBean>> callback) {
                        remoteService.terminateInstance(list, callback);
                    }

                    public String getProgressTitle() {
                        return "Terminate Instance";
                    }

                    public String getProgressText() {
                        return "terminating...";
                    }

                    public String getSuccessMsg() {
                        return "Successfully terminated "
                                + ((list.size() > 1) ? "these instances"
                                        : "the instance");
                    }

                    public String getConfirmMsg() {
                        InstanceInfoBean pooledInstance = null;
                        for (InstanceInfoBean i : list) {
                            if (i.getPooled()) {
                                pooledInstance = i;
                                break;
                            }
                        }
                        return "Really want to terminate "
                                + ((list.size() > 1) ? "these instances"
                                        : "the instance")
                                + ((pooledInstance != null) ? (", there's a pooled instance("
                                        + pooledInstance.getInstanceId() + ")")
                                        : "");
                    }

                    public void onComplete(List<InstanceInfoBean> obj) {
                        for (InstanceInfoBean bean : obj) {
                            groupStore.remove(bean);
                        }
                    }

                }.invoke(false);
            }

        });
        tmtBtn.setEnabled(false);
        sm
                .addSelectionChangedListener(new SelectionChangedListener<InstanceInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<InstanceInfoBean> se) {
                        if (se.getSelection().size() > 0) {
                            tmtBtn.setEnabled(true);
                            cancelRefresh();
                        } else {
                            tmtBtn.setEnabled(false);
                            scheduleRefresh();
                        }
                    }

                });
        topBar.add(tmtBtn);

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.add(grid);
        _contentPanel.setTopComponent(topBar);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    public Menu createContextMenu() {

        final Menu menu = new Menu();
        MenuItem outputItem = new MenuItem("Show Ouput Console");
        outputItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(InstanceOperation.SHOW_CONSOLE_OUTPUT);
                menu.hide();
            }

        });
        final MenuItem volumeItem = new MenuItem("Show Attached Volumes");
        volumeItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(InstanceOperation.SHOW_ATTACHED_VOLUMES);
                menu.hide();
            }
        });
        final MenuItem attachVolumeItem = new MenuItem("Attach a New Volume");
        attachVolumeItem
                .addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        createDialog(InstanceOperation.ATTACH_NEW_VOLUME);
                        menu.hide();
                    }

                });
        final MenuItem syncDateTimeItem = new MenuItem("Synchronize Date & Time");
        syncDateTimeItem
                .addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        final InstanceInfoBean ins = sm.getSelectedItem();
                        if (ins != null && ins.getState().equals("running")) {
                            new RPCInvocation<Boolean>() {
                                @Override
                                public void onComplete(Boolean result) {
                                    if (result) {
                                        MessageBox
                                                .info(
                                                        "Info",
                                                        "Date & Time synchronized successfully",
                                                        null).show();
                                    } else {
                                        MessageBox
                                                .info(
                                                        "Info",
                                                        "Failed to synchronize Date & Time",
                                                        null).show();
                                    }
                                }

                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<Boolean> callback) {
                                    remoteService.syncDateTime(ins, callback);
                                }
                            }.invoke(false);
                        }
                        menu.hide();
                    }
                });

        final MenuItem changePasswordItem = new MenuItem("Reset Password");
        changePasswordItem
                .addSelectionListener(new SelectionListener<MenuEvent>() {

                    @Override
                    public void componentSelected(MenuEvent ce) {
                        if (sm.getSelectedItem().getState().equals("running")) {
                            createDialog(InstanceOperation.CHANGE_ROOT_PASSWORD);
                        }
                        menu.hide();
                    }

                });

        final MenuItem remoteDesktop = new MenuItem("Remote Desktop");
        remoteDesktop.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                if (sm.getSelectedItem().getState().equals("running")) {
                    Window.Location.assign("/rest/"
                            + sm.getSelectedItem().getCloudName() + "_"
                            + sm.getSelectedItem().getInstanceId() + "/view");
                }
                menu.hide();
            }
        });

        final MenuItem remoteDisplay = new MenuItem("Remote Console");
        remoteDisplay.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                if (sm.getSelectedItem().getRemoteDisplay() != null
                        && !sm.getSelectedItem().getRemoteDisplay().startsWith(
                                "unknown")) {
                    Window.Location
                            .assign("/rest/"
                                    + sm.getSelectedItem().getCloudName() + "_"
                                    + sm.getSelectedItem().getInstanceId()
                                    + "/console");
                }
                menu.hide();
            }
        });

        final MenuItem addPort = new MenuItem("Add Port Mapping");
        addPort.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                if (sm.getSelectedItem().getGatewayId() > 0){
                    createAddPortDialog(sm.getSelectedItem()).show();
                }
                menu.hide();
            }
        });
        
        final MenuItem showDetailItem = new MenuItem("Show Detail Info");
        showDetailItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(InstanceOperation.SHOW_DETAIL);
                menu.hide();
            }

        });

        MenuItem rebootItem = new MenuItem("Reboot");
        rebootItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                if (!sm.getSelectedItem().getIp().equals("0.0.0.0")) {
                    final InstanceInfoBean instance = sm.getSelectedItem();
                    new RPCInvocation<Boolean>(false, false, true, false) {
                        @Override
                        public void execute(
                                CloudRemoteServiceAsync remoteService,
                                AsyncCallback<Boolean> callback) {
                            remoteService.rebootInstance(instance, callback);
                        }

                        public String getConfirmMsg() {
                            return "Really want to reboot the instance: "
                                    + instance.getIp() + "?";
                        }
                    }.invoke(false);
                }
                menu.hide();
            }
        });

        menu.add(outputItem);
        menu.add(volumeItem);
        menu.add(attachVolumeItem);
        menu.add(addPort);
        menu.add(new SeparatorMenuItem());
        menu.add(rebootItem);
        menu.add(syncDateTimeItem);
        menu.add(changePasswordItem);
        menu.add(remoteDesktop);
        menu.add(remoteDisplay);
        menu.add(new SeparatorMenuItem());
        menu.add(showDetailItem);

        menu.addListener(Events.BeforeShow, new Listener<MenuEvent>(){
            @Override
            public void handleEvent(MenuEvent be) {
                InstanceInfoBean i = (InstanceInfoBean)sm.getSelectedItem();
                addPort.disable();
                if(i.getManaged() && i.getState().equals("running")){
                    syncDateTimeItem.enable();
                    changePasswordItem.enable();
                    remoteDesktop.enable();
                }
                else{
                    syncDateTimeItem.disable();
                    changePasswordItem.disable();
                    remoteDesktop.disable();
                }
                if(i.getState().equals("running")){
                    volumeItem.enable();
                    attachVolumeItem.enable();
                    if(i.getGatewayId() > 0){
                        addPort.enable();
                    }
                }
                else {
                    volumeItem.disable();
                    attachVolumeItem.disable(); 
                }
                if(i.getRemoteDisplay() != null && !i.getRemoteDisplay().startsWith("unknown")){
                    remoteDisplay.enable();
                }
                else{
                    remoteDisplay.disable();
                }
            }     
        });
        return menu;

    }

    public enum InstanceOperation {
        SHOW_CONSOLE_OUTPUT, SYNC_DATETIME, SHOW_ATTACHED_VOLUMES, ATTACH_NEW_VOLUME, CHANGE_ROOT_PASSWORD, SHOW_DETAIL;
    }

    public void createDialog(InstanceOperation op) {
        InstanceInfoBean ins = sm.getSelectedItem();
        if (ins != null) {
            switch (op) {
            case SHOW_CONSOLE_OUTPUT:
                createSCODialog(ins);
                break;
            case SHOW_ATTACHED_VOLUMES:
                createSAVDialog(ins).show();
                break;
            case ATTACH_NEW_VOLUME:
                createAVDialog(ins).show();
                break;
            case CHANGE_ROOT_PASSWORD:
                createCRTDialog(ins).show();
                break;
            case SHOW_DETAIL:
                createSDialog(ins).show();
                break;
            default:
                break;
            }
        } else {
            MessageBox.alert("Alert", "Please select an instance", null).show();
        }
    }

    public Dialog createSCODialog(final InstanceInfoBean ins) {
        final Dialog dialog = new Dialog();
        dialog.setHeading("Console Ouput");
        dialog.setSize(400, 300);
        dialog.setResizable(false);
        dialog.setHideOnButtonClick(true);

        final FormPanel formPanel = new FormPanel();
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(60);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setLayout(fl);

        final TextField<String> insTF = new TextField<String>();
        insTF.setFieldLabel("Instance");
        insTF.setReadOnly(true);
        insTF.setValue(ins.getInstanceId());
        FormData insfd = new FormData();
        insfd.setWidth(100);
        insfd.setMargins(new Margins(0, 0, 10, 0));
        formPanel.add(insTF, insfd);

        final TextArea opTA = new TextArea();
        opTA.setFieldLabel("Output");
        opTA.setHeight(175);
        // opTA.setValue("Retrieving Output ...");
        FormData opfd = new FormData("0");
        formPanel.add(opTA, opfd);

        new RPCInvocation<String>(false, true, false, false) {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<String> callback) {
                remoteService.showConsoleOutput(ins, callback);
            }

            public void onComplete(String obj) {
                opTA.setValue(obj);
                dialog.show();
            }

            public String getProgressTitle() {
                return "Get Console Ouput";
            }

            public String getProgressText() {
                return "retrieving...please wait";
            }

        }.invoke(false);

        dialog.add(formPanel);
        return dialog;
    }
    
    public Dialog createAddPortDialog(final InstanceInfoBean ins) {
        Dialog dialog = new Dialog();
        dialog.setWidth(300);
        dialog.setHeading("Add");
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
        fp.add(pname, fd);

        final TextField<String> port = new TextField<String>();
        // port.setAllowBlank(false);
        port.setFieldLabel("Port");
        fp.add(port, fd);

        Button okbtn = dialog.getButtonById(Dialog.OK);
        okbtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                String pstr = pname.getValue();
                if (pstr == null || pstr.length() == 0) {

                }
                final NATInfoBean m = new NATInfoBean();
                String sport = port.getValue();
                Integer sNum = Integer.valueOf(sport);
                m.setName(pstr);
                m.setPort(sNum);
                m.refresh();
                
                new RPCInvocation<Integer>(false, true, false, false) {

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Integer> callback) {
                        remoteService.addPortMapping(ins, m, callback);
                    }

                    public void onComplete(Integer obj) {
                        if(obj > 0){
                            MessageBox
                            .info(
                                    "Info",
                                    m.getName() + " is mapped to " + obj + " successfully",
                                    null).show(); 
                        }
                        else{
                            MessageBox
                            .info(
                                    "Info",
                                    "Failed to add port mapping, already exists?",
                                    null).show();                            
                        }
                    }

                }.invoke(false);
            }
        });

        dialog.add(fp);
        return dialog;
    }
    
    public Dialog createSDialog(final InstanceInfoBean ins) {
        final Dialog dialog = new Dialog();
        dialog.setHeading("Instance Details");
        dialog.setSize(400, 300);
        dialog.setResizable(false);
        dialog.setHideOnButtonClick(true);

        FormPanel formPanel = new FormPanel();
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(60);
        formPanel.setLayout(fl);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);

        TextField<String> insTF = new TextField<String>();
        insTF.setFieldLabel("Instance");
        insTF.setReadOnly(true);
        insTF.setValue(ins.getInstanceId());
        FormData insfd = new FormData();
        insfd.setWidth(100);
        insfd.setMargins(new Margins(0, 0, 10, 0));
        formPanel.add(insTF, insfd);

        final TextArea opTA = new TextArea();
        opTA.setFieldLabel("Details");
        // opTA.setValue("Retrieving login info ...");
        opTA.setHeight(175);
        FormData opfd = new FormData("0");
        formPanel.add(opTA, opfd);

        new RPCInvocation<String>(false, true, false, false) {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<String> callback) {
                remoteService.showInstanceInfo(ins, callback);
            }

            public void onComplete(String obj) {
                opTA.setValue(obj);
                dialog.show();
            }

            public String getProgressTitle() {
                return "Get Login Info";
            }

            public String getProgressText() {
                return "retrieving...please wait";
            }

        }.invoke(false);

        dialog.add(formPanel);
        return dialog;
    }

    public Dialog createSAVDialog(final InstanceInfoBean ins) {
        Dialog dialog = new Dialog();
        dialog.setHeading("Show Volumes");
        dialog.setSize(400, 250);
        dialog.setResizable(false);
        dialog.setHideOnButtonClick(true);

        FormPanel formPanel = new FormPanel();
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(60);
        formPanel.setLayout(fl);
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);

        final TextField<String> insTextField = new TextField<String>();
        insTextField.setFieldLabel("Instance");
        insTextField.setReadOnly(false);
        insTextField.setValue(ins.getInstanceId());
        FormData insfd = new FormData();
        insfd.setWidth(100);
        insfd.setMargins(new Margins(0, 0, 10, 0));
        formPanel.add(insTextField, insfd);

        RpcProxy<BaseListLoadResult<VolumeInfoBean>> vProxy = new RPCInvocation<BaseListLoadResult<VolumeInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback) {
                remoteService.retrieveAttachedVolumes(ins, callback);
            }

        };
        final BaseListLoader<BaseListLoadResult<VolumeInfoBean>> vLoader = new BaseListLoader<BaseListLoadResult<VolumeInfoBean>>(
                vProxy);
        final ListStore<VolumeInfoBean> vStore = new ListStore<VolumeInfoBean>(
                vLoader);
        LayoutContainer container = new LayoutContainer();
        ColumnLayout columnLayout = new ColumnLayout();
        container.setLayout(columnLayout);
        Label label = new Label("Attached Volumes:");
        container.add(label,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.15));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(new ColumnConfig("VolumeId", "VolumeId", 70));
        configs.add(new ColumnConfig("Size", "Size", 40));
        configs.add(new ColumnConfig("Path", "Path", 80));
        ColumnModel cm = new ColumnModel(configs);
        vLoader.load();
        final GridSelectionModel<VolumeInfoBean> volsm = new GridSelectionModel<VolumeInfoBean>();
        volsm.setSelectionMode(SelectionMode.SINGLE);
        Grid<VolumeInfoBean> grid = new Grid<VolumeInfoBean>(vStore, cm);
        grid.setAutoExpandColumn("VolumeId");
        grid.setBorders(true);
        grid.setStyleAttribute("border-color", "#cfcfcf");
        grid.setStyleAttribute("border-style", "groove");
        grid.setStyleAttribute("margin-left", "10px");
        grid.setHeight(120);
        grid.setSelectionModel(volsm);
        container.add(grid,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.65));
        final Button detButton = new Button("Detach");
        detButton.setWidth(40);
        detButton.setStyleAttribute("padding", "0 0 0 10");
        detButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                new RPCInvocation<List<VolumeInfoBean>>(false, true, false,
                        false) {

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<List<VolumeInfoBean>> callback) {
                        List<VolumeInfoBean> list = volsm.getSelectedItems();
                        remoteService.detachVolumes(list, callback);
                    }

                    public void onComplete(List<VolumeInfoBean> obj) {
                        vLoader.load();
                    }

                    public String getProgressTitle() {
                        return "Detach Volume";
                    }

                    public String getProgressText() {
                        return "detaching... please wait";
                    }

                }.invoke(false);

            }
        });
        volsm
                .addSelectionChangedListener(new SelectionChangedListener<VolumeInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<VolumeInfoBean> se) {
                        if (volsm.getSelectedItems().size() > 0) {
                            detButton.enable();
                        } else {
                            detButton.disable();
                        }
                    }
                });
        if (volsm.getSelectedItems().size() > 0) {
            // detButton.enable();
        } else {
            detButton.disable();
        }
        container.add(detButton,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.15));

        FormData formData = new FormData("0");
        formPanel.add(container, formData);

        dialog.add(formPanel);
        return dialog;
    }

    public Dialog createAVDialog(final InstanceInfoBean ins) {
        Dialog dialog = new Dialog();
        dialog.setHeading("Attach Volume");
        dialog.setSize(400, 250);
        dialog.setResizable(false);
        dialog.setHideOnButtonClick(true);

        FormPanel formPanel = new FormPanel();
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(60);
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setLayout(formLayout);

        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel("Instance");
        textField.setReadOnly(true);
        textField.setValue(ins.getInstanceId());
        FormData insfd = new FormData();
        insfd.setWidth(100);
        insfd.setMargins(new Margins(0, 0, 10, 0));
        formPanel.add(textField, insfd);

        RpcProxy<BaseListLoadResult<VolumeInfoBean>> vProxy = new RPCInvocation<BaseListLoadResult<VolumeInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback) {
                remoteService.retrieveAvailableVolumes(ins, callback);
            }

        };
        final BaseListLoader<BaseListLoadResult<VolumeInfoBean>> vLoader = new BaseListLoader<BaseListLoadResult<VolumeInfoBean>>(
                vProxy);
        final ListStore<VolumeInfoBean> vStore = new ListStore<VolumeInfoBean>(
                vLoader);
        LayoutContainer container = new LayoutContainer();
        ColumnLayout columnLayout = new ColumnLayout();
        container.setLayout(columnLayout);
        Label label = new Label("Available Volumes:");
        container.add(label,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.15));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(new ColumnConfig("VolumeId", "VolumeId", 70));
        configs.add(new ColumnConfig("Size", "Size", 40));
        configs.add(new ColumnConfig("Comment", "Comment", 100));
        ColumnModel cm = new ColumnModel(configs);
        vLoader.load();
        final GridSelectionModel<VolumeInfoBean> volsm = new GridSelectionModel<VolumeInfoBean>();
        volsm.setSelectionMode(SelectionMode.SINGLE);
        Grid<VolumeInfoBean> grid = new Grid<VolumeInfoBean>(vStore, cm);
        grid.setAutoExpandColumn("Comment");
        grid.setBorders(true);
        grid.setStyleAttribute("border-color", "#cfcfcf");
        grid.setStyleAttribute("border-style", "groove");
        grid.setStyleAttribute("margin-left", "10px");
        grid.setHeight(120);
        grid.setSelectionModel(volsm);
        container.add(grid,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.65));
        final Button athButton = new Button("Attach");
        athButton.setWidth(40);
        athButton.setStyleAttribute("padding", "0 0 0 10");
        athButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                VolumeInfoBean vBean = volsm.getSelectedItem();
                String path = vBean.getPath();
                if (ins.getManaged()){
                    MessageBox box = new MessageBox();
                    box.setTitle("Confirm");
                    String mountMsg = "";
                    if(path != null && path.length() > 0) {
                        mountMsg = "This volume is previously mounted to " + path + "\n";
                    }
                    box.setMessage(mountMsg + "Click 'Yes' to format and attach it, 'No' to just attach it");
                    box.addCallback(new Listener<MessageBoxEvent>() {
                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            String btnText = be.getButtonClicked().getText();
                            if (btnText
                                    .equals(GXT.MESSAGES.messageBox_cancel()))
                                return;
                            boolean yesClicked = false;
                            if (btnText.equals(GXT.MESSAGES.messageBox_yes())) {
                                yesClicked = true;
                            }
                            final boolean formatAttach = yesClicked;
                            new RPCInvocation<VolumeInfoBean>(false, true,
                                    false, false) {
                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<VolumeInfoBean> callback) {
                                    VolumeInfoBean vBean = volsm
                                            .getSelectedItem();
                                    remoteService.attachVolume(formatAttach,
                                            vBean, ins, callback);
                                }

                                public void onComplete(VolumeInfoBean obj) {
                                    if (obj != null) {
                                        vStore.remove(obj);
                                        MessageBox.info("Info",
                                                "Attach successfully", null)
                                                .show();
                                    } else {
                                        MessageBox.alert("Alert",
                                                "Failed to attach", null)
                                                .show();
                                    }
                                    vLoader.load();
                                }

                                public String getProgressTitle() {
                                    return "Attach Volume";
                                }

                                public String getProgressText() {
                                    return "attaching... please wait";
                                }

                            }.invoke(false);
                        }
                    });
                    box.setIcon(MessageBox.QUESTION);
                    box.setButtons(MessageBox.YESNOCANCEL);
                    box.show();               
                } else {
                    new RPCInvocation<VolumeInfoBean>(false, true, false, false) {
                        @Override
                        public void execute(
                                CloudRemoteServiceAsync remoteService,
                                AsyncCallback<VolumeInfoBean> callback) {
                            VolumeInfoBean vBean = volsm.getSelectedItem();
                            remoteService.attachVolume(false, vBean, ins,
                                    callback);
                        }

                        public void onComplete(VolumeInfoBean obj) {
                            if (obj != null) {
                                vStore.remove(obj);
                                MessageBox.info("Info", "Attach successfully",
                                        null).show();
                            } else {
                                MessageBox.alert("Alert", "Failed to attach",
                                        null).show();
                            }
                            vLoader.load();
                        }

                        public String getProgressTitle() {
                            return "Attach Volume";
                        }

                        public String getProgressText() {
                            return "attaching... please wait";
                        }

                    }.invoke(false);
                }
            }
        });
        volsm
                .addSelectionChangedListener(new SelectionChangedListener<VolumeInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<VolumeInfoBean> se) {
                        if (volsm.getSelectedItems().size() > 0) {
                            athButton.enable();
                        } else {
                            athButton.disable();
                        }
                    }
                });
        if (volsm.getSelectedItems().size() > 0) {
            athButton.enable();
        } else {
            athButton.disable();
        }
        container.add(athButton,
                new com.extjs.gxt.ui.client.widget.layout.ColumnData(.15));

        FormData formData = new FormData("0");
        formPanel.add(container, formData);

        dialog.add(formPanel);
        return dialog;
    }

    public Dialog createCRTDialog(final InstanceInfoBean ins) {
        final Dialog dialog = new Dialog();
        dialog.setHeading("Reset password for "
                + (ins.getOs().contains("Win") ? "administrator" : "root"));
        dialog.setSize(300, 210);
        dialog.setResizable(false);
        dialog.setHideOnButtonClick(true);

        final FormPanel formPanel = new FormPanel();
        FormLayout formLayout = new FormLayout();
        FormData formData = new FormData("0");
        formData.setMargins(new Margins(0, 0, 6, 0));
        formLayout.setLabelWidth(100);
        formPanel.setLayout(formLayout);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);

        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel("Instance");
        textField.setReadOnly(true);
        textField.setValue(ins.getInstanceId());
        formPanel.add(textField, formData);

        final TextField<String> newPwd = new TextField<String>();
        newPwd.setFieldLabel("New Password");
        newPwd.setPassword(true);
        formPanel.add(newPwd, formData);

        final TextField<String> confirmPwd = new TextField<String>();
        confirmPwd.setFieldLabel("Confirm");
        confirmPwd.setPassword(true);
        formPanel.add(confirmPwd, formData);

        final Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.setEnabled(false);

        final Text note = new Text();
        final FormData notefd = new FormData();
        note.setTagName("p");
        note.setStyleName("warning");

        Listener<FieldEvent> rListener = new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                String nP = newPwd.getValue();
                String cP = confirmPwd.getValue();
                if (Util.isEmptyString(nP) || Util.isEmptyString(cP)) {
                    note.setText("password must not be empty");
                    int index = formPanel.indexOf(confirmPwd);
                    formPanel.insert(note, index + 1, notefd);
                    dialog.setSize(300, 240);
                    dialog.layout();
                    okBtn.setEnabled(false);
                    return;
                }
                if (nP.length() < 6 || cP.length() < 6) {
                    note.setText("password is too short");
                    int index = formPanel.indexOf(confirmPwd);
                    formPanel.insert(note, index + 1, notefd);
                    dialog.setSize(300, 240);
                    dialog.layout();
                    okBtn.setEnabled(false);
                    return;
                }
                if (!nP.equals(cP)) {
                    note.setText("two passwords must be the same");
                    int index = formPanel.indexOf(confirmPwd);
                    formPanel.insert(note, index + 1, notefd);
                    dialog.setSize(300, 240);
                    dialog.layout();
                    okBtn.setEnabled(false);
                    return;
                }
                if (formPanel.indexOf(note) >= 0) {
                    note.setText("");
                    formPanel.remove(note);
                    dialog.setSize(300, 210);
                    dialog.layout();
                    okBtn.setEnabled(true);
                }
            }
        };

        newPwd.addListener(Events.KeyUp, rListener);
        confirmPwd.addListener(Events.KeyUp, rListener);

        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final String nP = newPwd.getValue();
                if (note.getText() != null && note.getText().length() > 0) {
                    return;
                }

                new RPCInvocation<Boolean>() {
                    @Override
                    public void onComplete(Boolean result) {
                        if (result) {
                            MessageBox.info("Info",
                                    "Password reset successfully", null).show();
                        } else {
                            MessageBox.info("Info", "Failed to reset password",
                                    null).show();
                        }
                    }

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Boolean> callback) {
                        remoteService.changePassword(ins, nP, callback);
                    }
                }.invoke(false);
            }
        });

        dialog.add(formPanel);
        return dialog;
    }
}
