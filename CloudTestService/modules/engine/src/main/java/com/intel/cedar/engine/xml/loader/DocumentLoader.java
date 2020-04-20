package com.intel.cedar.engine.xml.loader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.xml.sax.XMLReader;

import com.intel.cedar.engine.xml.Configuration;
import com.intel.cedar.engine.xml.NamePool;
import com.intel.cedar.engine.xml.model.DocumentImpl;

public class DocumentLoader {
    protected Configuration config;
    protected DocumentImpl document;
    protected NamePool pool;

    public DocumentLoader() {
        config = new Configuration();
        document = new DocumentImpl();
        document.setConfiguration(config);
        pool = document.getNamePool();
    }

    public DocumentImpl load(String file) {
        try {
            InputStream fileStream = new FileInputStream(file);
            return load(fileStream);
        } catch (FileNotFoundException e) {
            // TO BE IMPROVED
            // deal with exception
            return null;
        }
    }

    public DocumentImpl load(InputStream inputStream) {
        StreamSource xmlSource;
        try {
            xmlSource = new StreamSource(inputStream);
            NodeFactory nodeFactory = new DefaultNodeFactory();
            TreeBuilder sourceBuilder = new TreeBuilder();
            ReceiverConfiguration receiverConfiguration = ReceiverConfiguration
                    .makeReceiverConfiguration(config);
            sourceBuilder.setReceiverConfiguration(receiverConfiguration);
            // sourceBuilder.setSystemId(schemaFile.getLocation().toOSString());
            sourceBuilder.setNodeFactory(nodeFactory);

            StartTagBuffer startTagBuffer = new StartTagBuffer();
            startTagBuffer.setUnderlyingReceiver(sourceBuilder);

            Stripper sourceStripper = new Stripper();
            sourceStripper.setUnderlyingReceiver(startTagBuffer);

            Sender sender = new Sender(receiverConfiguration);
            ProxySource proxy = ProxySource.makeProxySource(xmlSource);
            proxy.setLineNumbering(true);
            if (proxy.getXMLReader() == null) {
                XMLReader sourceParser = config.getSourceParser();
                proxy.setXMLReader(sourceParser);
                sender.send(proxy, sourceStripper);
                config.reuseSourceParser(sourceParser);
            }
            document = (DocumentImpl) sourceBuilder.getCurrentRoot();

            if (proxy.isPleaseCloseAfterUse()) {
                proxy.close();
            }
        } catch (Exception e) {
            // TO BE IMPROVED
            // deal with exception
            return null;
        }
        return document;
    }
}
