package com.intel.cedar.service.client;

import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.MemoryProxy;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.ImageMap;
import com.intel.cedar.service.client.feature.model.ImageModel;
import com.intel.cedar.service.client.feature.view.FeatureViewListener;
import com.intel.cedar.service.client.model.UserInfoBean;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.view.ComponentViewer;
import com.intel.cedar.service.client.view.InstanceLauncher;
import com.intel.cedar.service.client.view.ServerClock;
import com.intel.cedar.service.client.view.ViewCache;
import com.intel.cedar.service.client.view.ViewCache.ViewType;
import com.intel.cedar.service.client.widget.CedarIconButton;

public class MainPage extends LayoutContainer {
    private UserInfoBean userBean;
    private TabPanel center;
    private Viewport vp;
    private static MainPage instance = null;
    private ListView<BeanModel> view;
    private PagingLoadConfig loadConfig = new BasePagingLoadConfig(0, 5);
    private BasePagingLoader<PagingLoadResult<FeatureInfoBean>> fLoader;
    private ListView<BeanModel> fview;
    private ListView<BeanModel> uView;
    public static HashMap<String, String> treeItemtoTabItem = new HashMap<String, String>();
    public static HashMap<String, String> treeItemtoStyle = new HashMap<String, String>();
    static {
        // tree item name to tab tile mapping
        treeItemtoTabItem.put("Image", "Images");
        treeItemtoTabItem.put("Instance", "Instances");
        treeItemtoTabItem.put("Volume", "Volumes");
        treeItemtoTabItem.put("Type", "Types");
        treeItemtoTabItem.put("Key", "KeyPairs");
        treeItemtoTabItem.put("Launch", "Launch");
        treeItemtoTabItem.put("Cloud", "Clouds");
        treeItemtoTabItem.put("Host", "Host");

        treeItemtoTabItem.put("Users", "Users");
        treeItemtoTabItem.put("Features", "Features");
        treeItemtoTabItem.put("Jobs", "Jobs");
        treeItemtoTabItem.put("History", "History");

        // style mapping
        treeItemtoStyle.put("Image", "image");
        treeItemtoStyle.put("Instance", "instance");
        treeItemtoStyle.put("Volume", "volume");
        treeItemtoStyle.put("Type", "list");
        treeItemtoStyle.put("Key", "key");
        treeItemtoStyle.put("Launch", "addround");
        treeItemtoStyle.put("Cloud", "cloud");
        treeItemtoStyle.put("Host", "host");

        treeItemtoStyle.put("Users", "users");
        treeItemtoStyle.put("Features", "features");
        treeItemtoStyle.put("Jobs", "services");
        treeItemtoStyle.put("History", "history");
    }

    public static synchronized MainPage getInstance() {
        if (instance == null) {
            instance = new MainPage();
        }
        return instance;
    }

    private MainPage() {

    }

    public void selected() {
        vp = new Viewport();
        vp.setLayout(new FitLayout());
        FitData fitData = new FitData();

        HorizontalPanel hp = new HorizontalPanel();
        hp.setScrollMode(Scroll.AUTO);
        hp.setTableWidth("100%");

        TableData tableData = new TableData();
        tableData.setHorizontalAlign(HorizontalAlignment.CENTER);

        HorizontalPanel innerhp = new HorizontalPanel();
        int width = 1024, height = 760;
        innerhp.setSize(width, height);

        TableData innertd = new TableData();
        innertd.setHorizontalAlign(HorizontalAlignment.LEFT);
        ContentPanel cp = new ContentPanel();
        cp.setBodyStyleName("mainpageBody");
        cp.setBodyBorder(false);
        cp.setSize(1024, 760);
        cp.setHeaderVisible(false);
        cp.setLayout(new BorderLayout());

        LayoutContainer north = new LayoutContainer();
        int northSize = 80;
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
                northSize);
        north.setStyleName("mainpageHeader");
        north.setLayout(new FitLayout());
        VerticalPanel vpanel = new VerticalPanel();
        // vpanel.setBorders(true);
        vpanel.setSize(1024, 80);
        TableData vpaneltd = new TableData();
        vpaneltd.setPadding(2);
        HorizontalPanel empty = new HorizontalPanel();
        // empty.setBorders(true);
        empty.setSize(1010, 24);
        vpanel.add(empty, vpaneltd);
        HorizontalPanel welcome = new HorizontalPanel();
        // welcome.setBorders(true);
        welcome.setSize(1008, 24);
        TableData welTd = new TableData();
        welTd.setHorizontalAlign(HorizontalAlignment.CENTER);
        Label helloUser = new Label("hello, " + userBean.getUserName());
        helloUser.setWidth("1008px");
        helloUser.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        helloUser.setStyleName("mainpageHeader_user");
        welcome.add(helloUser, welTd);
        vpanel.add(welcome, vpaneltd);
        HorizontalPanel verbosehp = new HorizontalPanel();
        // verbosehp.setBorders(true);
        verbosehp.setSize(1010, 24);
        TableData northtd = new TableData();
        northtd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        northtd.setVerticalAlign(VerticalAlignment.BOTTOM);
        TableData clocktd1 = new TableData();
        clocktd1.setHorizontalAlign(HorizontalAlignment.CENTER);
        Label space1 = new Label();
        space1.setWidth("200px");
        space1.setStyleName("clock");
        verbosehp.add(space1, clocktd1);
        ServerClock clock = ServerClock.getInstance();
        clock.setLabel(space1);
        clock.start();
        Label space2 = new Label();
        space2.setWidth("724px");
        verbosehp.add(space2, northtd);
        final IconButton logoutBtn = new CedarIconButton("logout");
        logoutBtn.addListener(Events.Select, new Listener<IconButtonEvent>() {

            @Override
            public void handleEvent(IconButtonEvent be) {
                vp.hide();
                CloudTestService loginPanel = Registry
                        .get(Constants.LOGIN_PANEL_ID);
                RootPanel.get().add(loginPanel.createLoginPage());
                // CloudTestService.switchToNormal(true);
            }

        });
        logoutBtn.setSize(86, 22);
        verbosehp.add(logoutBtn, northtd);
        vpanel.add(verbosehp, vpaneltd);
        north.add(vpanel);

        BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
        westData.setSplit(false);
        westData.setMargins(new Margins(3, 3, 3, 3));
        ContentPanel west = createMainPanel();

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(3, 3, 3, 0));
        center = new TabPanel();
        center.setTabScroll(true);
        TabItem startTab = new TabItem("Start Page");
        // startTab.setClosable(true);
        center.setBodyBorder(false);
        startTab.setBorders(true);
        center.add(startTab);

        BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
                40);
        LayoutContainer south = new LayoutContainer();

        cp.add(north, northData);
        cp.add(west, westData);
        cp.add(center, centerData);
        cp.add(south, southData);

        innerhp.add(cp, innertd);
        hp.add(innerhp, tableData);
        vp.add(hp, fitData);
        RootPanel.get().add(vp);
    }

    public class ViewerListener extends SelectionListener<TabPanelEvent> {
        public void componentSelected(TabPanelEvent ce) {
            TabItem container = ce.getItem();
            String text = container.getText();
            if (text.equalsIgnoreCase("Images")) {
                renderTabItem(container, ViewType.TYPE_IMAGE_VIEW);
            } else if (text.equalsIgnoreCase("Instances")) {
                renderTabItem(container, ViewType.TYPE_INSTANCE_VIEW);
            } else if (text.equalsIgnoreCase("Types")) {
                renderTabItem(container, ViewType.TYPE_MACHINETYPE_VIEW);
            } else if (text.equalsIgnoreCase("KeyPairs")) {
                renderTabItem(container, ViewType.TYPE_KEY_VIEW);
            } else if (text.equalsIgnoreCase("Launch")) {
                container.removeAll();
                container.setLayout(new FitLayout());
                InstanceLauncher launcherView = new InstanceLauncher();
                launcherView.setParentItem(container);
                container.add(launcherView);
                container.layout();
            } else if (text.equalsIgnoreCase("Clouds")) {
                renderTabItem(container, ViewType.TYPE_CLOUD_VIEW);
            } else if (text.equals("Features")) {
                renderTabItem(container, ViewType.TYPE_FEATUREMGR_VIEW);
            } else if (text.equals("Users")) {
                renderTabItem(container, ViewType.TYPE_USERMGR_VIEW);
            } else if (text.equals("Volumes")) {
                renderTabItem(container, ViewType.TYPE_VOLUME_VIEW);
            } else if (text.equals("Jobs")) {
                renderTabItem(container, ViewType.TYPE_FEATUREJOB_VIEW);
            } else if (text.equals("History")) {
                renderTabItem(container, ViewType.TYPE_HISTORY_VIEW);
            } else if (text.equals("Host")) {
                renderTabItem(container, ViewType.TYPE_PHYSICALHOST_VIEW);
            }
        }
    }

    public TabPanel getTabPanel() {
        return center;
    }

    protected void renderTabItem(TabItem container, ViewType type) {
        container.removeAll();
        container.setLayout(new FitLayout());
        ComponentViewer viewer = ViewCache.createViewer(type);
        viewer.updateView();
        container.add(viewer);
        container.layout();
    }

    public ContentPanel createMainPanel() {
        ContentPanel cp = new ContentPanel();
        cp.setHeading("Navigator");
        cp.setLayout(new AccordionLayout());
        cp.setBodyBorder(false);

        ContentPanel subcp = new ContentPanel();
        subcp.setAnimCollapse(false);
        subcp.setHeading("Resource Management");
        String[] cloudElements = new String[] { "Cloud", "Image", "Instance",
                "Volume", "Type", "Launch", "Host" };
        List<ImageModel> plist = ImageMap.getImageModels(cloudElements);
        MemoryProxy<List<ImageModel>> proxy = new MemoryProxy<List<ImageModel>>(
                plist);
        ListLoader<ListLoadResult<ImageModel>> loader = new BaseListLoader<ListLoadResult<ImageModel>>(
                proxy, new BeanModelReader());
        ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
        loader.load();

        view = new ListView<BeanModel>() {
            @Override
            protected BeanModel prepareData(BeanModel model) {
                return model;
            }
        };
        view.setBorders(false);
        view.setHeight("100%");
        view.setId("img-chooser-viewer");
        view.setTemplate(getTemplate());
        view.setStore(store);
        view.setItemSelector("div.thumb-wrap");
        view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        view.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<BeanModel>>() {

                    @Override
                    public void handleEvent(SelectionChangedEvent<BeanModel> be) {
                        BeanModel model = be.getSelectedItem();
                        ImageModel p = model.getBean();
                        String op = p.getName();
                        addTabItem(op);
                    }

                });
        subcp.add(view);
        subcp.addListener(Events.Collapse, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                view.getSelectionModel().deselectAll();
            }

        });
        cp.add(subcp);

        subcp = new ContentPanel();
        subcp.setAnimCollapse(false);
        subcp.setHeading("Test Services");
        final Button upKnob = new Button();
        upKnob.setWidth("100%");
        upKnob.setIcon(AbstractImagePrototype.create(Resources.ICONS.up()));
        final Button downKnob = new Button();
        downKnob.setWidth("100%");
        downKnob.setIcon(AbstractImagePrototype.create(Resources.ICONS.down()));
        RpcProxy<PagingLoadResult<FeatureInfoBean>> fProxy = new RPCInvocation<PagingLoadResult<FeatureInfoBean>>() {

            @Override
            public void execute(CloudRemoteServiceAsync remoteService,
                    AsyncCallback<PagingLoadResult<FeatureInfoBean>> callback) {
                remoteService.retrieveFeatureList((PagingLoadConfig)this._loadConfig, callback);
            }

            public void onComplete(PagingLoadResult<FeatureInfoBean> obj) {
                if(obj.getOffset() + loadConfig.getLimit() >= obj.getTotalLength() || obj.getTotalLength() == 0){
                    downKnob.setEnabled(false);
                }
                else{
                    downKnob.setEnabled(true);
                }
                if(obj.getOffset() - loadConfig.getLimit() < 0 || obj.getTotalLength() == 0){
                    upKnob.setEnabled(false);
                }
                else{
                    upKnob.setEnabled(true);
                }
            }

        };
        fLoader = new BasePagingLoader<PagingLoadResult<FeatureInfoBean>>(
                fProxy, new BeanModelReader());
        fLoader.setReuseLoadConfig(true);
        fLoader.setLimit(loadConfig.getLimit());
        ListStore<BeanModel> fStore = new ListStore<BeanModel>(fLoader);
        fLoader.load(loadConfig);
        fview = new ListView<BeanModel>() {
            @Override
            protected BeanModel prepareData(BeanModel model) {
                FeatureInfoBean bean = model.getBean();
                model.set("eniconpath", "/features/" + bean.getId() + "/"
                        + bean.getEnIcon());
                return model;
            }
        };
        fview.setId("img-chooser-view");
        fview.setTemplate(getTemplateForFeature());
        fview.setBorders(false);
        fview.setStore(fStore);
        fview.setItemSelector("div.thumb-wrap");
        fview.setHeight(500);
        fview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        fview.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<BeanModel>>() {
                    public void handleEvent(SelectionChangedEvent<BeanModel> be) {
                        BeanModel model = be.getSelectedItem();
                        if (model != null) {
                            FeatureInfoBean bean = model.getBean();
                            if(!bean.getEnabled()){
                                MessageBox.info("Info", "This feature is disabled!", null);
                            }
                            else{
                                addAppTabItem(bean);
                            }
                        }
                    }
                });
        subcp.addListener(Events.Expand, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                loadConfig.setOffset(0);
                fLoader.load(loadConfig);
            }

        });
        subcp.addListener(Events.Collapse, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                fview.getSelectionModel().deselectAll();
            }

        });
        upKnob.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                loadConfig.setOffset(loadConfig.getOffset() - loadConfig.getLimit());
                fLoader.load(loadConfig);
            }
        });
        subcp.add(upKnob);
        subcp.add(fview);
        subcp.add(downKnob);
        downKnob.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                loadConfig.setOffset(loadConfig.getOffset() + loadConfig.getLimit());
                fLoader.load(loadConfig);  
            }          
        });
        cp.add(subcp);

        subcp = new ContentPanel();
        subcp.setAnimCollapse(false);
        subcp.setHeading("More...");
        String[] users = new String[] { "Users", "Features", "Jobs",
                "History" };
        List<ImageModel> uModels = ImageMap.getImageModels(users);
        MemoryProxy<List<ImageModel>> uProxy = new MemoryProxy<List<ImageModel>>(
                uModels);
        ListLoader<ListLoadResult<ImageModel>> uLoader = new BaseListLoader<ListLoadResult<ImageModel>>(
                uProxy, new BeanModelReader());
        ListStore<BeanModel> uStore = new ListStore<BeanModel>(uLoader);
        uLoader.load();
        uView = new ListView<BeanModel>();
        uView.setBorders(false);
        uView.setHeight("100%");
        uView.setId("img-chooser-viewer");
        uView.setTemplate(getTemplate());
        uView.setStore(uStore);
        uView.setItemSelector("div.thumb-wrap");
        uView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uView.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<BeanModel>>() {

                    @Override
                    public void handleEvent(SelectionChangedEvent<BeanModel> be) {
                        BeanModel model = be.getSelectedItem();
                        ImageModel t = model.getBean();
                        String name = t.getName();
                        addTabItem(name);
                    }

                });
        subcp.add(uView);
        subcp.addListener(Events.Collapse, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                uView.getSelectionModel().deselectAll();
            }

        });
        cp.add(subcp);

        return cp;
    }

    public native String getTemplate()/*-{
                                      return ['<tpl for=".">',
                                      '<div class="thumb-wrap" id="{name}" style="margin: 5px 50px 5px 50px; width: 100px; height: 70px">',
                                      '<div class="thumb" style="margin: 0px 26px 0px 26px"><img src="{path}" title="{name}"/></div>',
                                      '<span style="display: block; text-align: center; color: steelblue; font-weight: 500; font-family: Verdana">{name}</span></div>',
                                      '</tpl>'].join("");
                                      }-*/;

    private native String getTemplateForFeature() /*-{
                                                  return ['<tpl for=".">', 
                                                  '<div class="thumb-wrap" id="{name}" style="margin: 10px 50px 10px 50px; width: 100px; height: 80px">', 
                                                  '<div class="thumb" style="margin: 0px 26px 0px 26px"><img src="{eniconpath}" title="{name} (version: {version})"></div>', 
                                                  '<span style="display: block; text-align: center; color: steelblue; font-weight: 500; font-family: Verdana">{shortName}</span></div>', 
                                                  '</tpl>'].join("");
                                                  }-*/;

    public TabItem addTabItem(String op) {
        TabItem item = center.getItemByItemId(op);
        if (item == null || op.equalsIgnoreCase("launch")) {
            String tabName = treeItemtoTabItem.get(op);
            item = new TabItem(tabName);
            item.setClosable(true);
            item.setItemId(op);
            item.setIcon(IconHelper.createStyle(treeItemtoStyle.get(op)
                    .toLowerCase(), 16, 16));
            item.setBorders(true);
            item.addListener(Events.Select, new ViewerListener());
            center.add(item);
        }
        center.setSelection(item);

        return item;
    }

    public void updateFeaturePanel() {
        fLoader.load();
    }

    public TabItem addAppTabItem(FeatureInfoBean bean) {
        TabItem item = new TabItem(bean.getShortName());
        item.setClosable(true);
        item.setBorders(true);
        item.setIcon(AbstractImagePrototype.create(Resources.ICONS.app()));
        FeatureViewListener listener = new FeatureViewListener();
        listener.setBean(bean);
        item.addListener(Events.Select, listener);
        center.add(item);
        center.setSelection(item);
        return item;
    }

    public void setUserProfile(UserInfoBean userProfile) {
        this.userBean = userProfile;
    }

    public UserInfoBean getUserProfile() {
        return userBean;
    }
}
