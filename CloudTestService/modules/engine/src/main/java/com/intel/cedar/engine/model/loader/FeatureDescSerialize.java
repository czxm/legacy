package com.intel.cedar.engine.model.loader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.intel.cedar.engine.model.DataModel;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.feature.FeatureDoc;
import com.intel.cedar.engine.model.feature.Import;
import com.intel.cedar.engine.model.feature.Tasklet;
import com.intel.cedar.engine.model.feature.Tasklets;
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
import com.intel.cedar.engine.xml.NamespaceConstant;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.model.DocumentImpl;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.engine.xml.util.DOMSerializer;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.ui.BindModel;
import com.intel.cedar.service.client.feature.model.ui.CheckboxgroupModel;
import com.intel.cedar.service.client.feature.model.ui.ComboItemModel;
import com.intel.cedar.service.client.feature.model.ui.ComboModel;
import com.intel.cedar.service.client.feature.model.ui.CompositeModel;
import com.intel.cedar.service.client.feature.model.ui.DependModel;
import com.intel.cedar.service.client.feature.model.ui.DependsAction;
import com.intel.cedar.service.client.feature.model.ui.DependsModel;
import com.intel.cedar.service.client.feature.model.ui.FieldSetModel;
import com.intel.cedar.service.client.feature.model.ui.FileUploadModel;
import com.intel.cedar.service.client.feature.model.ui.FormItemModel;
import com.intel.cedar.service.client.feature.model.ui.FormModel;
import com.intel.cedar.service.client.feature.model.ui.ListFieldModel;
import com.intel.cedar.service.client.feature.model.ui.LogModel;
import com.intel.cedar.service.client.feature.model.ui.RevModel;
import com.intel.cedar.service.client.feature.model.ui.SVNModel;
import com.intel.cedar.service.client.feature.model.ui.SelectModel;
import com.intel.cedar.service.client.feature.model.ui.SubmitModel;
import com.intel.cedar.service.client.feature.model.ui.TextAreaModel;
import com.intel.cedar.service.client.feature.model.ui.TextfieldModel;
import com.intel.cedar.service.client.feature.model.ui.UIBaseNode;
import com.intel.cedar.service.client.feature.model.ui.URLModel;
import com.intel.cedar.service.client.feature.model.ui.WindowModel;

public class FeatureDescSerialize extends ModelSerializer {

