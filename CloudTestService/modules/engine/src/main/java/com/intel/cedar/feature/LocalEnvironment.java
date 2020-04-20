package com.intel.cedar.feature;

import java.net.URI;
import java.util.Properties;

import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public class LocalEnvironment extends AbstractEnvironment {
    private VariableManager vars;
    private Properties props;

    public LocalEnvironment(VariableManager vars, IFolder root) {
        super(LocalEnvironment.class.getClassLoader(), root);
        this.vars = vars;
        this.props = new Properties();
    }

    @Override
    public Variable getVariable(String name) throws Exception {
        return vars.getVariable(name);
    }

    @Override
    public void setVariable(Variable var) throws Exception {
        vars.putVariable(var);
    }

    @Override
    public Properties getFeatureProperties(String version) {
        return props;
    }

    @Override
    public String getFeatureProperty(String name, String version) {
        return props.getProperty(name);
    }

    @Override
    public void setFeatureProperties(Properties props, String version) {
        for (String key : props.stringPropertyNames()) {
            this.props.setProperty(key, props.getProperty(key));
        }
    }

    @Override
    public void setFeatureProperty(String name, String value, String version) {
        this.props.setProperty(name, value);
    }

    @Override
    public IFile getFileByURI(URI uri) {
        return root.getFile(uri);
    }

    @Override
    public IFolder getFolderByURI(URI uri) {
        return root.getFolder(uri);
    }
}
