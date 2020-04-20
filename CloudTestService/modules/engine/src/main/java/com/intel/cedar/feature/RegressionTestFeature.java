package com.intel.cedar.feature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intel.cedar.feature.util.SCMChangeItem;
import com.intel.cedar.feature.util.SCMChangeSet;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;

public abstract class RegressionTestFeature extends AbstractFeature {
    protected int maxModifications = 100;
    protected String username;
    protected String password;
    protected String url;
    protected String rev;
    protected String lastRev;
    protected boolean isCheckIn;
    protected boolean isNightly;
    protected String repository;
    protected List<Document> junitResultDocList = new ArrayList<Document>();
    protected HashMap<String, Pattern> patternCache = new HashMap<String, Pattern>();

    abstract protected String getTestName();
    abstract protected boolean isBuildCompleted(Environment env);
    protected abstract String getLatestModificationRevision(Environment env) throws Exception;
    protected abstract List<SCMChangeSet> getModifications(Environment env) throws Exception;
    
    protected String getTrackedUrl(){
        return url;
    }
    
    protected String getTrackedRev(){
        return rev;
    }
    
    protected String getTrackedUser(){
        return username;
    }
    
    protected String getTrackedPassword(){
        return password;
    }
    
    protected String getTrackedRepository(){
        return repository;
    }
    
    protected String getLastTrackedRev(){
        return lastRev;
    }
    
    protected List<JUnitReportConfig> getReportConfig(Environment env){
        return null;
    }
    
    protected final static String[] DIFF_TYPES = new String[]{
        "newFailedTests",
        "newPassedTests",
        "testsAdded",
        "testsRemoved"
    };
    
    protected final static String[] DETAIL_TYPES = new String[]{
        "tests",
        "errors",
        "failures"
    };
    
    static protected interface JUnitReportConfig{
        public String getTitle();
        public String getDetailTitle();
        public IFolder getResultFolder();
        public IFolder getReportFolder();
        public IFile getDiffSource();
        public String getDiffTitle();
        public String[] getGroups();
        public String getGroupPattern(String group);
        public String getDefaultGroup();
        public InputStream getScript();
        public InputStream getCSS();
        public InputStream getDetailScript();
        public InputStream getDetailCSS();
    }
    
    protected static abstract class AbstractJUnitReportConfig implements JUnitReportConfig {
        @Override
        public String getTitle() {
            return "JUnit Test Summary";
        }
        @Override
        public String getDetailTitle() {
            return "JUnit Test Details";
        }
        @Override
        public IFile getDiffSource() {
            return null;
        }
        @Override
        public String getDiffTitle() {
            return "JUnit Diff Details";
        }
        @Override
        public String[] getGroups() {
            return new String[]{};
        }
        @Override
        public String getGroupPattern(String group) {
            return null;
        }
        @Override
        public String getDefaultGroup() {
            return "All";
        }
        @Override
        public InputStream getScript() {
            return null;
        }
        @Override
        public InputStream getCSS() {
            return null;
        }
        @Override
        public InputStream getDetailScript() {
            return null;
        }
        @Override
        public InputStream getDetailCSS() {
            return null;
        }
    }
    
    protected String stringRegexMatch(String regexPattern, String input) {
        Pattern cachedPattern = patternCache.get(regexPattern);
        if(cachedPattern == null){
                cachedPattern = Pattern.compile(regexPattern);
                patternCache.put(regexPattern, cachedPattern);
        }
        Matcher matcher = cachedPattern.matcher(input);
        if (matcher.find()) {
                return matcher.group(1);
        }
        return null;
    }

    protected boolean regexMatch(String regexPattern, String input) {
        Pattern cachedPattern = patternCache.get(regexPattern);
        if(cachedPattern == null){
                cachedPattern = Pattern.compile(regexPattern);
                patternCache.put(regexPattern, cachedPattern);
        }
        Matcher matcher = cachedPattern.matcher(input);
        return matcher.matches();
    }
    
