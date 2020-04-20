/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.Favorite;
import com.intel.cedar.service.client.filebrowser.model.FileModel;


/**
 * A dialog specialized in opening a file on the server side.
 * 
 * @author Eric Taix
 */
public class OpenDialog {

  private Window window;
  private MainPanel mainPane;
  // The root directory when the dialog open
  private FileModel root;;
  
  /**
   * Constructor
   */
  public OpenDialog() {
    window = new Window();
    initUI();
    root = new FileModel();
    root.setAbsoluteName("/");
  }
  
  
  /**
	 * @param root the root to set
	 */
	public void setRoot(FileModel rootP) {
    root = rootP;
	}


	/**
   * Initialize all widgets
   */
  protected void initUI() {
    window.setSize(640, 400);
    window.setHeading("Open file");
    window.setLayout(new BorderLayout());
    window.setModal(true);
    // Address bar
    BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 30);
    north.setMargins(new Margins());
    window.add(new AddressPanel(), north);
    // Content panel: navigation panel + file detail panel
    BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, 50);
    south.setMargins(new Margins());
    window.add(new ActionPanel(), south);
    // Action bar: buttons, file extension filter
    BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
    center.setMargins(new Margins());
    mainPane = new MainPanel();
    mainPane.setBorders(false);
    window.add(mainPane, center);
  }
  
  /**
   * Show the OpenDialog (modal)
   */
  public void show() {
    FilesEvents.fireDialogOpening();
    FilesEvents.fireDirectoryChanged(root);
    window.show();
  }
  
  /**
   * Add a new favorite
   * @param favoriteP
   */
  public void addFavorite(Favorite favoriteP) {
    mainPane.addFavorite(favoriteP);
  }
  
}
