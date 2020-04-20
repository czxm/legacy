package com.intel.cedar.engine.model.loader;

import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.StandardNames;
import com.intel.cedar.engine.xml.iterator.Axis;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.iterator.NameTest;
import com.intel.cedar.engine.xml.iterator.NodeKindTest;
import com.intel.cedar.engine.xml.model.Element;
import com.intel.cedar.engine.xml.model.ElementImpl;
import com.intel.cedar.engine.xml.model.Node;
import com.intel.cedar.engine.xml.model.TextImpl;

public abstract class ModelLoader {

    public static Element getElement(Element node, int fingerprint,
            NamePool namePool) {
        AxisIterator scheduleIter = node.iterateAxis(Axis.CHILD, new NameTest(
                Node.ELEMENT, fingerprint, namePool));
        return (Element) scheduleIter.next();
    }

    public static Element getFirstElement(Element node, NamePool namePool) {
        AxisIterator scheduleIter = node.iterateAxis(Axis.CHILD,
                NodeKindTest.ELEMENT);

        return (Element) scheduleIter.next();
    }

    public static AxisIterator getElements(Element node, int fingerprint,
            NamePool namePool) {
        AxisIterator scheduleIter = node.iterateAxis(Axis.CHILD, new NameTest(
                Node.ELEMENT, fingerprint, namePool));
        return scheduleIter;
    }

    public static AxisIterator getElements(Element node, NamePool namePool) {
        AxisIterator scheduleIter = node.iterateAxis(Axis.CHILD,
                NodeKindTest.ELEMENT);
        return scheduleIter;
    }

    public static String getAttributeValue(Element node, int fingerprint,
            NamePool namePool) {
        int fp = namePool.allocateClarkName(StandardNames
                .getLocalName(fingerprint));
        return node.getAttributeValue(fp);
    }

    public static String getAttributeValue(Element node, String attr,
            NamePool namePool) {
        int fp = namePool.allocateClarkName(attr);
        return node.getAttributeValue(fp);
    }

    public static boolean stringToBoolean(String str) {
        return Boolean.parseBoolean(str);
    }

    public static int stringToInteger(String str) {
        return Integer.parseInt(str);
    }

    public static String getTextContent(Node node) {
        if (node instanceof ElementImpl) {
            for (Node child = node.getFirstChild(); null != child; child = child
                    .getNextSibling()) {
                String text = getTextContent(child);
                if (!text.equals(""))
                    return text;
            }
        } else if (node instanceof TextImpl) {
            return node.getNodeValue();
        }
        return "";
    }
}
