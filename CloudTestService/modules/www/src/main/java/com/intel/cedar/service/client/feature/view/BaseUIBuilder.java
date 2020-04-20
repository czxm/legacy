package com.intel.cedar.service.client.feature.view;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.IconButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.LayoutData;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.CloudRemoteServiceAsync;
import com.intel.cedar.service.client.MainPage;
import com.intel.cedar.service.client.RPCInvocation;
import com.intel.cedar.service.client.feature.model.ActionWidget;
import com.intel.cedar.service.client.feature.model.CedarSCMLogEntry;
import com.intel.cedar.service.client.feature.model.CedarScmLoadConfig;
import com.intel.cedar.service.client.feature.model.CedarScmType;
import com.intel.cedar.service.client.feature.model.EventWidget;
import com.intel.cedar.service.client.feature.model.FeatureInfoBean;
import com.intel.cedar.service.client.feature.model.FeatureJobRequestBean;
import com.intel.cedar.service.client.feature.model.Params;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.ui.BranchModel;
import com.intel.cedar.service.client.feature.model.ui.CheckboxgroupModel;
import com.intel.cedar.service.client.feature.model.ui.ComboModel;
import com.intel.cedar.service.client.feature.model.ui.CompositeModel;
import com.intel.cedar.service.client.feature.model.ui.DependsAction;
import com.intel.cedar.service.client.feature.model.ui.DependsModel;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
import com.intel.cedar.service.client.feature.model.ui.FieldSetModel;
import com.intel.cedar.service.client.feature.model.ui.FileUploadModel;
import com.intel.cedar.service.client.feature.model.ui.FormItemModel;
import com.intel.cedar.service.client.feature.model.ui.FormModel;
import com.intel.cedar.service.client.feature.model.ui.GitModel;
import com.intel.cedar.service.client.feature.model.ui.IBindModelProvider;
import com.intel.cedar.service.client.feature.model.ui.ListFieldModel;
import com.intel.cedar.service.client.feature.model.ui.LogModel;
import com.intel.cedar.service.client.feature.model.ui.RevModel;
import com.intel.cedar.service.client.feature.model.ui.SVNModel;
import com.intel.cedar.service.client.feature.model.ui.SubmitModel;
import com.intel.cedar.service.client.feature.model.ui.TextAreaModel;
import com.intel.cedar.service.client.feature.model.ui.TextfieldModel;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNode;
import com.intel.cedar.service.client.feature.model.ui.URLModel;
import com.intel.cedar.service.client.filebrowser.ui.OpenDialog;
import com.intel.cedar.service.client.util.Util;
import com.intel.cedar.service.client.widget.CedarIconButton;

public class BaseUIBuilder extends LayoutContainer implements UIBuilder {
    private static final long serialVersionUID = 1L;

    private LinkedList<TypedWidget> widgetStack = new LinkedList<TypedWidget>();

    private FormPanel formPanel;

    private TextArea opArea;

    private FeatureModel featureModel;

    private FeatureInfoBean featureInfoBean;

    private static boolean DBG_OUTPUT = false;

    public BaseUIBuilder() {

    }

    public void postVisit() {
        TypedWidget tWidget = widgetStack.getLast();
        int level = 0;
        String value = "";
        value = traverse(tWidget, level, value);
        if (DBG_OUTPUT)
            opArea.setValue(value);

        computeDepends(tWidget, tWidget);
        attachListener(tWidget);
    }

    public String traverse(TypedWidget tWidget, int level, String value) {
        if (tWidget == null)
            return value;
        String space = "";
        for (int i = 0; i < level; i++)
            space += "\t";
        value += (space + tWidget.getType() + "\n");
        for (TypedWidget t : tWidget.getChildren()) {
            value = traverse(t, level + 1, value);
        }
        return value;
    }

    public void computeDepends(TypedWidget root, TypedWidget tWidget) {
        if (tWidget == null)
            return;
        if (tWidget.getUiModel() != null
                && tWidget.getUiModel().getDepends().size() > 0) {
            findTypedWidget(root, tWidget);
        }
        for (TypedWidget t : tWidget.getChildren()) {
            // skip checkbox, svn revision, svn log
            UIBaseNode model = t.getUiModel();
            if (model != null
                    && (model instanceof RevModel || model instanceof LogModel))
                continue;

            computeDepends(root, t);
        }
    }

    public void findTypedWidget(TypedWidget root, TypedWidget tWidget) {
        if (tWidget.getUiModel() == null)
            return; // currently, we have no CheckBoxModel indeed
        DependsModel depends = tWidget.getUiModel().getDependsModel();
        // DependsModel dupDepends = depends.clone(); // should clone the
        // depends model
        // build dependency link
        buildRefAndDepLink(root, tWidget, depends);
    }

