package com.intel.cedar.service.client.feature.view;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.Element;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
import com.intel.cedar.service.client.view.ComponentViewer;

public class FeatureView extends ComponentViewer {

    private FeatureModel featureModel;

    private TabItem parentItem;

    private FeatureInfoBean featureInfoBean; // feature id should set in
                                             // featureModel

    public static FeatureView getInstance() {
        return new FeatureView();
    }

    private FeatureView() {

    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);  
        add(createForm());
    }

    public LayoutContainer createForm() {
        BaseUIBuilder builder = new BaseUIBuilder();
        builder.setFeatureModel(featureModel);
        builder.setParentItem(parentItem);
        builder.visitForm(featureModel.getForm());
        return builder.getFormPanel();
    }

    @Override
    public void updateView() {

    }

    public void setFeatureModel(FeatureModel fModel) {
        this.featureModel = fModel;
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public void setFeatureInfoBean(FeatureInfoBean featureInfoBean) {
        this.featureInfoBean = featureInfoBean;
    }

    public FeatureInfoBean getFeatureInfoBean() {
        return featureInfoBean;
    }

    public void setParentItem(TabItem parentItem) {
        this.parentItem = parentItem;
    }

    public TabItem getParentItem() {
        return parentItem;
    }

}
