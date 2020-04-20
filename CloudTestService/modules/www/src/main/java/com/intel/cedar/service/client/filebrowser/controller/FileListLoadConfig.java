/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.controller;

import com.extjs.gxt.ui.client.data.BaseListLoadConfig;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * 
 * @author Eric Taix
 *
 */
@SuppressWarnings("serial")
public class FileListLoadConfig extends BaseListLoadConfig {

  /** The directory where to find files */
  private FileModel homeDirectory;

  /**
   * @return the homeDirectory
   */
  public FileModel getHomeDirectory() {
    return homeDirectory;
  }

  /**
   * @param homeDirectoryP the homeDirectory to set
   */
  public void setHomeDirectory(FileModel homeDirectoryP) {
    homeDirectory = homeDirectoryP;
  }
  
  
  
}
