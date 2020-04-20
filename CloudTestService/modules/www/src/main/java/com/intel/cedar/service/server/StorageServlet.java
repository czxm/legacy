package com.intel.cedar.service.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.storage.impl.LocalFile;
import com.intel.cedar.storage.impl.LocalFolder;
import com.intel.cedar.util.Utils;
import com.intel.xml.rss.util.DateTimeRoutine;

public class StorageServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IFolder root;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        URI cedarURL = null;
        String cedarURLParam = request.getParameter("cedarURL");
        if (cedarURLParam == null)
            cedarURL = root.getURI();
        else{
            cedarURL = URI.create(Utils.encodeURL(cedarURLParam));
        }
        if (cedarURL.getPath().endsWith("/")) {
            writeHead(response, cedarURL.toString());
            if (!cedarURL.equals(root.getURI())) {
                String temp = cedarURL.getPath().substring(0, cedarURL.getPath().length() - 1);
                writeEntry(response, temp.substring(0,
                        temp.lastIndexOf("/") + 1), "..");
            }
            IFolder folder = root.getFolder(cedarURL);
            File dir = ((LocalFolder) folder).toFile();
            if (dir.isDirectory()) {
                File[] list = dir.listFiles();
                ArrayList<File> sorted = new ArrayList<File>();
                for (File i : list)
                    sorted.add(i);
                Collections.sort(sorted, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        if (o1.lastModified() > o2.lastModified())
                            return 1;
                        else
                            return 0;
                    }
                });
                for (File n : sorted) {
                    if (n.isDirectory()) {
                        writeEntry(response, folder.getFolder(n.getName()).getURI().toString(), n
                                .getName()
                                + "/", n.lastModified());
                    } else {
                        if (n.getName().endsWith(".png")
                                || n.getName().endsWith(".jpg")
                                || n.getName().endsWith(".gif"))
                            writeImage(response, folder.getFile(n.getName()).getURI().toString(), n
                                    .getName(), n.length(), n.lastModified());
                        else
                            writeEntry(response, folder.getFile(n.getName()).getURI().toString(), n
                                    .getName(), n.length(), n.lastModified());
                    }
                }
            }
            writeEnd(response);
        } else {
            IFile file = root.getFile(cedarURL);
            File theFile = ((LocalFile) file).toFile();
            if (theFile.isDirectory()) {
                response.sendRedirect(request.getRequestURI() + "?"
                        + request.getQueryString() + "/");
                return;
            }
            if (theFile.canRead()) {
                String fileName = theFile.getName();
                if (!fileName.endsWith(".log") && !fileName.endsWith(".htm")
                        && !fileName.endsWith(".xml")
                        && !fileName.endsWith(".html")
                        && !fileName.endsWith(".txt")
                        && !fileName.endsWith(".out")
                        && !fileName.endsWith(".png")
                        && !fileName.endsWith(".jpg")
                        && !fileName.endsWith(".gif")) {
                    response.setContentType("application/x-download");
                    response.setHeader("Content-Disposition",
                            "attachment; filename=" + fileName);
                }
                InputStream input = null;
                OutputStream output = null;
                try {
                    input = file.getContents();
                    output = response.getOutputStream();
                    if (fileName.endsWith(".log") || fileName.endsWith(".txt") || fileName.endsWith(".out")) {
                        output.write("<PRE>".getBytes());
                        output.flush();
                    }
                    FileUtils.copyStream(input, output);
                    if (fileName.endsWith(".log") || fileName.endsWith(".txt") || fileName.endsWith(".out")) {
                        output.write("</PRE>".getBytes());
                        output.flush();
                    }
                } catch (Exception e) {
                } finally {
                    if (input != null)
                        input.close();
                    if (output != null)
                        output.close();
                }
            }
        }
    }

    protected void writeHead(HttpServletResponse response, String url)
            throws IOException {
        url = Utils.decodeURL(url);
        response.getWriter().print(
                "<html><head><title>" + url
                        + "</title></head><body><h2>Index of " + url + "</h2>");
        response
                .getWriter()
                .print(
                        "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">");
        response.getWriter().print("<tr>");
        response
                .getWriter()
                .print(
                        "<td align=\"left\"><font size=\"+1\"><strong>Filename</strong></font></td>");
        response
                .getWriter()
                .print(
                        "<td align=\"center\"><font size=\"+1\"><strong>Last Modified</strong></font></td>");
        response
                .getWriter()
                .print(
                        "<td align=\"right\"><font size=\"+1\"><strong>Size</strong></font></td>");
        response.getWriter().print("</tr>");
    }

    protected void writeEnd(HttpServletResponse response) throws IOException {
        response.getWriter().print("</table></body></html>");
    }

    protected void writeEntry(HttpServletResponse response, String url,
            String name) throws IOException {
        url = Utils.decodeURL(url);
        response.getWriter().print("<tr>");
        response.getWriter().print(
                "<td align=\"left\">&nbsp;&nbsp;<a href=\""
                        + "/cloudtestservice/storage?cedarURL=" + url
                        + "\"><tt>" + name + "</tt></a></td>");
        response.getWriter().print("<td align=\"center\"><tt></tt></td>");
        response.getWriter().print("<td align=\"right\"><tt></tt></td>");
        response.getWriter().print("</tr>");
    }

    protected void writeEntry(HttpServletResponse response, String url,
            String name, Long modTime) throws IOException {
        url = Utils.decodeURL(url);
        response.getWriter().print("<tr>");
        response.getWriter().print(
                "<td align=\"left\">&nbsp;&nbsp;<a href=\""
                        + "/cloudtestservice/storage?cedarURL=" + url
                        + "\"><tt>" + name + "</tt></a></td>");
        response.getWriter().print(
                String.format("<td align=\"center\"><tt>%s</tt></td>",
                        DateTimeRoutine.millisToStdTimeString(modTime)));
        response.getWriter().print("<td align=\"right\"><tt></tt></td>");
        response.getWriter().print("</tr>");
    }

    protected void writeEntry(HttpServletResponse response, String url,
            String name, Long size, Long modTime) throws IOException {
        url = Utils.decodeURL(url);
        response.getWriter().print("<tr>");
        response.getWriter().print(
                "<td align=\"left\">&nbsp;&nbsp;<a href=\""
                        + "/cloudtestservice/storage?cedarURL=" + url
                        + "\"><tt>" + name + "</tt></a></td>");
        response.getWriter().print(
                String.format("<td align=\"center\"><tt>%s</tt></td>",
                        DateTimeRoutine.millisToStdTimeString(modTime)));
        String unit = "KB";
        float uSize = size.floatValue() / 1024;
        if (uSize > 10000) {
            unit = "MB";
            uSize = uSize / 1024;
        }
        response.getWriter().print(
                String.format("<td align=\"right\"><tt>%.02f %s</tt></td>",
                        uSize, unit));
        response.getWriter().print("</tr>");
    }

    protected void writeImage(HttpServletResponse response, String url,
            String name, Long size, Long modTime) throws IOException {
        url = Utils.decodeURL(url);
        response.getWriter().print("<tr>");
        response.getWriter().print(
                "<td align=\"left\">&nbsp;&nbsp;<img src=\""
                        + "/cloudtestservice/storage?cedarURL=" + url
                        + "\"></img>" + name + "</td>");
        response.getWriter().print(
                String.format("<td align=\"center\"><tt>%s</tt></td>",
                        DateTimeRoutine.millisToStdTimeString(modTime)));
        String unit = "KB";
        float uSize = size.floatValue() / 1024;
        if (uSize > 10000) {
            unit = "MB";
            uSize = uSize / 1024;
        }
        response.getWriter().print(
                String.format("<td align=\"right\"><tt>%.02f %s</tt></td>",
                        uSize, unit));
        response.getWriter().print("</tr>");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        root = StorageFactory.getInstance().getStorage().getRoot();
    }
}
