package com.intel.cedar.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Properties;

import com.intel.cedar.agent.impl.CedarRequest;
import com.intel.cedar.agent.impl.CedarResponse;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.storage.impl.RemoteFile;
import com.intel.cedar.storage.impl.RemoteFolder;
import com.intel.cedar.util.protocal.ModelStream;

public class TaskRunnerEnvironment extends AbstractEnvironment implements
        Environment {
    private static TaskRunnerEnvironment singleton;

    public static TaskRunnerEnvironment getInstance() {
        return singleton;
    }

    public static TaskRunnerEnvironment createInstance(ClassLoader loader,
            String storageRootURI) {
        if (singleton != null || loader == null)
            throw new RuntimeException(
                    "Permission denied to create Environment");
        singleton = new TaskRunnerEnvironment(loader, storageRootURI);
        return singleton;
    }

    private TaskRunnerEnvironment(ClassLoader loader, String storageRootURI) {
        super(loader, storageRootURI != null ? new RemoteFolder(URI.create(storageRootURI))
                : null);
    }

    private void setCedarStorageFile(File file) {
        String path = file.getAbsolutePath();
        System.err.println("@@CedarStorageFile");
        System.err.println(path);
        System.err.println("CedarStorageFile@@");
        System.err.flush();
    }

    private void sendCedarRequest(CedarRequest req) {
        String request = new ModelStream<CedarRequest>().serialize(req);
        System.err.println("@@CedarRequest");
        System.err.println(request);
        System.err.println("CedarRequest@@");
        System.err.flush();
    }

    private String readContent(BufferedReader reader, String endTag) {
        StringBuilder sb = new StringBuilder();
        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                if (line.equals(endTag))
                    break;
                sb.append(line);
            }
        } catch (Exception e) {
        }
        return sb.toString();
    }

    private CedarResponse getCedarResponse() throws Exception {
        try {
            String line = "";
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    System.in));
            while ((line = reader.readLine()) != null) {
                if (line.equals("@@CedarResponse")) {
                    String response = readContent(reader, "CedarResponse@@");
                    return new ModelStream<CedarResponse>().generate(response);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    public Variable getVariable(String name) throws Exception {
        try {
            CedarRequest request = CedarRequest.newReadVariableRequest(name);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            if (response.isReadVariableResponse())
                return (Variable) response.getPayload();
        } catch (Exception e) {
            throw e;
        }
        throw new RuntimeException("internal error");
    }

    public void setVariable(Variable var) throws Exception {
        try {
            CedarRequest request = CedarRequest.newWriteVariableRequest(var);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
        } catch (Exception e) {
            throw e;
        }
    }

    public void saveStorage(File local, IFile remote) throws Exception {
        if (!local.exists())
            local.createNewFile();
        setCedarStorageFile(local);
        CedarRequest request = CedarRequest.newWriteStorageFileRequest(remote
                .getURI().toString());
        sendCedarRequest(request);
        CedarResponse response = getCedarResponse();
        if (!response.isSucceeded())
            throw (Exception) response.getPayload();
    }

    public void loadStorage(File local, IFile remote) throws Exception {
        if (!local.exists())
            local.createNewFile();
        setCedarStorageFile(local);
        CedarRequest request = CedarRequest.newReadStorageFileRequest(remote
                .getURI().toString());
        sendCedarRequest(request);
        CedarResponse response = getCedarResponse();
        if (!response.isSucceeded())
            throw (Exception) response.getPayload();
    }

    public boolean createStorageFile(IFile remote) {
        try {
            CedarRequest request = CedarRequest
                    .newCreateStorageFileRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStorageFile(IFile remote) {
        try {
            CedarRequest request = CedarRequest
                    .newDeleteStorageFileRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createStorageFolder(IFolder remote) {
        try {
            CedarRequest request = CedarRequest
                    .newCreateStorageFolderRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStorageFolder(IFolder remote) {
        try {
            CedarRequest request = CedarRequest
                    .newDeleteStorageFolderRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean storageExist(IFile remote) {
        try {
            CedarRequest request = CedarRequest
                    .newStorageFileExistRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean storageExist(IFolder remote) {
        try {
            CedarRequest request = CedarRequest
                    .newStorageFolderExistRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Boolean) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public IStorage[] storageList(IFolder remote) {
        try {
            CedarRequest request = CedarRequest
                    .newStorageListRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            String[] urls = (String[]) response.getPayload();
            IStorage[] ret = new IStorage[urls.length];
            for(int i = 0; i < urls.length; i++){
                if(urls[i].endsWith("%2F") || urls[i].endsWith("/")){
                    ret[i] = root.getFolder(URI.create(urls[i]));
                }
                else{
                    ret[i] = root.getFile(URI.create(urls[i]));
                }
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new IStorage[]{};
    }
    
    public long storageLength (IFile remote) {
        try {
            CedarRequest request = CedarRequest
                    .newStorageLengthRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Long) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public long storageLastModified (IStorage remote) {
        try {
            CedarRequest request = CedarRequest
                    .newStorageLastModifiedRequest(remote.getURI().toString());
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (!response.isSucceeded())
                throw (Exception) response.getPayload();
            return (Long) response.getPayload();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    @Override
    public Properties getFeatureProperties(String version) {
        try {
            CedarRequest request = CedarRequest
                    .newGetFeaturePropertiesRequest(version);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (response.isGetFeaturePropertiesResponse())
                return (Properties) response.getPayload();
        } catch (Exception e) {
        }
        return new Properties();
    }

    @Override
    public String getFeatureProperty(String name, String version) {
        try {
            CedarRequest request = CedarRequest.newGetFeaturePropertyRequest(
                    name, version);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
            if (response.isGetFeaturePropertyResponse())
                return (String) response.getPayload();
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void setFeatureProperties(Properties props, String version) {
        try {
            CedarRequest request = CedarRequest.newSetFeaturePropertiesRequest(
                    props, version);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
        } catch (Exception e) {
        }
    }

    @Override
    public void setFeatureProperty(String name, String value, String version) {
        try {
            CedarRequest request = CedarRequest.newSetFeaturePropertyRequest(
                    name, value, version);
            sendCedarRequest(request);
            CedarResponse response = getCedarResponse();
        } catch (Exception e) {
        }
    }

    @Override
    public IFile getFileByURI(URI uri) {
        return new RemoteFile(uri, (RemoteFolder) root);
    }

    @Override
    public IFolder getFolderByURI(URI uri) {
        return new RemoteFolder(uri, (RemoteFolder) root);
    }
}
