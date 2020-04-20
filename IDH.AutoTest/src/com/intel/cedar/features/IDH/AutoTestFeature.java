package com.intel.cedar.features.IDH;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.feature.SVNRegressionTestFeature;
import com.intel.cedar.feature.TaskSummaryItem;
import com.intel.cedar.feature.util.GitClient;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.util.Utils;

public class AutoTestFeature extends SVNRegressionTestFeature {
	private String product = "IDH2";
	private IFolder comparedJob = null;

    private String build_type = ConstHelper.BUILD_TYPE_TGZ;
    private String iso_release_version = "";
    private String iso_language = ConstHelper.ISO_LANGUAGE_EN;
    private String license_type = ConstHelper.LICENSE_TYPE_EVALUATION;
    private String osVersionStr = ConstHelper.OS_VERSION_CENTOS_6d2;
    private String IM_svn_url = "";
    private String IM_svn_rev = ConstHelper.HEAD;
	
	private static class GroupPattern {
	    private String group;
	    private String pattern;	    
	    public GroupPattern(String g, String p){
	        group = g;
	        pattern = p;
	    }	    
	}
	protected List<GroupPattern> groupPatterns = new ArrayList<GroupPattern>();
	
	public AutoTestFeature(){
        try{
            InputStream ins = AutoTestFeature.class.getResourceAsStream("/conf/groups.conf");
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            String line = null;
            while((line = br.readLine()) != null){
                String[] sp = line.trim().split("=");
                if(sp.length == 2 && sp[0].length() > 0 && sp[1].length() > 0){
                    groupPatterns.add(new GroupPattern(sp[0], sp[1]));
                }
            }
            br.close();     
        }
        catch(Exception e){
            e.printStackTrace();
        }  
	}
	
	@Override
	public void onInit(Environment env) throws Exception{
		super.onInit(env);
		env.getStorageRoot().getFolder("junit").create();
		env.getStorageRoot().getFolder("bigtop").create();
        env.getStorageRoot().getFolder("nist").create();		
	    env.getStorageRoot().getFolder("logs").create();
	    
        osVersionStr = checkEnvVar(env, "target", ConstHelper.OS_VERSION_CENTOS_6d2);
        build_type = checkEnvVar(env, "build_type", ConstHelper.BUILD_TYPE_TGZ);
        iso_release_version = checkEnvVar(env, "iso_release_version", "");
        iso_language = checkEnvVar(env, "iso_language", ConstHelper.ISO_LANGUAGE_EN);
        license_type = checkEnvVar(env, "license_type", ConstHelper.LICENSE_TYPE_EVALUATION);   
        IM_svn_url = checkEnvVar(env, "IM_svn_url", "");
        IM_svn_rev = checkEnvVar(env, "IM_svn_rev", ConstHelper.HEAD);  
        if(!isCheckIn){
            if (rev == null || rev.equalsIgnoreCase(ConstHelper.HEAD)) {
                rev = this.getLatestModificationRevision(url, username, password);
                env.getVariable("svn_rev").setValue(rev);
            }
            if (IM_svn_rev == null || IM_svn_rev.equalsIgnoreCase(ConstHelper.HEAD)) {
                IM_svn_rev = this.getLatestModificationRevision(IM_svn_url, username, password);
                env.getVariable("IM_svn_rev").setValue(IM_svn_rev);
            }
        }	 
        product = checkEnvVar(env, "_scm_repository", "");
        if(product.equals("")){
            if(iso_release_version.startsWith(ConstHelper.ISO_RELEASE_VERSION_3)){
                product = "IDH3";
            }
            else{
                product = "IDH2";
            }
        }
        comparedJob = getComparedJob(env);
        
        try{
            Variable v = env.getVariable("nist_branch");
            String nist_branch = v.getValue();
            String branch = nist_branch.substring(nist_branch.lastIndexOf("/") + 1);
            v.setValue(branch);
            
            String git_url = env.getVariable("nist_url").getValue();
            String src = git_url.substring(git_url.lastIndexOf("/") + 1);
            env.getVariable("nist_src").setValue(src + "_" + branch);   
        }
        catch(Exception e){
        }
	}
	
	@Override
	public void onFinalize(Environment env) throws Exception{
	    super.onFinalize(env);
	}
	
	public static String getBaseName(String component){
	    int i = 0;
	    boolean found = false;
	    char[] chars = component.toCharArray();
	    while(i + 1 < chars.length){
	        if(chars[i] == '-' && chars[i+1] <= '9' && chars[i+1] >= '0'){
	            found = true;
	            break;
	        }
	        i++;
	    }
	    if(found){
	        return component.substring(0, i);
	    }
	    else{
	        return component;
	    }
	}
	
