package com.intel.cedar.service.client.filebrowser.controller;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;

/**
 * A file UI interface. All UI which display files, must implements this interface 
 * and must be referenced with a FileControler
 * @author Eric Taix
 */
public interface FileUI {

  /**
   * Set the store
   * @param storeP
   */
  public void setStore(ListStore<BeanModel> storeP);
}
