/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.intel.cedar.service.client.filebrowser.controller.FileControler;
import com.intel.cedar.service.client.filebrowser.controller.FileUI;
import com.intel.cedar.service.client.filebrowser.ui.detail.FileDetail;
import com.intel.cedar.service.client.filebrowser.ui.detail.FileMosaic;

/**
 * The panel which contains files details
 * @author Eric Taix
 */
public class FilePanel extends LayoutContainer {

  private CardLayout layout;

  /**
   * Constructor
   */
  public FilePanel(FileDescription fileDescriptionP) {
    initUI();
    // Register the view/controler
    Dispatcher dispatcher = Dispatcher.get();
    dispatcher.addController(new FileControler(this, fileDescriptionP));
    
  }
  
  /**
   * Initialiaze the user interface
   */
  protected void initUI() {
    setBorders(false);
    layout = new CardLayout();
    setLayout(layout);
    
    // Create mosaic panel
    FileMosaic fileMosaic = new FileMosaic();
    add(fileMosaic);
    /*
    // Create detail panel
    FileDetail fileDetail = new FileDetail();
    add(fileDetail);
    */
    layout.setActiveItem(getItem(0)); 
  }
  
  /**
   * Change the display type
   * @param typeP
   */
  public void changeDisplay(DisplayType typeP) {
    if (typeP == DisplayType.LIST) {
      layout.setActiveItem(getItem(2));       
    }
    else if (typeP == DisplayType.MOSAIC) {
      layout.setActiveItem(getItem(0));       
    }
    else if (typeP == DisplayType.DETAIL) {
      layout.setActiveItem(getItem(1));             
    }
  }
  

  /**
   * Set the store that must be used by file panels
   * @param storeP
   */
  public void setStore(ListStore<BeanModel> storeP) {
    for (int iLoop = 0; iLoop < getItemCount(); iLoop++) {
      Component comp = getItem(iLoop);
      if (comp instanceof FileUI) {
        ((FileUI)comp).setStore(storeP);
      }
    }
  }
  
}
