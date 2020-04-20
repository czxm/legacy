package com.intel.cedar.service.client.view;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.resources.Resources;
import com.intel.cedar.service.client.view.ViewCache.ViewType;

public class FeatureUploadView extends ComponentViewer {

    private static FeatureUploadView instance;

    private ToolBar toolBar;
    private FormPanel formPanel;

    private FeatureUploadView() {

    }

    public static FeatureUploadView getInstance() {
        if (instance == null) {
            instance = new FeatureUploadView();
        }
        return instance;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setBorders(false);
        cp.setLayout(new FlowLayout());
        cp.add(createForm());
        toolBar = new ToolBar();
        toolBar.setAlignment(HorizontalAlignment.RIGHT);
        toolBar.add(new Button("Return", AbstractImagePrototype
                .create(Resources.ICONS.back()),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        TabPanel tabPanel = MainPage.getInstance()
                                .getTabPanel();
                        TabItem featureTab = tabPanel
                                .getItemByItemId("Features");
                        featureTab.removeAll();
                        ComponentViewer viewer = ViewCache
                                .createViewer(ViewType.TYPE_FEATUREMGR_VIEW);
                        viewer.updateView();
                        featureTab.add(viewer);
                        featureTab.layout();
                    }

                }));
        cp.setTopComponent(toolBar);

        add(cp);
    }

    public LayoutContainer createForm() {
        formPanel = new FormPanel();
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setButtonAlign(HorizontalAlignment.LEFT);
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(100);
        formPanel.setLayout(formLayout);
        formPanel.setMethod(Method.POST);
        formPanel.setEncoding(Encoding.MULTIPART);
        formPanel.setAction("/cloudtestservice/Upload");

        FileUploadField fuf = new FileUploadField();
        fuf.setName("feature");
        fuf.setFieldLabel("Feature Package");
        FormData fufData = new FormData();
        fufData.setWidth(300);
        formPanel.add(fuf, fufData);

        Button uploadBtn = new Button("Upload",
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        formPanel.submit();
                    }

                });
        formPanel.addListener(Events.Submit, new Listener<FormEvent>() {

            @Override
            public void handleEvent(FormEvent be) {
                String result = be.getResultHtml();
                if (result == null || result.contains("Failed")) {
                    // showError
                }
                if (result.contains("OK")) {
                    new RPCInvocation<Boolean>(false, true, false, false) {

                        @Override
                        public void execute(
                                CloudRemoteServiceAsync remoteService,
                                AsyncCallback<Boolean> callback) {
                            remoteService.deployFeature(false, callback);
                        }

                        public void onComplete(Boolean obj) {
                            if (obj) {
                                TabPanel tabPanel = MainPage.getInstance()
                                        .getTabPanel();
                                TabItem featureTab = tabPanel
                                        .getItemByItemId("Features");
                                featureTab.removeAll();
                                ComponentViewer viewer = ViewCache
                                        .createViewer(ViewType.TYPE_FEATUREMGR_VIEW);
                                viewer.updateView();
                                featureTab.add(viewer);
                                featureTab.layout();

                                // also update feature panel
                                MainPage.getInstance().updateFeaturePanel();
                            }
                        }

                        public String getProgressTitle() {
                            return "Deploy Feature";
                        }

                        public String getProgressText() {
                            return "deploying...";
                        }

                    }.invoke(false);
                }
            }

        });
        uploadBtn.setWidth(50);
        formPanel.addButton(uploadBtn);

        return formPanel;
    }

    @Override
    public void updateView() {
        // TODO Auto-generated method stub

    }

}