    protected List<IFile> getJUnitResults(IFolder folder){
        final List<IFile> files = new ArrayList<IFile>();
        for(IStorage f : folder.list()){
            if(f instanceof IFolder){
                files.addAll(getJUnitResults((IFolder)f));
            }
            else if(f.getName().startsWith("TEST-") && f.getName().endsWith(".xml")){
                files.add((IFile)f);
            }
        }
        return files;
    }

    
    protected int getNumber(String num) {
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected List<TaskSummaryItem> getSummaryItems(Environment env) {
        List<TaskSummaryItem> items = new ArrayList<TaskSummaryItem>();
        String platform = "General";
        try {
            Variable os = env.getVariable("OS");
            Variable arch = env.getVariable("ARCH");
            platform = os.getValue() + " " + arch.getValue();
        } catch (Exception e) {
        }

        TaskSummaryItem item = new TaskSummaryItem();
        item.setName("Platform");
        item.setValue(platform);
        items.add(item);

        item = new TaskSummaryItem();
        item.setName("Build Result");
        boolean status = isBuildCompleted(env);
        item.setValue(status ? "Complete" : "Failed");
        if (!status) {
            item.setStyle("Failed");
        }
        items.add(item);
        
        int total = 0;
        int error = 0;
        for(Document junitResultDoc : junitResultDocList){
            if(junitResultDoc == null)
                continue;
            NodeList nl = junitResultDoc.getDocumentElement().getElementsByTagName("testsuite");
            for (int i = 0; i < nl.getLength(); i++) {
                Element testsuite = (Element) nl.item(i);
                error += getNumber(testsuite.getAttribute("errors"))
                        + getNumber(testsuite.getAttribute("failures"));
                total += getNumber(testsuite.getAttribute("tests"));
            }
        }
        if(total > 0){
            item = new TaskSummaryItem();
            item.setName("Tests");
            if (error > 0) {
                item.setValue(String.format("%d/%d Failed", error, total));
                item.setStyle("Failed");
            } else {
                item.setValue(String.format("%d Passed", total));
            }
            items.add(item);
        }
        return items;
    }

    protected Document buildModifications(List<SCMChangeSet> items)
            throws Exception {
        Document doc = newDocument();
        Element root = doc.createElement("modifications");
        doc.appendChild(root);
        for (SCMChangeSet t : items) {
            Element ele = doc.createElement("modification");
            root.appendChild(ele);
            ele.setAttribute("revision", t.getRev());
            Element author = doc.createElement("author");
            ele.appendChild(author);
            author.setTextContent(t.getUser());
            Element date = doc.createElement("date");
            ele.appendChild(date);
            date.setTextContent(t.getDateTime());
            Element msg = doc.createElement("msg");
            ele.appendChild(msg);
            msg.setTextContent(t.getLogMsg().trim());
            Element paths = doc.createElement("paths");
            ele.appendChild(paths);
            int count = 0;
            for (SCMChangeItem p : t.getChangeItems()) {                
                if(count > 30){
                    Element path = doc.createElement("path");
                    path.setAttribute("action", "");
                    path.setTextContent("...");
                    paths.appendChild(path);
                    break;
                }
                Element path = doc.createElement("path");
                path.setAttribute("action", p.getAction());
                path.setTextContent(p.getPath());
                paths.appendChild(path);
                count++;
            }
        }
        return doc;
    }

    protected String getModificationLogs(Environment env) throws Exception {
        List<SCMChangeSet> logItems = getModifications(env);
        Document doc = buildModifications(logItems);
        return doXSLTransform(doc, "html", "modifications.xsl");
    }

    @Override
    public void onInit(Environment env) throws Exception {
        super.onInit(env);
        try {
            username = env.getVariable("_scm_username").getValue();
            password = env.getVariable("_scm_password").getValue();
        } 
        catch (Exception e) {
        }
        
        try{
            url = env.getVariable("_scm_url").getValue();
            // url should not end with '/'
            if (url.endsWith("/")){
                url = url.substring(0, url.length() - 1);
                env.getVariable("_scm_url").setValue(url);
            }
        }
        catch(Exception e){            
        }     
        
        try {
            Variable isCheckInVar = env.getVariable("isCheckIn");
            isCheckIn = Boolean.parseBoolean(isCheckInVar.getValue());
        } catch (Exception e) {
        }
        
        try{
            Variable isNightlyVar = env.getVariable("isNightly");
            isNightly = Boolean.parseBoolean(isNightlyVar.getValue());
        }
        catch(Exception e){            
        }
        
        try{
            repository = env.getVariable("_scm_repository").getValue();
        }
        catch(Exception e){            
        }
        
        try{
            rev = env.getVariable("_scm_rev").getValue();
        }
        catch(Exception e){            
        }   
        
        try{
            if(isCheckIn){
                lastRev = env.getVariable("_last_scm_rev").getValue();
                if(lastRev == null || lastRev.length() == 0){
                    lastRev = env.getFeatureProperty("LAST_" + getTrackedRepository()
                            + "_COMMIT_REV", "common");
                }
            }
            else if(isNightly){
                lastRev = env.getFeatureProperty("LAST_" + getTrackedRepository()
                        + "_NIGHTLY_REV", "common");
            }
        }
        catch(Exception e){            
        }        
    }

    @Override
    public String getReportTitle(Environment env) throws Exception {
        if (isCheckIn) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(getTrackedRepository());
            sb.append(":");
            sb.append(getTrackedRev());
            sb.append("] ");
            sb.append(getTestName());
            return sb.toString();
        } else {
            return null;
        }
    }