    public static void main(String args[]) {
        System.out.println("FeatureLoader Test begin...");
        FeatureDescLoader fload = new FeatureDescLoader();
        FeatureDescSerialize seri = new FeatureDescSerialize();
        try {
            com.intel.cedar.feature.impl.FeatureJar fl = new com.intel.cedar.feature.impl.FeatureJar(
                    new String[] { "examples\\features\\cpp.conformance.jar" });

            FeatureDoc doc = fload.load("C:\\workspace\\cloud\\feature.xml");
            // FeatureDoc doc = fload.load(fl.getFeatureStream());

            StringWriter writer = new StringWriter();
            seri.serialize(doc, writer);

            // InputStream stream=new
            // ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
            File outFile = new File("C:\\workspace\\cloud\\feature-out-1.xml");
            RandomAccessFile outputFile = new RandomAccessFile(outFile, "rw");
            outputFile.writeBytes(writer.toString());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("FeatureLoader Test end...");
    }

    public void serialize(FeatureDoc featureDoc, Writer writer)
            throws IOException {
        initialize();

        serialize(featureDoc, document);

        // write to the writer
        DOMSerializer serializer = new DOMSerializer();
        serializer.getSerializeOptions().setForceLongForm(false);
        serializer.serialize(document, writer);
    }

    protected void serialize(FeatureDoc featureDoc, DocumentImpl doc) {
        Element featureNode = createElement(doc, StandardNames.CEDAR_FEATURE);
        serialize(featureDoc.getFeature(), featureNode);
    }

    protected void serialize(Feature featureData, Element node) {

        // xmlns
        createAttribute(node, "", NamespaceConstant.XML, "xmlns",
                NamespaceConstant.CEDAR);

        // name
        createAttribute(node, StandardNames.CEDAR_NAME, featureData.getName());

        // version
        createAttribute(node, StandardNames.CEDAR_VERSION, featureData
                .getVersion());

        // import element
        Element importNode = createElement(node, StandardNames.CEDAR_IMPORT);
        serialize(featureData.getImport(), importNode);

        // variables element
        Element variablesNode = createElement(node,
                StandardNames.CEDAR_VARIABLES);
        serialize(featureData.getVariablesData(), variablesNode);

        // tasklets element
        Element taskletsNode = createElement(node, StandardNames.CEDAR_TASKLETS);
        serialize(featureData.getTasklets(), taskletsNode);

        // ui element
        Element uiNode = createElement(node, StandardNames.CEDAR_FEATUREUI);
        serialize(featureData.getFeatureUI().getFeatureModel().getChildren(),
                uiNode);

        // flow element
        Element flowNode = createElement(node, StandardNames.CEDAR_FLOW);
        serialize(featureData.getFeatureFlow(), flowNode);
    }

    protected void serialize(Import importData, Element node) {
        List<Variable> vs = importData.getVariables();
        for (int i = 0; i < vs.size(); i++) {
            Variable v = vs.get(i);
            Element e = createElement(node, StandardNames.CEDAR_VARIABLE);
            createAttribute(e, StandardNames.CEDAR_NAME, v.getName());
        }

        List<Tasklet> ts = importData.getTasklets();
        for (int i = 0; i < ts.size(); i++) {
            Tasklet t = ts.get(i);
            Element e = createElement(node, StandardNames.CEDAR_TASKLET);
            createAttribute(e, StandardNames.CEDAR_ID, t.getID());
        }
    }

    protected void serialize(Variables vsData, Element node) {
        List<Variable> childs = vsData.getVariables();
        for (int i = 0; i < childs.size(); i++) {
            Element e = createElement(node, StandardNames.CEDAR_VARIABLE);
            Variable child = childs.get(i);
            serialize(child, e);
        }
    }

    protected void serialize(Tasklets tsData, Element node) {
        List<Tasklet> childs = tsData.getModelChildren();
        for (int i = 0; i < childs.size(); i++) {
            Tasklet t = childs.get(i);
            Element e = createElement(node, StandardNames.CEDAR_TASKLET);
            serialize(t, e);
        }
    }

    protected void serialize(List<UIBaseNode> childs, Element node) {
        for (int i = 0; i < childs.size(); i++) {
            UIBaseNode child = childs.get(i);
            if (child instanceof WindowModel) {
                Element e = createElement(node, StandardNames.CEDAR_WINDOW);
                serialize((WindowModel) child, e);
            } else if (child instanceof FormModel) {
                Element e = createElement(node, StandardNames.CEDAR_FORM);
                serialize((FormModel) child, e);
            } else if (child instanceof FieldSetModel) {
                Element e = createElement(node, StandardNames.CEDAR_FIELDSET);
                serialize((FieldSetModel) child, e);
            } else if (child instanceof CheckboxgroupModel) {
                Element e = createElement(node,
                        StandardNames.CEDAR_CHECKBOXGROUP);
                serialize((CheckboxgroupModel) child, e);
            } else if (child instanceof ComboModel) {
                Element e = createElement(node, StandardNames.CEDAR_COMBO);
                serialize((ComboModel) child, e);
            } else if (child instanceof ListFieldModel) {
                Element e = createElement(node, StandardNames.CEDAR_LISTFIELD);
                serialize((ListFieldModel) child, e);
            } else if (child instanceof CompositeModel) {
                Element e = createElement(node, StandardNames.CEDAR_COMPOSITE);
                serialize((CompositeModel) child, e);
            } else if (child instanceof SelectModel) {
                Element e = createElement(node, StandardNames.CEDAR_SELECT);
                serialize((SelectModel) child, e);
            } else if (child instanceof ComboItemModel) {
                Element e = createElement(node, StandardNames.CEDAR_COMBOITEM);
                serialize((ComboItemModel) child, e);
            } else if (child instanceof FileUploadModel) {
                Element e = createElement(node,
                        StandardNames.CEDAR_FILEUPLOADFIELD);
                serialize((FileUploadModel) child, e);
            } else if (child instanceof TextAreaModel) {
                Element e = createElement(node, StandardNames.CEDAR_TEXTAREA);
                serialize((TextAreaModel) child, e);
            } else if (child instanceof TextfieldModel) {
                Element e = createElement(node, StandardNames.CEDAR_TEXTFIELD);
                serialize((TextfieldModel) child, e);
            } else if (child instanceof SVNModel) {
                Element e = createElement(node, StandardNames.CEDAR_SVN);
                serialize((SVNModel) child, e);
            } else if (child instanceof RevModel) {
                Element e = createElement(node, StandardNames.CEDAR_URL);
                serialize((RevModel) child, e);
            } else if (child instanceof URLModel) {
                Element e = createElement(node, StandardNames.CEDAR_REV);
                serialize((URLModel) child, e);
            } else if (child instanceof LogModel) {
                Element e = createElement(node, StandardNames.CEDAR_LOG);
                serialize((LogModel) child, e);
            } else if (child instanceof SubmitModel) {
                Element e = createElement(node, StandardNames.CEDAR_SUBMIT);
                serialize((SubmitModel) child, e);
            } else if (child instanceof FormItemModel) {
                Element e = createElement(node, StandardNames.CEDAR_FORMITEM);
                serialize((FormItemModel) child, e);
            }
        }
    }

    protected void serialize(FeatureFlow flowData, Element node) {
        serialize(flowData.getTasklets(), node);
    }

    protected void serialize(Variable vData, Element node) {
        // name
        createAttribute(node, StandardNames.CEDAR_NAME, vData.getName());

        Element valusElement = createElement(node, StandardNames.CEDAR_VALUES);

        List<String> valuesChild = vData.getValues();
        for (int i = 0; i < valuesChild.size(); i++) {
            String v = valuesChild.get(i);
            Element e = createElement(valusElement, StandardNames.CEDAR_VALUE);
            createTextNode(e, v);
        }
    }

    protected void serialize(Tasklet t, Element node) {
        // public
        createAttribute(node, StandardNames.CEDAR_PUBLIC, booleanToString(t
                .getIsPublic()));

        // sharable
        createAttribute(node, StandardNames.CEDAR_SHARABLE, t.getSharable()
                .name());

        // id
        createAttribute(node, StandardNames.CEDAR_ID, t.getID());

        // description element
        Element desElement = createElement(node,
                StandardNames.CEDAR_DECLARATION);
        createTextNode(desElement, t.getDescription());

        // contributer
        Element contrElement = createElement(node,
                StandardNames.CEDAR_CONTRIBUTER);
        createTextNode(contrElement, t.getContributer());

        // provider
        Element providerElement = createElement(node,
                StandardNames.CEDAR_PROVIDER);
        createTextNode(providerElement, t.getProvider());
    }

    protected void serialize(WindowModel w, Element node) {
        serializeCommon((UIBaseNode) w, node);

        String title = w.getTitle();
        serializeAttr(title, StandardNames.CEDAR_TITLE, node);

        serialize(w.getChildren(), node);
    }

    protected void serialize(FormModel f, Element node) {
        serializeCommon((UIBaseNode) f, node);
        serialize(f.getChildren(), node);
    }

    protected void serialize(FieldSetModel fs, Element node) {
        serializeCommon((UIBaseNode) fs, node);
        serialize(fs.getChildren(), node);
    }

    protected void serialize(CheckboxgroupModel check, Element node) {
        serializeCommon((UIBaseNode) check, node);

        BindModel bind = check.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }
    }

