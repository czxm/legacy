package com.intel.cedar.service.client.feature.view;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
import com.intel.cedar.service.client.view.ViewCache;
import com.intel.cedar.service.client.view.ViewCache.ViewType;

public class FeatureViewListener extends SelectionListener<TabPanelEvent> {
    private FeatureInfoBean bean;

    public FeatureViewListener() {

    }

    @Override
    public void componentSelected(TabPanelEvent ce) {
        TabItem item = ce.getItem();
        new FeatureLoader(item).invoke(false);
    }

    public void setBean(FeatureInfoBean bean) {
        this.bean = bean;
    }

    public void renderPanel(TabItem item, FeatureModel uiObj) {
        item.removeAll();
        item.setAutoWidth(true);
        item.setAutoHeight(true);
        item.setScrollMode(Scroll.AUTO);
        item.setLayout(new FitLayout());
        FeatureView view = (FeatureView) ViewCache
                .createViewer(ViewType.TYPE_FEATURE_VIEW);
        view.setFeatureModel(uiObj);
        view.setParentItem(item);
        item.add(view);
        item.layout();
    }

    public static HashMap<String, String> appToUri = new HashMap<String, String>();
    static {
        appToUri.put("CPP.Conf", "../examples/features/cpp.conformance.jar");
        appToUri
                .put("JAVA.Nightly", "../examples/features/cpp.conformance.jar");
        appToUri.put("JAVA.PV", "../examples/features/cpp.conformance.jar");
        appToUri.put("SplitPoint", "../examples/features/cpp.conformance.jar");

        // appToUri.put("CPP.Conf",
        // "../examples/features/cpp.conformance/feature.xml");
        // appToUri.put("JAVA.Nightly",
        // "../examples/features/cpp.conformance/feature.xml");
        // appToUri.put("JAVA.PV",
        // "../examples/features/cpp.conformance/feature.xml");
        // appToUri.put("SplitPoint",
        // "../examples/features/cpp.conformance/feature.xml");

        // appToUri.put("CPP.Conf",
        // "1");
        // appToUri.put("JAVA.Nightly",
        // "1");
        // appToUri.put("JAVA.PV",
        // "1");
        // appToUri.put("SplitPoint",
        // "1");
    }

    public class FeatureLoader extends RPCInvocation<FeatureModel> {

        TabItem item;

        public FeatureLoader() {

        }

        public FeatureLoader(TabItem item) {
            this.item = item;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<FeatureModel> callback) {
            remoteService.loadFeatureUI(bean.getId(), callback);
        }

        public void onComplete(FeatureModel obj) {
            renderPanel(item, obj);
        }
    }

}