    protected String getFormattedModifications(Environment env)
            throws Exception {
        StringBuilder sb = new StringBuilder();
        if (isCheckIn) {
            env.setFeatureProperty("LAST_" + getTrackedRepository() + "_COMMIT_REV", getTrackedRev(),
                "common");
            sb.append(this.getModificationLogs(env));
        }
        else if(isNightly){
            env.setFeatureProperty("LAST_" + getTrackedRepository() + "_NIGHTLY_REV", getTrackedRev(),
            "common");
            sb.append(this.getModificationLogs(env));
        }
        return sb.toString();
    }

    protected Document getJUnitTestResult(Environment env, JUnitReportConfig config)
            throws Exception {
        Document doc = null;
        IFolder f = config.getResultFolder();
        if(config == null || !f.exist())
            return doc;
        List<IFile> files = getJUnitResults(f);
        if (files.size() > 0) {
            doc = newDocument("TestReport");
            Element root = doc.getDocumentElement();
            List<Element> added = new ArrayList<Element>();
            for(String g : config.getGroups()){
                Element t = doc.createElement("testsuites");
                t.setAttribute("name", g);
                root.appendChild(t);
                added.add(t);
            }
            
            // add default group
            String defaultGroup = config.getDefaultGroup();
            if(defaultGroup == null || defaultGroup.length() == 0)
                defaultGroup = "All";
            Element t = doc.createElement("testsuites");
            t.setAttribute("name", defaultGroup);
            root.appendChild(t);
            added.add(t);
            
            if(doc != null && files.size() > 0){
                for(int i = 0; i < files.size(); i++){
                    try{
                        IFile file = files.get(i);
                        Document newDoc = newDocument(file);
                        shrinkResult(newDoc);
                        Element newroot = newDoc.getDocumentElement();
                        String name = newroot.getAttribute("name");
                        Element e = findTestGroup(doc, name, config);
                        if(e != null){
                            e.appendChild(doc.importNode(newroot, true));
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            removeSkippedTests(doc);
            
            // remove empty testsuites if nothing is in this group
            NodeList nl = doc.getDocumentElement().getElementsByTagName("testsuites");
            List<Element> toRemove = new ArrayList<Element>();
            for(int i = 0; i < nl.getLength(); i++){
                Element e = (Element)nl.item(i);
                if(e.getElementsByTagName("testsuite").getLength() == 0)
                    toRemove.add(e);
            }
            for(Element e : toRemove){
                e.getParentNode().removeChild(e);
            }
        }
        return doc;
    }
        
    protected Element findTestGroup(Document doc, String group, JUnitReportConfig config){
        Element ret = null;
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("testsuites");
        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i);
            Element e = (Element)n;
            String groupName = e.getAttribute("name");
            String groupPattern = config.getGroupPattern(groupName);           
            if(groupPattern != null && (groupPattern.equals(group) || regexMatch(groupPattern, group))){
                ret = e; 
                break;
            }
            ret = e;
        }
        return ret;
    }
    
    protected List<String> getGroups(Document doc){
        List<String> groups = new ArrayList<String>();
        Element root = doc.getDocumentElement();
        NodeList nl = root.getElementsByTagName("testsuites");
        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i);
            Element e = (Element)n;
            String groupName = e.getAttribute("name");
            groups.add(groupName);
        }
        return groups;
    }
    
