/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.Favorite;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * The panel which contains directories and favorites directories or links
 * 
 * @author Eric Taix
 */
public class NavigationPanel extends LayoutContainer {

  // The favorite list
  private DataList favList;
  // The listener
  private Listener<ComponentEvent> listener;

  /**
   * Constructor
   */
  public NavigationPanel() {
    setLayout(new AccordionLayout());
    setBorders(false);

    // The favorite panel
    ContentPanel favoritePane = new ContentPanel();
    favoritePane.setBorders(true);
    favoritePane.setBodyBorder(false);
    favoritePane.setLayout(new FitLayout());
    favoritePane.setHeading("Favorites");
    favoritePane.setScrollMode(Scroll.AUTO);
    add(favoritePane);

    favList = new DataList();
    favList.setBorders(false);
    favoritePane.add(favList);

    listener = new Listener<ComponentEvent>() {
      public void handleEvent(ComponentEvent ce) {
        DataList dataList = (DataList) ce.getComponent();
        DataListItem item = dataList.getSelectedItem();
        FileModel dir = (FileModel)item.getData("directory");
        FilesEvents.fireDirectoryChanged(dir);
      }
    };
    favList.addListener(Events.SelectionChange, listener); 
  }

  /**
   * Add a new favorite
   * 
   * @param favP
   */
  public void addFavorite(Favorite favP) {
    DataListItem item = new DataListItem();      
    item.setText(favP.getTitle());  
    item.setIconStyle(favP.getIconStyle());  
    item.setData("directory", favP.getDirectory());
     
    favList.add(item);
  }
}
