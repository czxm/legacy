package com.intel.cedar.service.client.feature.view;

import java.io.Serializable;

import com.intel.cedar.service.client.feature.model.ui.CheckboxgroupModel;
import com.intel.cedar.service.client.feature.model.ui.ComboModel;
import com.intel.cedar.service.client.feature.model.ui.CompositeModel;
import com.intel.cedar.service.client.feature.model.ui.FieldSetModel;
import com.intel.cedar.service.client.feature.model.ui.FileUploadModel;
import com.intel.cedar.service.client.feature.model.ui.FormItemModel;
import com.intel.cedar.service.client.feature.model.ui.FormModel;
import com.intel.cedar.service.client.feature.model.ui.GitModel;
import com.intel.cedar.service.client.feature.model.ui.ListFieldModel;
import com.intel.cedar.service.client.feature.model.ui.SVNModel;
import com.intel.cedar.service.client.feature.model.ui.SubmitModel;
import com.intel.cedar.service.client.feature.model.ui.TextAreaModel;
import com.intel.cedar.service.client.feature.model.ui.TextfieldModel;

public interface UIBuilder extends Serializable {

    public void visitForm(FormModel model);

    public void visitComboBox(ComboModel model);

    public void visitList(ListFieldModel model);

    public void visitCheckBox(CheckboxgroupModel model);

    public void visitCheckBoxGroup(CheckboxgroupModel model);

    public void visitTextField(TextfieldModel model);

    public void visitTextArea(TextAreaModel model);

    public void visitFieldSet(FieldSetModel model);

    public void visitComposite(CompositeModel model);

    public void visitSVN(SVNModel model);
    
    public void visitGit(GitModel model);

    public void visitSubmit(SubmitModel model);

    public void visitFormItem(FormItemModel model);

    public void visitFileUploadField(FileUploadModel model);

}
