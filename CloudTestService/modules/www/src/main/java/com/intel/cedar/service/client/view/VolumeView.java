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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
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
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.CloudInfoBean;
import com.intel.cedar.service.client.model.VolumeInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.util.Util;

public class VolumeView extends ComponentViewer {

    private final static int REFRESH_INTERVAL = 10000;

    private static VolumeView instance;

    private Timer updater = new Timer() {

        @Override
        public void run() {
            vLoader.load();
        }

    };

    private boolean refreshPending = false;

    private VolumeView() {
        vProxy = new RPCInvocation<BaseListLoadResult<VolumeInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<VolumeInfoBean>> callback) {
                remoteService.retrieveVolumeList(callback);
            }

            public void onComplete(BaseListLoadResult<VolumeInfoBean> obj) {
                updater.schedule(REFRESH_INTERVAL);
            }

        };
        vLoader = new BaseListLoader<BaseListLoadResult<VolumeInfoBean>>(vProxy);
        vStore = new GroupingStore<VolumeInfoBean>(vLoader);
        sm = new CheckBoxSelectionModel<VolumeInfoBean>();
    }

    public static VolumeView getInstance() {
        if (instance == null) {
            instance = new VolumeView();
        }
        return instance;
    }

    public void scheduleRefresh() {
        if (refreshPending) {
            updater.cancel();
        }
        vLoader.load();
        updater.schedule(REFRESH_INTERVAL);
        refreshPending = true;
    }

    public void cancelRefresh() {
        updater.cancel();
        refreshPending = false;
    }

    private RpcProxy<BaseListLoadResult<VolumeInfoBean>> vProxy;
    private BaseListLoader<BaseListLoadResult<VolumeInfoBean>> vLoader;
    private GroupingStore<VolumeInfoBean> vStore;
    private CheckBoxSelectionModel<VolumeInfoBean> sm;
    private Button cBtn;
    private Button aBtn;
    private Button dBtn;

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        sm.setSelectionMode(SelectionMode.SINGLE);
        vLoader.setRemoteSort(true);
        vStore.groupBy("CloudName");
        vLoader.load();

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        configs.add(sm.getColumn());
        // configs.add(new ColumnConfig("Name", "Name", 100));
        configs.add(new ColumnConfig("CloudName", "CloudName", 100));
        configs.add(new ColumnConfig("VolumeId", "VolumeId", 100));
        configs.add(new ColumnConfig("Size", "Size (GB)", 100));
        configs.add(new ColumnConfig("Path", "Path", 100));
        configs.add(new ColumnConfig("Comment", "Comment", 100));
        if (MainPage.getInstance().getUserProfile().getAdmin()) {
            configs.add(new ColumnConfig("User", "User", 50));
        }
        final ColumnModel cm = new ColumnModel(configs);

        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(true);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {

            @Override
            public String render(GroupColumnData data) {
                String header = cm.getColumnById(data.field).getHeader();
                String u = data.models.size() == 1 ? "Volumn" : "Volumns";
                return header + ": " + data.group + " (" + data.models.size()
                        + " " + u + ")";
            }

        });

        Grid<VolumeInfoBean> grid = new Grid<VolumeInfoBean>(vStore, cm);
        grid.setBorders(false);
        grid.setAutoExpandColumn("VolumeId");
        grid.setView(view);
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        ToolBar toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        cBtn = new Button("Create", AbstractImagePrototype
                .create(Resources.ICONS.register()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        createDialog("Creat a new volumn").show();
                    }

                });
        cBtn.setToolTip("create new volumn");
        aBtn = new Button("Attach", AbstractImagePrototype
                .create(Resources.ICONS.attachVolumn()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TodoViewer.show();
                    }

                });
        aBtn.setToolTip("attach this volumn to instance");
        dBtn = new Button("Delete", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final List<VolumeInfoBean> selected = sm
                                .getSelectedItems();

                        new RPCInvocation<List<VolumeInfoBean>>(false, true,
                                true, false) {

                            @Override
                            public void execute(
                                    CloudRemoteServiceAsync remoteService,
                                    AsyncCallback<List<VolumeInfoBean>> callback) {

                                remoteService.deleteVolumes(selected, callback);
                            }

                            public void onComplete(List<VolumeInfoBean> res) {
                                for (VolumeInfoBean bean : res) {
                                    vStore.remove(bean);
                                }
                            }

                            public String getConfirmMsg() {
                                VolumeInfoBean pooledVolume = null;
                                for (VolumeInfoBean i : selected) {
                                    if (i.getPooled()) {
                                        pooledVolume = i;
                                        break;
                                    }
                                }
                                return "Really want to delete"
                                        + (selected.size() > 1 ? " these volumes?"
                                                : " the volume?")
                                        + (pooledVolume != null ? " There's a pooled volume ("
                                                + pooledVolume.getVolumeId()
                                                + ")"
                                                : "");
                            }

                            public String getProgressMsg() {
                                return "Deleting "
                                        + (selected.size() > 1 ? " volumes?"
                                                : " volume?");
                            }

                        }.invoke(false);
                    }

                });
        dBtn.setToolTip("delete volumn");
        aBtn.setEnabled(false);
        dBtn.setEnabled(false);
        sm
                .addSelectionChangedListener(new SelectionChangedListener<VolumeInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<VolumeInfoBean> se) {
                        if (sm.getSelection().size() > 0) {
                            aBtn.setEnabled(true);
                            dBtn.setEnabled(true);
                            cancelRefresh();
                        } else {
                            aBtn.setEnabled(false);
                            dBtn.setEnabled(false);
                            scheduleRefresh();
                        }
                    }

                });
        toolBar.add(cBtn);
        // toolBar.add(aBtn);
        toolBar.add(dBtn);

        ContentPanel _contentPanel = new ContentPanel();
        _contentPanel.setBodyBorder(false);
        _contentPanel.setHeaderVisible(false);
        _contentPanel.setLayout(new FitLayout());
        _contentPanel.add(grid);
        _contentPanel.setTopComponent(toolBar);

        setLayout(new FitLayout());
        add(_contentPanel);
    }

    private RpcProxy<BaseListLoadResult<CloudInfoBean>> cloudProxy;
    private ListLoader<BaseListLoadResult<CloudInfoBean>> cloudLoader;
    private ListStore<CloudInfoBean> cloudStore;

    public Dialog createDialog(String title) {
        final Dialog dialog = new Dialog();
        dialog.setWidth(400);
        dialog.setHeight(200);
        dialog.setHeading(title);
        dialog.setBodyBorder(false);
        // dialog.setHideOnButtonClick(true);

        final FormPanel fp = new FormPanel();
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(75);
        fp.setLayout(fl);
        fp.setHeaderVisible(false);
        fp.setBodyBorder(false);
        fp.setBorders(false);

        final TextField<String> des = new TextField<String>();
        final Text note = new Text();
        final FormData nfd = new FormData();
        nfd.setMargins(new Margins(0, 0, 10, 0));
        note.setTagName("p");
        note.setStyleName("warning");

        final TextField<String> sizeTF = new TextField<String>();
        sizeTF.setFieldLabel("Size (GB)");
        sizeTF.setValue("10");
        FormData sfd = new FormData();
        sfd.setWidth(75);
        sfd.setMargins(new Margins(0, 0, 10, 0));
        fp.add(sizeTF, sfd);

        final ComboBox<CloudInfoBean> comboCloud = new ComboBox<CloudInfoBean>();
        comboCloud.setFieldLabel("Cloud");
        comboCloud.setDisplayField("CloudName");
        cloudProxy = new RPCInvocation<BaseListLoadResult<CloudInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<CloudInfoBean>> callback) {
                remoteService.retrieveCloudList(new ArrayList<String>(),
                        callback);
            }

        };
        cloudLoader = new BaseListLoader<BaseListLoadResult<CloudInfoBean>>(
                cloudProxy);
        cloudStore = new ListStore<CloudInfoBean>(cloudLoader);
        comboCloud.setStore(cloudStore);
        comboCloud.setEditable(false);
        comboCloud.setEmptyText("Select a Cloud...");
        FormData fd = new FormData("0");
        fd.setMargins(new Margins(0, 0, 10, 0));
        fp.add(comboCloud, fd);

        des.setFieldLabel("Comment");
        fp.add(des, fd);

        Button okbtn = dialog.getButtonById(Dialog.OK);
        okbtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final String value = sizeTF.getValue();
                if (!Util.isEmpty(value)) {
                    for (int i = 0; i < value.length(); i++) {
                        if (!Util.isDigit(value.charAt(i))) {
                            dialog.setHeight(240);
                            int index = fp.indexOf(des);
                            note.setText("Size field contains invalid digit");
                            fp.insert(note, index + 1, nfd);
                            fp.layout();
                            return;
                        }
                        if (i == 0 && Util.isZero(value.charAt(0))) {
                            int index = fp.indexOf(des);
                            note.setText("Size field has heading zero");
                            fp.insert(note, index + 1, nfd);
                            fp.layout();
                            return;
                        }
                    }
                }

                final CloudInfoBean bean = comboCloud.getValue();
                if (bean == null) {
                    dialog.setHeight(220);
                    int index = fp.indexOf(des);
                    note
                            .setText("please create the volumn associated with a existing cloud");
                    fp.insert(note, index + 1, nfd);
                    fp.layout();
                    return;
                }

                final String description = des.getValue();
                if (Util.isEmpty(description)) {
                    dialog.setHeight(220);
                    int index = fp.indexOf(des);
                    note.setText("please give some comments for this volume");
                    fp.insert(note, index + 1, nfd);
                    fp.layout();
                    return;
                }

                new RPCInvocation<VolumeInfoBean>(false, true, false, false) {

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<VolumeInfoBean> callback) {
                        int size = Integer.parseInt(value);
                        remoteService.createVolume(bean, size, description,
                                callback);
                    }

                    public String getProgressTitle() {
                        return "Create new volume";
                    }

                    public String getProgressText() {
                        return "creating...";
                    }

                    public void onComplete(VolumeInfoBean obj) {
                        cancelRefresh();
                        vLoader.load();
                    }

                }.invoke(false);

                dialog.hide();
            }

        });

        Listener<FieldEvent> clistener = new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                if (fp.indexOf(note) >= 0) {
                    note.setText("");
                    fp.remove(note);
                }
                dialog.setHeight(200);
                fp.layout();
            }
        };
        sizeTF.addListener(Events.OnClick, clistener);
        comboCloud.addListener(Events.OnClick, clistener);

        dialog.add(fp);
        return dialog;
    }

    @Override
    public void updateView() {
        if (vLoader != null) {
            vLoader.load();
        }
        scheduleRefresh();
    }

}
