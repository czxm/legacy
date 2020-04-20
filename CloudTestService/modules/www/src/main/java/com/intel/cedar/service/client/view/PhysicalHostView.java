package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.model.OS;
import com.intel.cedar.service.client.model.PhysicalNodeInfoBean;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.util.Util;
import com.intel.cedar.service.client.view.ImageViewer.ImageOperation;

public class PhysicalHostView extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 10000;
    private boolean refreshPending = false;
    private static PhysicalHostView instance;
    private Grid<PhysicalNodeInfoBean> grid;
    private RpcProxy<BaseListLoadResult<PhysicalNodeInfoBean>> nodeProxy;
    private ListLoader<BaseListLoadResult<PhysicalNodeInfoBean>> nodeLoader;
    private GroupingStore<PhysicalNodeInfoBean> nodeStore;
    private CheckBoxSelectionModel<PhysicalNodeInfoBean> sm;

    private PhysicalHostView() {
        nodeProxy = new RPCInvocation<BaseListLoadResult<PhysicalNodeInfoBean>>() {
            @Override
            public void onComplete(BaseListLoadResult<PhysicalNodeInfoBean> obj) {
                UserInfoBean user = MainPage.getInstance().getUserProfile();
                ArrayList<PhysicalNodeInfoBean> result = new ArrayList<PhysicalNodeInfoBean>();
                for (PhysicalNodeInfoBean bean : obj.getData()) {
                    if (user.getAdmin()
                            || bean.getUserId().equals(user.getId())) {
                        result.add(bean);
                    }
                }
                obj.setData(result);
                super.onComplete(obj);
            }

            @Override
            public void execute(
                    CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<PhysicalNodeInfoBean>> callback) {
                remoteService.retrievePhysicalNodeList(null, callback);
            }

        };
        nodeLoader = new BaseListLoader<BaseListLoadResult<PhysicalNodeInfoBean>>(
                nodeProxy);
        nodeStore = new GroupingStore<PhysicalNodeInfoBean>(nodeLoader);
    }

    @Override
    public void updateView() {
        nodeLoader.load();
    }

    private ContentPanel cp;

    public void onRender(Element element, int index) {
        super.onRender(element, index);

        sm = new CheckBoxSelectionModel<PhysicalNodeInfoBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        nodeLoader.setRemoteSort(true);
        nodeStore.groupBy("CloudName");
        nodeLoader.load();

        GridCellRenderer<PhysicalNodeInfoBean> statusBarRenderer = new GridCellRenderer<PhysicalNodeInfoBean>() {

            @Override
            public Object render(PhysicalNodeInfoBean model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<PhysicalNodeInfoBean> store,
                    Grid<PhysicalNodeInfoBean> grid) {
                if (!((String) model.get(property)).equalsIgnoreCase("running")) {
                    Image img = new Image(Resources.ICONS.progressBar());
                    return img;
                } else
                    return "<span style=color:green;font-size:14px;font-weight:bold>"
                            + model.get(property) + "</span>";
            }

        };

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(sm.getColumn());
        configs.add(new ColumnConfig("CloudName", "CloudName", 100));
        configs.add(new ColumnConfig("Host", "Host", 130));
        configs.add(new ColumnConfig("Os", "OS", 180));
        configs.add(new ColumnConfig("Arch", "Arch", 50));
        configs.add(new ColumnConfig("Comment", "Comment", 200));
        configs.add(new ColumnConfig("RootPath", "Root Path", 100));
        ColumnConfig ccState = new ColumnConfig("State", "State", 100);
        ccState.setRenderer(statusBarRenderer);
        ccState.setFixed(true);
        configs.add(ccState);
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            configs.add(new ColumnConfig("User", "User", 50));
        }
        final ColumnModel columnModel = new ColumnModel(configs);

        ToolBar toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        toolBar.add(new Button("Register", AbstractImagePrototype
                .create(Resources.ICONS.register()),
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        createRegistrationDialog("Register a Host").show();
                    }
                }));

        final Button editBtn = new Button("Edit", AbstractImagePrototype
                .create(Resources.ICONS.editCloud()),
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TodoViewer.show();
                    }
                });
        editBtn.setEnabled(false);
        toolBar.add(editBtn);

        final Button discardBtn = new Button("Descard", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final PhysicalNodeInfoBean node = sm.getSelectedItem();
                        if (node != null) {
                            RPCInvocation<Boolean> invoke = new RPCInvocation<Boolean>() {
                                @Override
                                public void onComplete(Boolean obj) {
                                    if (obj)
                                        updateView();
                                }

                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<Boolean> callback) {
                                    remoteService.discardPhysicalNode(node,
                                            callback);
                                }
                            };
                            invoke.invoke(false);
                        }
                    }
                });
        discardBtn.setEnabled(false);
        toolBar.add(discardBtn);

        sm
                .addSelectionChangedListener(new SelectionChangedListener<PhysicalNodeInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<PhysicalNodeInfoBean> se) {
                        if (se.getSelectedItem() != null) {
                            editBtn.setEnabled(true);
                            discardBtn.setEnabled(true);
                        } else {
                            editBtn.setEnabled(false);
                            discardBtn.setEnabled(false);
                        }
                    }
                });

        GroupingView groupView = new GroupingView();
        groupView.setShowGroupedColumn(false);
        groupView.setForceFit(false);
        groupView.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = columnModel.getColumnById(data.field)
                        .getHeader();
                String u = data.models.size() == 1 ? "Host" : "Hosts";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });
        final Grid<PhysicalNodeInfoBean> grid = new Grid<PhysicalNodeInfoBean>(
                nodeStore, columnModel);
        grid.setBorders(false);
        grid.setView(groupView);
        grid.setStateful(true);
        grid.setAutoExpandColumn("Comment");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        grid.setContextMenu(createContextMenu());

        cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setBorders(false);
        cp.setLayout(new FitLayout());
        cp.add(grid);
        cp.setTopComponent(toolBar);

        setLayout(new FitLayout());
        add(cp);
    }

    public static ComponentViewer getInstance() {
        if (instance == null) {
            instance = new PhysicalHostView();
        }
        return instance;
    }

    public Menu createContextMenu() {

        final Menu menu = new Menu();

        MenuItem syncDateTimeItem = new MenuItem("Synchronize Date & Time");
        syncDateTimeItem
                .addSelectionListener(new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        final PhysicalNodeInfoBean ins = sm.getSelectedItem();
                        if (ins != null) {
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
                    }
                });

        MenuItem changePasswordItem = new MenuItem("Reset Password");
        changePasswordItem
                .addSelectionListener(new SelectionListener<MenuEvent>() {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        createCRTDialog(sm.getSelectedItem()).show();
                        menu.hide();
                    }
                });
        MenuItem remoteDesktop = new MenuItem("Remote Desktop");
        remoteDesktop.addSelectionListener(new SelectionListener<MenuEvent>() {
            @Override
            public void componentSelected(MenuEvent ce) {
                Window.Location.assign("/rest/"
                        + sm.getSelectedItem().getHost() + "/view");
                menu.hide();
            }
        });
        
        MenuItem propertyItem = new MenuItem("Set Properties");
        propertyItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(ImageOperation.SET_PROPERTIES);
                menu.hide();
            }
        });
        MenuItem capabilityItem = new MenuItem("Set Capabilities");
        capabilityItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(ImageOperation.SET_CAPATITIES);
                menu.hide();
            }
        });


        menu.add(syncDateTimeItem);
        menu.add(changePasswordItem);
        menu.add(new SeparatorMenuItem());
        menu.add(remoteDesktop);
        menu.add(propertyItem);
        menu.add(capabilityItem);

        return menu;

    }

    public void createDialog(ImageOperation op) {
        PhysicalNodeInfoBean mach = sm.getSelectedItem();
        if(op.equals(ImageOperation.SET_PROPERTIES)){
            createPropertiesDialog(mach).show();
        }else if(op.equals(ImageOperation.SET_CAPATITIES)){
            createCapabilitiesDialog(mach).show();
        }
    }

    protected  Dialog createPropertiesDialog(final PhysicalNodeInfoBean mach){
        
        final Dialog dialog = new Dialog();
        dialog.setClosable(true);
        dialog.setAutoHeight(true);
        dialog.setWidth(535);
        dialog.setScrollMode(Scroll.NONE);
        dialog.setLayout(new FlowLayout(10));    
        ContentPanel cp = new ContentPanel();
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();  
        final CheckBoxSelectionModel<PropertyPair> selc = new CheckBoxSelectionModel<PropertyPair>();
        selc.setSelectionMode(SelectionMode.SINGLE);
        configs.add(selc.getColumn());
        
        ColumnConfig column = new ColumnConfig();  
        column.setId("PropertyKey");  
        column.setHeader("Property Key");  
        column.setWidth(245);  
        
        TextField<String> key = new TextField<String>();  
        key.setAllowBlank(false);  
        column.setEditor(new CellEditor(key));  
        configs.add(column);  
      
        column = new ColumnConfig();  
        column.setId("PropertyValue");  
        column.setHeader("Property Value");  
        TextField<String> value = new TextField<String>(); 
        value.setAllowBlank(false);
        column.setWidth(245);  
        column.setEditor(new CellEditor(value));  
        configs.add(column);    
                
        RPCInvocation<BaseListLoadResult<PropertyPair>> properInvoke = new RPCInvocation<BaseListLoadResult<PropertyPair>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<PropertyPair>> callback) {
                remoteService.retrievePhysicalNodeProperties(mach.getId(), callback);
            }
            
        };
        
        ListLoader<BaseListLoadResult<PropertyPair>> propertyLoader = new BaseListLoader<BaseListLoadResult<PropertyPair>>(
                properInvoke);  
        final ListStore<PropertyPair> store = new ListStore<PropertyPair>(propertyLoader);
        ColumnModel cm = new ColumnModel(configs);  
          
        cp.setHeading("Edit Properties"); 
        cp.setSize(500, 300); 
