package com.intel.cedar.service.client.filebrowser.controller;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.intel.cedar.service.client.FileService;
import com.intel.cedar.service.client.FileServiceAsync;
import com.intel.cedar.service.client.filebrowser.model.FileModel;
import com.intel.cedar.service.client.filebrowser.ui.DisplayType;
import com.intel.cedar.service.client.filebrowser.ui.FileDescription;
import com.intel.cedar.service.client.filebrowser.ui.FilePanel;

/**
 * File controler
 * 
 * @author Eric Taix
 */
public class FileControler extends Controller {

  private FilePanel panel;
  private FileDescription fileDescription;
  private BaseListLoader<BaseListLoadResult<FileModel>> loader;

  /**
   * Contructor
   */
  public FileControler(FilePanel panelP, FileDescription fileDescP) {
    panel = panelP;
    fileDescription = fileDescP;
    registerEventTypes(FilesEvents.DIALOG_OPENING, FilesEvents.DIRECTORY_CHANGED,
            FilesEvents.DISPLAY_TYPE_CHANGED, FilesEvents.CURRENT_FILE_CHANGED);
    createRemote();
  }

  /**
   * Handles events fired by the dispatcher
   */
  @SuppressWarnings("unchecked")
  public void handleEvent(AppEvent eventP) {

    // The display type have been changed
    if (eventP.getType() == FilesEvents.DISPLAY_TYPE_CHANGED) {
      panel.changeDisplay((DisplayType) eventP.getData());
    }
    // The current directory have been changed : update the file listing
    else if (eventP.getType() == FilesEvents.DIRECTORY_CHANGED) {
      updateDirectory((FileModel) eventP.getData());
    }
    // Dialog is opening
    else if (eventP.getType() == FilesEvents.DIALOG_OPENING) {
      updateDirectory(null);
    }
    // Current selected file changed
    else if (eventP.getType() == FilesEvents.CURRENT_FILE_CHANGED) {
      fileDescription.currentFileChanged((FileModel)eventP.getData());
    }
  }

  /**
   * Update the content of the current directory
   * 
   * @param newDirP
   */
  private void updateDirectory(final FileModel newDirP) {
    FileListLoadConfig loadConfig = new FileListLoadConfig();
    loadConfig.setHomeDirectory(newDirP);
    loader.load(loadConfig);
  }

  /**
   * Create the data loader and the store
   */
  @SuppressWarnings("unchecked")
  private void createRemote() {
    // Define async RPC method 
    final FileServiceAsync service = FileService.Util.getInstance();
    RpcProxy<BaseListLoadResult<FileModel>> proxy = new RpcProxy<BaseListLoadResult<FileModel>>() {
      @Override
      protected void load(Object loadConfigP, AsyncCallback<BaseListLoadResult<FileModel>> callbackP) {
        service.getFiles((FileListLoadConfig)loadConfigP, callbackP);
      }
    };
    // Use a bean model reader
    BeanModelReader reader = new BeanModelReader();
    loader = new BaseListLoader<BaseListLoadResult<FileModel>>(
            proxy, reader);
    // Define the store
    ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
    // Set the store 
    panel.setStore(store);
  }
}