    public void buildRefAndDepLink(TypedWidget root, TypedWidget tWidget,
            DependsModel depends) {
        if (depends.getDepends().size() <= 0)
            return; // completed!
        if (root == null)
            return; // leaf node!
        if (depends.getDependNodes().contains(root.getUiModel())) {
            tWidget.addDep(depends.getDependsAction(root.getUiModel()), root);
            depends.removeDepend(depends.getDependModel(root.getUiModel()));
            if (!(tWidget.getUiModel() instanceof SubmitModel)
                    && !(tWidget.getUiModel() instanceof FormItemModel)) {
                root.addRef(Events.Select, tWidget);
            }
        }

        for (TypedWidget t : root.getChildren()) {
            buildRefAndDepLink(t, tWidget, depends);
        }
    }

    public void attachListener(final TypedWidget typedWidget) {
        if (typedWidget == null)
            return;
        for (final EventWidget ew : typedWidget.getRefs()) {
            if (typedWidget.getType() == UIType.COMBOBOX) {
                // addListener according to the data specify in the action list
                typedWidget.getWidget().addListener(Events.Select,
                        new Listener<FieldEvent>() {

                            @Override
                            public void handleEvent(FieldEvent be) {
                                fillTypedWidget(ew);
                            }

                        });
                fillTypedWidget(ew);
            }

            if (typedWidget.getType() == UIType.CHECKBOXGROUP) {
                for (TypedWidget t : typedWidget.getChildren()) {
                    CheckBox checkBox = (CheckBox) t.getWidget();
                    checkBox.addListener(Events.OnClick,
                            new Listener<FieldEvent>() {

                                @Override
                                public void handleEvent(FieldEvent be) {
                                    fillTypedWidget(ew);
                                }

                            });
                }

            }
        }

        UIBaseNode model = typedWidget.getUiModel();
        if (model instanceof SubmitModel) {
            for (TypedWidget item : typedWidget.getChildren()) {
                typedWidget.getDeps().addAll(item.getDeps());
            }
            IconButton smtBtn = (IconButton) typedWidget.getWidget();

            smtBtn
                    .addSelectionListener(new SelectionListener<IconButtonEvent>() {

                        @Override
                        public void componentSelected(IconButtonEvent ce) {
                            final Dialog dialog = new Dialog();
                            dialog.setHeading("Confirm");
                            dialog.setSize(400, 200);
                            dialog.setResizable(false);
                            dialog.setHideOnButtonClick(true);
                            dialog.setButtons(Dialog.OKCANCEL);

                            FormPanel formPanel = new FormPanel();
                            FormLayout formLayout = new FormLayout();
                            formLayout.setLabelWidth(80);
                            formPanel.setHeaderVisible(false);
                            formPanel.setBorders(false);
                            formPanel.setBodyBorder(false);
                            formPanel.setLayout(formLayout);

                            Label note = new Label();
                            note.setStyleName("warning");
                            note.setText("Really want to submit the task?");
                            formPanel.add(note);
                            FormData hfd = new FormData();
                            hfd.setMargins(new Margins(0, 0, 8, 0));
                            Html html = new Html(
                                    "<hr style='height:1px;color:#dcdcdc'/>");
                            formPanel.add(html, hfd);

                            final SimpleComboBox<String> saveResult = new SimpleComboBox<String>();
                            saveResult.add("Yes");
                            saveResult.add("No");
                            saveResult.setSimpleValue("No");
                            saveResult.setTriggerAction(TriggerAction.ALL);
                            saveResult.setFieldLabel("Reproducable");
                            FormData sfd = new FormData();
                            sfd.setMargins(new Margins(0, 0, 8, 0));
                            sfd.setWidth(100);
                            formPanel.add(saveResult, sfd);

                            final TextField<String> des = new TextField<String>();
                            des.setFieldLabel("Description");
                            FormData dfd = new FormData("0");
                            dfd.setMargins(new Margins(0, 0, 8, 0));
                            formPanel.add(des, dfd);

                            Button okBtn = dialog.getButtonById(Dialog.OK);
                            okBtn
                                    .addSelectionListener(new SelectionListener<ButtonEvent>() {

                                        @Override
                                        public void componentSelected(
                                                ButtonEvent ce) {
                                            final List<Variable> varList = new ArrayList<Variable>();
                                            for (ActionWidget aw : typedWidget
                                                    .getDeps()) {
                                                TypedWidget tw = aw
                                                        .getTWidget();
                                                Variable var = new Variable();
                                                if (tw.getType() == UIType.COMBOBOX){
                                                    if(tw.getUiModel() instanceof RevModel) {
                                                    // complex combobox,
                                                    // revision combo box
                                                    String name = ((RevModel) tw
                                                            .getUiModel())
                                                            .getBind()
                                                            .getName();
                                                    CedarSCMLogEntry logEntry = ((ComboBox<CedarSCMLogEntry>) tw
                                                            .getWidget())
                                                            .getValue();
                                                    String value = (logEntry == null ? ""
                                                            : logEntry.getRev());
                                                    var.setName(name);
                                                    var.addValue(value);
                                                    varList.add(var);
                                                   }else if (tw.getUiModel() instanceof BranchModel) {
                                                    // complex combobox,
                                                    // revision combo box
                                                    String name = ((BranchModel) tw
                                                            .getUiModel())
                                                            .getBind()
                                                            .getName();
                                                    SimpleComboBox<String> branch = (SimpleComboBox<String>) tw
                                                            .getWidget();
                                                    String value = "";
                                                    if(branch.getSelection().size() > 0){
                                                        value = branch.getSelection().get(0).getValue();
                                                    }
                                                    var.setName(name);
                                                    var.addValue(value);
                                                    varList.add(var);
                                                  }else{
                                                    // simple combobox
                                                    String name = ((ComboModel) tw
                                                            .getUiModel())
                                                            .getBind()
                                                            .getName();
                                                    String value = ((SimpleComboBox<String>) tw
                                                            .getWidget())
                                                            .getSimpleValue();
                                                    var.setName(name);
                                                    var.addValue(value);
                                                    varList.add(var);
                                                  }
                                                } else if (tw.getType() == UIType.CHECKBOXGROUP) {
                                                    String name = ((CheckboxgroupModel) tw
                                                            .getUiModel())
                                                            .getBind()
                                                            .getName();
                                                    var.setName(name);
                                                    for (TypedWidget ctw : tw.getChildren()) {
                                                        CheckBox cb = (CheckBox)ctw.getWidget();
                                                        if(cb.getValue()){
                                                            var.addValue(cb.getBoxLabel());
                                                        }
                                                    }
                                                    varList.add(var);

                                                } else if (tw.getType() == UIType.LISTFIELD) {
                                                    String name = ((ListFieldModel) tw
                                                            .getUiModel())
                                                            .getBind()
                                                            .getName();
                                                    var.setName(name);
                                                    List<VarValue> selectionList = ((ListField) tw
                                                            .getWidget())
                                                            .getSelection();
                                                    for (VarValue tc : selectionList) {
                                                        // serialize to single
                                                        // value line
                                                        // FIXME
                                                        // var.addValue(tc.getValue());
                                                        var.addVarValue(tc);
                                                    }
                                                    varList.add(var);

                                                } else if (tw.getType() == UIType.TEXTFIELD){                                                   
                                                    if(tw.getUiModel() instanceof URLModel) {
                                                        // customized textfield, svn
                                                        // url textfield
                                                        String name = ((URLModel) tw
                                                                .getUiModel())
                                                                .getBind()
                                                                .getName();
                                                        String value = ((TextField<String>) tw
                                                                .getWidget())
                                                                .getValue();
                                                        var.setName(name);
                                                        var.addValue(value);
                                                        varList.add(var);
                                                    }
                                                    else if (tw.getUiModel() instanceof TextfieldModel) {
                                                        String name = ((TextfieldModel) tw
                                                                .getUiModel())
                                                                .getBind()
                                                                .getName();
                                                        String value = ((TextField<String>) tw
                                                                .getWidget())
                                                                .getValue();
                                                        var.setName(name);
                                                        var.addValue(value);
                                                        varList.add(var);
                                                    } 
                                                    else if (tw.getUiModel() instanceof FileUploadModel) {
                                                        String name = ((FileUploadModel) tw
                                                                .getUiModel())
                                                                .getBind()
                                                                .getName();
                                                        String value = ((FileUploadField) tw
                                                            .getWidget())
                                                            .getValue();
                                                        if(value != null && value.length() > 0){
                                                            var.setName(name);
                                                            var.addValue(value);
                                                            varList.add(var);
                                                        }
                                                    }
                                                }
                                            }

                                            final FeatureJobRequestBean request = new FeatureJobRequestBean();
                                            request.setUser(MainPage
                                                    .getInstance()
                                                    .getUserProfile()
                                                    .getUserName());
                                            request.setUserId(MainPage
                                                    .getInstance()
                                                    .getUserProfile().getId());
                                            request.setReproducable(saveResult
                                                    .getSimpleValue().equals(
                                                            "No") ? false
                                                    : true);
                                            request.setDescription(des
                                                    .getValue());
                                            request.setVariables(varList);
                                            request.setFeatureId(featureModel
                                                    .getFeatureID());
                                            dialog.hide();

                                            new RPCInvocation<String>(false,
                                                    true, false, false) {

                                                @Override
                                                public void execute(
                                                        CloudRemoteServiceAsync remoteService,
                                                        AsyncCallback<String> callback) {
                                                    remoteService.submitTask(
                                                            request, callback);
                                                }

                                                public String getProgressTitle() {
                                                    return "Launch Test";
                                                }

                                                public String getProgressText() {
                                                    return "launching test...";
                                                }

                                                public void onComplete(
                                                        String obj) {
                                                    BaseUIBuilder.this
                                                            .getParentItem()
                                                            .close();
                                                    MainPage.getInstance()
                                                            .addTabItem(
                                                                    "Jobs");
                                                }

                                            }.invoke(false);
                                        }
                                    });
                            Button cancelBtn = dialog
                                    .getButtonById(Dialog.CANCEL);
                            cancelBtn
                                    .addSelectionListener(new SelectionListener<ButtonEvent>() {

                                        @Override
                                        public void componentSelected(
                                                ButtonEvent ce) {
                                            dialog.hide();
                                        }
                                    });
                            dialog.add(formPanel);
                            dialog.show();
                        }

                    });
        }

        for (TypedWidget t : typedWidget.getChildren()) {
            attachListener(t);
        }
    }

