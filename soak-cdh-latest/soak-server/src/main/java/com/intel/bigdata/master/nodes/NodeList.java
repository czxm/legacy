package com.intel.bigdata.master.nodes;

import com.intel.soak.config.ConfigReader;
import com.intel.soak.config.ConfigUtils;
import com.intel.soak.model.Cluster;
import com.intel.soak.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.xml.bind.JAXB;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jzhu61
 * Date: 10/31/13
 * Time: 1:55 PM
 * To change this template use File | Settings | File Templates.
 */

public class NodeList {

    protected static final Logger LOG = LoggerFactory.getLogger(NodeList.class);
    private HashMap<String, NodeState> nodeStates = new HashMap();
    private static final String NODES_CONFIG_PATH = "./soak-cluster/src/main/resources/clusternodes.xml";

    public NodeList(){

    }

    public Cluster readConfig(){
        try {
            return new ConfigReader<Cluster>().load(NodeList.class.getClassLoader().getResourceAsStream("clusternodes.xml"), Cluster.class);
        }catch (Exception e) {
            LOG.error("Couldn't read configuration from file", e);
            return null;
        }
    }

    public HashMap<String, NodeState> getNodesStatus(){
        Cluster clu = readConfig();
        List<Node> nodes = clu.getNode();
        for(Node node: nodes){
            nodeStates.put(node.getHostIdentifier(), new NodeState(node.getHostIdentifier()));
        }
        return  nodeStates;
    }

}
