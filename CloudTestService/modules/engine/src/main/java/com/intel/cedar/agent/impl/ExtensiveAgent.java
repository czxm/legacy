package com.intel.cedar.agent.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.agent.IExtensiveAgent;
import com.intel.cedar.engine.impl.FeaturePropsManager;
import com.intel.cedar.engine.impl.VariableManager;
import com.intel.cedar.feature.util.FeatureUtil;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.util.protocal.ModelStream;

public class ExtensiveAgent extends XmlBasedAgent implements IExtensiveAgent {
    private static Logger LOG = LoggerFactory.getLogger(ExtensiveAgent.class);
    private VariableManager variables;
    private FeaturePropsManager props;
    private IFolder storage;

    public ExtensiveAgent(String host, String port, boolean quickTimeOut) {
        super(host, port, quickTimeOut);
    }
    
    public ExtensiveAgent(String host, String port) {
        this(host, port, true);
    }

    public void setVariableManager(VariableManager variables) {
        this.variables = variables;
    }

    @Override
    public void setPropertiesManager(FeaturePropsManager props) {
        this.props = props;
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

    private void uploadFile(IFile file, String runningId) throws Exception {
        String url = "http://" + host + ":" + port + "/agent/putStorage";
        HttpPost post = new HttpPost(url);
        MultipartEntity entity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE);
        InputStreamBody body = new InputStreamBody(file.getContents(),
                runningId);
        entity.addPart("storage", (ContentBody) body);
        post.setEntity(entity);
        HttpResponse response = getClient().execute(post);
        EntityUtils.consume(response.getEntity());
    }

    protected void downloadFile(IFile file, String runningId) throws Exception {
        String url = "http://" + host + ":" + port + "/agent/Get"
                + "?action=getStorage&runningId=" + runningId;
        HttpGet httpget = new HttpGet(url);
        HttpEntity resEntity = getClient().execute(httpget).getEntity();
        file.setContents(resEntity.getContent());
        EntityUtils.consume(resEntity);
    }

