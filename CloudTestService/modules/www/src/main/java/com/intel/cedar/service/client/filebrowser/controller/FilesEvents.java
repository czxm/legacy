/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.controller;

import java.util.List;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.intel.cedar.service.client.filebrowser.model.FileModel;
import com.intel.cedar.service.client.filebrowser.ui.DisplayType;

/**
 * Events supported by files extension package and facade
 * method to dispatch specific events
 * @author Eric Taix
 */
public class FilesEvents {
  
  // The dialog is opening
  public static final EventType DIALOG_OPENING = new EventType(0);
  // The current selected file has been changed
  public static final EventType FILE_CHANGED = new EventType(1);
  // The current selected directory has been changed
  public static final EventType DIRECTORY_CHANGED = new EventType(2);
  // The current display type changed
  public static final EventType DISPLAY_TYPE_CHANGED = new EventType(3);
  // The current selected file has been changed
  public static final EventType CURRENT_FILE_CHANGED = new EventType(4);

  /**
   * Dispatch an event to inform controlers that the dialog is opening
   */
  public static void fireDialogOpening() {
    Dispatcher dispatcher = Dispatcher.get();
    AppEvent evt = new AppEvent(FilesEvents.DIALOG_OPENING);
    dispatcher.dispatch(evt);    
  }
  
  /**
   * Dispatch an event to inform controlers that the display type has been changed 
   * @param newTypeP
   */
  public static void fireDisplayTypeChanged(DisplayType newTypeP) {
    Dispatcher dispatcher = Dispatcher.get();
    AppEvent evt = new AppEvent(FilesEvents.DISPLAY_TYPE_CHANGED);
    evt.setData(newTypeP);
    dispatcher.dispatch(evt);
  }
  
  /**
   * Dispatch an event to inform controlers that current directory has been changed
   * @param newDirP
   */
  public static void fireDirectoryChanged(FileModel newDirP) {
    Dispatcher dispatcher = Dispatcher.get();
    AppEvent evt = new AppEvent(FilesEvents.DIRECTORY_CHANGED);
    evt.setData(newDirP);
    dispatcher.dispatch(evt);    
  }
  
  /**
   * Dispatch an event to inform controlers that current directory listing has been changed
   * @param dirP
   * @param filesP
   */
  public static void fireDirectoryListingChanged(FileModel dirP, List<FileModel> filesP) {
    Dispatcher dispatcher = Dispatcher.get();
    AppEvent evt = new AppEvent(FilesEvents.DIRECTORY_CHANGED);
    evt.setData(dirP);
    evt.setData("content", filesP);
    dispatcher.dispatch(evt);        
  }
  
  /**
   * Dispatch an event to inform controler that the curret selected file has been changed
   * @param currentP
   */
  public static void fireCurrentFileChanged(FileModel currentP) {
    Dispatcher dispatcher = Dispatcher.get();
    AppEvent evt = new AppEvent(FilesEvents.CURRENT_FILE_CHANGED);
    evt.setData(currentP);
    dispatcher.dispatch(evt);            
  }
  
}