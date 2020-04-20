package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.widget.CedarIconButton;

public class CloudTestService implements EntryPoint {
    public static int REFRESH_INTERVAL = 10000;
    private static Viewport vp;
    private static FormPanel loginPanel;
    private static TextField<String> userName;
    private static TextField<String> passwd;
    private static HorizontalPanel pgBar;
    private static FormData pgfd;
    private static HorizontalPanel btnBar;
    private static FormData btnBarfd;

    public static CloudRemoteServiceAsync _cloudRemoteService = GWT
            .create(CloudRemoteService.class);

    public void onModuleLoad() {
        Registry.register(Constants.LOGIN_PANEL_ID, this);
        RootPanel.get().add(createLoginPage());
        initByCookie();
    }

    public LayoutContainer createLoginPage() {
        // use Viewport to adapt the window resizing
        if (vp == null) {
            vp = new Viewport();
            FitLayout fitLayout = new FitLayout();
            FitData fitData = new FitData();
            vp.setLayout(fitLayout);
            final HorizontalPanel hp = new HorizontalPanel();
            hp.setTableHeight("100%");
            hp.setTableWidth("100%");

            // HorizontalPanel will treat the inside element as table element
            // if employing the ContentPanel, the scroll bar does not behave as
            // expected
            TableData tableData = new TableData();
            tableData.setHorizontalAlign(HorizontalAlignment.CENTER);
            tableData.setVerticalAlign(VerticalAlignment.MIDDLE);

            // HorizontalPanel inner = new HorizontalPanel();
            VerticalPanel inner = new VerticalPanel();
            ContentPanel imagePanel = new ContentPanel();
            imagePanel.setSize(300, 60);
            imagePanel.setHeaderVisible(false);
            imagePanel.setBodyBorder(false);
            imagePanel.add(new Image(
                    "resources/images/cloudtestservice/login_logo_small.png"));
            TableData innertd = new TableData();
            innertd.setHorizontalAlign(HorizontalAlignment.LEFT);
            innertd.setVerticalAlign(VerticalAlignment.TOP);

            loginPanel = new FormPanel();
            loginPanel.setSize(300, 140);
            loginPanel.setHeading("login");
            loginPanel.setFrame(true);

            ContentPanel emptyPanel = new ContentPanel();
            emptyPanel.setSize(300, 60);
            emptyPanel.setHeaderVisible(false);
            emptyPanel.setBodyBorder(false);

            FormData formData = new FormData("0");
            FormLayout formLayout = new FormLayout();
            loginPanel.setLayout(formLayout);

            userName = new TextField<String>();
            userName.setFieldLabel("User Name");
            // userName.setAllowBlank(false);
            loginPanel.add(userName, formData);

            passwd = new TextField<String>();
            passwd.setFieldLabel("Password");
            // passwd.setAllowBlank(false);
            passwd.setPassword(true);
            loginPanel.add(passwd, formData);

            pgBar = new HorizontalPanel();
            pgBar.setTableWidth("267px");
            pgBar.setHeight(16);
            pgfd = new FormData();
            pgfd.setMargins(new Margins(15, 0, 0, 0));
            pgBar.add(new Image("resources/images/cloudtestservice/pgBar.gif"));
            // loginPanel.add(pgBar, formData);

            btnBar = new HorizontalPanel();
            btnBar.setTableWidth("100%");
            IconButton signinBtn = new CedarIconButton("cedar_signin");
            signinBtn.setSize(80, 24);

            TableData signintd = new TableData();
            signintd.setWidth("70%");
            signintd.setHorizontalAlign(HorizontalAlignment.RIGHT);
            btnBar.add(signinBtn, signintd);
            IconButton regBtn = new CedarIconButton("cedar_register");
            regBtn.setSize(80, 24);
            regBtn
                    .addSelectionListener(new SelectionListener<IconButtonEvent>() {

                        @Override
                        public void componentSelected(IconButtonEvent ce) {
                            vp.hide();
                            Viewport register = new Viewport();
                            register.setLayout(new CenterLayout());
                            register.add(new RegisterPage(register));
                            RootPanel.get().add(register);
                        }

                    });
            TableData regtd = new TableData();
            regtd.setWidth("30%");
            regtd.setHorizontalAlign(HorizontalAlignment.RIGHT);
            btnBar.add(regBtn, regtd);
            btnBarfd = new FormData();
            btnBarfd.setMargins(new Margins(15, 0, 0, 0));
            signinBtn
                    .addSelectionListener(new SelectionListener<IconButtonEvent>() {

                        @Override
                        public void componentSelected(IconButtonEvent ce) {
                            CloudTestService.this.onSumbit();
                        }

                    });
            loginPanel.add(btnBar, btnBarfd);
            KeyListener keyListener = new KeyListener() {
                public void componentKeyPress(ComponentEvent event) {
                    if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
                        CloudTestService.this.onSumbit();
                    }
                }
            };
            userName.addKeyListener(keyListener);
            passwd.addKeyListener(keyListener);

            inner.add(imagePanel, innertd);
            inner.add(loginPanel, innertd);
            inner.add(emptyPanel, innertd);
            hp.add(inner, tableData);
            vp.add(hp, fitData);
        }
        vp.show();
        return vp;
    }

    public void initByCookie() {
        String credential = Cookies.getCookie(Constants.CTS_CREDENTIAL_COOKIE);
        // if(credential!=null){
        new RPCInvocation<UserInfoBean>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<UserInfoBean> callback) {
                remoteService.checkCredentialCookie(callback);
            }

            public void onComplete(UserInfoBean obj) {
                if (obj != null) {
                    userName.setValue(obj.getUserName());
                    passwd.setValue(obj.getPassword());
                } else {

                }
            }
        }.invoke(false);
        // }
    }

    public void onSumbit() {
        final UserInfoBean bean = new UserInfoBean();
        final String user = userName.getValue();
        String pwd = passwd.getValue();
        if (isEmpty(user) || isEmpty(pwd)) {
            MessageBox.alert("Error", "please input correct user credential",
                    null);
            return;
        }
        bean.setUserName(user);
        bean.setPassword(pwd);

        RPCInvocation<UserInfoBean> proxy = new RPCInvocation<UserInfoBean>(
                false, false, false, false) {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<UserInfoBean> callback) {
                remoteService.loginUser(bean, callback);
            }

            public void onComplete(UserInfoBean obj) {
                if (obj != null) {
                    switchToNormal(true);
                    vp.hide();
                    MainPage instance = MainPage.getInstance();
                    instance.setUserProfile(obj);
                    instance.selected();

                } else {
                    switchToNormal(true);
                    MessageBox.alert("Error",
                            "please input correct user credential", null);
                }
            }

            public String getProgressTitle() {
                return "Sign In";
            }

            public String getProgressText() {
                return "Logging...";
            }

        };
        proxy.invoke(false);
        switchToNormal(false);
    }

    public static void switchToNormal(boolean normal) {
        if (normal) {
            if (loginPanel.indexOf(pgBar) >= 0)
                loginPanel.remove(pgBar);
            loginPanel.setHeading("login");
            loginPanel.add(btnBar, btnBarfd);
            userName.enable();
            passwd.enable();
            loginPanel.layout();
        } else {
            if (loginPanel.indexOf(btnBar) >= 0)
                loginPanel.remove(btnBar);
            loginPanel.setHeading("logging...");
            userName.disable();
            passwd.disable();
            loginPanel.add(pgBar, pgfd);
            loginPanel.layout();
        }
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.equals(""))
            return true;
        return false;
    }

}