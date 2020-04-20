package com.intel.cedar.engine.xml.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import com.intel.cedar.engine.xml.XMLException;
import com.intel.cedar.engine.xml.iterator.Axis;
import com.intel.cedar.engine.xml.iterator.AxisIterator;
import com.intel.cedar.engine.xml.loader.DocumentLoader;
import com.intel.cedar.engine.xml.model.Attribute;
import com.intel.cedar.engine.xml.model.AttributeCollection;
import com.intel.cedar.engine.xml.model.Document;
import com.intel.cedar.engine.xml.model.Node;

public class DOMSerializer {
    protected SerializeOptions options;
    protected String versionNumber = "1.0";

    public DOMSerializer(SerializeOptions options) {
        if (options != null)
            this.options = options;
    }

    public DOMSerializer() {
        options = new SerializeOptions();
        options.setForceLongForm(true);
        options.setPretty(true);
        options.setOutputXMLHeader(true);
        options.setRootDefaultNamespace(false);
        options.setXMLEncoding("UTF-8");
        options.setLineSeparator("\n");
        options.setProcessPI(false);
    }

    public SerializeOptions getSerializeOptions() {
        return options;
    }

    public void serialize(Node node, Writer writer) throws IOException {

        if (writer instanceof StringWriter || writer instanceof BufferedWriter) {
            serialize(node, writer, options);
            writer.flush();
        } else {
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            serialize(node, bufferedWriter, options);
            bufferedWriter.flush();
        }

    }