    protected void serialize(ComboModel combo, Element node) {
        serializeCommon((UIBaseNode) combo, node);

        BindModel bind = combo.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }

        serialize(combo.getChildren(), node);
    }

    protected void serialize(ListFieldModel lf, Element node) {
        serializeCommon((UIBaseNode) lf, node);

        BindModel bind = lf.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }
    }

    protected void serialize(CompositeModel cp, Element node) {
        serializeCommon((UIBaseNode) cp, node);

        serialize(cp.getChildren(), node);
    }

    protected void serialize(SelectModel select, Element node) {
        serializeCommon((UIBaseNode) select, node);
        serialize(select.getChildren(), node);
    }

    protected void serialize(ComboItemModel item, Element node) {
        String value = item.getValue();
        serializeAttr(value, StandardNames.CEDAR_VALUE, node);
    }

    protected void serialize(FileUploadModel fup, Element node) {
        serializeCommon((UIBaseNode) fup, node);

    }

    protected void serialize(TextAreaModel ta, Element node) {
        serializeCommon((UIBaseNode) ta, node);
    }

    protected void serialize(TextfieldModel tf, Element node) {
        serializeCommon((UIBaseNode) tf, node);

        BindModel bind = tf.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }
    }

    protected void serialize(SVNModel sm, Element node) {
        serializeCommon((UIBaseNode) sm, node);

        serialize(sm.getChildren(), node);
    }

    protected void serialize(RevModel rm, Element node) {
        serializeCommon((UIBaseNode) rm, node);

        BindModel bind = rm.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }
    }

    protected void serialize(URLModel um, Element node) {
        serializeCommon((UIBaseNode) um, node);

        BindModel bind = um.getBind();
        if (bind != null) {
            serializeAttr(bind.getName(), StandardNames.CEDAR_BIND, node);
        }
    }

    protected void serialize(LogModel lm, Element node) {
        serializeCommon((UIBaseNode) lm, node);
    }

    protected void serialize(SubmitModel sm, Element node) {
        serializeCommon((UIBaseNode) sm, node);
        serialize(sm.getChildren(), node);
    }

    protected void serialize(FormItemModel fim, Element node) {
        serializeCommon((UIBaseNode) fim, node);
    }

    protected void serializeCommon(UIBaseNode node, Element element) {
        String id = node.getID();
        serializeAttr(id, StandardNames.CEDAR_ID, element);

        String label = node.getLabel();
        serializeAttr(label, StandardNames.CEDAR_LABEL, element);

        String showOnSelect = node.getShowOnSelect();
        serializeAttr(showOnSelect, StandardNames.CEDAR_SHOWONSELECT, element);

        DependsModel dm = node.getDependsModel();
        if (dm.getDepends().size() == 0) {
            return;
        }
        serializeDepends(node.getDependsModel(), element);
    }

    protected void serializeAttr(String value, int finger, Element element) {
        if (value == null) {
            return;
        }

        createAttribute(element, finger, value);
    }

    protected void serializeDepends(DependsModel models, Element element) {
        Element dependsElement = createElement(element,
                StandardNames.CEDAR_DEPENDS);
        List<DependModel> childs = models.getDepends();
        for (int i = 0; i < childs.size(); i++) {
            DependModel dm = childs.get(i);
            Element subElement = createElement(dependsElement,
                    StandardNames.CEDAR_DEPEND);
            // ref id
            String id = dm.getDependID();
            createAttribute(subElement, StandardNames.CEDAR_REFID, id);

            // action
            DependsAction action = dm.getAction();
            if (action != null) {
                createAttribute(subElement, StandardNames.CEDAR_ACTION, action
                        .getName());
            }
        }
    }

    protected void serialize(TaskletsFlow tsf, Element node) {
        Element subNode = null;
        if (tsf instanceof SequenceTasklets) {
            subNode = createElement(node, StandardNames.CEDAR_SEQUENCE);
        } else if (tsf instanceof ParallelTasklets) {
            subNode = createElement(node, StandardNames.CEDAR_PARALLEL);
            ParallelTasklets pt = (ParallelTasklets) tsf;
            if (pt.getLevel() != null)
                createAttribute(node, StandardNames.CEDAR_LEVEL, pt.getLevel());
        }

        if (subNode == null) {
            return;
        }

        List<DataModel> childs = tsf.getChilds();
        for (int i = 0; i < childs.size(); i++) {
            DataModel child = childs.get(i);
            if (child instanceof TaskletFlow) {
                Element e = createElement(subNode, StandardNames.CEDAR_TASKLET);
                serialize((TaskletFlow) child, e);
            } else if (child instanceof Machine) {
                Element e = createElement(subNode, StandardNames.CEDAR_MACHINE);
                serialize((Machine) child, e);
            } else if (child instanceof TaskletsFlow) {
                serialize((TaskletsFlow) child, subNode);
            }
        }
    }

    protected void serialize(TaskletFlow tf, Element node) {
        // id
        createAttribute(node, StandardNames.CEDAR_ID, tf.getID());
        // name
        createAttribute(node, StandardNames.CEDAR_NAME, tf.getName());
        // timeout
        createAttribute(node, StandardNames.CEDAR_TIMEOUT, tf.getTimeout());
        // onFail
        createAttribute(node, StandardNames.CEDAR_ONFAIL, tf.getOnFail());

        // machine
        if (tf.getMachine() != null) {
            Element machineElement = createElement(node,
                    StandardNames.CEDAR_MACHINE);
            serialize(tf.getMachine(), machineElement);
        }

        // cases
        Element itemsElement = createElement(node, StandardNames.CEDAR_ITEMS);
        serialize(tf.getItems(), itemsElement);
    }

    protected void serialize(Machine mc, Element node) {
        // properties
        Element psElement = createElement(node, StandardNames.CEDAR_PROPERTIES);
        serialize(mc.getProperties(), psElement);

        // arch
        Element archElement = createElement(node, StandardNames.CEDAR_ARCH);
        archElement.setNodeValue(mc.getARCH());

        // os
        Element osElement = createElement(node, StandardNames.CEDAR_OS);
        osElement.setNodeValue(mc.getOS());

        // host
        Element hostElement = createElement(node, StandardNames.CEDAR_HOST);
        hostElement.setNodeValue(mc.getHost());

        // count
        Element countElement = createElement(node, StandardNames.CEDAR_COUNT);
        serialize(mc.getCount(), countElement);

        // cpu
        Element cpuElement = createElement(node, StandardNames.CEDAR_CPU);
        serialize(mc.getCPU(), cpuElement);

        // memory
        Element menElement = createElement(node, StandardNames.CEDAR_MEM);
        serialize(mc.getMemory(), menElement);

        // disk
        Element diskElement = createElement(node, StandardNames.CEDAR_DISK);
        serialize(mc.getDisk(), diskElement);
    }

    protected void serialize(Properties ps, Element node) {
        Iterator<Object> keyIter = ps.keySet().iterator();
        while (keyIter.hasNext()) {
            String key = (String) keyIter.next();
            String value = (String) ps.get(key);
            Element e = createElement(node, StandardNames.CEDAR_PROPERTY);
            createAttribute(e, StandardNames.CEDAR_NAME, key);
            createAttribute(e, StandardNames.CEDAR_VALUE, value);
        }
    }

    protected void serialize(MachineParameter mp, Element node) {
        // min
        createAttribute(node, StandardNames.CEDAR_MIN, integerToString(mp
                .getMin()));

        // max
        createAttribute(node, StandardNames.CEDAR_MAX, integerToString(mp
                .getMax()));
    }

    protected void serialize(Items cs, Element node) {
        // provider
        if (cs.getProvider() != null) {
            createAttribute(node, StandardNames.CEDAR_PROVIDER, cs
                    .getProvider());
        }

        List<Item> childs = cs.getModelChildren();
        for (int i = 0; i < childs.size(); i++) {
            Item c = childs.get(i);
            Element e = createElement(node, StandardNames.CEDAR_ITEMS);
            serialize(c, e);
        }
    }

    protected void serialize(Item c, Element node) {
        if (c.getProvider() != null) {
            createAttribute(node, StandardNames.CEDAR_PROVIDER, c.getProvider());
        }
    }
}
