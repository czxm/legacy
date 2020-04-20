package com.intel.cedar.tasklet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.intel.cedar.feature.Environment;

public class SimpleTaskRunner extends AbstractTaskRunner {
    private static final long serialVersionUID = 6840868760025427466L;
    protected String pendingItemsFile = ".pending.items";
    
    protected void doStart(Environment env) throws Exception {
        
    }
    
    protected boolean doTask(SimpleTaskItem item, Environment env) throws Exception {
        return true;
    }
    
    protected void doFinish(SimpleTaskItem[] pendingItems, Environment env) throws Exception {
        
    }
    
    protected InputStream getTaskRunnerConfig(Environment env){
        try{
            env.extractResource("taskrunner.xml");
            return new FileInputStream("taskrunner.xml");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    
    protected static class ExecuteEntity{
        String content;
        String asUser;
        
        public ExecuteEntity(String content, String asUser){
            this.content = content;
            this.asUser = asUser;
        }
        
        public String getContent(){
            return this.content;
        }
        
        public String getAsUser(){
            return this.asUser;
        }
    }
    
    protected Document parseConfig(InputStream ins) throws Exception{
        DocumentBuilderFactory docFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(ins);
    }
    
    protected String expandVariables(String content, Environment env) throws Exception{
        String result = content;
        int startIndex = 0;
        while(startIndex < content.length()){
            int i = result.indexOf("%", startIndex);
            if(i < 0){
                break;
            }
            int j = result.indexOf("%", i + 1);
            if(j < 0){
                break;
            }
            if(i + 1 < j){
                String v = result.substring(i + 1, j);
                result = result.replaceAll("%" + v + "%", env.getVariable(v).getValue());
            }
            startIndex = j + 1;   
        }
        return result;
    }
    
    protected String getItemProperty(ITaskItem item, String prop){
        if(item instanceof SimpleTaskItem){
            return ((SimpleTaskItem)item).getProperty(prop, "null");
        }
        else{
            try{
                Method m = item.getClass().getMethod("get" + prop.substring(0, 1).toUpperCase() + prop.substring(1));
                return m.invoke(item).toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            return "null";
        }
    }
    
    protected String expandVariables(String content, Environment env, ITaskItem item) throws Exception{
        String result = content;
        int startIndex = 0;
        while(startIndex < result.length()){
            int i = result.indexOf("%", startIndex);
            if(i < 0){
                break;
            }
            int j = result.indexOf("%", i + 1);
            if(j < 0){
                break;
            }
            if(i + 1 < j){
                String v = result.substring(i + 1, j);
                if(v.startsWith(".")){
                    result = result.replaceAll("%" + v + "%", getItemProperty(item, v.substring(1)));
                }
                else{
                    result = result.replaceAll("%" + v + "%", env.getVariable(v).getValue());
                }
            }
            startIndex = i + 1;   
        }
        return result;
    }
    
    protected ExecuteEntity getStartCommands(Document config, Environment env) throws Exception{
        NodeList nl = config.getElementsByTagName("onStart");
        for(int i = 0; i < nl.getLength(); i++){
            NodeList cnl = ((Element)nl.item(i)).getElementsByTagName("execute");
            for(int j = 0; j < cnl.getLength(); j++){
                Element cn = (Element)cnl.item(j);
                if(cn.getAttribute("os") == null || env.getOSName().contains(cn.getAttribute("os"))){
                    String content = expandVariables(cn.getTextContent(), env);
                    String asUser = cn.getAttribute("runas");
                    return new ExecuteEntity(content, asUser);
                }
            }
        }
        return null;
    }
    
    protected ExecuteEntity getFinishCommands(Document config, Environment env) throws Exception{
        NodeList nl = config.getElementsByTagName("onFinish");
        for(int i = 0; i < nl.getLength(); i++){
            NodeList cnl = ((Element)nl.item(i)).getElementsByTagName("execute");
            for(int j = 0; j < cnl.getLength(); j++){
                Element cn = (Element)cnl.item(j);
                if(cn.getAttribute("os") == null || env.getOSName().contains(cn.getAttribute("os"))){
                    String content = expandVariables(cn.getTextContent(), env);
                    String asUser = cn.getAttribute("runas");
                    return new ExecuteEntity(content, asUser);
                }
            }
        }
        return null;
    }
    
    protected ExecuteEntity getItemCommands(ITaskItem item, Document config, Environment env) throws Exception{
        NodeList nl = config.getElementsByTagName("item");
        for(int i = 0; i < nl.getLength(); i++){
            Element nc = (Element)nl.item(i);
            Matcher matcher = null;
            if(nc.getAttribute("value") != null){
                Pattern pat = Pattern.compile(nc.getAttribute("value"));
                matcher = pat.matcher(item.getValue());
                if(!matcher.matches())
                   continue;
            }
            
            String content = null;
            String asUser = null;
            NodeList cnl = nc.getElementsByTagName("execute");
            for(int j = 0; j < cnl.getLength(); j++){
                Element cn = (Element)cnl.item(j);
                if(cn.getAttribute("os") == null || env.getOSName().contains(cn.getAttribute("os"))){
                    content = cn.getTextContent();
                    asUser = cn.getAttribute("runas");
                    break;
                }
            }
            
            if(content != null){
                if(matcher != null && matcher.groupCount() > 0){
                    for(int k = 1; k <= matcher.groupCount(); k++){
                        content = content.replaceAll("%" + k + "%", matcher.group(k));
                    }
                }
                return new ExecuteEntity(expandVariables(content, env, item), asUser);
            }
        }
        return null;
    }
    
    protected SimpleTaskItem[] getPendingItems(){
        if(new File(pendingItemsFile).exists()){
            try{
                FileInputStream ins = new FileInputStream(pendingItemsFile);
                ObjectInputStream ois = new ObjectInputStream(ins);
                Object o = ois.readObject();
                ois.close();
                if(o instanceof SimpleTaskItem[])
                    return (SimpleTaskItem[])o;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        return new SimpleTaskItem[]{};
     }
    
     protected void addPendingItem(SimpleTaskItem item){
         try{
             SimpleTaskItem[] curItems = getPendingItems();
             SimpleTaskItem[] items = new SimpleTaskItem[curItems.length + 1];
             for(int i = 0; i < curItems.length; i++){
                 items[i] = curItems[i];
             }
             items[curItems.length] = item;
             FileOutputStream ous = new FileOutputStream(pendingItemsFile);
             ObjectOutputStream oos = new ObjectOutputStream(ous);
             oos.writeObject(items);
             oos.close();
         }
         catch(Exception e){
             e.printStackTrace();
         }
     }
     
     protected void removePendingItem(SimpleTaskItem item){
         try{
             SimpleTaskItem[] curItems = getPendingItems();
             SimpleTaskItem[] items = new SimpleTaskItem[curItems.length - 1];
             int c = 0;
             for(int i = 0; i < curItems.length; i++){
                 if(!curItems[i].equals(item)){
                     items[c] = curItems[i];
                     c++;
                 }
             }
             FileOutputStream ous = new FileOutputStream(pendingItemsFile);
             ObjectOutputStream oos = new ObjectOutputStream(ous);
             oos.writeObject(items);
             oos.close();
         }
         catch(Exception e){
             e.printStackTrace();
         }
    }    
    
    @Override
    public void onStart(Environment env){
        InputStream ins = getTaskRunnerConfig(env);
        OutputStreamWriter sw = new OutputStreamWriter(System.out);
        try{
            Document config = parseConfig(ins);
            ExecuteEntity ee = getStartCommands(config, env);
            if(ee != null){
                String commands = ee.getContent();
                if(commands != null && commands.length() > 0){
                    if(ee.getAsUser() != null && ee.getAsUser().length() > 0)
                        env.executeAs(ee.getAsUser(), new String[]{commands}, sw);
                    else
                        env.execute(new String[]{commands}, sw);
                }
            }
            doStart(env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(ins != null){
                try{
                    ins.close();
                    sw.flush();
                    sw.close();
                }
                catch(Exception e){          
                }
            }
        }
    }

    @Override
    public ResultID run(ITaskItem ti, Writer output, Environment env) {
        boolean passed = false;
        InputStream ins = getTaskRunnerConfig(env);
        SimpleTaskItem item = null;        
        if(ti instanceof SimpleTaskItem){
            item = (SimpleTaskItem)ti;
        }
        try{
            if(item != null)
                addPendingItem(item);
            Document config = parseConfig(ins);
            ExecuteEntity ee = getItemCommands(ti, config, env);
            if(ee != null){
                String commands = ee.getContent();
                if(commands != null && commands.length() > 0){
                    if(ee.getAsUser() != null && ee.getAsUser().length() > 0)
                        env.executeAs(ee.getAsUser(), new String[]{commands}, output);
                    else
                        env.execute(new String[]{commands}, output);
                }
            }
            passed = doTask(item, env);
        }
        catch(Exception e){
            e.printStackTrace();
            passed = false;
        }
        finally{
            if(ins != null){
                try{
                    ins.close();
                }
                catch(Exception e){          
                }
            }
            if(item != null){
                removePendingItem(item);
            }
        }
        return passed ? ResultID.Passed : ResultID.Failed;
    }

    @Override
    public void onFinish(Environment env){
        InputStream ins = getTaskRunnerConfig(env);
        OutputStreamWriter sw = new OutputStreamWriter(System.out);
        try{
            Document config = parseConfig(ins);
            ExecuteEntity ee = getFinishCommands(config, env);
            if(ee != null){
                String commands = ee.getContent();
                if(commands != null && commands.length() > 0){
                    if(ee.getAsUser() != null && ee.getAsUser().length() > 0)
                        env.executeAs(ee.getAsUser(), new String[]{commands}, sw);
                    else
                        env.execute(new String[]{commands}, sw);
                }
            }
            doFinish(getPendingItems(), env);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            if(ins != null){
                try{
                    ins.close();
                    sw.flush();
                    sw.close();
                }
                catch(Exception e){          
                }
            }
        }
    }
}