    public void serialize(Node node, Writer writer, SerializeOptions options)
            throws IOException {

        boolean pretty = options.isPretty();
        boolean escapeEntity = options.isEscapeEntity();
        boolean processPI = options.isProcessPI();
        String lineSeparator = options.getLineSeparator();
        boolean forceLongForm = options.isForceLongForm();
        boolean isRootDefaultNamespace = options.isRootDefaultNamespace();
        String indent = options.getIndent();
        int currIndent = 0;

        Node currNode = node;

        int nodeType = 0;
        Node firstChildNode = null;
        AttributeCollection AttrNodes = null;
        String localName;
        String prefix;

        Stack namespaces = new Stack();
        NamespacePrefix nsPrefix = new NamespacePrefix(null);
        int namespaceScope = 0;

        int lineNumber = 1;

        if (options.isOutputXMLHeader()) {

            writer.write("<?xml version=\"");
            writer.write(this.versionNumber);
            writer.write("\" encoding=\"");
            writer.write(options.getXMLEncoding());
            writer.write("\"?>");
        }

        Node parentNode = node.getParentNode();
        Node nextSibling = node.getNextSibling();
        boolean open = true;
        boolean textContent = true;

        do {
            // get the node type
            nodeType = currNode.getNodeKind();

            switch (nodeType) {
            case Node.DOCUMENT: {

                if (open) {
                    firstChildNode = currNode.getFirstChild();
                    // iterate to its child
                    if (firstChildNode != null) {
                        // has one or more children
                        currNode = firstChildNode;
                        textContent = false;
                        continue;
                    }
                }

                break;
            }
            case Node.ELEMENT: {
                // element
                firstChildNode = currNode.getFirstChild();
                localName = currNode.getLocalPart();
                prefix = currNode.getPrefix();

                if (open) {
                    // element open
                    AttrNodes = currNode.getAttributes();

                    // output element open
                    if (pretty) {

                        // new line if this previous node is not text content
                        if (!textContent) {
                            writer.write(lineSeparator);
                            lineNumber++;
                            serializeIndent(writer, currIndent, indent);
                        }

                        currIndent++;
                    }

                    currNode.setLineNumber(-lineNumber);

                    // write open
                    writer.write('<');
                    if (prefix == null || prefix.equals(""))
                        writer.write(localName);
                    else {
                        writer.write(prefix);
                        writer.write(':');
                        writer.write(localName);
                    }

                    // attributes

                    if (AttrNodes != null) {
                        serializeAttributes(AttrNodes, writer);
                        for (int i = 0; i < AttrNodes.getLength(); i++)
                            AttrNodes.getAttribute(i).setLineNumber(lineNumber);
                    }

                    namespaceScope = 0;
                    AxisIterator namespaceIter = currNode
                            .iterateAxis(Axis.NAMESPACE);
                    Node nextNamespace = namespaceIter.next();
                    while (null != nextNamespace) {
                        String thePrefix = nextNamespace.getDisplayName();
                        String theURI = nextNamespace.getStringValue();
                        if (declareNamespace(thePrefix, theURI, namespaces,
                                nsPrefix)) {
                            namespaceScope++;
                            this.serializeNamespace(thePrefix, theURI, writer);
                        }
                        nextNamespace = namespaceIter.next();
                    }
                    namespaces.push(new Integer(namespaceScope));
                    if (forceLongForm || firstChildNode != null) {
                        // has children, long form
                        writer.write('>');

                        // if the first child is not text or text is empty
                        // change line

                    } else {
                        // has no child, close it
                        writer.write(" />");
                        if (pretty)
                            currIndent--;
                    }

                    // iterate to its child
                    if (firstChildNode != null) {
                        // has one or more children
                        currNode = firstChildNode;
                        textContent = false;
                        continue;
                    }
                }

                currNode.setLineNumber(-currNode.getLineNumber());
                // output element end if needed
                if (forceLongForm || firstChildNode != null) {
                    if (pretty) {
                        currIndent--;

                        // if the first child is not text or text is empty
                        // output indent
                    }

                    // output end tag
                    writer.write("</");
                    if (prefix == null || prefix.equals(""))
                        writer.write(localName);
                    else {
                        writer.write(prefix);
                        writer.write(':');
                        writer.write(localName);
                    }
                    writer.write(">");
                }
                // pop up namespace scope
                namespaceScope = ((Integer) namespaces.pop()).intValue();
                for (int i = 0; i < namespaceScope; i++) {
                    namespaces.pop();
                }
                textContent = false;
                break;
            }
            case Node.TEXT: {
                // text
                String text = currNode.getNodeValue();
                if (escapeEntity)
                    lineNumber += serializeEscapedText(currNode.getNodeValue(),
                            writer);
                else
                    lineNumber += serializeText(currNode.getNodeValue(), writer);
                textContent = true;
                break;
            }

            case Node.PROCESSING_INSTRUCTION: {
                // processing instruction

                // new line if this previous node is not text content
                if (pretty && !textContent) {
                    writer.write(lineSeparator);
                    lineNumber++;
                }

                // output processing instruction
                localName = currNode.getLocalPart();
                if (processPI) {
                    if (localName
                            .equals("javax.xml.transform.disable-output-escaping")) {
                        escapeEntity = false;
                        break;
                    } else if (localName
                            .equals("javax.xml.transform.enable-output-escaping")) {
                        escapeEntity = true;
                        break;
                    }
                }

                writer.write("<?");
                writer.write(localName);
                writer.write(' ');
                writer.write(currNode.getNodeValue());
                writer.write("?>");

                textContent = false;
                break;
            }

            case Node.COMMENT: {
                // comment node

                // new line if this previous node is not text content
                if (pretty && !textContent) {
                    writer.write(lineSeparator);
                    lineNumber++;
                }

                writer.write("<!--");
                lineNumber += serializeText(currNode.getNodeValue(), writer);
                writer.write("-->");

                textContent = false;
                break;
            }
            default:
                textContent = false;
                break;
            } // switch

            if (nodeType != Node.ELEMENT)
                currNode.setLineNumber(lineNumber);

            // next sibling
            if (currNode.getNextSibling() != null) {
                // has next sibling
                open = true;
                currNode = currNode.getNextSibling();
            } else {
                // to the end, up to one level, reset the start flag
                currNode = currNode.getParentNode();
                open = false;

                // check whether we need an indent for parent
                if (pretty && !textContent) {
                    // change line
                    writer.write(lineSeparator);
                    lineNumber++;
                    // output parent indent
                    serializeIndent(writer, currIndent - 1, indent);
                }

            }
        } while (currNode != parentNode && currNode != nextSibling);

    }

    private boolean declareNamespace(String prefix, String uri,
            Stack namespaces, NamespacePrefix nsPrefix) {
        // xml namespace, no need to be serialized
        if (prefix.equals("xml") && !options.isRootDefaultNamespace())
            return false;

        nsPrefix.setPrefix(prefix);
        int find = namespaces.search(nsPrefix);
        if (find == -1) {
            // no namespace prefix declaration found
            namespaces.push(new Namespace(prefix, uri));
            return true;
        }

        // prefix found,check whether it is the same uri declaration
        Namespace nsFound = (Namespace) namespaces
                .get(namespaces.size() - find);
        if (nsFound.getUri() != uri) {
            namespaces.push(new Namespace(prefix, uri));
            return true;
        }

        return false;
    }

