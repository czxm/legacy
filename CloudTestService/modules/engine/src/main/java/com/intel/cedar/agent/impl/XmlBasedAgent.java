package com.intel.cedar.agent.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.intel.cedar.agent.runtime.ServerRuntimeInfo;
import com.intel.cedar.tasklet.IProgressProvider;
import com.intel.cedar.tasklet.IResult;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ITaskRunner;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.impl.Result;
import com.intel.cedar.util.SubDirectory;
import com.intel.cedar.util.protocal.ConversionProtocal;
import com.intel.cedar.util.protocal.ModelStream;

public class XmlBasedAgent extends AbstractAgent {
    protected static String scheme = "http://";
    protected ConversionProtocal conversionProtocal = new ConversionProtocal();
    protected ClientConnectionManager connManager = HttpClientUtils
            .getClientConnManager();
    protected HttpHost proxy = null;
    protected CredentialsProvider credsProvider = null;
    protected HashMap<ITaskRunner, HashMap<String, String>> paramMap = new HashMap<ITaskRunner, HashMap<String, String>>();
    protected boolean quickTimeOut;
    
    public XmlBasedAgent(String host, String port, boolean quickTimeOut) {
        super(host, port);
        this.quickTimeOut = quickTimeOut;
    }

    public XmlBasedAgent(String host) {
        this(host, "10614", true);
    }

    protected void finalize() {
        connManager.shutdown();
    }

    public boolean testConnection() {
        try {
            String cedarVersion = System.getProperty("cedar.version");
            String cedarDigest = System.getProperty("cedar.digest");
            ServerRuntimeInfo s = getServerInfo();
            String remoteVersion = s.getVersion();
            String remoteDigest = s.getDigest();
            if (cedarVersion.equals(remoteVersion)
                    && cedarDigest.equals(remoteDigest)) {
                return true;
            }
        } catch (Exception e) {                
        }
        return false;
    }
    
    public ServerRuntimeInfo getServerInfo() {
        try {
            String resultContent = doAction("getServerInfo", "dummy");
            return new ModelStream<ServerRuntimeInfo>().generate(resultContent);
        } catch (Exception e) {
            return null;
        }
    }

    public void addPostParam(ITaskRunner runner, String name, String value) {
        HashMap<String, String> param = paramMap.get(runner);
        if (param == null) {
            param = new HashMap<String, String>();
            paramMap.put(runner, param);
        }
        param.put(name, value);
    }

    protected HashMap<String, String> getPostParams(ITaskRunner runner) {
        return this.paramMap.get(runner);
    }