    protected String loadFromStream(InputStream ins) throws Exception{
        StringBuffer sb = new StringBuffer();
        InputStreamReader fr = new InputStreamReader(ins);
        char[] buff = new char[4096];
        int size = fr.read(buff, 0, 4096);
        while (size > 0) {
            sb.append(buff, 0, size);
            size = fr.read(buff, 0, 4096);
        }

        if(fr != null)
            try {
                fr.close();
            } catch (IOException e) {
            }
        return sb.toString();
    }
    
    protected void generateDetailFile(String title, String content, JUnitReportConfig config, IFile file) throws Exception{
        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("<html><head><title>");
        resultBuffer.append(title);
        resultBuffer.append("</title>");

        resultBuffer.append("<style>");
        String csscontent = "";
        InputStream css = config.getDetailCSS();
        if(css == null){
            css = RegressionTestFeature.class.getClassLoader().getResourceAsStream("testdetail.css");
        }
        csscontent = loadFromStream(css);
        resultBuffer.append(csscontent);
        resultBuffer.append("</style>");

        resultBuffer.append("<script>");
        String scriptcontent = "";
        InputStream script = config.getDetailScript();
        if(script == null){
            script = RegressionTestFeature.class.getClassLoader().getResourceAsStream("testdetail.js");
        }
        scriptcontent = loadFromStream(script);
        resultBuffer.append(scriptcontent);
        resultBuffer.append("</script>");
        resultBuffer.append("</head>");

        resultBuffer.append(content);

        resultBuffer.append("</html>");

        if(!file.exist())
            file.create();
        file.setContents(new ByteArrayInputStream(resultBuffer.toString().getBytes()));
    }
    
    protected void generateSummaryFile(String title, String content, JUnitReportConfig config, IFile file) throws Exception{
        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("<html><head><title>");
        resultBuffer.append(title);
        resultBuffer.append("</title>");

        resultBuffer.append("<style>");
        String csscontent = "";
        InputStream css = config.getCSS();
        if(css == null){
            css = RegressionTestFeature.class.getClassLoader().getResourceAsStream("summary.css");
        }
        csscontent = loadFromStream(css);
        resultBuffer.append(csscontent);
        resultBuffer.append("</style>");

        resultBuffer.append("<script>");
        String scriptcontent = "";
        InputStream script = config.getScript();
        if(script == null){
            script = RegressionTestFeature.class.getClassLoader().getResourceAsStream("testdetail.js");
        }
        scriptcontent = loadFromStream(script);
        resultBuffer.append(scriptcontent);
        resultBuffer.append("</script>");
        resultBuffer.append("</head>");

        resultBuffer.append(content);

        resultBuffer.append("</html>");

        if(!file.exist())
            file.create();
        file.setContents(new ByteArrayInputStream(resultBuffer.toString().getBytes()));
    }
    
