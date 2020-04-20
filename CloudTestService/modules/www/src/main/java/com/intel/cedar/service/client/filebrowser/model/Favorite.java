/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.model;

/**
 * A favorite which can be added to an OpenDialog to provide directory
 * shortcut for user
 * 
 * @author Eric Taix
 */
public class Favorite {

  // The display title of the favorite
  private String title;
  // The directory the favorite is linked to
  private FileModel directory;
  // The favorite icon style (css style)
  private String iconStyle;

  /**
   * Default constructor
   */
  public Favorite() {    
  }
  
  /**
   * Constructor which initialize all properties
   * @param titleP
   * @param directoryP
   * @param iconStyleP
   */
  public Favorite(String titleP, FileModel directoryP, String iconStyleP) {
    setTitle(titleP);
    setDirectory(directoryP);
    setIconStyle(iconStyleP);
  }
  
  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param titleP the title to set
   */
  public void setTitle(String titleP) {
    title = titleP;
  }

  /**
   * @return the directory
   */
  public FileModel getDirectory() {
    return directory;
  }

  /**
   * @param directoryP the directory to set
   */
  public void setDirectory(FileModel directoryP) {
    if (directoryP.isDirectory()) {
      directory = directoryP;
    }
  }

  /**
   * @return the iconStyle
   */
  public String getIconStyle() {
    return iconStyle;
  }

  /**
   * @param iconStyleP the iconStyle to set
   */
  public void setIconStyle(String iconStyleP) {
    iconStyle = iconStyleP;
  }

}