    public void fillTypedWidget(EventWidget ew) {
        UIType type = ew.getTWidget().getType();
        if (type == UIType.LISTFIELD) {
            ArrayList<ActionWidget> widgetDeps = ew.getTWidget().getDeps();
            Params param = getValueOfIDs(widgetDeps);
            ArrayList<String> selectionList = getSelectedValue(widgetDeps);
            loadAndSelectTestCase(param, selectionList, ew.getTWidget());
        }
    }

    @SuppressWarnings("unchecked")
    public Params getValueOfIDs(List<ActionWidget> list) {
        Params km = new Params();
        for (ActionWidget aw : list) {
            if (aw.getAction().equals(DependsAction.GET_VALUE)) {
                if (aw.getTWidget().getType() == UIType.COMBOBOX) {
                    km.addParam(aw.getTWidget().getUiModel().getID(),
                            ((SimpleComboBox<String>) (aw.getTWidget()
                                    .getWidget())).getSimpleValue());
                }
            }
        }

        return km;
    }

    public ArrayList<String> getSelectedValue(List<ActionWidget> list) {
        ArrayList<String> res = new ArrayList<String>();
        for (ActionWidget t : list) {
            if (t.getAction() == DependsAction.GET_SELECTIONS) {
                if (t.getTWidget().getType() == UIType.CHECKBOXGROUP) {
                    for (TypedWidget b : t.getTWidget().getChildren()) {
                        CheckBox cb = (CheckBox)b.getWidget();
                        if(cb.getValue())
                            res.add(cb.getBoxLabel());
                    }
                }
            }
        }

        return res;
    }

