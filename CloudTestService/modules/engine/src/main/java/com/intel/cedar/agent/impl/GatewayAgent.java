package com.intel.cedar.agent.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.agent.IGatewayAgent;
import com.intel.cedar.agent.NATEntry;
import com.intel.cedar.core.entities.AbstractHostInfo;
import com.intel.cedar.core.entities.CloudNodeInfo;
import com.intel.cedar.core.entities.InstanceInfo;
import com.intel.cedar.core.entities.NATInfo;
import com.intel.cedar.core.entities.PhysicalNodeInfo;
import com.intel.cedar.util.CloudUtil;
import com.intel.cedar.util.protocal.ModelStream;

public class GatewayAgent extends XmlBasedAgent implements IGatewayAgent {
    private static Logger LOG = LoggerFactory.getLogger(GatewayAgent.class);
    
    public GatewayAgent(String host, String port, boolean quickTimeOut) {
        super(host, port, quickTimeOut);
    }

    public int createPortMapping(NATInfo n) {
        String url = "http://" + host + ":" + port + "/agent/newPortMapping";
        HttpPost httppost = new HttpPost(url);
        AbstractHostInfo i = null;
        if (n.getInstanceId() != null)
            i = InstanceInfo.load(n.getInstanceId());
        else if (n.getNodeId() != null)
            i = PhysicalNodeInfo.load(n.getNodeId());
        else if (n.getCloudNodeId() != null)
            i = CloudNodeInfo.load(n.getCloudNodeId());
        if (i == null || (!(i instanceof CloudNodeInfo)) && !i.isValidHost()) {
            return -1;
        }
        try {
            ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
            postParams.add(new BasicNameValuePair("host", i.getHost()));
            postParams.add(new BasicNameValuePair("port", String.format("%d", n
                    .getPort())));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                    "UTF-8");
            httppost.setEntity(entity);

            HttpResponse response = getClient().execute(httppost);
            HttpEntity resEntity = response.getEntity();
            int mappedPort = Integer.parseInt(EntityUtils.toString(resEntity));
            LOG.info("Created port " + mappedPort + " on " + host + " for " + i.getHost() + ":" + n.getPort());
            EntityUtils.consume(resEntity);
            return mappedPort;
        } catch (Exception e) {
            return -1;
        }
    }

    public List<NATInfo> getMappedPorts(AbstractHostInfo i) {
        String url = "http://" + host + ":" + port + "/agent/Get"
                + "?action=listNAT&host=" + i.getHost();
        HttpGet httpget = new HttpGet(url);
        List<NATInfo> result = new ArrayList<NATInfo>();
        try {
            HttpResponse response = getClient().execute(httpget);
            HttpEntity resEntity = response.getEntity();
            for (NATEntry entry : new ModelStream<List<NATEntry>>()
                    .generate(EntityUtils.toString(resEntity))) {
                Long gatewayId = CloudUtil.getGatewayByHost(host).getId();
                NATInfo n = new NATInfo(i, Integer.parseInt(entry.port));
                n.setGatewayId(gatewayId);
                n.setMappedPort(Integer.parseInt(entry.mappedPort));
                result.add(n);
            }
            EntityUtils.consume(resEntity);
        } catch (Exception e) {
        }
        return result;
    }

    public void releasePortMapping(NATInfo n) {
        String url = "http://" + host + ":" + port + "/agent/deletePortMapping";
        HttpPost httppost = new HttpPost(url);
        AbstractHostInfo i = null;
        if (n.getInstanceId() != null)
            i = InstanceInfo.load(n.getInstanceId());
        else if (n.getNodeId() != null)
            i = PhysicalNodeInfo.load(n.getNodeId());
        else if (n.getCloudNodeId() != null)
            i = CloudNodeInfo.load(n.getCloudNodeId());
        if (i == null || !i.isValidHost()) {
            return;
        }
        try {
            ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
            postParams.add(new BasicNameValuePair("host", i.getHost()));
            postParams.add(new BasicNameValuePair("port", String.format("%d", n
                    .getPort())));
            postParams.add(new BasicNameValuePair("mapped", String.format("%d",
                    n.getMappedPort())));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                    "UTF-8");
            httppost.setEntity(entity);

            HttpResponse response = getClient().execute(httppost);
            HttpEntity resEntity = response.getEntity();
            EntityUtils.consume(resEntity);
            LOG.info("Released port " + n.getMappedPort() + " on " + host + " for " + i.getHost() + ":" + n.getPort());
        } catch (Exception e) {
        }
    }

    public void clearPortMappings() {
        String url = "http://" + host + ":" + port + "/agent/clearPortMappings";
        HttpPost httppost = new HttpPost(url);
        try {
            ArrayList<BasicNameValuePair> postParams = new ArrayList<BasicNameValuePair>();
            postParams.add(new BasicNameValuePair("host", host));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postParams,
                    "UTF-8");
            httppost.setEntity(entity);

            HttpResponse response = getClient().execute(httppost);
            HttpEntity resEntity = response.getEntity();
            EntityUtils.consume(resEntity);
            LOG.info("Cleared all port mappings on " + host);            
        } catch (Exception e) {
        }
    }

    @Override
    public String getHostName(String host) {
        StringBuilder sb = new StringBuilder();
        String url = scheme + this.host + ":" + port + "/agent/Get"
                + "?action=getHostname" + "&host=" + host;
        HttpGet httpget = new HttpGet(url);
        try {
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
        } catch (Exception e) {
        }
        return sb.toString();
    }
}