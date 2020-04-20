package com.intel.cedar.service.client.filebrowser.controller;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.intel.cedar.service.client.filebrowser.model.FileModel;
import com.intel.cedar.service.client.filebrowser.ui.AddressPanel;

/**
 * Address bar controler
 * 
 * @author Eric Taix
 */
public class AddressControler extends Controller {

  private AddressPanel panel;

  /**
   * Contructor
   */
  public AddressControler(AddressPanel panelP) {
    panel = panelP;
    registerEventTypes(FilesEvents.DIRECTORY_CHANGED, FilesEvents.DIALOG_OPENING);
  }

  /**
   * Handles events fired by the dispatcher
   */
  @SuppressWarnings("unchecked")
  public void handleEvent(AppEvent eventP) {
    if (eventP.getType() == FilesEvents.DIRECTORY_CHANGED) {
      panel.changeDirectory((FileModel)eventP.getData());
    }
    else if (eventP.getType() == FilesEvents.DIALOG_OPENING) {
    	panel.initHistory();
    }
  }
}