	public static List<String> loadComponents(Environment env){
        List<String> components = new ArrayList<String>();
        String imTestEnabledVar = checkEnvVar(env, "imTestEnabled", "false");
        Boolean imTestEnabled = Boolean.parseBoolean(imTestEnabledVar);  
        if(imTestEnabled){
            components.add("manager");
        }
        try{
            List<String> exclude_components = env.getVariable("exclude_components").getValues();
            IFile sources = env.getStorageRoot().getFile("sources.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(sources.getContents()));
            String line = null;
            while((line = br.readLine()) != null){
                line = line.trim();
                boolean exclude = false;
                for(String c : exclude_components){
                    if(line.contains(c)){
                        exclude = true;
                        break;
                    }
                }
                if(!exclude){
                    components.add(line);
                }
            }
            br.close();
        }
        catch(Exception e){                 
        }
        return components;
	}
	
    protected IFolder getComparedJob(Environment env){
        try{
            Variable v = env.getVariable("compareResult");
            String l = null;
            if(v.getValue().toLowerCase().contains("checkin")){
                l = env.getFeatureProperty("last_checkin_job", product);
            }
            else if(v.getValue().toLowerCase().contains("nightly")){
                l = env.getFeatureProperty("last_nightly_job", product);
            }
            if(l != null){
                return env.getFolderByURI(URI.create(l));                        
            }
        }
        catch(Exception e){                
        }
        return null;
    }
	
	class IDHJUnitReportConfig extends AbstractJUnitReportConfig{
	    protected Environment env;
	    public IDHJUnitReportConfig(Environment env){
	        this.env = env;
	    }
	    
        @Override
        public String[] getGroups() {
            List<String> components = loadComponents(env);
            List<String> groups = new ArrayList<String>();
            for(GroupPattern gp : groupPatterns){
                for(String c : components){
                    if(c.contains(gp.group)){
                        groups.add(c);
                        break;
                    }
                }
            }
            return groups.toArray(new String[]{});
        }
        @Override
        public String getGroupPattern(String group) {
            for(GroupPattern gp : groupPatterns){
                if(group.contains(gp.group))
                    return gp.pattern;
            }
            return null;
        }
        @Override
        public String getDefaultGroup() {
            return "Others";
        }
        @Override
        public IFolder getResultFolder(){
            return env.getStorageRoot().getFolder("junit");
        }
        @Override
        public IFolder getReportFolder(){
            return env.getStorageRoot().getFolder("junit_report");
        }
        @Override
        public IFile getDiffSource(){
            if(comparedJob != null && comparedJob.exist()){
                IFolder reportFolder = comparedJob.getFolder("junit_report");
                if(reportFolder.exist())
                    return reportFolder.getFile("report.xml");
            }
            return null;
        }
	}
	
	class IDHBigtopReportConfig extends IDHJUnitReportConfig {
	    public IDHBigtopReportConfig(Environment env){
	        super(env);
	    }
        @Override
        public String getTitle() {
            return "Bigtop Test Summary";
        }
        @Override
        public String getDetailTitle() {
            return "Bigtop Test Details";
        }
        @Override
        public IFolder getResultFolder(){
            return env.getStorageRoot().getFolder("bigtop");
        }
        @Override
        public IFolder getReportFolder(){
            return env.getStorageRoot().getFolder("bigtop_report");
        }    
        @Override
        public IFile getDiffSource(){
            if(comparedJob != null && comparedJob.exist()){
                IFolder reportFolder = comparedJob.getFolder("bigtop_report");
                if(reportFolder.exist())
                    return reportFolder.getFile("report.xml");
            }
            return null;
        }      
	}
	
    class PantheraJUnitReportConfig extends AbstractJUnitReportConfig{
        protected Environment env;
        public PantheraJUnitReportConfig(Environment env){
            this.env = env;
        }
        @Override
        public String getTitle() {
            return "Panthera Test Summary";
        }
        @Override
        public String getDetailTitle() {
            return "Panthera Test Details";
        }        
        @Override
        public String[] getGroups() {
            List<String> components = loadComponents(env);
            List<String> groups = new ArrayList<String>();
            for(GroupPattern gp : groupPatterns){
                for(String c : components){
                    if(c.contains(gp.group)){
                        groups.add(c);
                        break;
                    }
                }
            }
            return groups.toArray(new String[]{});
        }
        @Override
        public String getGroupPattern(String group) {
            for(GroupPattern gp : groupPatterns){
                if(group.contains(gp.group))
                    return gp.pattern;
            }
            return null;
        }
        @Override
        public String getDefaultGroup() {
            return "Others";
        }
        @Override
        public IFolder getResultFolder(){
            return env.getStorageRoot().getFolder("nist");
        }
        @Override
        public IFolder getReportFolder(){
            return env.getStorageRoot().getFolder("nist_report");
        }
        @Override
        public IFile getDiffSource(){
            if(comparedJob != null && comparedJob.exist()){
                IFolder reportFolder = comparedJob.getFolder("nist_report");
                if(reportFolder.exist())
                    return reportFolder.getFile("report.xml");
            }
            return null;
        }
    }	
	
	@Override
	protected List<JUnitReportConfig> getReportConfig(final Environment env) {
		List<JUnitReportConfig> results = new ArrayList<JUnitReportConfig>();
		results.add(new IDHJUnitReportConfig(env));
		results.add(new PantheraJUnitReportConfig(env));
	    results.add(new IDHBigtopReportConfig(env));
		return results;
	}

    @Override
	protected String getTestName() {
		return "IDH" + iso_release_version + " Test";
	}
	
	@Override
	protected boolean isBuildCompleted(Environment env) {
        IFile tagFile = env.getStorageRoot().getFile(ConstHelper.FNAME_BUILD_COMPLETE_TAG);
        return tagFile.exist();	    
	}
	
	
	@Override
	protected List<TaskSummaryItem> getSummaryItems(Environment env){
        List<TaskSummaryItem> items = super.getSummaryItems(env);
        for(TaskSummaryItem i : items){
            if(i.getName().equals("Build Result")){
                i.setHyperLink(true);
                i.setUrl(env.getHyperlink(env.getStorageRoot()));
            }else if(i.getName().equals("Platform")){
                i.setValue(osVersionStr);               
            }
            continue;
        }

        TaskSummaryItem item = null;
        
        if(!isCheckIn && ! (build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_RPM))){
            item = new TaskSummaryItem();
            item.setName("IM Url");
            item.setValue(IM_svn_url);
            items.add(item);
            
            item = new TaskSummaryItem();
            item.setName("IM Rev");
            item.setValue(IM_svn_rev);
            items.add(item);
        }
        
        item = new TaskSummaryItem();
        item.setName("Target Version");
        item.setValue(iso_release_version);
        items.add(item);
        
        item = new TaskSummaryItem();
        item.setName("Target Language");
        item.setValue(iso_language);
        items.add(item);
        
        if(!(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_RPM))){
            item = new TaskSummaryItem();
            item.setName("License Type");
            item.setValue(license_type);
            items.add(item);
        }
        
