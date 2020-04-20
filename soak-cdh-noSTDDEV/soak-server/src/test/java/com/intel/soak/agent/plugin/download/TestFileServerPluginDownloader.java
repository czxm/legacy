package com.intel.soak.agent.plugin.download;

import com.intel.soak.plugin.dto.PluginInfo;
import com.intel.soak.plugin.loader.RemotePluginFetcher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: joshua
 * Date: 11/7/13
 * Time: 11:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestFileServerPluginDownloader {

//    @Test
    public void testDownload() throws MalformedURLException, UnknownHostException {
        RemotePluginFetcher downloader = new RemotePluginFetcher();
        File destDir = new File("jobid");
        URL url = new URL("http", "127.0.0.1", 9447, "/soak-plugin-helloworld-0.0.1-SNAPSHOT.jar");
        PluginInfo info1 = new PluginInfo.Builder("a", "xx.jar", url).build();
        List<PluginInfo> infoList = new ArrayList<PluginInfo>();
        infoList.add(info1);
        downloader.fetch(destDir, infoList.toArray(new PluginInfo[infoList.size()]));
    }

}
