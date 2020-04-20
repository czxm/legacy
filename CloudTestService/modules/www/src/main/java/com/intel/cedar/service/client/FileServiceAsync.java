/**
 * 
 */
package com.intel.cedar.service.client;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.filebrowser.controller.FileListLoadConfig;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * The client file remote service
 * @author Eric Taix
 */
public interface FileServiceAsync {

  /**
   * Return a list of FileModel according to a directory
   * @param directoryP
   * @return
   */
  public void getFiles(FileListLoadConfig configP, AsyncCallback<BaseListLoadResult<FileModel>> callback);

}
