package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.widget.CedarIconButton;

public class RegisterPage extends LayoutContainer {
    private Viewport vp;
    private FormPanel fp;
    private TextField<String> username;
    private TextField<String> password;
    private TextField<String> confirmPwd;
    private TextField<String> email;
    private Text note;
    private LayoutContainer noteArea;
    private static RegisterPage instance;
    private Listener<FieldEvent> listener;

    private static int MIN_KEY_LENGTH = 6;
    private static String EXISTED_WARNING = "username or email has already been used!";
    private static String EMPTY_WARNING = "invalid email address";
    private static String MISMATCH_WARNING = "re-enter the password";
    private static String LENGTH_WARNING = "little";

    public RegisterPage() {

    }

    public RegisterPage(Viewport vp) {
        this.vp = vp;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setSize(1024, 760);
        cp.setLayout(new FlowLayout());
        cp
                .add(new Image(
                        "resources/images/cloudtestservice/mainpage_header_bg_99bbe8.png"));

        ContentPanel body = new ContentPanel();
        body.setSize(1024, 680);
        body.setBodyStyleName("regpageBody");
        body.setBodyBorder(false);
        body.setHeaderVisible(false);
        body.setLayout(new FlowLayout());

        fp = new FormPanel();
        FormLayout fl = new FormLayout();
        fl.setLabelWidth(110);
        fp.setSize(420, 560);
        fp.setHeaderVisible(false);
        fp.setBodyBorder(false);
        fp.setBorders(false);
        fp.setStyleName("reg_form");
        fp.setPadding(10);
        fp.setLayout(fl);

        IconButton cancelBtn = new CedarIconButton("reg_cancel");
        cancelBtn.setSize(25, 25);
        FormData cfd = new FormData();
        cfd.setMargins(new Margins(0, 0, 10, 375));
        cancelBtn
                .addSelectionListener(new SelectionListener<IconButtonEvent>() {

                    @Override
                    public void componentSelected(IconButtonEvent ce) {
                        vp.hide();
                        CloudTestService loginPanel = Registry
                                .get(Constants.LOGIN_PANEL_ID);
                        RootPanel.get().add(loginPanel.createLoginPage());
                    }

                });
        fp.add(cancelBtn, cfd);

        Label cap = new Label("Sign Up");
        cap.setStyleName("reg_form_header");
        fp.add(cap);

        FormData fdh = new FormData();
        fdh.setMargins(new Margins(0, 0, 20, 0));
        Html h = new Html("<hr style='height:3px;color:#dcdcdc'/>");
        fp.add(h, fdh);

        FormData fd = new FormData();
        fd.setWidth(250);
        fd.setHeight(25);
        fd.setMargins(new Margins(0, 0, 20, 0));
        username = new TextField<String>();
        username.setFieldLabel("User Name");
        username.setLabelStyle("reg_form_label");
        fp.add(username, fd);

        password = new TextField<String>();
        password.setFieldLabel("Password");
        password.setPassword(true);
        password.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                // if(isEmpty(password.getValue())) return;
                // if(password.getValue().length()<6){
                // int index = fp.indexOf(email);
                // fp.insert(noteArea, index + 1);
                // note.setText(LENGTH_WARNING);
                // fp.layout();
                // }
            }

        });
        fp.add(password, fd);

        confirmPwd = new TextField<String>();
        confirmPwd.setFieldLabel("Confirm Password");
        confirmPwd.setPassword(true);
        fp.add(confirmPwd, fd);

        email = new TextField<String>();
        email.setFieldLabel("E-Mail");
        fp.add(email, fd);

        noteArea = new LayoutContainer();
        noteArea.setLayout(new ColumnLayout());
        LayoutContainer left = new LayoutContainer();
        noteArea.add(left, new ColumnData(.3));
        note = new Text();
        note.setTagName("p");
        note.setStyleName("warning");
        noteArea.add(note, new ColumnData(.7));

        IconButton iconBtn = new CedarIconButton("reg_signup");
        iconBtn.setSize(120, 24);
        FormData fdBtn = new FormData();
        fdBtn.setMargins(new Margins(20, 140, 20, 140));
        iconBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                final UserInfoBean bean = new UserInfoBean();
                String user = username.getValue();
                String pwd = password.getValue();
                String cpwd = confirmPwd.getValue();
                String mail = email.getValue();
                if (isEmpty(user) || isEmpty(pwd) || isEmpty(cpwd)
                        || isEmpty(mail) || !mail.contains("@")
                        || mail.contains(",")) {
                    int index = fp.indexOf(email);
                    fp.insert(noteArea, index + 1);
                    note.setText(EMPTY_WARNING);
                    fp.layout();
                    return;
                }
                if (!pwd.equals(cpwd)) {
                    int index = fp.indexOf(email);
                    fp.insert(noteArea, index + 1);
                    note.setText(MISMATCH_WARNING);
                    confirmPwd.setValue("");
                    fp.layout();
                    return;
                }
                bean.setUserName(user);
                bean.setPassword(pwd);
                bean.setEmail(mail);
                bean.setAdmin(false);

                RPCInvocation<Boolean> proxy = new RPCInvocation<Boolean>() {

                    @Override
                    public void execute(CloudRemoteServiceAsync remoteService,
                            AsyncCallback<Boolean> callback) {
                        remoteService.registerUser(bean, callback);
                    }

                    public void onComplete(Boolean obj) {
                        if (obj) {
                            vp.hide();
                            MainPage instance = MainPage.getInstance();
                            instance.setUserProfile(bean);
                            instance.selected();
                            Window.Location.reload();
                        } else {
                            int index = fp.indexOf(email);
                            fp.insert(noteArea, index + 1);
                            note.setText(EXISTED_WARNING);
                            fp.layout();
                        }
                    }

                };
                proxy.invoke(false);
            }

        });
        fp.add(iconBtn, fdBtn);

        // fp.addButton(new Button("Try the user", new
        // SelectionListener<ButtonEvent>(){
        //
        // @Override
        // public void componentSelected(ButtonEvent ce) {
        // final UserInfoBean bean = new UserInfoBean();
        // String user = username.getValue();
        // String pwd = password.getValue();
        // String cpwd = confirmPwd.getValue();
        // String mail = email.getValue();
        // if(isEmpty(user)||isEmpty(pwd)||isEmpty(cpwd)||isEmpty(mail)){
        // int index = fp.indexOf(email);
        // fp.insert(noteArea, index + 1);
        // note.setText(EMPTY_WARNING);
        // fp.layout();
        // return;
        // }
        // if(!pwd.equals(cpwd)){
        // int index = fp.indexOf(email);
        // fp.insert(noteArea, index + 1);
        // note.setText(MISMATCH_WARNING);
        // confirmPwd.setValue("");
        // fp.layout();
        // return;
        // }
        // bean.setUserName(user);
        // bean.setPassword(pwd);
        // bean.setEmail(bean.getEmail());
        //				
        // RPCInvocation<Boolean> proxy = new RPCInvocation<Boolean>(){
        //
        // @Override
        // public void execute(CloudRemoteServiceAsync remoteService,
        // AsyncCallback<Boolean> callback) {
        // remoteService.registerUser(bean, callback);
        // }
        //					
        // public void onComplete(Boolean obj){
        // if(obj){
        // vp.hide();
        // MainPage instance = MainPage.getInstance();
        // instance.setUserProfile(bean.getUserName());
        // instance.selected();
        // }else{
        // int index = fp.indexOf(email);
        // fp.insert(noteArea, index + 1);
        // note.setText(EXISTED_WARNING);
        // fp.layout();
        // }
        // }
        //					
        // };
        // proxy.invoke(false);
        // }
        //			
        // }));

        // fp.addButton(new Button("Cancel", new
        // SelectionListener<ButtonEvent>(){
        //
        // @Override
        // public void componentSelected(ButtonEvent ce) {
        // vp.hide();
        // RootPanel.get().add(CloudTestService.createLoginPage());
        // }
        //			
        // }));

        listener = new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent be) {
                if (fp.indexOf(noteArea) >= 0) {
                    note.setText("");
                    fp.remove(noteArea);
                }
            }

        };
        username.addListener(Events.OnClick, listener);
        password.addListener(Events.OnClick, listener);
        confirmPwd.addListener(Events.OnClick, listener);
        email.addListener(Events.OnClick, listener);

        FlowData flowData = new FlowData(new Margins(40, 40, 0, 40));
        body.add(fp, flowData);
        cp.add(body);
        add(cp);
    }

    private boolean isEmpty(String str) {
        if (str == null || str.equals(""))
            return true;
        return false;
    }

}
