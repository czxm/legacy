/**
 * 
 */
package com.intel.cedar.service.server.filebrowser;

import java.util.List;

import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * A simple interface which substitute a virtual directory to a real directory
 * @author Eric Taix
 */
public interface FileSystem {
    
  /**
   * Return root directories
   * @param directoryP
   * @return
   */
  public List<FileModel> getRootDirectories();

  /**
   * Return files from a specific sub directory
   * @param directoryP
   * @return
   */
  public List<FileModel> getFiles(FileModel directoryP);
  
}