        if(isBuildCompleted(env)){            
            if(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_RPM)){
                item = new TaskSummaryItem();
                item.setName("IDH RPMs");
                item.setValue(ConstHelper.FNAME_RPMS_ZIP);
                item.setHyperLink(true);
                item.setUrl(env.getHyperlink(env.getStorageRoot().getFile(ConstHelper.FNAME_RPMS_ZIP)));
                items.add(item);
            }
            
            if(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_TGZ) || build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ALL))
            {           
                item = new TaskSummaryItem();
                item.setName("IDH TGZ Package");
                
                String tgzFileName = "";
                IFile tgzBuildCompleteTagFile = env.getStorageRoot().getFile(ConstHelper.FNAME_TGZ_BUILD_COMPLETE_TAG);
                try {
                     InputStream in = tgzBuildCompleteTagFile.getContents();
                     tgzFileName = getStringFromInputStream(in, 100);
                    if( !tgzFileName.isEmpty())
                        item.setValue(tgzFileName);
                    else
                        item.setValue(ConstHelper.TEXT_NA);                     
                } catch (Exception e) {
                    e.printStackTrace();
                    item.setValue(ConstHelper.TEXT_NA);
                }
                
                if(!(item.getValue().equals(ConstHelper.TEXT_NA))){     
                    item.setHyperLink(true);
                    item.setUrl(env.getHyperlink(env.getStorageRoot().getFile(tgzFileName)));
                }
                items.add(item);
            }
            
            if(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ISO) || build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ALL))
            {           
                item = new TaskSummaryItem();
                item.setName("IDH ISO Package");
                
                String isoFileName = "";
                IFile isoBuildCompleteTagFile = env.getStorageRoot().getFile(ConstHelper.FNAME_ISO_BUILD_COMPLETE_TAG);
                try {
                     InputStream in = isoBuildCompleteTagFile.getContents();
                     isoFileName = getStringFromInputStream(in, 100);
                    if( !isoFileName.isEmpty())
                        item.setValue(isoFileName);
                    else
                        item.setValue(ConstHelper.TEXT_NA);                     
                } catch (Exception e) {
                    e.printStackTrace();
                    item.setValue(ConstHelper.TEXT_NA);
                }
                
                if(!(item.getValue().equals(ConstHelper.TEXT_NA))){     
                    item.setHyperLink(true);
                    item.setUrl(env.getHyperlink(env.getStorageRoot().getFile(isoFileName)));
                }
                items.add(item);
            }
            
        }
        // generate Nist failure report
        try{
            IFolder nistResult = env.getStorageRoot().getFolder("nist");
            if(nistResult.exist()){
                IFolder failuresFolder = nistResult.getFolder("failures");
                List<String[]> failures = new ArrayList<String[]>();
                if(failuresFolder.exist()){
                    for(IStorage f : failuresFolder.list()){
                        if(f instanceof IFile){
                            failures.addAll(loadFailures((IFile)f));
                        }
                    }
                }
                if(failures.size() > 0){
                    sortFailures(failures);
                    IFile failReport = env.getStorageRoot().getFile("nist_failures.html");
                    generateFailureReport(failures, failReport);
                    int compileFailCount = getNISTCompileFailures(failures);
                    item = new TaskSummaryItem();
                    item.setName("NIST Failures");
                    item.setStyle("Failed");
                    item.setHyperLink(true);
                    item.setUrl(env.getHyperlink(failReport));
                    item.setValue(compileFailCount + " compile errors");
                    items.add(item);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return items;	    
	}

	protected String getStringFromInputStream(InputStream in, int count) {
		 byte[] b = new byte[count];
		 int readCount = 0;
		 try {
			 while (readCount < count) {
					int tmpCount = in.read(b, readCount, count - readCount);
					if(tmpCount == -1){
						break;
					}
					readCount += tmpCount; 
			 }
		 } catch (IOException e) {
				e.printStackTrace();
		 }
		 String s = new String(b);	 
		 s = s.trim();
		 return s;
	}
	
    @Override
    public String getReportTitle(Environment env) throws Exception {
        boolean isNightly = false;
        try{
            if(Boolean.parseBoolean(env.getVariable("isNightly").getValue())){
                isNightly = true;
            }
        }
        catch(Exception e){            
        }
        StringBuilder sb = new StringBuilder();
        if (isCheckIn) {
            sb.append("[");
            sb.append(product.startsWith("IM") ? "IM" : "IDH");
            sb.append(iso_release_version);
            sb.append(":");
            sb.append(rev);
            sb.append("] Checkin Test");         
        }
        else if(isNightly){
            sb.append(product.startsWith("IM") ? "IM" : "IDH");
            sb.append(iso_release_version);
            sb.append(" Nightly Test"); 
        }
        else{
           return null;
        }
        return sb.toString();
    }

    @Override
    protected String getFeatureReport(Environment env) {
        if(this.junitResultDocList.size() != 3){
            return "";
        }
        Document nistResults = this.junitResultDocList.get(1);
        if(nistResults != null){
            collectNistResults(nistResults);
        }
        StringBuilder sb = new StringBuilder();
        if(comparedJob != null && comparedJob.exist()){
            sb.append("The following result is compared against ");
            sb.append("<a href=\"");
            sb.append(env.getHyperlink(comparedJob.getFile("job.log")));
            sb.append("\">");
            try{
                Variable v = env.getVariable("compareResult");
                sb.append(v.getValue());
                sb.append("</a>");
            }
            catch(Exception e){
            }
        }
        try{
            if(isCheckIn){
                env.setFeatureProperty("last_checkin_job", env.getStorageRoot().getURI().toString(), product);
            }
            if(Boolean.parseBoolean(env.getVariable("isNightly").getValue())){
                env.setFeatureProperty("last_nightly_job", env.getStorageRoot().getURI().toString(), product);
            }
        }
        catch(Exception e){            
        }
        return sb.toString();
    }
    
    public static String checkEnvVar(Environment env, String varName, String defaultValue) {
        String value = "";
        try {
            value = env.getVariable(varName).getValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(value.isEmpty())
            return defaultValue;
        else
            return value;
    }
    
    protected void collectNistResults(Document results) {
        // Collect Multi-Table and SubQuery cases from NIST
        String mtList = groupPatterns.get(0).pattern;
        String mtName = groupPatterns.get(0).group;
        String subList = groupPatterns.get(1).pattern;
        String subName = groupPatterns.get(1).group;
        String tpchList = groupPatterns.get(2).pattern;
        String tpchName = groupPatterns.get(2).group;
        String nistName = groupPatterns.get(3).group;
        List<Element> mtElements = new ArrayList<Element>();
        List<Element> subElements = new ArrayList<Element>();
        List<Element> tpchElements = new ArrayList<Element>();
        // case id >= 1073 categorized into UnitTests
        List<Element> utElements = new ArrayList<Element>();
        Element root = results.getDocumentElement();
        NodeList nl = root.getElementsByTagName("testsuites");
        for(int i = 0; i < nl.getLength(); i++){
            Node n = nl.item(i);
            Element e = (Element)n;
            String name = e.getAttribute("name");
            if(name.equals(nistName)){
                NodeList tl = e.getElementsByTagName("testcase");
                for(int j = 0; j < tl.getLength(); j++){
                    Node t = tl.item(j);
                    Element c = (Element)t;
                    String cn = c.getAttribute("name");
                    if(cn != null && cn.length() > 0){
                        for(String m : mtList.split("\\|")){
                            if(cn.endsWith(m)){
                                mtElements.add((Element)c.cloneNode(true));
                                break;
                            }
                        }
                        for(String m : subList.split("\\|")){
                            if(cn.endsWith(m)){
                                subElements.add((Element)c.cloneNode(true));
                                break;
                            }
                        }
                        for(String m : tpchList.split("\\|")){
                            if(cn.endsWith(m)){
                                tpchElements.add((Element)c.cloneNode(true));
                                break;
                            }
                        }
                        try{
                            String caseIdStr = cn.substring(cn.lastIndexOf("_") + 1);
                            Integer caseId = Integer.parseInt(caseIdStr);
                            if(caseId >= 1073)
                                utElements.add((Element)c.cloneNode(true));
                        }
                        catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            }
        }
        if(subElements.size() > 0){
            Element subElement = results.createElement("testsuites");
            subElement.setAttribute("name", subName);
            root.insertBefore(subElement, root.getFirstChild());
            Element subSuite = results.createElement("testsuite");
            subElement.appendChild(subSuite);
            fillTestSuite(subSuite, subElements);
        }
        if(mtElements.size() > 0){
            Element mtElement = results.createElement("testsuites");
            mtElement.setAttribute("name", mtName);
            root.insertBefore(mtElement, root.getFirstChild());
            Element mtSuite = results.createElement("testsuite");
            mtElement.appendChild(mtSuite);
            fillTestSuite(mtSuite, mtElements);
        }
        if(tpchElements.size() > 0){
            Element tpchElement = results.createElement("testsuites");
            tpchElement.setAttribute("name", tpchName);
            root.insertBefore(tpchElement, root.getFirstChild());
            Element tpchSuite = results.createElement("testsuite");
            tpchElement.appendChild(tpchSuite);
            fillTestSuite(tpchSuite, tpchElements);
        }
        if(utElements.size() > 0){
            Element utElement = results.createElement("testsuites");
            utElement.setAttribute("name", "UnitTests");
            root.insertBefore(utElement, root.getFirstChild());
            Element utSuite = results.createElement("testsuite");
            utElement.appendChild(utSuite);
            fillTestSuite(utSuite, utElements);
        }    
    }
    
    protected List<String[]> loadFailures(IFile file) throws Exception{
        List<String[]> result = new ArrayList<String[]>();
        if(file.exist()){
            InputStreamReader isr = new InputStreamReader(file.getContents());
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while((line = br.readLine()) != null){
                result.add(line.split("#"));
            }
            br.close();
        }
        return result;
    }
    
    protected void generateFailureReport(List<String[]> failures, IFile output) throws Exception{
        if(!output.exist()){
            output.create();
        }
        StringBuilder result = new StringBuilder();
        result.append("<html><body>");
        result.append("<table width=100% border=\"0\" style==\"border: 1px solid #000000><tr bgcolor=#A52A2A><th>seq</th><th>sql</th><th>diff</th></tr>");
        int count=0;
        for (String[] e : failures) {            
            result.append("<tr bgcolor="+(count++%2==0?"#00FFFF":"#FFEBCD")+"><th>");
            result.append(e[0]);
            result.append("</th><th>");
            result.append(e[1]);
            result.append("</th><th>");
            if(e.length > 2){
                result.append(e[2] == null ? "" : e[2]);
            }
            result.append("</th></tr>");
        }
        result.append("</table>");
        result.append("</body></html>");
        output.setContents(new ByteArrayInputStream(result.toString().getBytes()));
    }
    
    protected int getNISTCompileFailures(List<String[]> failures){
        int count = 0;
        for(String[] e : failures){
            if(e.length < 3 || e[2] == null || e[2].length() == 0){
                count++;
            }
        }
        return count;
    }
    
    protected void sortFailures(List<String[]> failures){
        Collections.sort(failures, new Comparator<String[]>(){

            @Override
            public int compare(String[] o1, String[] o2) {
                Integer s1 = Integer.parseInt(o1[0]);
                Integer s2 = Integer.parseInt(o2[0]);
                return s1.compareTo(s2);
            }
            
        });
    }
    
    protected boolean testFailed(Element testcase){
        int count = testcase.getChildNodes().getLength();
        for(int i = 0; i < count; i++){
            Node n = testcase.getChildNodes().item(i);
            if(n.getNodeName().equals("failure")){
                return true;
            }
        }
        return false;
    }
    
    protected boolean testError(Element testcase){
        int count = testcase.getChildNodes().getLength();
        for(int i = 0; i < count; i++){
            Node n = testcase.getChildNodes().item(i);
            if(n.getNodeName().equals("error")){
                return true;
            }
        }
        return false;
    }
    
    protected void fillTestSuite(Element suite, List<Element> cases){
        int errors = 0;
        int failures = 0;
        int tests = 0;
        float time = 0f;
        String name = null;
        for(Element e : cases){
            if(name == null)
                name = e.getAttribute("classname");
            tests++;
            if(testFailed(e))
                failures++;
            if(testError(e))
                errors++;
            String ts = e.getAttribute("time");
            if(ts != null && ts.length() > 0){
                time += Float.parseFloat(ts);                    
            }
            suite.appendChild(e);
        }
        suite.setAttribute("errors", String.format("%d", errors));
        suite.setAttribute("failures", String.format("%d", failures));
        suite.setAttribute("tests", String.format("%d", tests));
        suite.setAttribute("time", String.format("%.3f", time));
        if(name != null)
            suite.setAttribute("name", name);
    }    
    
    public static GitClient getGitClient(String url, String dest, Environment env) throws Exception{
        String username = null;
        String password = null;
        String privatekey = null;
        String proxy = null;       
        int port = 0;
        
        try{
            Variable vu = env.getVariable("git_username");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                username = vu.getValue();
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_password");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                password = vu.getValue();
        }
        catch(Exception e){           
        }
  
        try{
            Variable vu = env.getVariable("git_privatekey");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                privatekey = vu.getValue();
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_proxyhost");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                proxy = vu.getValue();   
        }
        catch(Exception e){            
        }
        
        try{
            Variable vu = env.getVariable("git_proxyport");
            if(vu.getValue() != null && vu.getValue().length() > 0)
                port = Integer.parseInt(vu.getValue());
        }
        catch(Exception e){            
        }

        GitClient git = null;
        if(password != null){
            git = new GitClient(url, dest, username, password);
        }
        else if(privatekey != null){
            git = new GitClient(url, dest, username, Utils.decodeBase64(privatekey));
        }
        if(git != null){
            git.setProxy(proxy, port);
        }
        return git;
    }
        
    public static void doCheckout(String url, String branch, String dest, String rev, Environment env) throws Exception {
        GitClient client = getGitClient(url, dest, env);
        if(client != null && client.openRepository()){
            client.checkout(branch, rev);            
        }
    }

    public static void doChangeOwner(String dest, String user, String group, Environment env) throws Exception{
        env.execute("chown -R " + user + "." + group + " \"" + dest + "\"");
    }
    
    public static void doApplyPatch(IFile file, int level, String dest, Environment env) throws Exception {
        if(file != null && file.exist()){
            File patch = new File(file.getName());
            env.copyFile(file, patch);
            env.execute(new String[]{"cd " + dest, "patch -p" + level + " < " + patch.getAbsolutePath()});
        }
    }    
    
    public static String doGetLatestRev(String url, String branch, String dest, Environment env) throws Exception {
        GitClient client = getGitClient(url, dest, env);
        if(client != null && client.openRepository()){
            client.checkout(branch);
            client.update();
            return client.getHeadCommit().getName();
        }
        return null;
    }    
}
