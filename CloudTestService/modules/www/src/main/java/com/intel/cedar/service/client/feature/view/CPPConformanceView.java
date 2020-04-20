package com.intel.cedar.service.client.feature.view;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import com.intel.cedar.service.client.view.ComponentViewer;

public class CPPConformanceView extends ComponentViewer {

    @Override
    public void updateView() {
        // TODO Auto-generated method stub

    }

    public static CPPConformanceView getInstance() {

        return new CPPConformanceView();
    }

    private CPPConformanceView() {

    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);

        ContentPanel cp = new ContentPanel();
        cp.setHeaderVisible(false);
        cp.setBodyBorder(false);
        cp.setBorders(false);
        cp.setLayout(new FlowLayout());
        cp.add(createForm2());

        add(cp);
    }

    private FormPanel formPanel;
    private FormData formData;
    private FormLayout formLayout;

    public LayoutContainer createForm() {
        formPanel = new FormPanel();
        formData = new FormData("0");
        formLayout = new FormLayout();
        formLayout.setLabelWidth(150);
        formPanel.setLayout(formLayout);
        formPanel.setHeaderVisible(false);
        formPanel.setPadding(20);

        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading("Target");
        FormLayout innerLayout = new FormLayout();
        innerLayout.setLabelWidth(140);
        fieldSet.setLayout(innerLayout);
        SimpleComboBox<String> osCombo = new SimpleComboBox<String>();
        osCombo.setFieldLabel("OS");
        osCombo.add("Windows");
        osCombo.add("RHEL");
        osCombo.setSimpleValue("Windows");
        fieldSet.add(osCombo, formData);
        SimpleComboBox<String> archCombo = new SimpleComboBox<String>();
        archCombo.setFieldLabel("ARCH");
        archCombo.add("x86");
        archCombo.add("x86_64");
        archCombo.setSimpleValue("x86");
        fieldSet.add(archCombo, formData);
        formPanel.add(fieldSet);

        CheckBoxGroup checkBoxGroup = new CheckBoxGroup();
        checkBoxGroup.setFieldLabel("Select Component");
        CheckBox checkBox1 = new CheckBox();
        checkBox1.setBoxLabel("Parser");
        CheckBox checkBox2 = new CheckBox();
        checkBox2.setBoxLabel("Validator");
        CheckBox checkBox3 = new CheckBox();
        checkBox3.setBoxLabel("Transform");
        checkBoxGroup.add(checkBox1);
        checkBoxGroup.add(checkBox2);
        checkBoxGroup.add(checkBox3);
        formPanel.add(checkBoxGroup);

        return formPanel;
    }

    public LayoutContainer createForm2() {
        BaseUIBuilder builder = new BaseUIBuilder();
        return builder.getFormPanel();
    }
}
