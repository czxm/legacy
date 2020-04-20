package com.intel.cedar.engine.model.loader;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.engine.model.DataModelException;
import com.intel.cedar.engine.model.IDataModelDocument;
import com.intel.cedar.engine.model.feature.CronTrigger;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.FeatureDoc;
import com.intel.cedar.engine.model.feature.FeatureUI;
import com.intel.cedar.engine.model.feature.GitTrigger;
import com.intel.cedar.engine.model.feature.IVarValueParser;
import com.intel.cedar.engine.model.feature.IVarValueProvider;
import com.intel.cedar.engine.model.feature.Import;
import com.intel.cedar.engine.model.feature.Launch;
import com.intel.cedar.engine.model.feature.LaunchSet;
import com.intel.cedar.engine.model.feature.Launches;
import com.intel.cedar.engine.model.feature.Option;
import com.intel.cedar.engine.model.feature.Shortcut;
import com.intel.cedar.engine.model.feature.Shortcuts;
import com.intel.cedar.engine.model.feature.SvnTrigger;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.engine.model.feature.Tasklets;
import com.intel.cedar.engine.model.feature.Trigger;
import com.intel.cedar.engine.model.feature.Triggers;
import com.intel.cedar.engine.model.feature.Variables;
import com.intel.cedar.engine.model.feature.flow.FeatureFlow;
import com.intel.cedar.engine.model.feature.flow.Item;
import com.intel.cedar.engine.model.feature.flow.Items;
import com.intel.cedar.engine.model.feature.flow.Machine;
import com.intel.cedar.engine.model.feature.flow.MachineParameter;
import com.intel.cedar.engine.model.feature.flow.ParallelTasklets;
import com.intel.cedar.engine.model.feature.flow.SequenceTasklets;
import com.intel.cedar.engine.model.feature.flow.TaskletFlow;
import com.intel.cedar.engine.model.feature.flow.TaskletsFlow;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.loader.DocumentLoader;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.feature.impl.FeatureJar;
import com.intel.cedar.feature.util.JarUtility;
import com.intel.cedar.service.client.feature.model.VarValue;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.ui.BranchModel;
import com.intel.cedar.service.client.feature.model.ui.CheckboxgroupModel;
import com.intel.cedar.service.client.feature.model.ui.ComboItemModel;
import com.intel.cedar.service.client.feature.model.ui.ComboModel;
import com.intel.cedar.service.client.feature.model.ui.CompositeModel;
import com.intel.cedar.service.client.feature.model.ui.DependModel;
import com.intel.cedar.service.client.feature.model.ui.DependsModel;
import com.intel.cedar.service.client.feature.model.ui.FieldSetModel;
import com.intel.cedar.service.client.feature.model.ui.FileUploadModel;
import com.intel.cedar.service.client.feature.model.ui.FormItemModel;
import com.intel.cedar.service.client.feature.model.ui.FormModel;
import com.intel.cedar.service.client.feature.model.ui.GitModel;
import com.intel.cedar.service.client.feature.model.ui.ListFieldModel;
import com.intel.cedar.service.client.feature.model.ui.LogModel;
import com.intel.cedar.service.client.feature.model.ui.RevModel;
import com.intel.cedar.service.client.feature.model.ui.SVNModel;
import com.intel.cedar.service.client.feature.model.ui.SelectModel;
import com.intel.cedar.service.client.feature.model.ui.SubmitModel;
import com.intel.cedar.service.client.feature.model.ui.TextAreaModel;
import com.intel.cedar.service.client.feature.model.ui.TextfieldModel;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNode;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNodes;
import com.intel.cedar.service.client.feature.model.ui.URLModel;
import com.intel.cedar.service.client.feature.model.ui.WindowModel;

public class FeatureDescLoader extends ModelLoader {
    protected IDataModelDocument document;
    protected NamePool namePool;
    protected ClassLoader classLoader;
    protected FeatureJar featureJar;

    private static Logger LOG = LoggerFactory
            .getLogger(FeatureDescLoader.class);

