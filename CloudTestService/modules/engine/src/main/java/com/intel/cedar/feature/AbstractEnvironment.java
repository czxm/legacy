package com.intel.cedar.feature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.NetUtil;

public abstract class AbstractEnvironment implements Environment {
    protected ClassLoader loader;
    protected IFolder root;
    private ExecutorService exe = Executors.newCachedThreadPool();

    public AbstractEnvironment(ClassLoader loader, IFolder root) {
        this.loader = loader;
        this.root = root;
    }

    public String getCWD() {
        return System.getProperty("user.dir");
    }

    public void extractResource(String name) throws Exception {
        extractResource(name, name.substring(name.lastIndexOf("/") + 1));
    }

    public void extractResource(String name, String file) throws Exception {
        if (loader != null) {
            InputStream input = loader.getResourceAsStream(name);
            FileOutputStream output = new FileOutputStream(file);
            FileUtils.copyStream(input, output);
            output.close();
            input.close();
        }
    }

    public int execute(String[] commands) throws Exception {
        return execute(commands, new OutputStreamWriter(System.out));
    }

    public int execute(String[] commands, IFile output) throws Exception {
        boolean isWindows = getOSName().contains("Windows");
        File tmpFile = File.createTempFile("cedar", isWindows ? ".cmd" : ".sh");
        FileOutputStream stream = new FileOutputStream(tmpFile);
        for (String command : commands) {
            stream.write(command.getBytes());
            stream.write("\n".getBytes());
        }
        stream.close();
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        if (isWindows)
            pb.command(new String[] { "cmd", "/E:ON", "/V:ON", "/C",
                    tmpFile.getAbsolutePath() });
        else
            pb.command(new String[] { "sh", tmpFile.getAbsolutePath() });
        Process p = pb.start();
        InputStream pis = p.getInputStream();
        output.setContents(pis);
        int ret = p.waitFor();
        pis.close();
        tmpFile.delete();
        return ret;
    }

    public int execute(String[] commands, Writer output) throws Exception {
        boolean isWindows = getOSName().contains("Windows");
        File tmpFile = File.createTempFile("cedar", isWindows ? ".cmd" : ".sh");
        FileOutputStream stream = new FileOutputStream(tmpFile);
        for (String command : commands) {
            stream.write(command.getBytes());
            stream.write("\n".getBytes());
        }
        stream.close();
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        if (isWindows)
            pb.command(new String[] { "cmd", "/E:ON", "/V:ON", "/C",
                    tmpFile.getAbsolutePath() });
        else
            pb.command(new String[] { "sh", tmpFile.getAbsolutePath() });
        Process p = pb.start();
        InputStream pis = p.getInputStream();
        OutputStream pos = p.getOutputStream();
        InputStream pes = p.getErrorStream();
        InputStreamReader input = new InputStreamReader(pis);
        int n = 0;
        char[] buf = new char[2048];
        while ((n = input.read(buf)) != -1) {
            output.write(buf, 0, n);
            output.flush();
        }
        int ret = p.waitFor();
        pis.close();
        pos.flush();
        pes.close();
        tmpFile.delete();
        return ret;
    }

    public int executeAs(String user, String[] commands, IFile output)
            throws Exception {
        boolean isWindows = getOSName().contains("Windows");
        if (isWindows)
            throw new UnsupportedOperationException();
        File tmpFile = File.createTempFile("cedar", isWindows ? ".cmd" : ".sh");
        FileOutputStream stream = new FileOutputStream(tmpFile);
        for (String command : commands) {
            stream.write(command.getBytes());
            stream.write("\n".getBytes());
        }
        stream.close();
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        if (isWindows)
            pb.command(new String[] { "cmd", "/E:ON", "/V:ON", "/C",
                    tmpFile.getAbsolutePath() });
        else
            pb.command(new String[] { "sudo", "-u", user,
                    "sh", tmpFile.getAbsolutePath()});
        Process p = pb.start();
        InputStream pis = p.getInputStream();
        output.setContents(pis);
        int ret = p.waitFor();
        pis.close();
        tmpFile.delete();
        return ret;
    }

