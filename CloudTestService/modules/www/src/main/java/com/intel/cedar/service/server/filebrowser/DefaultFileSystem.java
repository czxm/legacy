/**
 * 
 */
package com.intel.cedar.service.server.filebrowser;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * Default implementation of DirectorySubsitutor. Only tranform a directory
 * name to another if the source starts with specific keyword
 * 
 * @author Eric Taix
 */
public class DefaultFileSystem implements FileSystem {

  // Map association between a virtual and a real directory
  private Map<String, String> directories = new HashMap<String, String>();
  // An icon provider
  private DescriptionProvider descriptionProvider;
  
  /**
   * @return the directories
   */
  public Map<String, String> getDirectories() {
    return directories;
  }

  /**
   * @param directoriesP the directories to set
   */
  public void setDirectories(Map<String, String> directoriesP) {
    directories = directoriesP;
  }

  /**
   * @return the iconProvider
   */
  public DescriptionProvider getDescriptionProvider() {
    return descriptionProvider;
  }

  /**
   * @param iconProviderP the iconProvider to set
   */
  public void setDescriptionProvider(DescriptionProvider descriptionProviderP) {
  	descriptionProvider = descriptionProviderP;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.ux.files.server.FileSystem#getFiles(ext.ux.files.client.dialog.model.FileModel)
   */
  public List<FileModel> getFiles(FileModel directoryP) {
    // Si root directories requested
    if (directoryP == null) {
      return getRootDirectories();
    }

    List<FileModel> result = new ArrayList<FileModel>();
    // First verify if home is requested or if the directory is well knwon
    // for this file system
    String home = getRealDirectory(directoryP.getAbsoluteName());
    if (home != null) {
      File dir = new File(home);
      if (dir.isDirectory()) {
        // This filter only returns files which matchs file extension
        FileFilter fileFilter = new FileFilter() {
          public boolean accept(File file) {
            return true;
          }
        };
        File[] files = dir.listFiles(fileFilter);
        // Create response list
        for (int i = 0; i < files.length; i++) {
          File file = files[i];
          String absolute = getVirtualDirectory(file.getAbsolutePath());
          if (absolute != null) {
            FileModel model = new FileModel();
            model.setName(file.getName());
            model.setBytes(file.length());
            model.setDirectory(file.isDirectory());
            model.setLastModified(new Date(file.lastModified()));
            model.setAbsoluteName(absolute);
            if (descriptionProvider != null) {
            	descriptionProvider.provideDescription(model);
            }
            result.add(model);
          }
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see ext.ux.files.server.FileSystem#getRootDirectories()
   */
  public List<FileModel> getRootDirectories() {
    List<FileModel> result = new ArrayList<FileModel>();
    
    for (String virtual : directories.keySet()) {
      FileModel model = new FileModel();
      model.setName(virtual);
      model.setBytes(0);
      model.setDirectory(true);
      model.setAbsoluteName(virtual);
      if (descriptionProvider != null) {
      	descriptionProvider.provideDescription(model);
      }
      result.add(model);
    }
    return result;
  }

  /**
   * Try to transform a virtual directory to a real directory
   * 
   * @param virtualDirectoryP
   * @return The real path of the directory or null if directory does not
   *         match any virtual directory
   */
  private String getRealDirectory(String virtualDirectoryP) {
    for (String virtualName : directories.keySet()) {
      if (virtualDirectoryP.startsWith(virtualName)) {
        String real = directories.get(virtualName);
        return real + virtualDirectoryP.substring(virtualName.length());
      }
    }
    return null;
  }

  /**
   * Try to transform a real directory to a virtual directory
   * 
   * @param realDirectoryP
   * @return The real path of the directory or null if directory does not
   *         match any virtual directory
   */
  private String getVirtualDirectory(String realDirectoryP) {
    for (Entry<String, String> entry : directories.entrySet()) {
      String value = entry.getValue();
      if (realDirectoryP.startsWith(value)) {
        String virtual = entry.getKey();
        return virtual + realDirectoryP.substring(entry.getValue().length());
      }
    }
    return null;
  }

}
