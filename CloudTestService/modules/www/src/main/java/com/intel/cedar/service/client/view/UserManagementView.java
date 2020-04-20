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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.resources.Resources;

public class UserManagementView extends ComponentViewer {

    private static UserManagementView instance;
    private Grid<UserInfoBean> grid;
    private RpcProxy<BaseListLoadResult<UserInfoBean>> userProxy;
    private ListLoader<BaseListLoadResult<UserInfoBean>> userLoader;
    private ListStore<UserInfoBean> userStore;

    public static UserManagementView getInstance() {
        if (instance == null) {
            instance = new UserManagementView();
        }
        return instance;
    }

    private UserManagementView() {
        userProxy = new RPCInvocation<BaseListLoadResult<UserInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<BaseListLoadResult<UserInfoBean>> callback) {
                remoteService.retrieveUserList(new ArrayList<String>(),
                        callback);
            }

        };
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        final CheckBoxSelectionModel<UserInfoBean> sm = new CheckBoxSelectionModel<UserInfoBean>();
        sm.setSelectionMode(SelectionMode.SINGLE);

        userLoader = new BaseListLoader<BaseListLoadResult<UserInfoBean>>(
                userProxy);
        userStore = new ListStore<UserInfoBean>(userLoader);
        userLoader.load();

        ArrayList<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
        columnConfigs.add(sm.getColumn());
        columnConfigs.add(new ColumnConfig("UserName", "UserName", 150));
        columnConfigs.add(new ColumnConfig("Email", "Email", 100));
        columnConfigs.add(new ColumnConfig("Admin", "Admin", 150));
        ColumnModel cm = new ColumnModel(columnConfigs);

        grid = new Grid<UserInfoBean>(userStore, cm);
        grid.setBorders(false);
        grid.setAutoExpandColumn("Email");
        grid.setSelectionModel(sm);
        grid.addPlugin(sm);

        ToolBar toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        final Button delBtn = new Button("Delete", AbstractImagePrototype
                .create(Resources.ICONS.deRegister()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        final List<UserInfoBean> list = sm.getSelectedItems();
                        if (list == null || list.size() == 0) {
                            MessageBox.info("Info",
                                    "not any user selected to delete", null)
                                    .show();
                        } else {
                            new RPCInvocation<List<UserInfoBean>>() {

                                @Override
                                public void execute(
                                        CloudRemoteServiceAsync remoteService,
                                        AsyncCallback<List<UserInfoBean>> callback) {
                                    remoteService.deleteUser(list, callback);
                                }

                                public void onComplete(List<UserInfoBean> res) {
                                    for (UserInfoBean bean : res) {
                                        grid.getStore().remove(bean);
                                    }
                                    updateView();
                                }

                            }.invoke(false);
                        }
                    }

                });
        toolBar.add(delBtn);

        final Button editBtn = new Button("Edit", AbstractImagePrototype
                .create(Resources.ICONS.editCloud()),
                new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        createEditDialog(sm.getSelectedItem()).show();
                    }
                });
        toolBar.add(editBtn);
        delBtn.setEnabled(false);
        editBtn.setEnabled(false);

        sm
                .addSelectionChangedListener(new SelectionChangedListener<UserInfoBean>() {

                    @Override
                    public void selectionChanged(
                            SelectionChangedEvent<UserInfoBean> se) {
                        UserInfoBean logonUser = MainPage.getInstance()
                                .getUserProfile();
                        UserInfoBean user = se.getSelectedItem();
                        if (user != null) {
                            if (logonUser.getAdmin()) {
                                delBtn.setEnabled(true);
                                editBtn.setEnabled(true);
                                return;
                            } else if (logonUser.getId().equals(user.getId())) {
                                editBtn.setEnabled(true);
                                return;
                            }
                        }
                        delBtn.setEnabled(false);
                        editBtn.setEnabled(false);
                    }
                });

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setBorders(false);
        cp.setLayout(new FitLayout());
        cp.add(grid);
        cp.setTopComponent(toolBar);

        setLayout(new FitLayout());
        add(cp);
    }

    @Override
    public void updateView() {
        if (userLoader != null)
            userLoader.load();
    }

    public Dialog createEditDialog(final UserInfoBean user) {
        final Dialog dialog = new Dialog();
        dialog.setHeading("Change Settings for " + user.getUserName());
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

        final TextField<String> email = new TextField<String>();
        email.setFieldLabel("Email");
        email.setValue(user.getEmail());
        formPanel.add(email, formData);

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
                String e = email.getValue();
                // very naive email validation1
                if (e == null || !e.contains("@") || e.contains(",")) {
                    note.setText("Email address is not valid");
                    int index = formPanel.indexOf(confirmPwd);
                    formPanel.insert(note, index + 1, notefd);
                    dialog.setSize(300, 240);
                    dialog.layout();
                    okBtn.setEnabled(false);
                    return;
                }

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

        email.addListener(Events.KeyUp, rListener);
        newPwd.addListener(Events.KeyUp, rListener);
        confirmPwd.addListener(Events.KeyUp, rListener);

        okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                final String nP = newPwd.getValue();
                if (note.getText() != null && note.getText().length() > 0) {
                    return;
                }
                final UserInfoBean change = new UserInfoBean();
                change.setAdmin(user.getAdmin());
                change.setUserName(user.getUserName());
                change.setEmail(email.getValue());
                change.setId(user.getId());
                change.setPassword(nP);

                new RPCInvocation<Boolean>() {
                    @Override
                    public void onComplete(Boolean result) {
                        if (!result) {
                            MessageBox.info("Info", "Failed to apply changes!",
                                    null).show();
                        }
                        updateView();
                    }

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Boolean> callback) {
                        remoteService.changeUserSettings(change, callback);
                    }
                }.invoke(false);
            }
        });

        dialog.add(formPanel);
        return dialog;
    }
}