    public int executeAs(String user, String[] commands, Writer output)
            throws Exception {
        boolean isWindows = getOSName().contains("Windows");
        if (isWindows)
            throw new UnsupportedOperationException();
        File tmpFile = File.createTempFile("cedar", isWindows ? ".cmd" : ".sh");
        FileOutputStream stream = new FileOutputStream(tmpFile);
        for (String command : commands) {
            stream.write(command.getBytes());
            stream.write("\n".getBytes());
        }
        stream.close();
        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);
        if (isWindows)
            pb.command(new String[] { "cmd", "/E:ON", "/V:ON", "/C",
                    tmpFile.getAbsolutePath() });
        else
            pb.command(new String[] { "sudo", "-u", user,
                    "sh", tmpFile.getAbsolutePath()});
        Process p = pb.start();
        InputStream pis = p.getInputStream();
        OutputStream pos = p.getOutputStream();
        InputStream pes = p.getErrorStream();
        InputStreamReader input = new InputStreamReader(pis);
        int n = 0;
        char[] buf = new char[2048];
        while ((n = input.read(buf)) != -1) {
            output.write(buf, 0, n);
            output.flush();
        }
        int ret = p.waitFor();
        pis.close();
        pos.flush();
        pes.close();
        tmpFile.delete();
        return ret;
    }

    public int execute(String command) throws Exception {
        return execute(command, new OutputStreamWriter(System.out));
    }

    public int execute(String command, Writer output) throws Exception {
        return execute(new String[] { command }, output);
    }

    public void asyncExec(Runnable runable) {
        if (runable != null) {
            exe.submit(runable);
        }
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public String getArchitecture() {
        if (System.getProperty("java.vm.name").contains("64-Bit")) {
            return "x86_64";
        } else {
            return "x86";
        }
    }
    
    public String getHostName() {
        return NetUtil.getHostName();
    }
    
    public IFolder getStorageRoot() {
        return this.root;
    }

    public void copyFile(IFile src, File dest) throws Exception {
        InputStream stream = src.getContents();
        FileOutputStream ous = new FileOutputStream(dest);
        FileUtils.copyStream(stream, ous);
        ous.close();
        stream.close();
    }

    public void copyFile(File src, IFile dest) throws Exception {
        FileInputStream stream = new FileInputStream(src);
        dest.setContents(stream);
        stream.close();
    }
    
    public void copyFolder(IFolder src, File dest) throws Exception {
        if(dest.exists()){
            if(!dest.isDirectory()){
                throw new RuntimeException(dest.getName() + " is not a directory");
            }
        }
        else{
             if(!dest.mkdir()){
                 throw new RuntimeException("Failed to create " + dest.getName());
             }
        }
        if(!src.exist()){
            throw new RuntimeException("Cannot find " + src.getName());
        }
        for(IStorage s : src.list()){
            if(s instanceof IFile){
                IFile f = (IFile)s;
                File df = new File(dest, f.getName());
                copyFile(f, df);
            }
            else if(s instanceof IFolder){
                IFolder d = (IFolder)s;
                copyFolder(d, new File(dest, d.getName()));
            }
        }
    }

    public void copyFolder(File src, IFolder dest) throws Exception {
        if(!dest.exist() && !dest.create()){
            throw new RuntimeException("Failed to create " + dest.getName());
        }
        if(!src.exists()){
            throw new RuntimeException("Cannot find " + src.getName());
        }
        for(File s : src.listFiles()){
            if(s.isFile()){
                IFile df = dest.getFile(s.getName());
                copyFile(s, df);
            }
            else if(s.isDirectory()){
                IFolder d = dest.getFolder(s.getName());
                copyFolder(s, d);
            }
        }
    }

    public String getHyperlink(IStorage storage) {
        return CedarConfiguration.getStorageServiceURL() + "?cedarURL="
                + storage.getURI().toString();
    }
}