//        cp.setScrollMode(Scroll.NONE);
        cp.setLayout(new FitLayout());  
      
        final EditorGrid<PropertyPair> grid = new EditorGrid<PropertyPair>(store, cm);  
        grid.setAutoExpandColumn("PropertyKey");  
        grid.setBorders(false);
        grid.setSelectionModel(selc);
        grid.addPlugin(selc);  
        cp.add(grid);  
        ToolBar toolBar = new ToolBar();  
        Button add = new Button("Add Property");  
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {  
      
          @Override  
          public void componentSelected(ButtonEvent ce) {  
            PropertyPair pair = new PropertyPair();  
            pair.setKey("New Key");  
            pair.setValue("New Value");    
            grid.stopEditing();  
            store.insert(pair, 0);  
            grid.startEditing(store.indexOf(pair), 0);  
          }  
      
        });  
        toolBar.add(add);  
        Button del = new Button("Delete Property");
        del.addSelectionListener(new SelectionListener<ButtonEvent>() {
            
            @Override
            public void componentSelected(ButtonEvent ce) {

                if(selc.getSelectedItem()!= null){
                    grid.stopEditing();
                    store.remove(selc.getSelectedItem());
                    grid.startEditing(0, 0);
                }               
            }
            
        });
        toolBar.add(del);
        cp.setTopComponent(toolBar);  
        cp.setButtonAlign(HorizontalAlignment.CENTER);  
        cp.addButton(new Button("Reset", new SelectionListener<ButtonEvent>() {  
      
          @Override  
          public void componentSelected(ButtonEvent ce) {  
            store.rejectChanges();  
          }  
        }));  

        cp.addButton(new Button("Save", new SelectionListener<ButtonEvent>() {  
      
          @Override  
          public void componentSelected(ButtonEvent ce) {  
            store.commitChanges();
            RPCInvocation<Boolean> invoke = new RPCInvocation<Boolean>() {

                @Override
                public void execute(CloudRemoteServiceAsync remoteService,
                        AsyncCallback<Boolean> callback) {
                    remoteService.addPhysicalNodeProperties(store.getModels(), mach.getId(), callback);
                    showAlertMessageBox("Properties Save Success");
                }
            };
            
          invoke.invoke(false);
          }       
        }));  
        propertyLoader.load();
        dialog.add(cp);
        Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                dialog.hide();
            }
        });
        return dialog;
    }

    protected Dialog createCapabilitiesDialog(final PhysicalNodeInfoBean mach) {
        final Dialog dialog = new Dialog();
        dialog.setClosable(true);
        dialog.setAutoHeight(true);
        dialog.setWidth(535);
        dialog.setScrollMode(Scroll.NONE);
        dialog.setLayout(new FlowLayout(10)); 
        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setBodyBorder(false);
        _contentPanel.setHeading("Edit Capabilities");
        _contentPanel.setSize(500, 300);  
//        _contentPanel.setScrollMode(Scroll.NONE);
        _contentPanel.setLayout(new FitLayout());

        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        final CheckBoxSelectionModel<FeatureBean> feaSm = new CheckBoxSelectionModel<FeatureBean>();
        columnConfigs.add(feaSm.getColumn());
        columnConfigs.add(new ColumnConfig("Name", "FeatureName", 200));
        columnConfigs.add(new ColumnConfig("Contributor", "Contributor", 300));
        columnConfigs.add(new ColumnConfig("Version", "Version", 100));
        ColumnModel cm = new ColumnModel(columnConfigs);
        
        RpcProxy<BaseListLoadResult<FeatureBean>> fProxy = new RPCInvocation<BaseListLoadResult<FeatureBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<FeatureBean>> callback) {
                remoteService.retrieveFeatureBeanList(new ArrayList<String>(),
                        callback);
            }

        };
        BaseListLoader<BaseListLoadResult<FeatureBean>> fLoader = new BaseListLoader<BaseListLoadResult<FeatureBean>>(fProxy);
        final ListStore<FeatureBean> fStore = new ListStore<FeatureBean>(fLoader);
        Grid<FeatureBean> grid = new Grid<FeatureBean>(fStore, cm);
        grid.setBorders(false);
        grid.setAutoExpandColumn("Name");
        grid.setSelectionModel(feaSm);
        grid.addPlugin(feaSm);
        _contentPanel.add(grid);        
        _contentPanel.setButtonAlign(HorizontalAlignment.CENTER);
        _contentPanel.addButton(new Button("Save", new SelectionListener<ButtonEvent>() {  
            
            @Override  
            public void componentSelected(ButtonEvent ce) {  
              
              RPCInvocation<Boolean> invoke = new RPCInvocation<Boolean>() {
                
                @Override
                public void execute(CloudRemoteServiceAsync remoteService,
                        AsyncCallback<Boolean> callback) {
                    remoteService.addPhysicalNodeCapabilities(feaSm.getSelectedItems(), mach.getId(), callback);
                    showAlertMessageBox("Capabilities Save Success");
                }
            };
            
          invoke.invoke(false);
            }       
          }));
        fLoader.load();
        RPCInvocation<List<String>> invocation = new RPCInvocation<List<String>>() {

                @Override
                public void execute(CloudRemoteServiceAsync remoteService,
                        AsyncCallback<List<String>> callback) {
                    remoteService.retrievePhysicalNodeCapabilities(mach.getId(), callback);
                }
                
                @Override
                public void onComplete(List<String> result){
                    for(String c : result){
                        for(FeatureBean f : fStore.getModels()){
                            if(c.equals(f.getName())){
                                feaSm.select(f, true);
                            }
                        }
                        
                    }
                }
        };
        invocation.invoke(false);
        dialog.add(_contentPanel);
        Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                dialog.hide();
            }
        });
        return dialog;
    }    
    
    public Dialog createCRTDialog(final PhysicalNodeInfoBean ins) {
        final Dialog dialog = new Dialog();
        dialog.setHeading("Reset password for "
                + (ins.getOsName().contains("Win") ? "administrator" : "root"));
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
        textField.setFieldLabel("Host");
        textField.setReadOnly(true);
        textField.setValue(ins.getHost());
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
                if (com.extjs.gxt.ui.client.util.Util.isEmptyString(nP)
                        || com.extjs.gxt.ui.client.util.Util.isEmptyString(cP)) {
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

    private Dialog createRegistrationDialog(String title) {
        final Dialog dialog = new Dialog();
        dialog.setClosable(true);
        dialog.setHeading(title);
        dialog.setAutoHeight(true);
        dialog.setWidth(400);
        dialog.setButtons(Dialog.OKCANCEL);
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(140);
        dialog.setLayout(formLayout);
        FormData formData = new FormData("95%");
        formData.setMargins(new Margins(10, 0, 0, 0));
        final TextField<String> nodeName = new TextField<String>();
        nodeName.setFieldLabel("HostName/IP");
        nodeName.setData("text", "ensure host can be accessed from network");
        nodeName.addPlugin(Util.createComponentPlugin());
        dialog.add(nodeName, formData);

        final SimpleComboBox<String> osName = new SimpleComboBox<String>();
        osName.setFieldLabel("OS Name");
        osName.add(Util.getSupportedOS());
        osName.setTriggerAction(TriggerAction.ALL);
        osName.setEditable(false);
        dialog.add(osName, formData);

        final SimpleComboBox<String> archName = new SimpleComboBox<String>();
        archName.setFieldLabel("Arch Name");
        archName.add(Util.getSupportedARCH());
        archName.setTriggerAction(TriggerAction.ALL);
        archName.setEditable(false);
        dialog.add(archName, formData);

        final NumberField cpuNum = new NumberField();
        cpuNum.setPropertyEditorType(Integer.class);
        cpuNum.setAllowNegative(false);
        cpuNum.setAllowDecimals(false);
        cpuNum.setFieldLabel("CPU Core(s)");
        dialog.add(cpuNum, formData);

        final NumberField memNum = new NumberField();
        memNum.setPropertyEditorType(Integer.class);
        memNum.setAllowDecimals(false);
        memNum.setAllowNegative(false);
        memNum.setFieldLabel("Memory Capacity(GB)");
        dialog.add(memNum, formData);

        final TextField<String> appDir = new TextField<String>();
        appDir.setFieldLabel("Root Path");
        appDir.setData("text", "the absolute path: /testdir, E:\\testdir");
        appDir.addPlugin(Util.createComponentPlugin());
        dialog.add(appDir, formData);

        final NumberField diskNum = new NumberField();
        diskNum.setPropertyEditorType(Integer.class);
        diskNum.setAllowDecimals(false);
        diskNum.setAllowNegative(false);
        diskNum.setFieldLabel("Disk Capacity(GB)");
        diskNum
                .setData("text",
                        "the maximum allowed capacity for above folder");
        diskNum.addPlugin(Util.createComponentPlugin());
        dialog.add(diskNum, formData);

        final ComboBox<CloudInfoBean> comboCloud = new ComboBox<CloudInfoBean>();
        comboCloud.setFieldLabel("Join Cloud");
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
        comboCloud.setEditable(true);
        comboCloud.setEmptyText("Select a Cloud...");
        comboCloud.setTriggerAction(TriggerAction.ALL);
        dialog.add(comboCloud, formData);

        final CheckBoxGroup group = new CheckBoxGroup();
        final CheckBox pooledCK = new CheckBox();
        pooledCK.setBoxLabel("Pooled");
        final CheckBox sharedCK = new CheckBox();
        sharedCK.setBoxLabel("Shared");
        group.add(pooledCK);
        group.add(sharedCK);
        group
                .setData(
                        "text",
                        "indicate if the registered host should be added to machine pool of Cloud Test Service");
        group.addPlugin(Util.createComponentPlugin());
        group.setFieldLabel("Options");
        dialog.add(group, formData);

        final TextField<String> desField = new TextField<String>();
        desField.setFieldLabel("Comment");
        dialog.add(desField, formData);

        Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                String host = nodeName.getValue();
                String os = osName.getSimpleValue();
                String arch = archName.getSimpleValue();
                Integer cpu = (Integer) cpuNum.getValue();
                Integer mem = (Integer) memNum.getValue();
                Integer disk = (Integer) diskNum.getValue();
                String path = appDir.getValue();
                final PhysicalNodeInfoBean nodeInfoBean = new PhysicalNodeInfoBean(
                        host, os, arch, cpu, mem, disk, path);
                boolean verifyResult = verifyFields(nodeInfoBean);
                if (!verifyResult) {
                    return;
                }
                UserInfoBean userInfoBean = MainPage.getInstance()
                        .getUserProfile();
                nodeInfoBean.setUserId(userInfoBean.getId());
                CloudInfoBean cloudInfoBean = comboCloud.getValue();
                if (cloudInfoBean != null) {
                    nodeInfoBean.setCloudId(cloudInfoBean.getId());
                }
                nodeInfoBean.setShared(sharedCK.getValue());
                nodeInfoBean.setPooled(pooledCK.getValue());
                nodeInfoBean.setComment(desField.getValue() == null ? ""
                        : desField.getValue());

                RPCInvocation<Boolean> invoke = new RPCInvocation<Boolean>() {
                    @Override
                    public Command onFailure(Throwable t) {
                        showAlertMessageBox(t.getMessage());
                        return super.onFailure(t);
                    }

                    @Override
                    public void onComplete(Boolean obj) {
                        if (obj)
                            updateView();
                    }

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Boolean> callback) {
                        remoteService.registerPhysicalNode(nodeInfoBean,
                                callback);
                    }

                };
                invoke.invoke(false);
                dialog.hide();
            }
        });
        Button cancelBtn = dialog.getButtonById(Dialog.CANCEL);
        cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                dialog.hide();
            }
        });

        return dialog;
    }

    private boolean verifyFields(PhysicalNodeInfoBean nodeInfoBean) {
        String host = nodeInfoBean.getHost();
        if (host == null || host.isEmpty()) {
            showAlertMessageBox("the host field is empty");
            return false;
        }
        String os = nodeInfoBean.getOsName();
        if (os == null || os.isEmpty()) {
            showAlertMessageBox("the os field is empty");
            return false;
        }
        String arch = nodeInfoBean.getArchName();
        if (arch == null || arch.isEmpty()) {
            showAlertMessageBox("the arch field is empty");
            return false;
        }
        Integer cpu = nodeInfoBean.getCpu();
        if (cpu == null || cpu == 0) {
            showAlertMessageBox("the cpu number cannot be zero");
            return false;
        }
        Integer mem = nodeInfoBean.getMem();
        if (mem == null || mem == 0) {
            showAlertMessageBox("the memory capacity cannot be zero");
            return false;
        }
        String rootPath = nodeInfoBean.getRootPath();
        if (rootPath == null || rootPath.isEmpty()
                || !checkedPath(OS.fromString(os), rootPath)) {
            showAlertMessageBox("the root path is not valid");
            return false;
        }
        Integer disk = nodeInfoBean.getDisk();
        if (disk == null || disk == 0) {
            showAlertMessageBox("the disk size cannot be zero");
            return false;
        }
        return true;
    }

    private void showAlertMessageBox(String message) {
        MessageBox.alert("Alert", message, null);
    }

    private boolean checkedPath(OS os, String path) {
        if (os == null)
            return false;
        if (os.isWindows()) {
            if (path.length() < 3)
                return false;
            return Character.isLetter(path.charAt(0))
                    && path.substring(1, 3).equals(":\\");
        } else {
            return path.startsWith("/");
        }
    }

}
