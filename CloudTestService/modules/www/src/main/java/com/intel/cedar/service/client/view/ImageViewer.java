package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.FeatureBean;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.util.Util;

public class ImageViewer extends ComponentViewer {

    public enum ImageOperation {
        SET_PROPERTIES, SET_CAPATITIES;
    }

    @Override
    public void updateView() {
        mLoader.load();
    }

    private static ImageViewer instance;

    private RpcProxy<BaseListLoadResult<MachineInfoBean>> mProxy;

    private BaseListLoader<BaseListLoadResult<MachineInfoBean>> mLoader;

    private GroupingStore<MachineInfoBean> mStore;

    private boolean refreshPending;

    private ArrayList<MachineInfoBean> beanList = new ArrayList<MachineInfoBean>();

    protected CheckBoxSelectionModel<MachineInfoBean> sm;

    private Menu imageMenu;

    private Timer machineTimer = new Timer() {

        @Override
        public void run() {
            updateView();

        }

    };

    public static ImageViewer getInstance() {
        if (instance == null) {
            instance = new ImageViewer();
        }

        return instance;
    }

    private ImageViewer() {
        mProxy = new RPCInvocation<BaseListLoadResult<MachineInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback) {
                remoteService.retrieveImageList(new ArrayList<String>(),
                        callback);
            }

        };
        mLoader = new BaseListLoader<BaseListLoadResult<MachineInfoBean>>(
                mProxy);
        mStore = new GroupingStore<MachineInfoBean>(mLoader);
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        sm = new CheckBoxSelectionModel<MachineInfoBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        mLoader.setRemoteSort(true);
        mStore.groupBy("CloudName");
        mLoader.load();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(sm.getColumn());
        configs.add(new ColumnConfig("CloudName", "CloudName", 50));
        configs.add(new ColumnConfig("ImageId", "ImageId", 100));
        configs.add(new ColumnConfig("Os", "OS", 160));
        configs.add(new ColumnConfig("Arch", "Arch", 60));
        configs.add(new ColumnConfig("Comment", "Comment", 250));

        final ColumnModel columnModel = new ColumnModel(configs);

        ToolBar topBar = new ToolBar();
        topBar.setAlignment(HorizontalAlignment.RIGHT);
        final Button launchBtn = new Button("Launch", AbstractImagePrototype
                .create(Resources.ICONS.register()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        MachineInfoBean bean = sm.getSelectedItem();
                        if(!bean.getEnabled()){
                            MessageBox.info("Info", "This image is disabled!", null);
                            return;
                        }
                        String op = "Launch";
                        TabPanel tabpanel = MainPage.getInstance()
                                .getTabPanel();
                        TabItem item = new TabItem(op);
                        item.setClosable(true);
                        item.setIcon(IconHelper.createStyle(
                                MainPage.treeItemtoStyle.get(op).toLowerCase(),
                                16, 16));
                        item.setBorders(true);
                        item.setLayout(new FitLayout());
                        InstanceLauncher launcherView = new InstanceLauncher();
                        launcherView.setParentItem(item);
                        launcherView.setMachineBean(bean);
                        item.add(launcherView);
                        item.layout();

                        tabpanel.add(item);
                        tabpanel.setSelection(item);
                    }

                });

        final Button discardBtn = new Button("Discard", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final MachineInfoBean image = sm.getSelectedItem();
                        if (image != null) {
                            RPCInvocation<Boolean> invoke = new RPCInvocation<Boolean>() {

                                @Override
                                public void onComplete(Boolean obj) {
                                    if (obj)
                                        updateView();     
                                    else{
                                        MessageBox.info("Failed", "There's instance using this image!", null);
                                    }
                                }

                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<Boolean> callback) {
                                    remoteService.discardCloudImage(image,
                                            callback);
                                }

                            };
                            invoke.invoke(false);
                        }
                    }
                });
        discardBtn.setEnabled(false);

        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            topBar.add(new Button("Import", AbstractImagePrototype
                    .create(Resources.ICONS.register()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            createImportDialog("Import Cloud Image").show();
                        }
                    }));
            topBar.add(discardBtn);
        }
        sm
                .addSelectionChangedListener(new SelectionChangedListener<MachineInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<MachineInfoBean> se) {
                        if (se.getSelectedItem() != null) {
                            launchBtn.setEnabled(true);
                        } else {
                            launchBtn.setEnabled(false);
                        }
                        if (MainPage.getInstance().getUserProfile().getAdmin()) {
                            if (se.getSelectedItem() != null) {
                                discardBtn.setEnabled(true);
                            } else {
                                discardBtn.setEnabled(false);
                            }
                        }
                    }
                });
        launchBtn.setEnabled(false);
        launchBtn.setToolTip("Launch a new instance");
        topBar.add(launchBtn);
        GroupingView groupView = new GroupingView();
        groupView.setShowGroupedColumn(false);
        groupView.setForceFit(true);
        groupView.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = columnModel.getColumnById(data.field)
                        .getHeader();
                String u = data.models.size() == 1 ? "Image" : "Images";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });
        final Grid<MachineInfoBean> grid = new Grid<MachineInfoBean>(mStore,
                columnModel);
        grid.setBorders(false);
        grid.setView(groupView);
        grid.setStateful(true);
        grid.setAutoExpandColumn("Comment");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);
        if(MainPage.getInstance().getUserProfile().getAdmin()){
        	grid.setContextMenu(createMenu());
        }
        
        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.setTopComponent(topBar);
        _contentPanel.add(grid);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    protected Menu createMenu() {
        if (imageMenu != null)
            return imageMenu;
        imageMenu = new Menu();
        MenuItem propertyItem = new MenuItem("Set Properties");
        propertyItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(ImageOperation.SET_PROPERTIES);
                imageMenu.hide();
            }
        });
        MenuItem capabilityItem = new MenuItem("Set Capabilities");
        capabilityItem.addSelectionListener(new SelectionListener<MenuEvent>() {

            @Override
            public void componentSelected(MenuEvent ce) {
                createDialog(ImageOperation.SET_CAPATITIES);
                imageMenu.hide();
            }
        });

        imageMenu.add(propertyItem);
        imageMenu.add(capabilityItem);

        return imageMenu;
    }

    public void createDialog(ImageOperation op) {
    	MachineInfoBean mach = sm.getSelectedItem();
    	if(op.equals(ImageOperation.SET_PROPERTIES)){
    		createPropertiesDialog(mach).show();
    	}else if(op.equals(ImageOperation.SET_CAPATITIES)){
    		createCapabilitiesDialog(mach).show();
    	}
    }

    protected  Dialog createPropertiesDialog(final MachineInfoBean mach){
    	
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
    			remoteService.retrieveImageProperties(mach.getId(), callback);
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
    				remoteService.addProperties(store.getModels(), mach.getId(), callback);
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

    protected Dialog createCapabilitiesDialog(final MachineInfoBean mach) {
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
      				remoteService.addCapabilities(feaSm.getSelectedItems(), mach.getId(), callback);
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
					remoteService.retrieveCapabilities(mach.getId(), callback);
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

    private Dialog createImportDialog(String title) {
        final Dialog dialog = new Dialog();
        dialog.setClosable(true);
        dialog.setHeading(title);
        dialog.setAutoHeight(true);
        dialog.setWidth(400);
        dialog.setButtons(Dialog.OKCANCEL);
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(80);
        dialog.setLayout(formLayout);
        FormData formData = new FormData("95%");
        formData.setMargins(new Margins(10, 0, 0, 0));

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
        comboCloud.setTriggerAction(TriggerAction.ALL);
        dialog.add(comboCloud, formData);

        final ComboBox<MachineInfoBean> comboImage = new ComboBox<MachineInfoBean>();
        comboImage.setFieldLabel("Image");
        comboImage.setDisplayField("Verbose");
        comboImage.getView().setModelProcessor(
                new ModelProcessor<MachineInfoBean>() {
                    public MachineInfoBean prepareData(MachineInfoBean model) {
                        CloudInfoBean bean = comboCloud.getValue();
                        if (bean != null) {
                            model.set("Verbose", model.getImageId() + " -- "
                                    + model.getImageName());
                        }
                        return model;
                    }
                });
        RpcProxy<BaseListLoadResult<MachineInfoBean>> imageProxy = new RPCInvocation<BaseListLoadResult<MachineInfoBean>>() {
            @Override
            public void onComplete(BaseListLoadResult<MachineInfoBean> obj) {
                ArrayList<MachineInfoBean> result = new ArrayList<MachineInfoBean>();
                for (MachineInfoBean bean : obj.getData()) {
                    boolean found = false;
                    for (MachineInfoBean n : mStore.getModels()) {
                        if (n.getCloudName().equals(bean.getCloudName()) && 
                            n.getImageId().equals(bean.getImageId())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        result.add(bean);
                    }
                }
                obj.setData(result);
                super.onComplete(obj);
            }

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<MachineInfoBean>> callback) {
                remoteService.retrieveMachineListInCloud(comboCloud.getValue(),
                        callback);
            }
        };
        final ListLoader<BaseListLoadResult<MachineInfoBean>> imageLoader = new BaseListLoader<BaseListLoadResult<MachineInfoBean>>(
                imageProxy);
        ListStore<MachineInfoBean> imageStore = new ListStore<MachineInfoBean>(
                imageLoader);
        comboImage.setStore(imageStore);
        comboImage.setEditable(true);
        comboImage.setTriggerAction(TriggerAction.ALL);
        comboImage.setEmptyText("Select an image...");
        dialog.add(comboImage, formData);

        comboCloud.addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent>() {
                    @Override
                    public void handleEvent(SelectionChangedEvent be) {
                        imageLoader.load();
                    }
                });

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

        final CheckBoxGroup group = new CheckBoxGroup();
        final CheckBox managedCK = new CheckBox();
        managedCK.setBoxLabel("Managed");
        final CheckBox enabledCK = new CheckBox();
        enabledCK.setBoxLabel("Enabled");
        group.add(managedCK);
        group.add(enabledCK);
        group.setFieldLabel("Image Status");
        dialog.add(group, formData);

        final TextField<String> comment = new TextField<String>();
        comment.setFieldLabel("Comment");
        dialog.add(comment, formData);

        final Button okBtn = dialog.getButtonById(Dialog.OK);
        comboImage.addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent>() {

                    @Override
                    public void handleEvent(SelectionChangedEvent be) {
                        MachineInfoBean bean = comboImage.getValue();
                        if (bean != null) {
                            okBtn.setEnabled(true);
                            osName.setSimpleValue(bean.getOs());
                            archName.setSimpleValue(bean.getArch());
                            managedCK.setValue(bean.getManaged());
                            enabledCK.setValue(bean.getEnabled());
                            comment.setValue(bean.getComment());
                        }
                    }
                });

        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final MachineInfoBean image = comboImage.getValue();
                if (image != null) {
                    image.setOs(osName.getSimpleValue());
                    image.setArch(archName.getSimpleValue());
                    image.setComment(comment.getValue());
                    image.setManaged(managedCK.getValue());
                    image.setEnabled(enabledCK.getValue());
                    image.setCloudId(comboCloud.getValue().getId());
                }
                if (verifyFields(image)) {
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
                            remoteService.importCloudImage(image, callback);
                        }

                    };
                    invoke.invoke(false);
                    dialog.hide();
                }
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

    private boolean verifyFields(MachineInfoBean nodeInfoBean) {
        String os = nodeInfoBean.getOs();
        if (os == null || os.isEmpty()) {
            showAlertMessageBox("the os field is empty");
            return false;
        }
        String arch = nodeInfoBean.getArch();
        if (arch == null || arch.isEmpty()) {
            showAlertMessageBox("the arch field is empty");
            return false;
        }
        return true;
    }

    private void showAlertMessageBox(String message) {
        MessageBox.alert("Alert", message, null);
    }
}