    protected void shrinkElement(Element ele, String tag){
        NodeList nl = ele.getElementsByTagName(tag);
        for(int k = 0; k < nl.getLength(); k++){
            Node n = nl.item(k);
            n.getParentNode().removeChild(n);
        }
    }
    
    protected float getTimeAndFix(Element ele){
        float time = 0;
        String strTime = ele.getAttribute("time");
        if(strTime != null && strTime.contains(",")){
            strTime = strTime.replace(",", "");
            ele.setAttribute("time", strTime);
        }
        try{
            time = Float.parseFloat(strTime);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return time;
    }
    
    protected void shrinkResult(Document doc){
        Element root = doc.getDocumentElement();
        if(root.getNodeName().equals("testsuite")){
            shrinkElement(root, "system-out");
            shrinkElement(root, "system-err");
            shrinkElement(root, "properties");
            float time = 0;
            NodeList nl = root.getElementsByTagName("testcase");
            for(int i = 0; i < nl.getLength(); i++){
                Element e = (Element)nl.item(i);
                shrinkElement(e, "system-out");
                shrinkElement(e, "system-err");
                time = time + getTimeAndFix(e);
            }
            root.setAttribute("time", Float.toString(time));
        }
    }
    
    protected void removeSkippedTests(Document doc){
        Element root = doc.getDocumentElement();
        List<Element> toRemoveSuite = new ArrayList<Element>();
        List<Element> toRemoveCase = new ArrayList<Element>();
        NodeList nl = root.getElementsByTagName("testsuite");
        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i);
            Element e = (Element)n;
            int skipNum = 0;
            int totalNum = 0;
            try{
                skipNum = Integer.parseInt(e.getAttribute("skipped"));
            }
            catch(Throwable t){                
            }
            try{
                totalNum = Integer.parseInt(e.getAttribute("tests"));
            }
            catch(Throwable t){                
            }
            if(totalNum >= skipNum && totalNum >= 0){
                totalNum = totalNum - skipNum;
                e.setAttribute("tests", Integer.toString(totalNum));
            }
            
            if(totalNum == 0){
                toRemoveSuite.add(e);
            }
            else{
                toRemoveCase.clear();
                NodeList nlt = e.getElementsByTagName("testcase");
                for(int j = 0; j < nlt.getLength(); j++){
                    Element et = (Element)nlt.item(j);
                    if(et.getElementsByTagName("skipped").getLength() > 0)
                        toRemoveCase.add(et);
                }
                for(Element rt : toRemoveCase){
                    rt.getParentNode().removeChild(rt);
                }
            }
        } 
        
