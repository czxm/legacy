package com.intel.xml.rss.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WebXmlFragmentMerger {
    public WebXmlFragmentMerger() {
    }

    public void doMerge(File webXmlFile, File fragmentFile,
            String outputFileName) throws IOException {
        long webXmlTS = webXmlFile.lastModified();
        long fragmentTS = fragmentFile.lastModified();
        File outputFile = new File(outputFileName);
        if (outputFile.exists()) {
            long l = outputFile.lastModified();
            if (l > webXmlTS && l > fragmentTS) {
                System.out.println("Skip merge");
                return;
            }
        }

        BufferedReader breader1 = new BufferedReader(new FileReader(webXmlFile));
        BufferedWriter bwriter = new BufferedWriter(new FileWriter(
                outputFileName));
        String line1 = breader1.readLine();
        while (line1 != null) {
            if (line1.indexOf("<display-name>") >= 0
                    || line1.indexOf("<icon>") >= 0
                    || line1.indexOf("<description>") >= 0
                    || line1.indexOf("<distributable>") >= 0
                    || line1.indexOf("<context-param>") >= 0) {
                BufferedReader breader2 = new BufferedReader(new FileReader(
                        fragmentFile));
                String line2 = breader2.readLine();
                while (line2 != null) {
                    bwriter.write(line2);
                    bwriter.newLine();
                    line2 = breader2.readLine();
                }
                breader2.close();
                break;
            }
            bwriter.write(line1);
            bwriter.newLine();
            line1 = breader1.readLine();
        }
        while (line1 != null) {
            bwriter.write(line1);
            bwriter.newLine();
            line1 = breader1.readLine();
        }
        breader1.close();
        bwriter.close();
        System.out.println("Merged " + webXmlFile.getName() + " and "
                + fragmentFile.getName());
    }

    public static void main(String[] args) {
        try {
            new WebXmlFragmentMerger().doMerge(new File(args[0]), new File(
                    args[1]), args[2]);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