    /**
     * Serialize all the attributes of the element
     * 
     * @param firstAttrNodeId
     *            first attribute id
     * @param writer
     *            writer to write the data to
     * @param namespaces
     *            the namespace stack
     * 
     * @return the namespace count in the attributes
     */
    private void serializeAttributes(AttributeCollection AttrNodes,
            Writer writer) throws IOException {
        String prefix;
        String uri;
        String localName;
        int attributes = AttrNodes.getLength();

        for (int i = 0; i < attributes; i++) {
            Attribute attr = AttrNodes.getAttribute(i);
            prefix = attr.getPrefix();
            localName = attr.getLocalPart();
            uri = attr.getURI();
            if (prefix == null || prefix.equals("")) {
                writer.write(' ');
                writer.write(localName);
            } else {
                writer.write(' ');
                writer.write(prefix);
                writer.write(':');
                writer.write(localName);
            }

            writer.write("=\"");
            // write value
            String text = attr.getValue();
            if (text != null)
                serializeEscapedAttribute(text, writer);
            writer.write("\"");
        } // while
    }

    private class NamespacePrefix {
        private String prefix;

        public NamespacePrefix(String prefix) {
            this.prefix = prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Namespace) {
                Namespace ns = (Namespace) obj;
                if (!prefix.equals(ns.prefix))
                    return false;

                return true;
            } else if (obj instanceof NamespacePrefix) {
                NamespacePrefix nsPrefix = (NamespacePrefix) obj;
                if (!prefix.equals(nsPrefix.prefix))
                    return false;

                return true;
            } else
                return false;
        }

        public int hashCode() {
            return prefix.hashCode();
        }

    }

    private class Namespace {
        private String prefix;
        private String uri;

        public Namespace(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Namespace) {
                Namespace ns = (Namespace) obj;
                if (!prefix.equals(ns.prefix))
                    return false;

                return (uri.equals(ns.uri));
            }

            return false;
        }

        public int hashCode() {
            return uri.hashCode() | prefix.hashCode();
        }
    }

    private void serializeNamespace(String prefix, String uri, Writer writer)
            throws IOException {
        writer.write(' ');
        writer.write("xmlns");

        if (prefix != null && !prefix.equals("")) {
            writer.write(':');
            writer.write(prefix);
        }

        writer.write("=\"");

        // write value
        if (uri != null)
            writer.write(uri);

        writer.write("\"");
    }

    /**
     * Serialize a attribute value with escaping
     * 
     * @param string
     *            to be escaped
     */
    private void serializeEscapedAttribute(String text, Writer writer)
            throws IOException {
        if (text == null)
            return;

        int len = text.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            switch (c) {
            case '<': {
                // output the escaped string
                writer.write("&lt;");
                break;
            }
            case '>': {
                // output the escaped string
                writer.write("&gt;");
                break;
            }
            case '&': {
                // output the escaped string
                writer.write("&amp;");
                break;
            }
            case '"': {
                // output the escaped string
                writer.write("&quot;");
                break;
            }
            case '\r': {
                // output the escaped string
                writer.write("&#xD;");
                break;
            }
            case '\n': {
                // output the escaped string
                writer.write("&#xA;");
                break;
            }
            case '\t': {
                // output the escaped string
                writer.write("&#x9;");
                break;
            }
            default:
                writer.write(c);
                break;
            }
        }
    }

    /**
     * Serialize a text w/o escaping
     * 
     * @param string
     *            to be serialize
     * @return the lines
     * @throws IOException
     */
    private int serializeText(String text, Writer writer) throws IOException {
        if (text == null)
            return 0;

        int lines = 0;
        int len = text.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (c == '\n')
                lines++;
            writer.write(c);
        }
        return lines;
    }

    /**
     * Serialize a text with escaping
     * 
     * @param string
     *            to be escaped
     * @return the lines
     */
    private int serializeEscapedText(String text, Writer writer)
            throws IOException {
        if (text == null)
            return 0;

        int lines = 0;
        int len = text.length();
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (c == '\n')
                lines++;
            switch (c) {
            case '<': {
                // output the escaped string
                writer.write("&lt;");
                break;
            }
            case '>': {
                // output the escaped string
                writer.write("&gt;");
                break;
            }
            case '&': {
                // output the escaped string
                writer.write("&amp;");
                break;
            }
            case '\r': {
                // output the escaped string
                writer.write("&#xD;");
                break;
            }
            default:
                writer.write(c);
                break;
            }
        }
        return lines;
    }

    private void serializeIndent(Writer writer, int indentCount, String indent)
            throws IOException {
        for (int i = 0; i < indentCount; i++)
            writer.write(indent);
    }

    public static void main(String[] args) {
        System.out.println("Loading " + args[0]);
        DocumentLoader loader = new DocumentLoader();
        try {
            Document doc = loader.load(args[0]);
            StringWriter sw = new StringWriter();
            new DOMSerializer().serialize(doc, sw);
            System.out.println(sw.toString());
        } catch (XMLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
