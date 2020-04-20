package com.intel.cedar.engine.model.feature;

import java.util.Iterator;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.DataModelException;
import com.intel.cedar.engine.model.IDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.Variable.VarType;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNode;
import com.intel.cedar.service.client.feature.model.ui.VariableBuffer;

public class FeatureUI extends DataModel {
    FeatureModel featureModel;
    Feature feature;

    public FeatureUI(IDataModelDocument document) {
        super(document);

        featureModel = new FeatureModel();
    }

    public FeatureModel getFeatureModel() {
        return featureModel;
    }

    public void addChild(UIBaseNode child) {
        featureModel.addChild(child);
    }

    @Override
    public void onLoaded() throws DataModelException {
        super.onLoaded();

        setFeatureName();

        computeVarBuff();

        featureModel.init();
    }

    protected void setFeatureName() {
        if (feature == null) {
            getFeature();
        }

        String name = feature.getName();
        featureModel.setName(name);
    }

    protected void computeVarBuff() {
        if (feature == null) {
            getFeature();
        }

        addVarBuff(feature.getLocalVariables(), VarType.LOCAL_V);
        addVarBuff(feature.getImportVariables(), VarType.IMPORT_V);
    }

    private void addVarBuff(List<Variable> vars, VarType type) {
        try {

            VariableBuffer varBuff = featureModel.getVarBuff();

            Iterator<Variable> varIter = vars.iterator();
            while (varIter.hasNext()) {
                Variable var = varIter.next();
                Variable uvar = new Variable(var.getName(), type);
                uvar.addVarValues(var.getVarValues());
                varBuff.addVariable(uvar);
            }
        } catch (Exception e) {

        }
    }

    protected void getFeature() {
        IDataModel o = this.getParent();
        if (!(o instanceof Feature)) {
            return;
        }

        feature = (Feature) o;
    }
}
