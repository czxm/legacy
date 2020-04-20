package com.intel.cedar.service.client.view.registerwizard;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.intel.cedar.service.client.view.wizard.impl.WizardPage;

public class GeneralSettingWizardPage extends WizardPage {
    private FormPanel formPanel;

    public GeneralSettingWizardPage() {
        setName("General settings");
        setDescription("set some general properties");

        init();
    }

    public void init() {
        setLayout(new FormLayout());
        setLayoutOnChange(true);

        formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setLabelWidth(80);

        TextField<String> hostAddr = new TextField<String>();
        hostAddr.setFieldLabel("Host Address");
        hostAddr.setEmptyText("input the host address");
        FormData hostAddrFd = new FormData();
        hostAddrFd.setMargins(new Margins(20, 0, 0, 0));
        hostAddrFd.setWidth(200);
        formPanel.add(hostAddr, hostAddrFd);

        add(formPanel);
    }

    @Override
    public boolean isPageComplete() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

}