    @SuppressWarnings("unchecked")
    public void loadAndSelectTestCase(Params params, List<String> components,
            TypedWidget tWidget) {
        // FIXME
        String v = null;
        if(tWidget.getUiModel() instanceof IBindModelProvider){
            v = ((IBindModelProvider)tWidget.getUiModel()).getBind().getName();
        }
        if(v == null)
            return;
        Variable var = featureModel.getVarBuff().getVariable(v);
        List<VarValue> res = var.getVarValues(params);

        tWidget.getStore().removeAll();
        tWidget.getStore().add(res);
        if (tWidget.getType() == UIType.LISTFIELD) {
            List<VarValue> sel = new ArrayList<VarValue>();
            ListField lf = (ListField) tWidget.getWidget();
            for (String comp : components) {
                for (VarValue tcase : res) {
                    if (tcase.match(comp)) {
                        sel.add(tcase);
                    }
                }
            }
            lf.setSelection(sel);
        }
    }

    public String assembleValue(List<String> list) {
        String res = "";
        for (String str : list) {
            res += str;
        }
        return res;
    }

    public void getValue(UIType type, Component widget) {
        return;
    }

    public void onRender(Element target, int index) {
        super.onRender(target, index);
    }

    public void visitFeatureUI() {

    }

    @Override
    public void visitCheckBox(CheckboxgroupModel model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitCheckBoxGroup(CheckboxgroupModel model) {
        CheckBoxGroup checkBoxGroup = new CheckBoxGroup();
        checkBoxGroup.setOrientation(Orientation.HORIZONTAL);
        checkBoxGroup.setSpacing(0);
        List<String> children = model.getValues();
        if(children.size() == 0)
        	return;
        int numCols = 5;
        CheckBoxGroup[] cols = createCheckBoxRows(numCols);
        checkBoxGroup.setFieldLabel(model.getLabel());
        for (CheckBoxGroup column : cols) {
            checkBoxGroup.add(column);
            column.setWidth(85);
        }
        checkBoxGroup.setWidth(450);
        TypedWidget tWidget = new TypedWidget(checkBoxGroup, model, null,
                UIType.CHECKBOXGROUP);
        int i = 0;
        for (String label : model.getValues()) {
            CheckBox checkBox = new CheckBox();
            checkBox.setWidth(80);
            checkBox.setBoxLabel(label);
            TypedWidget cWidget = new TypedWidget(checkBox, null, null,
                    UIType.CHECKBOX);
            cols[i++ % numCols].add(checkBox);
            tWidget.addChild(cWidget);
        }
        addToTypedWidget(tWidget, null);
    }

    private CheckBoxGroup[] createCheckBoxRows(int colNumber) {
        CheckBoxGroup[] cols = new CheckBoxGroup[colNumber];
        for (int i = 0; i < cols.length; i++) {
        	cols[i] = new CheckBoxGroup();
        	cols[i].setOrientation(Orientation.VERTICAL);
        	cols[i].setSpacing(0);
        }
        return cols;
    }
    
    @Override
    public void visitComboBox(ComboModel model) {
        SimpleComboBox<String> comboBox = new SimpleComboBox<String>();
        comboBox.setFieldLabel(model.getLabel());
        comboBox.setFireChangeEventOnSetValue(true);
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setEditable(false);
        comboBox.add(model.getValues());
        comboBox.setSimpleValue(model.getValues().get(0));
        TypedWidget tWidget = new TypedWidget(comboBox, model, null,
                UIType.COMBOBOX);
        addToTypedWidget(tWidget, null);
    }

    @Override
    public void visitForm(FormModel model) {
        formPanel = new FormPanel();
        FormData formData = new FormData();
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(120);
        formPanel.setWidth(800);
        formPanel.setHeaderVisible(false);
        formPanel.setBorders(false);
        formPanel.setBodyBorder(false);
        formPanel.setPadding(20);
        formPanel.setLayout(formLayout);
        formPanel.setLayoutData(formData);
        formPanel.setMethod(Method.POST);
        formPanel.setEncoding(Encoding.MULTIPART);
        formPanel.setAction("/cloudtestservice/Upload");
        
        formPanel.addListener(Events.Submit, new Listener<FormEvent>() {
            @Override
            public void handleEvent(FormEvent be) {
                String result = be.getResultHtml();
                if (result == null || result.contains("Failed")) {
                    // showError
                }
                if (result.contains("OK")) {
                    new RPCInvocation<String>(true, true, false, false) {

                        @Override
                        public void execute(
                                CloudRemoteServiceAsync remoteService,
                                AsyncCallback<String> callback) {
                            remoteService.getUploadedFile(callback);
                        }

                        public void onComplete(String fileurl) {
                            String id = formPanel.getData("uploadid");
                            Component c = formPanel.getItemByItemId(id);
                            if(c instanceof FileUploadField){
                                ((FileUploadField)c).setValue(fileurl);
                                ((FileUploadField)c).getFileInput().setValue(null);
                            }
                        }
                    }.invoke(false);
                }
            }
        });
        widgetStack
                .add(new TypedWidget(formPanel, model, formData, UIType.FORM));
        List<UIBaseNode> list = model.getChildren();
        for (UIBaseNode uiobj : list) {
            uiobj.accept(this);
        }

        visitCusField();

        postVisit();
    }

    @Override
    public void visitList(ListFieldModel model) {
        ListField<VarValue> listField = new ListField<VarValue>();
        ListStore<VarValue> store = new ListStore<VarValue>();
        listField.setFieldLabel(model.getLabel());
        listField.setHeight(200);
        listField.setStore(store);
        listField.setDisplayField("VariableValue");
        // should specify a componentplugin;
        // listField.setData("text", "(Hold Ctrl to do multi selection)");
        FormData lf = new FormData();
        lf.setWidth(600);
        TypedWidget tWidget = new TypedWidget(listField, model, null,
                UIType.LISTFIELD);
        tWidget.setStore(store);

        addToTypedWidget(tWidget, lf);
    }

    @Override
    public void visitTextArea(TextAreaModel textArea) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitTextField(TextfieldModel model) {
        TextField<String> textField = new TextField<String>();
        List<String> values = model.getValues();
        if (values != null && values.size() > 0) {
            textField.setValue(model.getValues().get(0));
        }
        textField.setFieldLabel(model.getLabel());
        textField.setWidth(600);
        TypedWidget tWidget = new TypedWidget(textField, model, null,
                UIType.TEXTFIELD);
        addToTypedWidget(tWidget, null);
    }

    @Override
    public void visitComposite(CompositeModel composite) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitFieldSet(FieldSetModel model) {
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(model.getLabel());
        FormLayout inner = new FormLayout();
        inner.setLabelWidth(110);
        FormData formData = new FormData();
        formData.setWidth(100);
        fieldSet.setLayout(inner);
        TypedWidget tWidget = new TypedWidget(fieldSet, model, formData,
                UIType.FIELDSET);
        widgetStack.add(tWidget);
        List<UIBaseNode> list = model.getChildren();
        for (UIBaseNode uiObj : list) {
            uiObj.accept(this);
        }
        widgetStack.removeLast();
        FormData fs = new FormData();
        fs.setAnchorSpec("-500");

        addToTypedWidget(tWidget, fs);
    }

    @Override
    public void visitFileUploadField(FileUploadModel model) {
        final FileUploadField fuf = new FileUploadField(){
			@Override
			protected void onChange(ComponentEvent ce) {
				super.onChange(ce);
                formPanel.setData("uploadid", this.getItemId());
                this.setValue("Uploading ...");
                formPanel.submit();
			}
        };
        fuf.setName("upload" + System.currentTimeMillis());
        fuf.setItemId(model.getID());
        fuf.setFieldLabel(model.getLabel());
        FormData fd = new FormData();
        fd.setWidth(260);
        TypedWidget tWidget = new TypedWidget(fuf, model, null,
                UIType.TEXTFIELD);
        addToTypedWidget(tWidget, fd);
        
        final Button browser = new Button("Browse...");
        browser.addSelectionListener(new SelectionListener<ButtonEvent>(){

			@Override
			public void componentSelected(ButtonEvent ce) {
				OpenDialog dialog = new OpenDialog();
			    dialog.show();
			}
        	
        });
        tWidget = new TypedWidget(browser, model, null,
                UIType.TEXTFIELD);
        addToTypedWidget(tWidget, fd);        
    }

    private String defaultSVNURLLabel = "SVN Repository";
    private String defaultSVNREVLabel = "SVN Revision";
    private String defaultSVNLOGLabel = "SVN Log Message";
    private String defaultGitURLLabel = "Git Repository";
    private String defaultGitBranchLabel = "Git Branch";
    private String defaultGitREVLabel = "Git Commit";
    private String defaultGitLOGLabel = "Git Log Message";
    private Integer perPage = 10;

    private TabItem parentItem;

    public void visitSVN(SVNModel model) {
        URLModel urlModel = model.getURLModel();
        if(urlModel == null)
            return;
        final TextField<String> textField = new TextField<String>();
        textField
                .setFieldLabel(Util.isEmpty(model.getLabel()) ? defaultSVNURLLabel
                        : model.getLabel());

        // TODO: substitute with getting value by variable name
        List<String> vals = urlModel.getValues();
        String svnurl = null;
        if (vals != null && vals.size() > 0)
            svnurl = vals.get(0);

        if (Util.isEmpty(svnurl))
            svnurl = "";
        textField.setValue(svnurl);
        FormData tf = new FormData();
        tf.setWidth(500);
        TypedWidget tWidget = new TypedWidget(textField, urlModel, null,
                UIType.TEXTFIELD);
        addToTypedWidget(tWidget, tf);

        UIBaseNode n = model.getRevModel();
        if(n instanceof RevModel){
            RevModel revModel = (RevModel)n ;
            final ComboBox<CedarSCMLogEntry> comboRev = new ComboBox<CedarSCMLogEntry>();
            final ListStore<CedarSCMLogEntry> store = new ListStore<CedarSCMLogEntry>();
            final CedarScmLoadConfig config = new CedarScmLoadConfig();
            config.setFeatureId(featureModel.getFeatureID());
            config.setRev(-1L);
            config.setNumOfRev(model.getMax() == 0 ? perPage : model.getMax());
            config.setType(CedarScmType.SVN);
            comboRev
                    .setFieldLabel(Util.isEmpty(revModel.getLabel()) ? defaultSVNREVLabel
                            : revModel.getLabel());
            comboRev.setDisplayField("Verbose");
            comboRev.setTriggerAction(TriggerAction.ALL);
            comboRev.setStore(store);
            FormData cf = new FormData();
            cf.setWidth(350);
            tWidget = new TypedWidget(comboRev, revModel, null, UIType.COMBOBOX);
            addToTypedWidget(tWidget, cf);
            
            n = model.getLogModel();
            if(n instanceof LogModel){
                LogModel logModel = (LogModel)n ;
                final TextArea textArea = new TextArea();
                textArea
                        .setFieldLabel(Util.isEmpty(logModel.getLabel()) ? defaultSVNLOGLabel
                                : logModel.getLabel());
                FormData ta = new FormData();
                ta.setWidth(500);
                ta.setHeight(150);
                tWidget = new TypedWidget(textArea, logModel, null, UIType.TEXTAREA);
                addToTypedWidget(tWidget, ta);
                
                textField.addKeyListener(new KeyListener() {
                    public void componentKeyUp(ComponentEvent ce) {
                        String url = textField.getValue();
                        new ScmLogRetriever(comboRev, store, textArea, url, config)
                                .invoke(false);
                    }
                });

                comboRev.addListener(Events.Select, new Listener<FieldEvent>() {
                    @Override
                    public void handleEvent(FieldEvent be) {
                        String svnurl = textField.getValue();
                        String rev = comboRev.getValue().getRev();
                        new ScmLogMessageRetriever(textArea, svnurl, rev, config).invoke(false);
                    }
                });
                
                new ScmLogRetriever(comboRev, store, textArea, svnurl, config)
                .invoke(false);
            }
            else{
                textField.addKeyListener(new KeyListener() {
                    public void componentKeyUp(ComponentEvent ce) {
                        String url = textField.getValue();
                        new ScmLogRetriever(comboRev, store, null, url, config)
                                .invoke(false);
                    }
                });
                
                new ScmLogRetriever(comboRev, store, null, svnurl, config)
                .invoke(false);
            }
        }               
    }

    public void visitGit(GitModel model) {
        URLModel urlModel = model.getURLModel();
        if(urlModel == null)
            return;
        final TextField<String> textField = new TextField<String>();
        textField
                .setFieldLabel(Util.isEmpty(model.getLabel()) ? defaultGitURLLabel
                        : model.getLabel());

        // TODO: substitute with getting value by variable name
        List<String> vals = urlModel.getValues();
        String url = null;
        if (vals != null && vals.size() > 0)
            url = vals.get(0);

        if (Util.isEmpty(url))
            url = "";
        textField.setValue(url);
        textField.setReadOnly(true);
        textField.disable();
        FormData tf = new FormData();
        tf.setWidth(500);
        TypedWidget tWidget = new TypedWidget(textField, urlModel, null,
                UIType.TEXTFIELD);
        addToTypedWidget(tWidget, tf);

        final BranchModel branch = model.getBranchModel();
        final SimpleComboBox<String> branchField = new SimpleComboBox<String>();
        branchField
                .setFieldLabel(defaultGitBranchLabel);
        branchField.setEditable(false);
        branchField.setTriggerAction(TriggerAction.ALL);
        tf = new FormData();
        tf.setWidth(300);
        tWidget = new TypedWidget(branchField, branch, null,
                UIType.COMBOBOX);
        addToTypedWidget(tWidget, tf);
        
        UIBaseNode n = model.getRevModel();
        if(n instanceof RevModel){
            RevModel revModel = (RevModel)n ;
            final ComboBox<CedarSCMLogEntry> comboRev = new ComboBox<CedarSCMLogEntry>();
            final ListStore<CedarSCMLogEntry> store = new ListStore<CedarSCMLogEntry>();
            final CedarScmLoadConfig config = new CedarScmLoadConfig();
            config.setFeatureId(featureModel.getFeatureID());
            config.setRev(-1L);
            config.setNumOfRev(model.getMax() == 0 ? perPage : model.getMax());
            config.setType(CedarScmType.GIT);
            comboRev
                    .setFieldLabel(Util.isEmpty(revModel.getLabel()) ? defaultGitREVLabel
                            : revModel.getLabel());
            comboRev.setDisplayField("Verbose");
            comboRev.setTriggerAction(TriggerAction.ALL);
            comboRev.setStore(store);
            FormData cf = new FormData();
            cf.setWidth(350);
            tWidget = new TypedWidget(comboRev, revModel, null, UIType.COMBOBOX);
            addToTypedWidget(tWidget, cf);
            
            n = model.getLogModel();
            if(n instanceof LogModel){
                LogModel logModel = (LogModel)n ;
                final TextArea textArea = new TextArea();
                textArea
                        .setFieldLabel(Util.isEmpty(logModel.getLabel()) ? defaultGitLOGLabel
                                : logModel.getLabel());
                FormData ta = new FormData();
                ta.setWidth(500);
                ta.setHeight(150);
                tWidget = new TypedWidget(textArea, logModel, null, UIType.TEXTAREA);
                addToTypedWidget(tWidget, ta);
                
                branchField.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
                        String url = textField.getValue();
                        config.setBranch(branchField.getSelection().get(0).getValue());
                        new ScmLogRetriever(comboRev, store, textArea, url, config)
                                .invoke(false);
                    }
                });

                comboRev.addListener(Events.Select, new Listener<FieldEvent>() {
                    @Override
                    public void handleEvent(FieldEvent be) {
                        String svnurl = textField.getValue();
                        String rev = comboRev.getValue().getRev();
                        config.setBranch(branchField.getSelection().get(0).getValue());
                        new ScmLogMessageRetriever(textArea, svnurl, rev, config).invoke(false);
                    }
                });
            }
            else{
                branchField.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
                        String url = textField.getValue();
                        config.setBranch(branchField.getSelection().get(0).getValue());
                        new ScmLogRetriever(comboRev, store, null, url, config)
                                .invoke(false);
                    }
                });                
            }
            new GitBranchRetriever(textField.getValue(), config){
                @Override
                public void onComplete(List<String> branches){
                    branchField.add(branches);
                    if(branch.getValues().size() > 0){
                        String t = branch.getValues().get(0);
                        for(int i = 0; i < branches.size(); i++){
                            if(branches.get(i).endsWith(t)){
                                branchField.setSimpleValue(branches.get(i));
                                break;
                            }
                        }
                    }
                    else{
                        if(branches.size() > 0)
                            branchField.setSimpleValue(branches.get(0));
                    }
                }           
            }.invoke(false);
        }               
    }
    
    public void visitCusField() {
        if (DBG_OUTPUT) {
            opArea = new TextArea();
            opArea.setFieldLabel("output");
            FormData ta = new FormData();
            ta.setWidth(250);
            ta.setHeight(100);
            TypedWidget tWidget = new TypedWidget(opArea, null, null,
                    UIType.TEXTAREA);

            addToTypedWidget(tWidget, ta);
        }
    }

    public void addToTypedWidget(TypedWidget tWidget, LayoutData ldata) {
        TypedWidget parent = widgetStack.getLast();
        UIType type = parent.getType();
        LayoutData actual = ((ldata != null) ? ldata : parent.getLayoutData());
        switch (type) {
        case FORM:
            FormPanel formPanel = (FormPanel) parent.getWidget();
            formPanel.add(tWidget.getWidget(), actual);
            break;
        case FIELDSET:
            FieldSet fieldSet = (FieldSet) parent.getWidget();
            fieldSet.add(tWidget.getWidget(), actual);
            break;
        case BUTTONBAR:
            HorizontalPanel hp = (HorizontalPanel) parent.getWidget();
            hp.add(tWidget.getWidget(), actual);
            break;
        default:
            break;
        }
        parent.addChild(tWidget);
    }

    public FormPanel getFormPanel() {
        return formPanel;
    }

    public void root() {

    }

    public void setFeatureModel(FeatureModel featureModel) {
        this.featureModel = featureModel;
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public class ScmLogRetriever extends
            RPCInvocation<BaseListLoadResult<CedarSCMLogEntry>> {
        private ComboBox<CedarSCMLogEntry> comboRev;
        private ListStore<CedarSCMLogEntry> store;
        private TextArea textArea;
        private String url;
        private CedarScmLoadConfig config;

        public ScmLogRetriever() {

        }

        public ScmLogRetriever(ComboBox<CedarSCMLogEntry> comboRev,
                ListStore<CedarSCMLogEntry> store, TextArea textArea,
                String svnurl, CedarScmLoadConfig config) {
            this.comboRev = comboRev;
            this.store = store;
            this.textArea = textArea;
            this.url = svnurl;
            this.config = config;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<BaseListLoadResult<CedarSCMLogEntry>> callback) {
            remoteService.loadSVNEntries(url, config, callback);
        }

        public void onComplete(BaseListLoadResult<CedarSCMLogEntry> obj) {
            comboRev.clear();
            if(textArea != null)
                textArea.clear();
            store.removeAll();
            store.add(obj.getData());
            if (obj.getData().size() > 0) {
                String rev = obj.getData().get(0).getRev();
                comboRev.setValue(obj.getData().get(0));
                if(textArea != null)
                    new ScmLogMessageRetriever(textArea, url, rev, config).invoke(false);
            }
        }

        public Command onFailure(Throwable t) {
            return null;
        }
    }

    public class ScmLogMessageRetriever extends RPCInvocation<String> {
        private TextArea textArea;
        private String url;
        private String rev;
        private CedarScmLoadConfig config;
        
        public ScmLogMessageRetriever() {

        }

        public ScmLogMessageRetriever(TextArea textArea, String svnurl,
                String svnrev, CedarScmLoadConfig config) {
            this.textArea = textArea;
            this.url = svnurl;
            this.rev = svnrev;
            this.config = config;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<String> callback) {
            remoteService.getSingleSVNLogMessage(url, rev, config, callback);
        }

        public void onComplete(String obj) {
            textArea.setValue(obj);
        }
    }
    
    public class GitBranchRetriever extends RPCInvocation<List<String>> {
        private String url;
        private CedarScmLoadConfig config;
        
        public GitBranchRetriever() {

        }

        public GitBranchRetriever(String url, CedarScmLoadConfig config) {
            this.url = url;
            this.config = config;
        }

        @Override
        public void execute(CloudRemoteServiceAsync remoteService,
                AsyncCallback<List<String>> callback) {
            remoteService.getGitBranches(url, config, callback);
        }
    }

    protected HorizontalPanel createButtonBar() {
        HorizontalPanel btnBar = new HorizontalPanel();
        btnBar.setTableWidth("100%");
        IconButton smtBtn = new CedarIconButton("cedar_submit");
        smtBtn.setSize(80, 24);
        TableData smtfd = new TableData();
        smtfd.setWidth("80%");
        smtfd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(smtBtn, smtfd);
        IconButton celBtn = new CedarIconButton("cedar_cancel");
        celBtn.setSize(80, 24);
        TableData celfd = new TableData();
        celfd.setWidth("20%");
        celfd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        btnBar.add(celBtn, celfd);

        return btnBar;
    }

    public void visitSubmit(SubmitModel model) {
        HorizontalPanel btnBar = new HorizontalPanel();
        btnBar.setTableWidth("100%");
        FormData btnBarfd = new FormData();
        btnBarfd.setMargins(new Margins(40, 0, 0, 0));
        TypedWidget tWidget = new TypedWidget(btnBar, null, new TableData(),
                UIType.BUTTONBAR);
        widgetStack.add(tWidget);

        IconButton smtBtn = new CedarIconButton("cedar_submit");
        smtBtn.setSize(80, 24);
        TableData smtfd = new TableData();
        smtfd.setWidth("75%");
        smtfd.setHorizontalAlign(HorizontalAlignment.RIGHT);
        tWidget = new TypedWidget(smtBtn, model, null, UIType.ICONBUTTON);
        widgetStack.add(tWidget);
        for (UIBaseNode obj : model.getChildren()) {
            obj.accept(this);
        }
        widgetStack.removeLast();
        addToTypedWidget(tWidget, smtfd);

        IconButton celBtn = new CedarIconButton("cedar_cancel");
        celBtn.setSize(80, 24);
        celBtn.addSelectionListener(new SelectionListener<IconButtonEvent>() {

            @Override
            public void componentSelected(IconButtonEvent ce) {
                // TodoViewer.show();
                parentItem.close();
            }

        });
        TableData celfd = new TableData();
        celfd.setWidth("25%");
        celfd.setHorizontalAlign(HorizontalAlignment.CENTER);
        tWidget = new TypedWidget(celBtn, null, null, UIType.ICONBUTTON);
        addToTypedWidget(tWidget, celfd);

        tWidget = widgetStack.removeLast();
        addToTypedWidget(tWidget, btnBarfd);
    }

    public void visitFormItem(FormItemModel model) {
        TypedWidget tWidget = new TypedWidget(null, model, null,
                UIType.FORMITEM);
        addToTypedWidget(tWidget, null);
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
        return this.parentItem;
    }
}