    public static void main(String args[]) {
        System.out.println("FeatureLoader Test begin...");
        FeatureDescLoader fload = new FeatureDescLoader();
        try {
            fload
                    .load(FeatureDescLoader.class
                            .getResourceAsStream("/examples/features/cpp.conformance/feature.xml"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("FeatureLoader Test end...");
    }

    public FeatureDoc load(String file) throws DataModelException {
        DocumentLoader loader = new DocumentLoader();
        DocumentImpl doc = loader.load(file);
        return load(doc);
    }

    public FeatureDoc load(InputStream inputStream) throws DataModelException {
        DocumentLoader loader = new DocumentLoader();
        DocumentImpl doc = loader.load(inputStream);
        return load(doc);
    }

    public FeatureFlow loadFeatureFlow(InputStream inputStream)
            throws DataModelException {
        DocumentLoader loader = new DocumentLoader();
        DocumentImpl doc = loader.load(inputStream);
        FeatureDoc featureDoc = new FeatureDoc();
        Element featureNode = doc.getDocumentElement();

        // store the document for later use
        document = featureDoc;
        namePool = featureNode.getNamePool();

        FeatureFlow flow = featureDoc.getFeature().getFeatureFlow();
        Element flowNode = getElement(featureNode, StandardNames.CEDAR_FLOW,
                namePool);
        if (flowNode == null) {
            throw new DataModelException("Flow is not found in feature.");
        }
        load(flowNode, flow);
        return flow;
    }

    public LaunchSet loadLaunchSet(InputStream inputStream)
            throws DataModelException {
        DocumentLoader loader = new DocumentLoader();
        DocumentImpl doc = loader.load(inputStream);
        FeatureDoc featureDoc = new FeatureDoc();
        Element featureNode = doc.getDocumentElement();

        // store the document for later use
        document = featureDoc;
        namePool = featureNode.getNamePool();

        Element launchsetNode = getElement(featureNode,
                StandardNames.CEDAR_LAUNCHSET, namePool);
        if (launchsetNode == null) {
            throw new DataModelException("can't found LaunchSet");
        }
        LaunchSet ls = new LaunchSet(document);
        load(launchsetNode, ls);
        return ls;
    }

    public FeatureDoc load(DocumentImpl doc) throws DataModelException {
        if (doc == null || doc.getDocumentElement() == null)
            throw new DataModelException("Unexpected error in document.");

        FeatureDoc featureDoc = new FeatureDoc();
        Element featureNode = doc.getDocumentElement();

        // store the document for later use
        document = featureDoc;
        namePool = featureNode.getNamePool();

        load(featureNode, featureDoc.getFeature());

        // call post load of the the document
        featureDoc.onLoaded();

        return featureDoc;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setFeatureJar(FeatureJar featureJar) {
        this.featureJar = featureJar;
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
            ;
        }
        return classLoader;
    }

    protected void load(Element featureNode, Feature featureData)
            throws DataModelException {
        String name = getAttributeValue(featureNode, StandardNames.CEDAR_NAME,
                namePool);
        if (name != null) {
            featureData.setName(name);
        }

        String version = getAttributeValue(featureNode,
                StandardNames.CEDAR_VERSION, namePool);
        if (version != null) {
            featureData.setVersion(version);
        }

        String provider = getAttributeValue(featureNode,
                StandardNames.CEDAR_PROVIDER, namePool);
        if (provider != null) {
            featureData.setProvider(provider);
        }

        Element importNode = getElement(featureNode,
                StandardNames.CEDAR_IMPORT, namePool);
        if (importNode != null) {
            load(importNode, featureData.getImport());
        }

        Element variablesNode = getElement(featureNode,
                StandardNames.CEDAR_VARIABLES, namePool);
        if (variablesNode != null) {
            load(variablesNode, featureData.getVariablesData());
        }

        Element taskletsNode = getElement(featureNode,
                StandardNames.CEDAR_TASKLETS, namePool);
        if (taskletsNode == null) {
            throw new DataModelException("Tasklets is not found in feature.");
        }

        load(taskletsNode, featureData.getTasklets());

        Element uiNode = getElement(featureNode, StandardNames.CEDAR_FEATUREUI,
                namePool);
        if (uiNode == null) {
            throw new DataModelException("UI is not found in feature.");
        }

        load(uiNode, featureData.getFeatureUI());

        Element flowNode = getElement(featureNode, StandardNames.CEDAR_FLOW,
                namePool);
        if (flowNode == null) {
            throw new DataModelException("Flow is not found in feature.");
        }

        load(flowNode, featureData.getFeatureFlow());

        Element shortcutsNode = getElement(featureNode,
                StandardNames.CEDAR_SHORTCUTS, namePool);
        if (shortcutsNode != null) {
            load(shortcutsNode, featureData.getShortcuts());
        }

        Element launchesNode = getElement(featureNode,
                StandardNames.CEDAR_LAUNCHES, namePool);
        if (launchesNode != null) {
            load(launchesNode, featureData.getLaunches());
        }

        Element triggersNode = getElement(featureNode,
                StandardNames.CEDAR_TRIGGERS, namePool);
        if (triggersNode != null) {
            load(triggersNode, featureData.getTriggers());
        }
    }

    protected void load(Element node, Shortcut shortcut)
            throws DataModelException {
        String name = getAttributeValue(node, StandardNames.CEDAR_NAME,
                namePool);
        if (name != null) {
            shortcut.setName(name);
        }

        String desc = getAttributeValue(node, StandardNames.CEDAR_DECLARATION,
                namePool);
        if (desc != null) {
            shortcut.setDesc(desc);
        }

        String enable = getAttributeValue(node, "enable", namePool);
        if (enable != null) {
            shortcut.setEnabled(Boolean.parseBoolean(enable));
        }

        Element window = getElement(node, StandardNames.CEDAR_WINDOW, namePool);
        if (window != null) {
            load(window, shortcut.getWindow());
        }

        AxisIterator matchIter = getElements(node, StandardNames.CEDAR_LAUNCH,
                namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            Launch t = new Launch(document);
            shortcut.buildChild(t);

            load(matchNode, t);
        }
    }

    protected void load(Element node, Shortcuts shortcuts)
            throws DataModelException {
        AxisIterator matchIter = getElements(node,
                StandardNames.CEDAR_SHORTCUT, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            Shortcut t = new Shortcut(document);
            shortcuts.buildChild(t);

            load(matchNode, t);
        }
    }

    protected void load(Element node, Option option) throws DataModelException {
        AxisIterator matchIter = getElements(node, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            if (matchNode.getLocalPart().equals("sendReport"))
                option.setSendReport(Boolean
                        .parseBoolean(getTextContent(matchNode)));
            else if (matchNode.getLocalPart().equals("reproducable"))
                option.setReproducable(Boolean
                        .parseBoolean(getTextContent(matchNode)));
            else if (matchNode.getLocalPart().equals("user"))
                option.setUser(getTextContent(matchNode));
            else if (matchNode.getLocalPart().equals("comment"))
                option.setComment(getTextContent(matchNode));
            else if (matchNode.getLocalPart().equals("receivers"))
                option.setReceivers(getTextContent(matchNode));
            else if (matchNode.getLocalPart().equals("failure_receivers"))
                option.setFailureReceivers(getTextContent(matchNode));
        }
    }

    protected void load(Element node, Launch launch) throws DataModelException {
        Element variables = getElement(node, StandardNames.CEDAR_VARIABLES,
                namePool);
        if (variables != null) {
            load(variables, launch.getVariables());
        }
    }

    protected void load(Element node, LaunchSet launchset)
            throws DataModelException {
        String name = getAttributeValue(node, StandardNames.CEDAR_NAME,
                namePool);
        if (name != null) {
            launchset.setName(name);
        }

        String desc = getAttributeValue(node, StandardNames.CEDAR_DECLARATION,
                namePool);
        if (desc != null) {
            launchset.setDesc(desc);
        }

        String enable = getAttributeValue(node, "enable", namePool);
        if (enable != null) {
            launchset.setEnabled(Boolean.parseBoolean(enable));
        }

        Element option = getElement(node, StandardNames.CEDAR_OPTION, namePool);
        if (option != null) {
            load(option, launchset.getOption());
        }

        AxisIterator matchIter = getElements(node, StandardNames.CEDAR_LAUNCH,
                namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            Launch t = new Launch(document);
            launchset.buildChild(t);

            load(matchNode, t);
        }
    }

    protected void load(Element node, Launches launches)
            throws DataModelException {
        AxisIterator matchIter = getElements(node,
                StandardNames.CEDAR_LAUNCHSET, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            LaunchSet t = new LaunchSet(document);
            launches.buildChild(t);

            load(matchNode, t);
        }
    }

    protected void load(Element node, Trigger trigger)
            throws DataModelException {
        if (trigger instanceof CronTrigger) {
            ((CronTrigger) trigger).setCron(getTextContent(node));
        } else if (trigger instanceof SvnTrigger) {
            SvnTrigger st = (SvnTrigger) trigger;
            AxisIterator matchIter = getElements(node, namePool);
            while (true) {
                Element matchNode = (Element) matchIter.next();
                if (matchNode == null)
                    break;

                if (matchNode.getLocalPart().equals("url")) {
                    st.setUrl(getTextContent(matchNode));
                    st.setUrl_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("rev")) {
                    st.setRev(getTextContent(matchNode));
                    st.setRev_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("user")) {
                    st.setUser(getTextContent(matchNode));
                    st.setUser_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("password")) {
                    st.setPassword(getTextContent(matchNode));
                    st.setPassword_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("repository")) {
                    st.setRepoName(getTextContent(matchNode));
                    st.setRepo_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("interval")) {
                    st.setInterval(getTextContent(matchNode));
                }
            }
        }
        else if (trigger instanceof GitTrigger) {
            GitTrigger st = (GitTrigger) trigger;
            AxisIterator matchIter = getElements(node, namePool);
            while (true) {
                Element matchNode = (Element) matchIter.next();
                if (matchNode == null)
                    break;

                if (matchNode.getLocalPart().equals("url")) {
                    st.setUrl(getTextContent(matchNode));
                    st.setUrl_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("rev")) {
                    st.setRev(getTextContent(matchNode));
                    st.setRev_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("user")) {
                    st.setUser(getTextContent(matchNode));
                    st.setUser_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("password")) {
                    st.setPassword(getTextContent(matchNode));
                    st.setPassword_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("repository")) {
                    st.setRepoName(getTextContent(matchNode));
                    st.setRepo_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));
                } else if (matchNode.getLocalPart().equals("interval")) {
                    st.setInterval(getTextContent(matchNode));
                } else if (matchNode.getLocalPart().equals("privatekey")) {
                    st.setPrivatekey(getTextContent(matchNode));
                    st.setPrivatekey_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));                                       
                } else if (matchNode.getLocalPart().equals("proxy")) {
                    st.setProxyHost(getTextContent(matchNode));
                    st.setProxyHost_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));                                       
                } else if (matchNode.getLocalPart().equals("port")) {
                    st.setProxyPort(Integer.parseInt(getTextContent(matchNode)));
                    st.setProxyPort_bind(getAttributeValue(matchNode,
                            StandardNames.CEDAR_BIND, namePool));                                       
                }
            }
        }
    }

    protected Trigger load(Element node) throws DataModelException {
        Trigger trigger = null;
        String type = getAttributeValue(node, "type", namePool);
        String clzName = "com.intel.cedar.engine.model.feature."
                + type.substring(0, 1).toUpperCase() + type.substring(1)
                + "Trigger";
        try {
            Constructor ctr = Class.forName(clzName).getConstructor(
                    IDataModelDocument.class);
            trigger = (Trigger) ctr.newInstance(document);
        } catch (Exception e) {
            throw new DataModelException(e);
        }

        String name = getAttributeValue(node, StandardNames.CEDAR_NAME,
                namePool);
        if (name != null) {
            trigger.setName(name);
        }

        String launch = getAttributeValue(node, StandardNames.CEDAR_LAUNCH,
                namePool);
        if (launch != null) {
            trigger.setLaunch(launch);
        }

        load(getFirstElement(node, namePool), trigger);
        return trigger;
    }

    protected void load(Element node, Triggers triggers)
            throws DataModelException {
        AxisIterator matchIter = getElements(node, StandardNames.CEDAR_TRIGGER,
                namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            triggers.buildChild(load(matchNode));
        }
    }

    protected void load(Element importNode, Import importData)
            throws DataModelException {
        // load the variables
        AxisIterator variablesIter = getElements(importNode,
                StandardNames.CEDAR_VARIABLE, namePool);
        while (true) {
            Element variableElement = (Element) variablesIter.next();
            if (variableElement == null) {
                break;
            }

            String name = getAttributeValue(variableElement,
                    StandardNames.CEDAR_NAME, namePool);
            if (name == null) {
                throw new DataModelException(
                        "The name filed in the variable is empty.");
            }

            importData.addVariable(name);
        }

        AxisIterator taskletsIter = getElements(importNode,
                StandardNames.CEDAR_TASKLET, namePool);
        while (true) {
            Element taskletElement = (Element) taskletsIter.next();
            if (taskletElement == null) {
                break;
            }

            String id = getAttributeValue(taskletElement,
                    StandardNames.CEDAR_ID, namePool);
            if (id == null) {
                throw new DataModelException(
                        "The id filed in the tasklet is empty.");
            }

            importData.addTasklet(id);
        }
    }

    protected void load(Element variablesNode, Variables variablesData)
            throws DataModelException {

        AxisIterator matchIter = getElements(variablesNode,
                StandardNames.CEDAR_VARIABLE, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            Variable v = new Variable();
            variablesData.addVariable(v);

            load(matchNode, v);
        }
    }

    protected void load(Element taskletsNode, Tasklets taskletsData)
            throws DataModelException {
        AxisIterator matchIter = getElements(taskletsNode,
                StandardNames.CEDAR_TASKLET, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            Tasklet t = new Tasklet(document);
            taskletsData.buildChild(t);

            load(matchNode, t);
        }
    }

    protected void load(Element uiNode, FeatureUI featureUI)
            throws DataModelException {
        load(uiNode, featureUI.getFeatureModel());
    }

    protected void load(Element flowNode, FeatureFlow flowData)
            throws DataModelException {
        Element seqTaskletElement = getElement(flowNode,
                StandardNames.CEDAR_SEQUENCE, namePool);
        if (seqTaskletElement != null) {
            SequenceTasklets sequence = new SequenceTasklets(flowData
                    .getDocument());
            flowData.setTasklets(sequence);
            load(seqTaskletElement, sequence);
            return;
        }

        Element parallTaskletElement = getElement(flowNode,
                StandardNames.CEDAR_PARALLEL, namePool);
        if (parallTaskletElement != null) {
            ParallelTasklets parallel = new ParallelTasklets(flowData
                    .getDocument());
            flowData.setTasklets(parallel);
            load(parallTaskletElement, parallel);

        }

        if (seqTaskletElement == null && parallTaskletElement == null) {
            throw new DataModelException(
                    "There is no sequence tasklets, nor the parallel takslets.");
        }
        flowData.setMachine(getDefaultMachine());
    }

    protected Machine getDefaultMachine() {
        Machine m = new Machine(document);
        m.setRecycle(true);
        m.setVisible(false);
        m.setARCH(null);
        m.setOS(null);
        m.setHost(null);
        MachineParameter p = new MachineParameter(StandardNames.CEDAR_COUNT, 1,
                1);
        m.setCount(p);
        p = new MachineParameter(StandardNames.CEDAR_CPU, 1, 1);
        m.setCPU(p);
        p = new MachineParameter(StandardNames.CEDAR_MEM, 1, 1);
        m.setMemory(p);
        p = new MachineParameter(StandardNames.CEDAR_DISK, 0, 0);
        m.setDisk(p);
        return m;
    }

    protected void load(Element variableNode, Variable var)
            throws DataModelException {
        String name = getAttributeValue(variableNode, StandardNames.CEDAR_NAME,
                namePool);
        if (name != null) {
            var.setName(name);
        }

        String provider = getAttributeValue(variableNode,
                StandardNames.CEDAR_PROVIDER, namePool);
        fillVarValues(var, provider);

        Element valuesNode = getElement(variableNode,
                StandardNames.CEDAR_VALUES, namePool);
        if (valuesNode == null) {
            return;
        }

        AxisIterator matchIter = getElements(valuesNode,
                StandardNames.CEDAR_VALUE, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            String parser = getAttributeValue(matchNode,
                    StandardNames.CEDAR_PARSER, namePool);
            String value = getTextContent(matchNode);
            if (parser == null) {
                var.addValue(value);
            } else {
                fillVarValues(var, parser, value);
            }
        }
    }

    protected void load(Element taskletNode, Tasklet taskletData)
            throws DataModelException {
        String strpublic = getAttributeValue(taskletNode,
                StandardNames.CEDAR_PUBLIC, namePool);
        boolean ispublic = stringToBoolean(strpublic);
        taskletData.setIsPublic(ispublic);

        String sharable = getAttributeValue(taskletNode,
                StandardNames.CEDAR_SHARABLE, namePool);
        Tasklet.Sharable s = Tasklet.Sharable.fromString(sharable);
        if (s == null)
            throw new DataModelException("invalid sharable attribute value");
        taskletData.setSharable(s);

        String id = getAttributeValue(taskletNode, StandardNames.CEDAR_ID,
                namePool);
        if (id == null) {
            throw new DataModelException("no id defined for the tasklet");
        }
        taskletData.setID(id);

        Element descElement = getElement(taskletNode,
                StandardNames.CEDAR_DECLARATION, namePool);
        if (descElement != null) {
            String value = getTextContent(descElement);
            taskletData.setDescription(value);
        }

        Element contributer = getElement(taskletNode,
                StandardNames.CEDAR_CONTRIBUTER, namePool);
        if (contributer != null) {
            String value = getTextContent(contributer);
            taskletData.setContributer(value);
        }

        Element providerElement = getElement(taskletNode,
                StandardNames.CEDAR_PROVIDER, namePool);
        if (providerElement == null) {
            throw new DataModelException(
                    "The provider in the takslet is empty.");
        }
        String provider = getTextContent(providerElement);
        taskletData.setProvider(provider);
    }

    protected void load(Element taskletsNode, TaskletsFlow taskletFlow)
            throws DataModelException {
        AxisIterator matchIter = getElements(taskletsNode, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            int finger = matchNode.getFingerprint();
            if (finger == StandardNames.CEDAR_SEQUENCE) {
                SequenceTasklets st = new SequenceTasklets(document);
                taskletFlow.addChild(st);
                load(matchNode, st);
            } else if (finger == StandardNames.CEDAR_PARALLEL) {
                ParallelTasklets pt = new ParallelTasklets(document);
                String level = getAttributeValue(matchNode,
                        StandardNames.CEDAR_LEVEL, namePool);
                if (level != null)
                    pt.setLevel(level);
                taskletFlow.addChild(pt);
                load(matchNode, pt);
            } else if (finger == StandardNames.CEDAR_TASKLET) {
                TaskletFlow tf = new TaskletFlow(document);
                taskletFlow.addChild(tf);
                load(matchNode, tf);
            } else if (finger == StandardNames.CEDAR_MACHINE) {
                Machine m = new Machine(document);
                taskletFlow.setMachine(m);
                load(matchNode, m);
            }
        }
    }

    protected void load(Element taskletNode, TaskletFlow taskletData)
            throws DataModelException {
        String id = getAttributeValue(taskletNode, StandardNames.CEDAR_ID,
                namePool);
        if (id == null) {
            throw new DataModelException(
                    "The id filed in the tasklet is empty.");
        }
        taskletData.setID(id);

        String name = getAttributeValue(taskletNode, StandardNames.CEDAR_NAME,
                namePool);
        if (name == null) {
            name = id;
        }
        taskletData.setName(name);

        String onFail = getAttributeValue(taskletNode,
                StandardNames.CEDAR_ONFAIL, namePool);
        if (onFail == null) {
            onFail = "exit";
        }
        taskletData.setOnFail(onFail);

        String timeout = getAttributeValue(taskletNode,
                StandardNames.CEDAR_TIMEOUT, namePool);
        if (timeout == null) {
            timeout = "0";
        }
        taskletData.setTimeout(timeout);

        boolean debug = false;
        String debugStr = getAttributeValue(taskletNode,
                StandardNames.CEDAR_DEBUG, namePool);
        if (debugStr != null && debugStr.length() > 0) {
            debug = Boolean.parseBoolean(debugStr);
        }
        taskletData.setDebug(debug);

        Element machineElement = getElement(taskletNode,
                StandardNames.CEDAR_MACHINE, namePool);
        if (machineElement != null) {
            Machine m = new Machine(document);
            load(machineElement, m);
            taskletData.setMachine(m);
        }

        Element itemsElement = getElement(taskletNode,
                StandardNames.CEDAR_ITEMS, namePool);
        if (itemsElement == null) {
            throw new DataModelException(
                    "The items node in the tasklet is empty.");
        }

        load(itemsElement, taskletData.getItems());
    }

    protected void load(Element machineNode, Machine machineData)
            throws DataModelException {
        Element propertiesElement = getElement(machineNode,
                StandardNames.CEDAR_PROPERTIES, namePool);
        if (propertiesElement != null) {
            AxisIterator matchIter = getElements(propertiesElement,
                    StandardNames.CEDAR_PROPERTY, namePool);
            while (true) {
                Element matchNode = (Element) matchIter.next();
                if (matchNode == null)
                    break;

                String name = getAttributeValue(matchNode,
                        StandardNames.CEDAR_NAME, namePool);
                String value = getAttributeValue(matchNode,
                        StandardNames.CEDAR_VALUE, namePool);
                if (name == null || value == null) {
                    throw new DataModelException("The name or value is empty.");
                }
                machineData.addProperty(name, value);
            }
        }

        MachineParameter mp = getMachineParameter(StandardNames.CEDAR_COUNT,
                machineNode);
        machineData.setCount(mp);

        mp = getMachineParameter(StandardNames.CEDAR_CPU, machineNode);
        machineData.setCPU(mp);

        mp = getMachineParameter(StandardNames.CEDAR_MEM, machineNode);
        machineData.setMemory(mp);

        mp = getMachineParameter(StandardNames.CEDAR_DISK, machineNode);
        machineData.setDisk(mp);

        Element osElement = getElement(machineNode, StandardNames.CEDAR_OS,
                namePool);
        if (osElement != null) {
            machineData.setOS(getTextContent(osElement));
        }
        Element archElement = getElement(machineNode, StandardNames.CEDAR_ARCH,
                namePool);
        if (osElement != null) {
            machineData.setARCH(getTextContent(archElement));
        }
        Element hostElement = getElement(machineNode, StandardNames.CEDAR_HOST,
                namePool);
        if (hostElement != null) {
            machineData.setHost(getTextContent(hostElement));
        }
        String recycle = getAttributeValue(machineNode, StandardNames.CEDAR_RECYCLE,
                namePool);
        machineData.setRecycle(recycle !=  null ? Boolean.parseBoolean(recycle) : true);
        String visible = getAttributeValue(machineNode, StandardNames.CEDAR_VISIBLE,
                namePool);
        machineData.setVisible(visible !=  null ? Boolean.parseBoolean(visible) : false);
    }

    protected void load(Element casesNode, Items itemsData)
            throws DataModelException {
        String provider = getAttributeValue(casesNode,
                StandardNames.CEDAR_PROVIDER, namePool);
        if (provider != null) {
            if (!provider.equals("embedded") || provider.contains("$"))
                throw new DataModelException("invalid provider value");
            itemsData.setProvider(provider);
        }

        boolean hasChild = false;
        AxisIterator matchIter = getElements(casesNode,
                StandardNames.CEDAR_ITEM, namePool);
        while (true) {
            Element matchNode = (Element) matchIter.next();
            if (matchNode == null)
                break;

            hasChild = true;
            Item it = new Item(document);
            itemsData.buildChild(it);

            load(matchNode, it);
        }
        if (!hasChild && provider == null) {
            throw new DataModelException("no items defined");
        }
    }

    protected void load(Element caseElement, Item itemData)
            throws DataModelException {
        String provider = super.getTextContent(caseElement);
        if (provider != null) {
            itemData.setProvider(provider);
        }
    }

    protected void load(Element element, UIBaseNodes parent)
            throws DataModelException {
        AxisIterator childs = getElements(element, namePool);
        while (true) {
            Element subElement = (Element) childs.next();
            if (subElement == null)
                break;

            int finger = subElement.getFingerprint();

            if (finger == StandardNames.CEDAR_WINDOW) {
                WindowModel w = new WindowModel();
                parent.addChild(w);
                load(subElement, w);
            } else if (finger == StandardNames.CEDAR_FORM) {
                FormModel form = new FormModel();
                parent.addChild(form);
                load(subElement, form);
            } else if (finger == StandardNames.CEDAR_FIELDSET) {
                FieldSetModel fieldset = new FieldSetModel();
                parent.addChild(fieldset);
                load(subElement, fieldset);
            } else if (finger == StandardNames.CEDAR_CHECKBOXGROUP) {
                CheckboxgroupModel ch = new CheckboxgroupModel();
                parent.addChild(ch);
                load(subElement, ch);
            } else if (finger == StandardNames.CEDAR_COMBO) {
                ComboModel co = new ComboModel();
                parent.addChild(co);
                load(subElement, co);
            } else if (finger == StandardNames.CEDAR_LISTFIELD) {
                ListFieldModel ul = new ListFieldModel();
                parent.addChild(ul);
                load(subElement, ul);
            } else if (finger == StandardNames.CEDAR_COMPOSITE) {
                CompositeModel cp = new CompositeModel();
                parent.addChild(cp);
                load(subElement, cp);
            } else if (finger == StandardNames.CEDAR_COMBOITEM) {
                ComboItemModel citem = new ComboItemModel();
                parent.addChild(citem);
                load(subElement, citem);
            } else if (finger == StandardNames.CEDAR_SELECT) {
                SelectModel se = new SelectModel();
                parent.addChild(se);
                load(subElement, se);
            } else if (finger == StandardNames.CEDAR_TEXTFIELD) {
                TextfieldModel tf = new TextfieldModel();
                parent.addChild(tf);
                load(subElement, tf);
            } else if (finger == StandardNames.CEDAR_TEXTAREA) {
                TextAreaModel ta = new TextAreaModel();
                parent.addChild(ta);
                load(subElement, ta);
            } else if (finger == StandardNames.CEDAR_FILEUPLOADFIELD) {
                FileUploadModel fuf = new FileUploadModel();
                parent.addChild(fuf);
                load(subElement, fuf);
            } else if (finger == StandardNames.CEDAR_SVN) {
                SVNModel o = new SVNModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_GIT) {
                GitModel o = new GitModel();
                parent.addChild(o);
                load(subElement, o);
            }  else if (finger == StandardNames.CEDAR_URL) {
                URLModel o = new URLModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_REV) {
                RevModel o = new RevModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_BRANCH) {
                BranchModel o = new BranchModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_LOG) {
                LogModel o = new LogModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_SUBMIT) {
                SubmitModel o = new SubmitModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_FORMITEM) {
                FormItemModel o = new FormItemModel();
                parent.addChild(o);
                load(subElement, o);
            } else if (finger == StandardNames.CEDAR_DEPENDS) {
                // do nothing
            } else {
                throw new DataModelException("Find the unsupported UI element.");
            }
        }
    }

    protected void load(Element element, WindowModel windata)
            throws DataModelException {
        loadID(element, windata, true, StandardNames.CEDAR_WINDOW);
        loadLabel(element, windata, false, StandardNames.CEDAR_WINDOW);
        loadDepends(element, windata, StandardNames.CEDAR_WINDOW);
        loadShowOnSelect(element, windata);

        String title = getAttributeValue(element, StandardNames.CEDAR_TITLE,
                namePool);
        if (title == null) {
            throw new DataModelException(
                    "The title field in the window is empty.");
        }
        windata.setTitle(title);

        load(element, (UIBaseNodes) windata);
    }

    protected void load(Element element, FormModel form)
            throws DataModelException {
        loadCommon(element, form, false, StandardNames.CEDAR_FORM);

        load(element, (UIBaseNodes) form);
    }

    protected void load(Element element, FieldSetModel fd)
            throws DataModelException {
        loadID(element, fd, false, StandardNames.CEDAR_FIELDSET);
        loadLabel(element, fd, true, StandardNames.CEDAR_FIELDSET);
        loadDepends(element, fd, StandardNames.CEDAR_FIELDSET);
        loadShowOnSelect(element, fd);

        load(element, (UIBaseNodes) fd);
    }

    protected void load(Element element, CheckboxgroupModel check)
            throws DataModelException {
        loadID(element, check, true, StandardNames.CEDAR_CHECKBOXGROUP);
        loadLabel(element, check, true, StandardNames.CEDAR_CHECKBOXGROUP);
        loadDepends(element, check, StandardNames.CEDAR_CHECKBOXGROUP);
        loadShowOnSelect(element, check);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException(
                    "The bind field in the checkbox group is empty.");
        }
        check.setBind(bind);
    }

    protected void load(Element element, ComboModel combo)
            throws DataModelException {
        loadID(element, combo, true, StandardNames.CEDAR_COMBO);
        loadLabel(element, combo, true, StandardNames.CEDAR_COMBO);
        loadDepends(element, combo, StandardNames.CEDAR_COMBO);
        loadShowOnSelect(element, combo);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind != null) {
            combo.setBind(bind);
        }

        // if((combo.getDepends()==null)&&(combo.getBind()==null)){
        // throw new
        // DataModelException("There is no bind nor depends field in the combo.");
        // }

        load(element, (UIBaseNodes) combo);
    }

    protected void load(Element element, ListFieldModel lf)
            throws DataModelException {
        loadID(element, lf, true, StandardNames.CEDAR_LISTFIELD);
        loadLabel(element, lf, true, StandardNames.CEDAR_LISTFIELD);
        loadDepends(element, lf, StandardNames.CEDAR_LISTFIELD);
        loadShowOnSelect(element, lf);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException(
                    "The bind field in the ListField is empty.");
        }
        lf.setBind(bind);
    }

    protected void load(Element element, CompositeModel cp)
            throws DataModelException {
        loadCommon(element, cp, false, StandardNames.CEDAR_COMPOSITE);
        load(element, (UIBaseNodes) cp);
    }

    protected void load(Element element, ComboItemModel cItem)
            throws DataModelException {
        loadCommon(element, cItem, false, StandardNames.CEDAR_COMBOITEM);
        String value = getAttributeValue(element, StandardNames.CEDAR_VALUE,
                namePool);
        if (value == null) {
            throw new DataModelException(
                    "The value field in the comboitem is empty.");
        }

        cItem.setValue(value);
    }

    protected void load(Element element, SelectModel select)
            throws DataModelException {
        loadID(element, select, false, StandardNames.CEDAR_SELECT);
        loadLabel(element, select, false, StandardNames.CEDAR_SELECT);
        loadDepends(element, select, StandardNames.CEDAR_SELECT);
        loadShowOnSelect(element, select);

        load(element, (UIBaseNodes) select);
    }

    protected void load(Element element, TextfieldModel tf)
            throws DataModelException {
        loadID(element, tf, true, StandardNames.CEDAR_TEXTFIELD);
        loadLabel(element, tf, true, StandardNames.CEDAR_TEXTFIELD);
        loadDepends(element, tf, StandardNames.CEDAR_TEXTFIELD);
        loadShowOnSelect(element, tf);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException(
                    "The default field in the textfield is empty.");
        }

        tf.setBind(bind);
    }

    protected void load(Element element, TextAreaModel ta)
            throws DataModelException {
        loadCommon(element, ta, true, StandardNames.CEDAR_TEXTAREA);
    }

    protected void load(Element element, FileUploadModel fup)
            throws DataModelException {
        loadID(element, fup, true, StandardNames.CEDAR_FILEUPLOADFIELD);
        loadLabel(element, fup, true, StandardNames.CEDAR_FILEUPLOADFIELD);
        loadDepends(element, fup, StandardNames.CEDAR_FILEUPLOADFIELD);
        loadShowOnSelect(element, fup);
        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException("The bind field in the rev is empty.");
        }
        fup.setBind(bind);
    }

    protected void load(Element element, SVNModel sm) throws DataModelException {
        loadID(element, sm, true, StandardNames.CEDAR_SVN);
        loadLabel(element, sm, true, StandardNames.CEDAR_SVN);
        loadDepends(element, sm, StandardNames.CEDAR_SVN);

        String max = getAttributeValue(element, StandardNames.CEDAR_MAX, namePool);
        if (max != null && max.length() > 0){
            try{
                sm.setMax(Integer.parseInt(max));                
            }
            catch(Exception e){
                LOG.error(e.getMessage(), e);
            }
        }
                
        load(element, (UIBaseNodes) sm);
    }
    
    protected void load(Element element, GitModel sm) throws DataModelException {
        loadID(element, sm, true, StandardNames.CEDAR_GIT);
        loadLabel(element, sm, true, StandardNames.CEDAR_GIT);
        loadDepends(element, sm, StandardNames.CEDAR_GIT);

        String max = getAttributeValue(element, StandardNames.CEDAR_MAX, namePool);
        if (max != null && max.length() > 0){
            try{
                sm.setMax(Integer.parseInt(max));                
            }
            catch(Exception e){
                LOG.error(e.getMessage(), e);
            }
        }
                
        load(element, (UIBaseNodes) sm);
    }

    protected void load(Element element, RevModel rm) throws DataModelException {
        loadID(element, rm, true, StandardNames.CEDAR_REV);
        loadLabel(element, rm, false, StandardNames.CEDAR_REV);
        loadDepends(element, rm, StandardNames.CEDAR_REV);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException("The bind field in the rev is empty.");
        }
        rm.setBind(bind);
    }
    
    protected void load(Element element, BranchModel rm) throws DataModelException {
        loadID(element, rm, true, StandardNames.CEDAR_BRANCH);
        loadLabel(element, rm, false, StandardNames.CEDAR_BRANCH);
        loadDepends(element, rm, StandardNames.CEDAR_BRANCH);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException("The bind field in the branch is empty.");
        }
        rm.setBind(bind);
    }

    protected void load(Element element, URLModel um) throws DataModelException {
        loadID(element, um, true, StandardNames.CEDAR_URL);
        loadLabel(element, um, false, StandardNames.CEDAR_URL);
        loadDepends(element, um, StandardNames.CEDAR_URL);

        String bind = getAttributeValue(element, StandardNames.CEDAR_BIND,
                namePool);
        if (bind == null) {
            throw new DataModelException("The bind field in the URL is empty.");
        }
        um.setBind(bind);
    }

    protected void load(Element element, LogModel lm) throws DataModelException {
        loadID(element, lm, true, StandardNames.CEDAR_LOG);
        loadLabel(element, lm, false, StandardNames.CEDAR_LOG);
        loadDepends(element, lm, StandardNames.CEDAR_LOG);
    }

    protected void load(Element element, SubmitModel sm)
            throws DataModelException {
        loadCommon(element, sm, false, StandardNames.CEDAR_SUBMIT);
        load(element, (UIBaseNodes) sm);
    }

    protected void load(Element element, FormItemModel fim)
            throws DataModelException {
        loadCommon(element, fim, false, StandardNames.CEDAR_FORMITEM);
    }

    protected void loadCommon(Element element, UIBaseNode node,
            boolean required, int finger) throws DataModelException {
        loadID(element, node, required, finger);
        loadLabel(element, node, required, finger);
        loadDepends(element, node, finger);
        loadShowOnSelect(element, node);
    }

    protected void loadID(Element element, UIBaseNode node, boolean required,
            int finger) throws DataModelException {
        String id = getAttributeValue(element, StandardNames.CEDAR_ID, namePool);
        if ((id == null) && required) {
            String name = StandardNames.getLocalName(finger);
            String err = "The id field in the " + name + " is empty.";
            throw new DataModelException(err);
        }

        if (id == null) {
            return;
        }

        node.setID(id);
    }

    protected void loadLabel(Element element, UIBaseNode node,
            boolean required, int finger) throws DataModelException {
        String label = getAttributeValue(element, StandardNames.CEDAR_LABEL,
                namePool);
        if ((label == null) && required) {
            String name = StandardNames.getLocalName(finger);
            String err = "The label field in the " + name + " is empty.";
            throw new DataModelException(err);
        }

        if (label == null) {
            return;
        }

        node.setLabel(label);
    }

    protected void loadDepends(Element element, UIBaseNode node, int finger)
            throws DataModelException {
        String depends = getAttributeValue(element,
                StandardNames.CEDAR_DEPENDS, namePool);

        if (depends != null) {
            node.addDepend(depends);
        }

        Element de = getElement(element, StandardNames.CEDAR_DEPENDS, namePool);
        if (de == null) {
            return;
        }
        loadDepends(de, node.getDependsModel());
    }

    protected void loadDepends(Element depends, DependsModel dependsModel)
            throws DataModelException {
        AxisIterator childs = getElements(depends, StandardNames.CEDAR_DEPEND,
                namePool);
        while (true) {
            Element depend = (Element) childs.next();
            if (depend == null)
                break;
            // ref id
            String refid = getAttributeValue(depend, StandardNames.CEDAR_REFID,
                    namePool);
            if (refid == null) {
                throw new DataModelException(
                        "The refid in the depend is empty.");
            }
            DependModel dm = dependsModel.addDepend(refid);

            // action
            String action = getAttributeValue(depend,
                    StandardNames.CEDAR_ACTION, namePool);
            if (action != null) {
                dm.setAction(action);
            }
        }
    }

    protected void loadShowOnSelect(Element element, UIBaseNode node) {
        String showOnselct = getAttributeValue(element,
                StandardNames.CEDAR_SHOWONSELECT, namePool);
        if (showOnselct == null) {
            return;
        }
        node.setShowOnSelect(showOnselct);
    }

    private MachineParameter getMachineParameter(int fingerprint,
            Element machineNode) throws DataModelException {
        Element subElement = getElement(machineNode, fingerprint, namePool);

        if (subElement == null) {
            MachineParameter mp = new MachineParameter(fingerprint);
            if (fingerprint == StandardNames.CEDAR_DISK) {
                mp.setMin(0);
                mp.setMax(0);
                mp.setValue("0");
            } else {
                mp.setMin(1);
                mp.setMax(1);
                mp.setValue("1");
            }
            return mp;
        }

        MachineParameter mp = new MachineParameter(fingerprint);

        String str_min = getAttributeValue(subElement, StandardNames.CEDAR_MIN,
                namePool);
        if (str_min == null) {
            String name = StandardNames.getLocalName(fingerprint);
            String errmsg = "The min is empty in the " + name;
            throw new DataModelException(errmsg);
        }

        String str_max = getAttributeValue(subElement, StandardNames.CEDAR_MAX,
                namePool);
        if (str_max == null) {
            String name = StandardNames.getLocalName(fingerprint);
            String errmsg = "The max is empty in the " + name;
            throw new DataModelException(errmsg);
        }

        int min = stringToInteger(str_min);
        int max = stringToInteger(str_max);
        if (min > max) {
            throw new DataModelException("The min is larger than the max.");
        }

        mp.setMin(min);
        mp.setMax(max);

        mp.setValue(getTextContent(subElement));

        return mp;
    }

    private void fillVarValues(Variable var, String provider) {
        if (provider == null) {
            return;
        }

        try {
            ClassLoader clsloader = getClassLoader();
            Class<?> cl = JarUtility.loadClass(provider, clsloader);
            IVarValueProvider varValueProvider = (IVarValueProvider) cl
                    .newInstance();
            List<VarValue> values = varValueProvider.getVarValues(featureJar);
            var.addVarValues(values);
        } catch (Exception e) {
            LOG.error("Failed to load variable values from the provider", e);
        }
    }

    private void fillVarValues(Variable var, String strParser, String value) {
        try {
            ClassLoader clsloader = getClassLoader();
            Class<?> cl = JarUtility.loadClass(strParser, clsloader);
            IVarValueParser parser = (IVarValueParser) cl.newInstance();
            var.addVarValues(parser.parse(value));
        } catch (Exception e) {
            LOG.error("Failed to parse the variable value", e);
        }
    }
}
