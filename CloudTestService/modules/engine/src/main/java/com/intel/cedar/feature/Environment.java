package com.intel.cedar.feature;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.util.Properties;

import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;

public interface Environment {
    public String getOSName();

    public String getHostName();
    
    public String getArchitecture();

    public String getCWD();

    public void extractResource(String name) throws Exception;

    public void extractResource(String name, String file) throws Exception;

    public int execute(String[] commands) throws Exception;

    public int execute(String[] commands, Writer output) throws Exception;

    public int execute(String[] commands, IFile output) throws Exception;

    public int execute(String command) throws Exception;

    public int execute(String command, Writer output) throws Exception;

    public int executeAs(String user, String[] commands, Writer output)
            throws Exception;

    public int executeAs(String user, String[] commands, IFile output)
            throws Exception;

    public void asyncExec(Runnable runable);

    public Variable getVariable(String name) throws Exception;

    public void setVariable(Variable var) throws Exception;

    public String getFeatureProperty(String name, String version);

    public void setFeatureProperty(String name, String value, String version);

    public Properties getFeatureProperties(String version);

    public void setFeatureProperties(Properties props, String version);

    public IFolder getStorageRoot();

    public void copyFile(IFile src, File dest) throws Exception;

    public void copyFile(File src, IFile dest) throws Exception;
    
    public void copyFolder(IFolder src, File dest) throws Exception;
    
    public void copyFolder(File src, IFolder dest) throws Exception;

    public IFolder getFolderByURI(URI uri);

    public IFile getFileByURI(URI uri);

    public String getHyperlink(IStorage storage);
}