    public void setProxy(String proxyHost, String proxyPort, String authName,
            String authPasswd) {
        proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
        UsernamePasswordCredentials auth = new UsernamePasswordCredentials(
                authName, authPasswd);
        credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
                AuthScope.ANY_PORT), auth);
    }

    protected HttpClient getClient() {
        HttpClient client = HttpClientUtils.getHttpClient(connManager, quickTimeOut);
        if (proxy != null) {
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    proxy);
        }
        if (credsProvider != null) {
            ((DefaultHttpClient) client).setCredentialsProvider(credsProvider);
        }
        return client;
    }

    class LogThread extends Thread {
        private IProgressProvider runner;
        private String runningId;
        private Writer logger;

        public LogThread(Writer logger, String runningId) {
            this.runner = null;
            this.runningId = runningId;
            this.logger = logger;
        }

        public void setProgressProvider(IProgressProvider runner) {
            this.runner = runner;
        }

        public void run() {
            try {
                getLog(new BufferedWriter(logger), runner, runningId);
            } catch (Exception e) {
            }
        }
    }

    public IResult run(ITaskRunner runner, ITaskItem taskItem, String timeout,
            String cwd) {
        Result result = new Result();
        LogThread thread = null;
        Writer logger = null;
        OutputStream output = getOutputStream(runner);
        if (output != null)
            logger = new OutputStreamWriter(output);
        else
            logger = new StringWriter();
        try {
            String runnerClassName = runner.getClass().getCanonicalName();
            String taskItemClassName = taskItem.getClass().getCanonicalName();
            String taskItemContent = conversionProtocal
                    .serializeTaskItem(taskItem);
            String runningId = submit(runnerClassName, taskItemClassName,
                    taskItemContent, timeout, cwd, getPostParams(runner));
            setRunningId(runner, runningId);
            thread = new LogThread(logger, runningId);
            if (runner instanceof IProgressProvider) {
                thread.setProgressProvider((IProgressProvider) runner);
            }
            thread.start();
            result = conversionProtocal.generateResult(doWait(runningId));
            if (result.getFailureMessage() != null)
                result.setFailureMessage(StringEscapeUtils.unescapeXml(result
                        .getFailureMessage()));
            setRunningId(runner, null);
        } catch (IOException e) {
            StringWriter buf = new StringWriter();
            PrintWriter writer = new PrintWriter(buf);
            e.printStackTrace(writer);
            writer.close();
            result.setID(ResultID.Unreachable);
            result.setFailureMessage(buf.toString());
        } catch (Exception e) {
            StringWriter buf = new StringWriter();
            PrintWriter writer = new PrintWriter(buf);
            e.printStackTrace(writer);
            writer.close();
            result.setID(ResultID.Failed);
            result.setFailureMessage(buf.toString());
        } finally {
            try {
                if (output == null)
                    result.setLog(logger.toString());
                taskItem.setResult(result);
                if (result.getID().isUnreachable()) {
                    if(thread != null)
                        thread.interrupt();
                }
                if(thread != null)
                    thread.join(300 * 1000);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return result;
    }

    public void kill(ITaskRunner runner) {
        String runningId = getRunningId(runner);
        if (null == runningId) {
            return;
        }
        try {
            doAction("kill", runningId);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            setRunningId(runner, null);
        }
    }

    public void kill(String runningId) {
        if (null == runningId) {
            return;
        }
        try {
            doAction("kill", runningId);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listTaskRunners() {
        try {
            BufferedReader reader = new BufferedReader(new StringReader(
                    doAction("listTasklets", "")));
            String line = "";
            while ((line = reader.readLine()) != null)
                System.out.println(line);
            reader.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TaskRunnerStatus getStatus(ITaskRunner runner) {
        String runningId = getRunningId(runner);
        if (null == runningId) {
            return TaskRunnerStatus.NotAvailable;
        }

        String status = TaskRunnerStatus.NotAvailable.name();
        try {
            status = doAction("getStatus", runningId).trim();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return TaskRunnerStatus.valueOf(status);
    }

    protected void getLog(BufferedWriter log, IProgressProvider runner,
            String runningId) throws Exception {
        String url = scheme + host + ":" + port + "/agent/Get"
                + "?action=getLog" + "&runningId=" + runningId;
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = getClient().execute(httpget);
        HttpEntity resEntity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                resEntity.getContent()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            if (!line.equals("@@CedarHeartBeat@@")) {
                log.write(line);
                log.newLine();
                log.flush();
                if (runner != null)
                    runner.encounterLine(line);
            }
        }
        reader.close();
        EntityUtils.consume(resEntity);
    }

    // Submit a task
    protected String submit(String runnerClassName, String taskItemClassName,
            String taskItemContent, String timeout, String cwd,
            HashMap<String, String> params) throws ClientProtocolException,
            IOException {
        String url = scheme + host + ":" + port + "/agent/submit";
        HttpPost httppost = new HttpPost(url);

        ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
        postParams.add(new BasicNameValuePair("ClassNameOfTaskRunner",
                runnerClassName));
        postParams.add(new BasicNameValuePair("ClassNameOfTaskItem",
                taskItemClassName));
        postParams.add(new BasicNameValuePair("ContentOfTaskItem",
                taskItemContent));
        postParams.add(new BasicNameValuePair("timeout", timeout));
        postParams.add(new BasicNameValuePair("cwd", cwd));
        postParams.add(new BasicNameValuePair("agentID", this.getAgentID()));
        if (params != null) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                postParams.add(new BasicNameValuePair(key, value));
            }
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                "UTF-8");
        httppost.setEntity(entity);

        HttpResponse response = getClient().execute(httppost);
        HttpEntity resEntity = response.getEntity();
        String res = EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);
        return res;
    }

    protected String doWait(String runningId) throws ClientProtocolException,
            IOException {
        StringBuilder sb = new StringBuilder();
        String url = scheme + host + ":" + port + "/agent/Get"
                + "?action=wait&runningId=" + runningId;
        HttpGet httpget = new HttpGet(url);

        HttpEntity resEntity = getClient().execute(httpget).getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                resEntity.getContent()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            if (!line.contains("@@Cedar")) {
                sb.append(line);
            }
        }
        reader.close();
        EntityUtils.consume(resEntity);
        return sb.toString();
    }

    // Do http action: kill, getProgress, getStatus...
    protected String doAction(String action, String runningId)
            throws ClientProtocolException, IOException {
        StringBuilder sb = new StringBuilder();
        String url = scheme + host + ":" + port + "/agent/Get" + "?action="
                + action + "&runningId=" + runningId;
        HttpGet httpget = new HttpGet(url);

        HttpResponse response = getClient().execute(httpget);
        HttpEntity resEntity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                resEntity.getContent()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        reader.close();
        EntityUtils.consume(resEntity);
        return sb.toString();
    }

    private String uploadCedar(String jarPath) throws ClientProtocolException,
            IOException {
        File jarFile = new File(jarPath);
        String url = scheme + host + ":" + port + "/agent/updateCedar";
        HttpPost post = new HttpPost(url);
        MultipartEntity entity = new MultipartEntity(
                HttpMultipartMode.BROWSER_COMPATIBLE);
        entity.addPart("cedar", (ContentBody) new FileBody(jarFile));
        post.setEntity(entity);
        HttpResponse response = getClient().execute(post);
        HttpEntity resEntity = response.getEntity();
        String res = EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);
        return res;
    }

    public boolean updateCedar() {
        try {
            String cedarVersion = System.getProperty("cedar.version");
            String cedarDigest = System.getProperty("cedar.digest");
            ServerRuntimeInfo s = getServerInfo();
            String remoteVersion = s.getVersion();
            String remoteDigest = s.getDigest();
            if (!cedarVersion.equals(remoteVersion)
                    || !cedarDigest.equals(remoteDigest)) {
                String ret = uploadCedar(SubDirectory.LIBS.toString()
                        + "cedar-" + cedarVersion + ".jar");
                if (ret.equals("OK"))
                    return true;
                else
                    return false;
            }
        } catch (Exception e) {
        }
        return false;
    }

    protected String removeFeature(String feature)
            throws ClientProtocolException, IOException {
        String url = scheme + host + ":" + port + "/agent/removeFeature";
        HttpPost httppost = new HttpPost(url);

        ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
        postParams.add(new BasicNameValuePair("feature", feature));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                "UTF-8");
        httppost.setEntity(entity);

        HttpResponse response = getClient().execute(httppost);
        HttpEntity resEntity = response.getEntity();
        String res = EntityUtils.toString(resEntity);
        EntityUtils.consume(resEntity);
        return res;
    }

    public boolean removeFeatures(String[] features) {
        try {
            for (String f : features) {
                if (!"OK".equals(removeFeature(f)))
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