        for(Element e : toRemoveSuite){
            e.getParentNode().removeChild(e);
        }
    }    
    
    protected Document diffReport(Document doc, JUnitReportConfig config) throws Exception{
        final IFile src = config.getDiffSource();
        if(src == null || !src.exist())
            return newDocument();
        IFolder reportFolder = config.getReportFolder();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("previous", src);
        DOMResult domOutput = new DOMResult();
        doXSLTransform(doc, "xml", "diff.xsl", params, domOutput);
        Document diffDoc = (Document)domOutput.getNode();
        String diffdetail = doXSLTransform(diffDoc, "html", "diff-detail.xsl");
        IFile diffdetailFile = reportFolder.getFile("diff-detail.html");
        generateSummaryFile(config.getDiffTitle(), diffdetail, config, diffdetailFile);
        IFolder diffFolder = reportFolder.getFolder("diffs");
        if(!diffFolder.exist())
            diffFolder.create();
        for(String groupName:getGroups(doc)){
            for(String diffType:DIFF_TYPES){
                String groupNameParam = "groupName";
                String diffTypeParam = "diffType";
                params = new HashMap<String, Object>();
                params.put(groupNameParam, groupName);
                params.put(diffTypeParam, diffType);
                diffdetail = doXSLTransform(diffDoc, "html", "diff-detail.xsl", params);
                diffdetailFile = diffFolder.getFile(groupName + "-" + diffType + ".html");
                generateDetailFile(diffType + ": " + config.getDiffTitle() + " for " + groupName, diffdetail, config, diffdetailFile);
            }     
        }
        return diffDoc;
    }
    
    protected String generateJUnitReport(Environment env, Document junitResultDoc, JUnitReportConfig config) throws Exception{
        IFolder theReportFolder = config.getReportFolder();
        if(!theReportFolder.exist()){
            if(!theReportFolder.create())
                throw new Exception("Failed to create: " + theReportFolder.getName());
        }
        
        HashMap<String, Object> params = new HashMap<String, Object>();
        String testdetail = doXSLTransform(junitResultDoc, "html", "testdetail.xsl", params);
        IFile testdetailFile = theReportFolder.getFile("test-detail.html");
        generateDetailFile(config.getDetailTitle(), testdetail, config, testdetailFile);
        
        IFolder detailFolder = theReportFolder.getFolder("details");
        if(!detailFolder.exist())
            detailFolder.create();
        for(String groupName : getGroups(junitResultDoc)){
            for(String detailType : DETAIL_TYPES){
                String groupNameParam = "groupName";
                String detailTypeParam = "detailType";
                params = new HashMap<String, Object>();
                params.put(groupNameParam, groupName);
                params.put(detailTypeParam, detailType);
                testdetail = doXSLTransform(junitResultDoc, "html", "testdetail.xsl", params);
                testdetailFile = detailFolder.getFile(groupName + "-" + detailType + ".html");
                generateDetailFile(detailType + ": " + config.getDetailTitle() + " for " + groupName, testdetail, config, testdetailFile);
            }
        }
        
        Document diffDoc = diffReport(junitResultDoc, config);
        String diffDocParam = "diffDocUrl";
        String testDetailHtmlParam = "testDetailHtml";
        String diffDetailHtmlParam = "diffDetailHtml";
        String reportServerBaseParam = "reportServerBase";
        String codeCoverageUrlParam = "codeCoverageUrl";
        String packageLinkParam = "packageLink";
        params = new HashMap<String, Object>();
        params.put(testDetailHtmlParam, "test-detail.html");
        params.put(reportServerBaseParam, env.getHyperlink(theReportFolder));
        params.put("title", config.getTitle());
        params.put(diffDocParam, diffDoc);
        params.put(diffDetailHtmlParam, "diff-detail.html");
        params.put(packageLinkParam, env.getHyperlink(config.getResultFolder()));
        String testsummary = doXSLTransform(junitResultDoc, "html", "summary.xsl", params);
        IFile reportFile = theReportFolder.getFile("report.html");
        generateSummaryFile(config.getTitle(), testsummary, config, reportFile);
        String xmlResult = doXSLTransform(junitResultDoc, "xml");
        ByteArrayInputStream bis = new ByteArrayInputStream(xmlResult.getBytes());
        IFile xmlFile = theReportFolder.getFile("report.xml");
        xmlFile.create();
        xmlFile.setContents(bis);
        bis.close();
        return testsummary;
    }

    @Override
    public String getReportBody(Environment env) throws Exception {
        StringBuilder sb = new StringBuilder();
        List<JUnitReportConfig> configs = this.getReportConfig(env);
        if (configs != null && configs.size() > 0) {
            for(JUnitReportConfig c : configs){
                junitResultDocList.add(getJUnitTestResult(env, c));
            }
        }
        sb.append(super.getReportBody(env));
        sb.append("<br>");
        
        if(configs != null){
            for(int i = 0; i < configs.size(); i++){
                Document junitResultDoc = junitResultDocList.get(i);
                JUnitReportConfig config = configs.get(i);
                if(junitResultDoc != null && config != null){
                    sb.append(generateJUnitReport(env, junitResultDoc, config));
                    sb.append("<br>");
                }
            }
        }
        
        sb.append(getFormattedModifications(env));
        return sb.toString();
    }
}
