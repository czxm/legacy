package com.intel.cedar.engine.model.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.IDataModel;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.engine.model.feature.flow.FeatureFlow;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.ui.FeatureModel;

public class Feature extends DataModel {
    protected Import importData;
    protected Variables variablesData;
    protected Tasklets tasklets;
    protected FeatureUI featureUI;
    protected FeatureFlow featureFlow;
    private String name;
    private String version;
    private String provider;
    private List<Variable> variables;
    private Launches launches;
    private Triggers triggers;
    private Shortcuts shortcuts;
    private ClassLoader classLoader;

    public Feature(IDataModelDocument document) {
        super(document);

        importData = new Import(document);
        variablesData = new Variables(document);
        variablesData.setType(Variable.VarType.LOCAL_V);
        tasklets = new Tasklets(document);
        featureUI = new FeatureUI(document);
        featureFlow = new FeatureFlow(document);
        launches = new Launches(document);
        triggers = new Triggers(document);
        shortcuts = new Shortcuts(document);

        initChild(importData);
        initChild(variablesData);
        initChild(tasklets);
        initChild(featureUI);
        initChild(featureFlow);
    }

    public Iterator<IDataModel> iterate() {
        ArrayList<IDataModel> children = new ArrayList<IDataModel>(4);
        children.add(importData);
        children.add(variablesData);
        children.add(tasklets);
        children.add(featureUI);
        children.add(featureFlow);
        return children.iterator();
    }

    public Import getImport() {
        return importData;
    }

    public List<Variable> getVariables() {
        if (variables == null) {
            computeVariables();
        }

        return variables;
    }

    public List<Variable> getLocalVariables() {
        return variablesData.getVariables();
    }

    public List<Variable> getImportVariables() {
        return importData.getVariables();
    }

    public Variables getVariablesData() {
        return variablesData;
    }

    public Tasklets getTasklets() {
        return this.tasklets;
    }

    public FeatureUI getFeatureUI() {
        return featureUI;
    }

    public FeatureFlow getFeatureFlow() {
        return this.featureFlow;
    }

    public void setName(String name) {
        this.name = name;
        // use event may be better
        featureUI.setFeatureName();
    }

    public String getName() {
        return this.name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return this.version;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProvider() {
        return this.provider;
    }

    public FeatureModel getFeatureModel() {
        if (featureUI == null) {
            return null;
        }
        return featureUI.getFeatureModel();
    }

    public void setFeatureClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getFeatureClassLoader() {
        return this.classLoader;
    }

    protected void computeVariables() {
        variables = new ArrayList<Variable>();
        variables.addAll(variablesData.getVariables());
        variables.addAll(importData.getVariables());
    }

    public Launches getLaunches() {
        return launches;
    }

    public Triggers getTriggers() {
        return triggers;
    }

    public Shortcuts getShortcuts() {
        return shortcuts;
    }
}
