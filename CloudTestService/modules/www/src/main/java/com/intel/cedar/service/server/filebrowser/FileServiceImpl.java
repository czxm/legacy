package com.intel.cedar.service.server.filebrowser;

import java.util.List;

import org.apache.log4j.Logger;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.intel.cedar.service.client.FileService;
import com.intel.cedar.service.client.filebrowser.controller.FileListLoadConfig;
import com.intel.cedar.service.client.filebrowser.model.FileModel;
import com.intel.cedar.service.server.ContextListener;

/**
 * The file service implementation
 * 
 * @author Eric Taix
 */
@SuppressWarnings("serial")
public class FileServiceImpl extends RemoteServiceServlet implements FileService {

  private static final Logger log = Logger.getLogger(FileServiceImpl.class);
  
  /*
   * (non-Javadoc)
   * 
   * @see ext.ux.files.client.FileService#getFiles(ext.ux.files.client.dialog.model.FileModel)
   */
  public BaseListLoadResult<FileModel> getFiles(FileListLoadConfig configP) {
    FileModel directoryP = configP.getHomeDirectory();
    FileSystem fileSystem = (FileSystem)ContextListener.getFactory().getBean("fileSystem");
    List<FileModel> files = fileSystem.getFiles(directoryP);
    if (log.isDebugEnabled()) {
      String dir = "ROOT";
      if (directoryP != null) {
        dir = directoryP.getAbsoluteName();
      }
      log.debug("Result count for directory: "+dir+" is "+files.size());
    }
    BaseListLoadResult<FileModel> result = new BaseListLoadResult<FileModel>(files);
    return result;
  }

}
