/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.model;

import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BeanModelTag;

/**
 * A modle which represents a File (File means it can be a file or a directory depending on the isDirectory returns)
 * @author Eric Taix
 */
@SuppressWarnings("serial")
public class FileModel implements BeanModelTag, Serializable {

  // The short name of the file
  private String name;
  // The long name (absolute) of the file
  private String absoluteName;
  // The size of the size in bytes
  private long bytes;
  // A flag to know if it is a directory or not
  private boolean directory = false;
  // Last modified
  private Date lastModified;
  // 16x16 image
  private String icon16x16;
  // 48x48 image
  private String icon48x48;
  // File description
  private String description;
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  /**
   * @param nameP the name to set
   */
  public void setName(String nameP) {
    name = nameP;
  }
  /**
   * @return the longName
   */
  public String getAbsoluteName() {
    return absoluteName;
  }
  /**
   * @param longNameP the longName to set
   */
  public void setAbsoluteName(String longNameP) {
    absoluteName = longNameP;
  }
  /**
   * @return the bytes
   */
  public long getBytes() {
    return bytes;
  }
  /**
   * @param bytesP the bytes to set
   */
  public void setBytes(long bytesP) {
    bytes = bytesP;
  }
  /**
   * @return the directory
   */
  public boolean isDirectory() {
    return directory;
  }
  /**
   * @param directoryP the directory to set
   */
  public void setDirectory(boolean directoryP) {
    directory = directoryP;
  }
  /**
   * @return the lastModified
   */
  public Date getLastModified() {
    return lastModified;
  }
  /**
   * @param lastModifiedP the lastModified to set
   */
  public void setLastModified(Date lastModifiedP) {
    lastModified = lastModifiedP;
  }
  /**
   * @return the img16x16
   */
  public String getIcon16x16() {
    return icon16x16;
  }
  /**
   * @param img16x16P the img16x16 to set
   */
  public void setIcon16x16(String img16x16P) {
    icon16x16 = img16x16P;
  }
  /**
   * @return the img48x48
   */
  public String getIcon48x48() {
    return icon48x48;
  }
  /**
   * @param img48x48P the img48x48 to set
   */
  public void setIcon48x48(String img48x48P) {
    icon48x48 = img48x48P;
  }
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param descriptionP the description to set
	 */
	public void setDescription(String descriptionP) {
		description = descriptionP;
	}
  
}
