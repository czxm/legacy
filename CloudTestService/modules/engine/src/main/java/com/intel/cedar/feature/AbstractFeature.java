package com.intel.cedar.feature;

import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;

public abstract class AbstractFeature implements IFeature {
    public static final String TRANS_IMPL = "org.apache.xalan.processor.TransformerFactoryImpl";

    private long submitTSC;

    abstract protected List<TaskSummaryItem> getSummaryItems(Environment env);

    protected String getFeatureReport(Environment env) {
        return null;
    }

    @Override
    public void onInit(Environment env) throws Exception {
        submitTSC = System.currentTimeMillis();
    }

    @Override
    public void onFinalize(Environment env) throws Exception {
        String countStr = env.getFeatureProperty("_SUBMIT_COUNT", null);
        int count = 0;
        try {
            count = Integer.parseInt(countStr);
        } catch (Exception e) {
            count = 0;
        }
        String avgTimeStr = env.getFeatureProperty("_AVG_TIME", null);
        long avgTime = 0;
        try {
            avgTime = Long.parseLong(avgTimeStr);
        } catch (Exception e) {
            avgTime = 0;
        }
        long elapseTime = System.currentTimeMillis() - submitTSC;
        avgTime = (avgTime * count + elapseTime) / (count + 1);
        env.setFeatureProperty("_SUBMIT_COUNT", Integer.toString(count + 1),
                null);
        env.setFeatureProperty("_AVG_TIME", Long.toString(avgTime), null);
    }

    @Override
    public String getReportBody(Environment env) throws Exception {
        StringBuilder sb = new StringBuilder();
        List<TaskSummaryItem> sumItems = getSummaryItems(env);
        if (sumItems != null && sumItems.size() > 0) {
            sb.append(getFormattedSummary(env, sumItems));
        }
        String content = getFeatureReport(env);
        if (content != null && content.length() > 0) {
            sb.append("<br>");
            sb.append(content);
        }
        return sb.toString();
    }

    @Override
    public String getReportTitle(Environment env) throws Exception {
        return null;
    }
    
    @Override
    public String getReportFootnote(Environment env) throws Exception {
        return null;
    }
    
    @Override
    public INotifyConfig getNotifyConfig(Environment env) throws Exception {
        return null;
    }
    
    @Override
    public InputStream getReportCSS(Environment env) throws Exception {
        return null;
    }

    protected Document newDocument() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }
    
    protected Document newDocument(String root) throws Exception{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.appendChild(doc.createElement(root));
        return doc;
    }   
    
    protected Document newDocument(IFile file) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(file.getContents());
    }
    
    protected String doXSLTransform(Document doc, String method) throws Exception {
        Source xmlInput = new DOMSource(doc);
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        TransformerFactory tfFactory = TransformerFactory.newInstance(TRANS_IMPL, null);
        Transformer transformer = tfFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, method);
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    protected String doXSLTransform(Document doc, String method, String xsl) throws Exception {
        Source xmlInput = new DOMSource(doc);
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        Source xslSource = new StreamSource(getClass().getClassLoader()
                .getResourceAsStream(xsl));
        TransformerFactory tfFactory = TransformerFactory.newInstance(TRANS_IMPL, null);
        Transformer transformer = tfFactory.newTransformer(xslSource);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, method);
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }
    
    protected String doXSLTransform(Document doc, String method, String xsl, HashMap<String, Object> params) throws Exception{
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        doXSLTransform(doc, method, xsl, params, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    protected void doXSLTransform(Document doc, String method, String xsl, HashMap<String, Object> params, Result output) throws Exception{
        Source xmlInput = new DOMSource(doc);
        Source xslSource = new StreamSource(getClass().getClassLoader().getResourceAsStream(xsl));
        TransformerFactory tfFactory = TransformerFactory.newInstance(TRANS_IMPL, null);
        Transformer transformer = tfFactory.newTransformer(xslSource);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, method);
        final HashMap<String, IFile> fileMaps = new HashMap<String, IFile>();
        final HashMap<String, Document> docMaps = new HashMap<String, Document>();
        for(String key : params.keySet()){
            Object v = params.get(key);
            if(v instanceof IFile){
                IFile f = (IFile)v;
                fileMaps.put(f.getName(), f);
                transformer.setParameter(key, "ifile://" + f.getName());
            }
            else if(v instanceof Document){
                Document d = (Document)v;
                String k = Integer.toHexString(d.hashCode());
                docMaps.put(k, d);
                transformer.setParameter(key, "idoc://" + k);
            }
            else{
                transformer.setParameter(key, v);
            }
        }
        if(fileMaps.size() > 0 || docMaps.size() > 0){
            transformer.setURIResolver(new URIResolver(){
                @Override
                public Source resolve(String href, String base)
                        throws TransformerException {
                    try{
                        if(href.startsWith("ifile://")){
                            String k = href.substring("ifile://".length());
                            IFile f = fileMaps.get(k);
                            if(f != null)
                                return new DOMSource(newDocument(f));
                        }
                        else if(href.startsWith("idoc://")){
                            String k = href.substring("idoc://".length());
                            Document d = docMaps.get(k);
                            if(d != null)
                                return new DOMSource(d);
                        }
                        return null;
                    }
                    catch(Exception e){
                        throw new TransformerException(e);
                    }
                }            
            });
        }
        transformer.transform(xmlInput, output);
    }
    
    protected String getFormattedSummary(Environment env,
            List<TaskSummaryItem> items) throws Exception {
        Document doc = newDocument();
        Element tbl = doc.createElement("table");
        doc.appendChild(tbl);
        tbl.setAttribute("class", "details");
        Element tbody = doc.createElement("tbody");
        tbl.appendChild(tbody);
        for (TaskSummaryItem item : items) {
            Element tr = doc.createElement("tr");
            Element td1 = doc.createElement("td");
            td1.setTextContent(item.getName());
            tr.appendChild(td1);
            Element td2 = doc.createElement("td");
            if (item.getStyle() != null && item.getStyle().length() > 0) {
                td2.setAttribute("class", item.getStyle());
            }
            if (item.isHyperLink()) {
                Element a = doc.createElement("a");
                a.setAttribute("href", item.getUrl());
                a.setAttribute("class", item.getStyle());
                a.setTextContent(item.getValue());
                td2.appendChild(a);
            } else {
                td2.setTextContent(item.getValue());
            }
            tr.appendChild(td2);
            tbody.appendChild(tr);
        }
        return doXSLTransform(doc, "html");
    }

    protected void createZipBundle(String zipFileName, String folder) {
        if (new File(folder).isDirectory()) {
            try {
                FileUtils.zip(zipFileName, new File(folder));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void createZipBundle(IFile zipFile, IFolder folder) {
        if (folder.exist()) {
            try {
                FileUtils.zip(zipFile, folder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected void extractZipBundle(IFile zipFile, IFolder folder) {
        try {
            FileUtils.unzip(zipFile.getContents(), folder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
