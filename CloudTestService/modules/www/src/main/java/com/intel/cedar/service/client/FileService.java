/**
 * 
 */
package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.intel.cedar.service.client.filebrowser.controller.FileListLoadConfig;
import com.intel.cedar.service.client.filebrowser.model.FileModel;
/**
 * The client file remote service
 * @author Eric Taix
 */
@RemoteServiceRelativePath("browse")
public interface FileService extends RemoteService {

  public static class Util {
    public static FileServiceAsync getInstance() {
      return GWT.create(FileService.class);
    }
  }
  
  /**
   * Return a list of FileModel according to a directory
   * @param directoryP
   * @return
   */
  public BaseListLoadResult<FileModel> getFiles(FileListLoadConfig configP);

}
