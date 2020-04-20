package com.intel.cedar.service.client.view;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelProcessor;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
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
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.InstanceInfoBean;
import com.intel.cedar.service.client.model.MachineInfoBean;
import com.intel.cedar.service.client.model.MachineTypeInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.util.Util;

public class TypeViewer extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 10000;

    private Timer updater = new Timer() {

        @Override
        public void run() {
            mLoader.load();
        }

    };

    private boolean refreshPending = false;

    @Override
    public void updateView() {
        mLoader.load();
    }

    private static TypeViewer instance;

    private RpcProxy<BaseListLoadResult<MachineTypeInfoBean>> mProxy;

    private BaseListLoader<BaseListLoadResult<MachineTypeInfoBean>> mLoader;

    private GroupingStore<MachineTypeInfoBean> mStore;

    private CheckBoxSelectionModel<MachineTypeInfoBean> sm;

    private TypeViewer() {
        mProxy = new RPCInvocation<BaseListLoadResult<MachineTypeInfoBean>>() {

            @Override
            public void execute(
                    CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<MachineTypeInfoBean>> callback) {
                remoteService.retrieveMachineTypeList(new ArrayList<String>(),
                        Boolean.TRUE, callback);
            }

            public void onComplete(BaseListLoadResult<MachineTypeInfoBean> obj) {
                updater.schedule(REFRESH_INTERVAL);
            }
        };
        mLoader = new BaseListLoader<BaseListLoadResult<MachineTypeInfoBean>>(
                mProxy);
        mStore = new GroupingStore<MachineTypeInfoBean>(mLoader);
    }

    public static TypeViewer getInstance() {
        if (instance == null) {
            instance = new TypeViewer();
        }

        return instance;
    }

    public void scheduleRefresh() {
        if (refreshPending) {
            updater.cancel();
        }
        mLoader.load();
        updater.schedule(REFRESH_INTERVAL);
        refreshPending = true;
    }

    public void cancelRefresh() {
        updater.cancel();
        refreshPending = false;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        sm = new CheckBoxSelectionModel<MachineTypeInfoBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        mLoader.setRemoteSort(true);
        mStore.groupBy("CloudName");
        mLoader.load();

        GridCellRenderer<MachineTypeInfoBean> availColumnRenderer = new GridCellRenderer<MachineTypeInfoBean>() {
            @Override
            public Object render(MachineTypeInfoBean model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<MachineTypeInfoBean> store,
                    Grid<MachineTypeInfoBean> grid) {
                if (((Integer) model.get(property)) < 0) {
                    return "N/A";
                } else
                    return model.get(property);
            }
        };

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(sm.getColumn());
        configs.add(new ColumnConfig("CloudName", "CloudName", 100));
        configs.add(new ColumnConfig("Type", "Type", 150));
        configs.add(new ColumnConfig("Cpu", "CPU", 60));
        configs.add(new ColumnConfig("Memory", "Memory (MB)", 80));
        configs.add(new ColumnConfig("Disk", "Disk (GB)", 60));
        configs.add(new ColumnConfig("SecondDisk", "Second Disk (GB)", 90));
        ColumnConfig availColumn = new ColumnConfig("Free", "Availability", 60);
        configs.add(new ColumnConfig("Max", "Maximum", 60));
        availColumn.setRenderer(availColumnRenderer);
        configs.add(availColumn);

        final ColumnModel columnModel = new ColumnModel(configs);
        GroupingView groupView = new GroupingView();
        groupView.setShowGroupedColumn(true);
        groupView.setForceFit(true);
        groupView.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = columnModel.getColumnById(data.field)
                        .getHeader();
                String u = data.models.size() == 1 ? "Type" : "Types";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });
        final Grid<MachineTypeInfoBean> grid = new Grid<MachineTypeInfoBean>(
                mStore, columnModel);
        grid.setBorders(false);
        grid.setView(groupView);
        grid.setStateful(true);
        grid.setAutoExpandColumn("CloudName");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        ToolBar topBar = new ToolBar();
        topBar.setAlignment(HorizontalAlignment.RIGHT);

        final Button discardBtn = new Button("Discard", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final MachineTypeInfoBean type = sm.getSelectedItem();
                        if (type != null) {
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
                                    remoteService.discardMachineType(type,
                                            callback);
                                }

                            };
                            invoke.invoke(false);
                        }
                    }
                });
        discardBtn.setEnabled(false);
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            topBar.add(new Button("Register", AbstractImagePrototype
                    .create(Resources.ICONS.register()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            showCreateDialog("Register Machine Type").show();
                        }
                    }));
            topBar.add(new Button("Edit", AbstractImagePrototype
                    .create(Resources.ICONS.editCloud()),
                    new SelectionListener<ButtonEvent>() {

                        @Override
                        public void componentSelected(ButtonEvent ce) {
                            showEditDialog("Edit Machine Type",
                                    sm.getSelectedItem()).show();
                        }
                    }));
            topBar.add(discardBtn);
        }
        sm
                .addSelectionChangedListener(new SelectionChangedListener<MachineTypeInfoBean>() {
                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<MachineTypeInfoBean> se) {
                        if (MainPage.getInstance().getUserProfile().getAdmin()) {
                            MachineTypeInfoBean bean = se.getSelectedItem();
                            if (bean != null) {
                                // only EC2 and OpenStack type can be discarded
                                // for now!
                                if (bean.getFree() < 0)
                                    discardBtn.setEnabled(true);
                            } else {
                                discardBtn.setEnabled(false);
                            }
                        }
                    }
                });

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setBodyBorder(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.setTopComponent(topBar);
        _contentPanel.add(grid);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    private Dialog showCreateDialog(String title) {
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
            public void onComplete(BaseListLoadResult<CloudInfoBean> obj) {
                ArrayList<CloudInfoBean> result = new ArrayList<CloudInfoBean>();
                for (CloudInfoBean c : obj.getData()) {
                    if (c.getProtocol().equalsIgnoreCase("EC2")
                            || c.getProtocol().equalsIgnoreCase("OpenStack")) {
                        result.add(c);
                    }
                }
                obj.setData(result);
                super.onComplete(obj);
            }

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

        final TextField<String> type = new TextField<String>();
        type.setFieldLabel("Name");
        type.setData("text", "e.g: m1.tiny, must be valid for selected cloud");
        type.addPlugin(Util.createComponentPlugin());
        dialog.add(type, formData);

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
        memNum.setFieldLabel("Memory Capacity(MB)");
        dialog.add(memNum, formData);

        final NumberField diskNum = new NumberField();
        diskNum.setPropertyEditorType(Integer.class);
        diskNum.setAllowDecimals(false);
        diskNum.setAllowNegative(false);
        diskNum.setFieldLabel("Disk Capacity(GB)");
        dialog.add(diskNum, formData);

        final NumberField secondDiskNum = new NumberField();
        secondDiskNum.setPropertyEditorType(Integer.class);
        secondDiskNum.setAllowDecimals(false);
        secondDiskNum.setAllowNegative(false);
        secondDiskNum.setFieldLabel("Second Disk Capacity(GB)");
        dialog.add(secondDiskNum, formData);

        final NumberField maxNum = new NumberField();
        maxNum.setPropertyEditorType(Integer.class);
        maxNum.setAllowDecimals(false);
        maxNum.setAllowNegative(false);
        maxNum.setFieldLabel("Allowed Maximum");
        dialog.add(maxNum, formData);

        final Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (comboCloud.getValue() == null) {
                    showAlertMessageBox("Cloud must be specified!");
                    return;
                }
                if (type.getValue() == null || type.getValue().length() == 0) {
                    showAlertMessageBox("Type is not valid!");
                    return;
                }
                if (cpuNum.getValue() == null
                        || cpuNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("CPU number is not valid!");
                    return;
                }
                if (memNum.getValue() == null
                        || memNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Memory capacity is not valid!");
                    return;
                }
                if (diskNum.getValue() == null
                        || diskNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Disk capacity is not valid!");
                    return;
                }
                if (secondDiskNum.getValue() == null
                        || secondDiskNum.getValue().intValue() < 0) {
                    showAlertMessageBox("Second Disk capacity is not valid!");
                    return;
                }
                if (maxNum.getValue() == null
                        || maxNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Allowed maximum is not valid!");
                    return;
                }

                final MachineTypeInfoBean bean = new MachineTypeInfoBean();
                bean.setCloudId(comboCloud.getValue().getId());
                bean.setType(type.getValue());
                bean.setCpu(cpuNum.getValue().intValue());
                bean.setMemory(memNum.getValue().intValue());
                bean.setDisk(diskNum.getValue().intValue());
                bean.setSecondDisk(secondDiskNum.getValue().intValue());
                bean.setFree(-1);
                bean.setMax(maxNum.getValue().intValue());

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
                        remoteService.registerMachineType(bean, callback);
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

    private Dialog showEditDialog(final String title,
            final MachineTypeInfoBean mt) {
        final Dialog dialog = new Dialog();
        dialog.setClosable(true);
        dialog.setAutoHeight(true);
        dialog.setWidth(400);
        dialog.setButtons(Dialog.OKCANCEL);
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(80);
        dialog.setLayout(formLayout);
        FormData formData = new FormData("95%");
        formData.setMargins(new Margins(10, 0, 0, 0));

        RPCInvocation<BaseListLoadResult<CloudInfoBean>> cloudInvoker = new RPCInvocation<BaseListLoadResult<CloudInfoBean>>() {
            @Override
            public void onComplete(BaseListLoadResult<CloudInfoBean> obj) {
                ArrayList<CloudInfoBean> result = new ArrayList<CloudInfoBean>();
                for (CloudInfoBean c : obj.getData()) {
                    if (c.getId().equals(mt.getCloudId())) {
                        dialog.setHeading(title + " for " + c.getCloudName());
                        break;
                    }
                }
                obj.setData(result);
                super.onComplete(obj);
            }

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<CloudInfoBean>> callback) {
                remoteService.retrieveCloudList(new ArrayList<String>(),
                        callback);
            }
        };
        cloudInvoker.invoke(false);

        final TextField<String> type = new TextField<String>();
        type.setFieldLabel("Name");
        type.setData("text", "e.g: m1.tiny, must be valid for selected cloud");
        type.setValue(mt.getType());
        type.addPlugin(Util.createComponentPlugin());
        dialog.add(type, formData);

        final NumberField cpuNum = new NumberField();
        cpuNum.setPropertyEditorType(Integer.class);
        cpuNum.setAllowNegative(false);
        cpuNum.setAllowDecimals(false);
        cpuNum.setFieldLabel("CPU Core(s)");
        cpuNum.setValue(mt.getCpu());
        dialog.add(cpuNum, formData);

        final NumberField memNum = new NumberField();
        memNum.setPropertyEditorType(Integer.class);
        memNum.setAllowDecimals(false);
        memNum.setAllowNegative(false);
        memNum.setFieldLabel("Memory Capacity(MB)");
        memNum.setValue(mt.getMemory());
        dialog.add(memNum, formData);

        final NumberField diskNum = new NumberField();
        diskNum.setPropertyEditorType(Integer.class);
        diskNum.setAllowDecimals(false);
        diskNum.setAllowNegative(false);
        diskNum.setFieldLabel("Disk Capacity(GB)");
        diskNum.setValue(mt.getDisk());
        dialog.add(diskNum, formData);

        final NumberField secondDiskNum = new NumberField();
        secondDiskNum.setPropertyEditorType(Integer.class);
        secondDiskNum.setAllowDecimals(false);
        secondDiskNum.setAllowNegative(false);
        secondDiskNum.setFieldLabel("Second Disk Capacity(GB)");
        secondDiskNum.setValue(mt.getSecondDisk());
        dialog.add(secondDiskNum, formData);

        final NumberField maxNum = new NumberField();
        maxNum.setPropertyEditorType(Integer.class);
        maxNum.setAllowDecimals(false);
        maxNum.setAllowNegative(false);
        maxNum.setFieldLabel("Allowed Maximum");
        maxNum.setValue(mt.getMax());
        dialog.add(maxNum, formData);

        final Button okBtn = dialog.getButtonById(Dialog.OK);
        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (type.getValue() == null || type.getValue().length() == 0) {
                    showAlertMessageBox("Type is not valid!");
                    return;
                }
                if (cpuNum.getValue() == null
                        || cpuNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("CPU number is not valid!");
                    return;
                }
                if (memNum.getValue() == null
                        || memNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Memory capacity is not valid!");
                    return;
                }
                if (diskNum.getValue() == null
                        || diskNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Disk capacity is not valid!");
                    return;
                }
                if (secondDiskNum.getValue() == null
                        || secondDiskNum.getValue().intValue() < 0) {
                    showAlertMessageBox("Second Disk capacity is not valid!");
                    return;
                }
                if (maxNum.getValue() == null
                        || maxNum.getValue().intValue() <= 0) {
                    showAlertMessageBox("Allowed maximum is not valid!");
                    return;
                }

                mt.setType(type.getValue());
                mt.setCpu(cpuNum.getValue().intValue());
                mt.setMemory(memNum.getValue().intValue());
                mt.setDisk(diskNum.getValue().intValue());
                mt.setSecondDisk(secondDiskNum.getValue().intValue());
                mt.setMax(maxNum.getValue().intValue());

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
                        remoteService.updateMachineType(mt, callback);
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

    private void showAlertMessageBox(String message) {
        MessageBox.alert("Alert", message, null);
    }
}
