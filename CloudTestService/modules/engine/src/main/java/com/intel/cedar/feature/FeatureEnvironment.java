package com.intel.cedar.feature;

import java.net.URI;
import java.util.Properties;

import com.intel.cedar.engine.impl.FeaturePropsManager;
import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;

public class FeatureEnvironment extends AbstractEnvironment implements
        Environment {
    private VariableManager vars;
    private FeaturePropsManager props;

    public FeatureEnvironment(ClassLoader loader, IFolder root,
            VariableManager vars, FeaturePropsManager props) {
        super(loader, root);
        this.vars = vars;
        this.props = props;
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
        return props.getProperties(version);
    }

    @Override
    public String getFeatureProperty(String name, String version) {
        return props.getProperty(name, version);
    }

    @Override
    public void setFeatureProperties(Properties props, String version) {
        this.props.setProperties(props, version);
    }

    @Override
    public void setFeatureProperty(String name, String value, String version) {
        this.props.setProperty(name, value, version);
    }

    @Override
    public IFile getFileByURI(URI uri) {
        return StorageFactory.getInstance().getStorage().getRoot().getFile(uri);
    }

    @Override
    public IFolder getFolderByURI(URI uri) {
        return StorageFactory.getInstance().getStorage().getRoot().getFolder(uri);
    }
}
