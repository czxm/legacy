package com.intel.soak.plugin.loader;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Joshua Yao (yi.a.yao@intel.com)
 * @since: 12/19/13 10:59 PM
 */
public enum BeanReader {

    INSTANCE;

    public static class BeanDefinition {
        private String id;
        private String clazz;

        public BeanDefinition(String id, String clazz) {
            this.id = id;
            this.clazz = clazz;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public String toString() {
            return String.format("[id=%s,class=%s]", this.id, this.clazz);
        }
    }

    public List<BeanDefinition> load(UrlResource xmlResource) {
        Assert.notNull(xmlResource, "spring xml must not be null");
        List<BeanDefinition> bds = new ArrayList<BeanDefinition>();
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(xmlResource.getInputStream());

            Map<String, String> namespace = new HashMap<String, String>();
            namespace.put("ns", "http://www.springframework.org/schema/beans");

            XPath xPath = document.createXPath("//ns:beans/ns:bean");
            xPath.setNamespaceURIs(namespace);

            List<Element> beans = xPath.selectNodes(document);
            for (Element bean : beans) {
                String id = bean.attributeValue("id");
                String clazz = bean.attributeValue("class");
                bds.add(new BeanDefinition(id, clazz));
            }
            return bds;
        } catch (Exception e) {
            throw new RuntimeException("Read bean definitions failed.", e);
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        List<BeanDefinition> rs = BeanReader.INSTANCE.load(new UrlResource("file:///home/joshua/idh/sourcecode/QA/perf/soak/soak-plugins/mapred/src/main/resources/mapred-plugin.xml"));
        for (BeanDefinition r : rs) {
            System.out.println(r);
        }
    }

}