    protected CedarResponse serviceRequest(CedarRequest request,
            String runningId) {
        CedarResponse response = null;
        Properties payload = null;
        String key = null;
        String value = null;
        String version = null;
        switch (request.getAction()) {
        case ReadVariable:
            String name = (String) request.getPayload();
            Variable result = variables.getVariable(name);
            response = CedarResponse.newReadVariableResponse(result);
            if (result == null) {
                response.setPayload(new RuntimeException(String.format(
                        "variable '%s' not found", name)));
            }
            break;
        case WriteVariable:
            Variable var = (Variable) request.getPayload();
            response = CedarResponse.newWriteVariableResponse(var);
            if (variables.getVariable(var.getName()) != null)
                variables.putVariable(var);
            else
                response.setPayload(new RuntimeException(String.format(
                        "variable '%s' not found", var.getName())));
            break;
        case GetFeatureProperty:
            payload = (Properties) request.getPayload();
            key = payload.getProperty("_CEDAR_GET_FEATURE_PROPERTY_KEY");
            version = payload.getProperty("_CEDAR_FEATURE_VERSION");
            response = CedarResponse.newGetFeaturePropertyResponse(props
                    .getProperty(key, version));
            break;
        case SetFeatureProperty:
            payload = (Properties) request.getPayload();
            key = payload.getProperty("_CEDAR_SET_FEATURE_PROPERTY_KEY");
            value = payload.getProperty("_CEDAR_SET_FEATURE_PROPERTY_VALUE");
            version = payload.getProperty("_CEDAR_FEATURE_VERSION");
            props.setProperty(key, value, version);
            response = CedarResponse.newSetFeaturePropertyResponse(true);
            break;
        case GetFeatureProperties:
            payload = (Properties) request.getPayload();
            version = payload.getProperty("_CEDAR_FEATURE_VERSION");
            response = CedarResponse.newGetFeaturePropertiesResponse(props
                    .getProperties(version));
            break;
        case SetFeatureProperties:
            payload = (Properties) request.getPayload();
            version = payload.getProperty("_CEDAR_FEATURE_VERSION");
            payload.remove("_CEDAR_FEATURE_VERSION");
            props.setProperties(payload, version);
            response = CedarResponse.newSetFeaturePropertiesResponse(true);
            break;
        case ReadStorageFile: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse.newReadStorageFileResponse(true);
            try {
                uploadFile(file, runningId);
            } catch (Exception e) {
                response.setPayload(e);
            }
            break;
        }
        case WriteStorageFile: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse.newWriteStorageFileResponse(true);
            try {
                downloadFile(file, runningId);
            } catch (Exception e) {
                response.setPayload(e);
            }
            break;
        }
        case StorageFileExist: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse.newStorageFileExistResponse(file.exist());
            break;
        }
        case StorageFolderExist: {
            String uri = (String) request.getPayload();
            IFolder folder = storage.getFolder(URI.create(uri));
            response = CedarResponse.newStorageFolderExistResponse(folder
                    .exist());
            break;
        }
        case StorageList: {
            String uri = (String) request.getPayload();
            IFolder folder = storage.getFolder(URI.create(uri));
            response = CedarResponse.newStorageListResponse(folder
                    .list());
            break;
        }
        case CreateStorageFile: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse
                    .newCreateStorageFileResponse(file.create());
            break;
        }
        case CreateStorageFolder: {
            String uri = (String) request.getPayload();
            IFolder folder = storage.getFolder(URI.create(uri));
            response = CedarResponse.newCreateStorageFolderResponse(folder
                    .create());
            break;
        }
        case DeleteStorageFile: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse
                    .newDeleteStorageFileResponse(file.delete());
            break;
        }
        case DeleteStorageFolder: {
            String uri = (String) request.getPayload();
            IFolder folder = storage.getFolder(URI.create(uri));
            response = CedarResponse.newDeleteStorageFolderResponse(folder
                    .delete());
            break;
        }
        case StorageLength: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse.newStorageLengthResponse(file.length());
            break;
        }       
        case StorageLastModified: {
            String uri = (String) request.getPayload();
            IFile file = storage.getFile(URI.create(uri));
            response = CedarResponse.newStorageLastModifiedResponse(file.lastModified());
            break;
        }          
        default:
            throw new RuntimeException("unsupported action");
        }
        return response;
    }

    protected String submitCedarResponse(CedarResponse response,
            String runningId) throws ClientProtocolException, IOException {
        String url = "http://" + host + ":" + port + "/agent/cedarResponse";
        HttpPost httppost = new HttpPost(url);

        ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
        postParams.add(new BasicNameValuePair("runningId", runningId));
        postParams.add(new BasicNameValuePair("CedarResponse",
                new ModelStream<CedarResponse>().serialize(response)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                "UTF-8");
        httppost.setEntity(entity);
        HttpEntity resEntity = getClient().execute(httppost).getEntity();
        String res = EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);
        return res;
    }

    protected String doWait(String runningId) throws ClientProtocolException,
            IOException {
        StringBuilder sb = new StringBuilder();
        String url = "http://" + host + ":" + port + "/agent/Get"
                + "?action=wait&runningId=" + runningId;
        HttpGet httpget = new HttpGet(url);
        String server = getServerInfo().getServer();
        HttpEntity resEntity = getClient().execute(httpget).getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                resEntity.getContent()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            LOG.debug("agent {} output line for wait: {}", new Object[] {
                    server, line });
            if (line.equals("@@CedarRequest")) {
                String content = readContent(reader, "CedarRequest@@");
                CedarRequest request = new ModelStream<CedarRequest>()
                        .generate(content);
                CedarResponse response = serviceRequest(request, runningId);
                submitCedarResponse(response, runningId);
            } else if (!line.contains("@@Cedar")) {
                sb.append(line);
            }
        }
        reader.close();
        EntityUtils.consume(resEntity);
        return sb.toString();
    }

    public String uploadFeature(String jarPath) throws ClientProtocolException,
            IOException {
        File jarFile = new File(jarPath);
        String url = "http://" + host + ":" + port + "/agent/installFeature";
        HttpPost post = new HttpPost(url);
        MultipartEntity entity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE);
        entity.addPart("feature", (ContentBody) new FileBody(jarFile));
        post.setEntity(entity);
        HttpResponse response = getClient().execute(post);
        HttpEntity resEntity = response.getEntity();
        String res = EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);
        return res;
    }

    public void installFeatures(ITaskRunner runner, String[] features) {
        String[] remoteFeatures = new String[] {};
        try {
            String resultContent = doAction("listFeatures", "dummy");
            remoteFeatures = resultContent.split(" ");
        } catch (Exception e) {
        }
        StringBuilder sb = new StringBuilder();
        for (String feature : features) {
            sb.append(feature);
            sb.append(" ");
            try {
                boolean needInstall = true;
                for (String rf : remoteFeatures) {
                    if (feature.equals(rf)) {
                        needInstall = false;
                        break;
                    }
                }
                if (needInstall) {
                    uploadFeature(FeatureUtil.getFeatureJarById(feature));
                }
            } catch (Exception e) {
                LOG.info("failed to install feature for " + feature, e);
            }
        }
        this.addPostParam(runner, "features", sb.deleteCharAt(sb.length() - 1)
                .toString());
    }

    public void setStorageRoot(ITaskRunner runner, IFolder storage) {
        this.storage = storage;
        this.addPostParam(runner, "storageRoot", storage.getURI().toString());
    }
}
