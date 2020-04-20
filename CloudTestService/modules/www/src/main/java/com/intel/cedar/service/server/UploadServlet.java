package com.intel.cedar.service.server;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.storage.impl.LocalFile;

public class UploadServlet extends HttpServlet {
    private IFolder root;
    private static final long serialVersionUID = 1L;

    protected IFile saveUploadedFile(DiskFileItem item) throws Exception{
        String origin = item.getName();
        String orgFileName;
        int lastSep = origin.lastIndexOf('/') != -1 ? origin
                .lastIndexOf('/') : origin.lastIndexOf('\\');
        if (lastSep == -1)
            orgFileName = origin;
        else
            orgFileName = origin.substring(lastSep + 1);

        IFolder uploadRoot = root.getFolder("upload");
        String filename = orgFileName + "." + System.currentTimeMillis();
        IFile uploadFile = uploadRoot.getFile(filename);
        uploadFile.create();
        //TODO: assumes local FileSystem
        item.write(((LocalFile)uploadFile).toFile());
        return uploadFile;
    }
    
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            FileItemFactory fileItemFactory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
            List form = upload.parseRequest(request);
            for (Iterator it = form.iterator(); it.hasNext();) {
                DiskFileItem item = (DiskFileItem) it.next();
                if(item.getSize() == 0)
                    continue;
                if ("feature".equals(item.getFieldName())
                        && !item.isFormField()) {
                    HttpSession session = request.getSession();
                    IFile file = saveUploadedFile(item);
                    session.setAttribute("UploadedFeaturePath", ((LocalFile)file).toFile().toString());
                }
                else if(item.getFieldName().startsWith("upload") && !item.isFormField()){
                    HttpSession session = request.getSession();
                    IFile file = saveUploadedFile(item);
                    String encUrl = file.getURI().toString();
                    session.setAttribute("UploadedFile", encUrl.replace("%3A%2F%2F", "://").replaceAll("%2F", "/"));
                }
                item.getStoreLocation().delete();
            }
        } catch (Exception e) {
            String message = "Failed to upload feature";
            writeResponse(response, message);
        }

        writeResponse(response, "OK");
    }

    public static String getUploadedFeaturePath(HttpSession session) {
        return (String) session.getAttribute("UploadedFeaturePath");
    }
    
    public static String getUploadedFile(HttpSession session) {
        return (String) session.getAttribute("UploadedFile");
    }

    protected void writeResponse(HttpServletResponse response, String text)
            throws IOException {
        response.getWriter().print("<html><body>" + text + "</body></html>");
    }
    
    @Override
    public void init() throws ServletException {
        super.init();
        root = StorageFactory.getInstance().getStorage().getRoot();
        IFolder uploadRoot = root.getFolder("upload");
        if(!uploadRoot.exist()){
            uploadRoot.create();
        }
    }
}
