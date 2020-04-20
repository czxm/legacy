package com.intel.cedar.service.client.view.registerwizard;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.intel.cedar.service.client.view.wizard.impl.WizardPage;

public class TypeConfigWizardPage extends WizardPage {
    private FormPanel formPanel;
    private SimpleComboBox<String> chooseType;

    public TypeConfigWizardPage() {
        setName("Type Configuration");
        setDescription("configure the host type, e.g. Gateway, PhysicalNode");

        init();
    }

    protected void init() {
        setLayout(new FormLayout());
        setLayoutOnChange(true);

        formPanel = new FormPanel();
        formPanel.setLabelWidth(80);
        formPanel.setHeaderVisible(false);

        chooseType = new SimpleComboBox<String>();
        chooseType.setFieldLabel("Choose Type");
        chooseType.setTriggerAction(TriggerAction.ALL);
        chooseType.add("Gateway");
        chooseType.add("Physical Node");
        chooseType.setSimpleValue("Gateway");
        FormData chooseTypeFd = new FormData();
        chooseTypeFd.setWidth(150);
        formPanel.add(chooseType, chooseTypeFd);

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
