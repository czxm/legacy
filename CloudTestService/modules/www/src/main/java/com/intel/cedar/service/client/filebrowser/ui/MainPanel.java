/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.Favorite;

/**
 * A container of Toolbar panel, navigation (favorites and directories)
 * 
 * @author Eric Taix
 */
public class MainPanel extends LayoutContainer {
	
  // The split button which change the display type
  private SplitButton displaySplitItem;
  // The current display type
  private DisplayType currentType = DisplayType.MOSAIC;
  // The navigation panel (favorites and directories)
  private NavigationPanel navPane;
  
  
  /**
   * Constructor
   */
  public MainPanel() {
    setLayout(new BorderLayout());
    setBorders(false);
    /*
    // Navigation panel
    BorderLayoutData west = new BorderLayoutData(LayoutRegion.WEST, 180);
    west.setMargins(new Margins(0,5,0,0));
    west.setSplit(true);  
    west.setCollapsible(true);
    navPane = new NavigationPanel();
    add(navPane, west);
    */
    // File Description
    BorderLayoutData south = new BorderLayoutData(LayoutRegion.SOUTH, 50);
    south.setMargins(new Margins(5, 0, 0, 0));
    FileDescription fileDescription = new FileDescription();
    add(fileDescription, south);
    // Files details
    BorderLayoutData center = new BorderLayoutData(LayoutRegion.CENTER);
    center.setMargins(new Margins(0, 0, 0, 0));
    add(new FilePanel(fileDescription), center);
    
    /*
    // Create the top toolbar
    ToolBar tb = new ToolBar();
    BorderLayoutData north = new BorderLayoutData(LayoutRegion.NORTH, 22);
    north.setMargins(new Margins(0,0,0,0));
    add(tb, north);

    // Organize button
    Button organize = new Button(" Organize");
    tb.add(organize);
    organize.setIconStyle("icon-organize");

    Menu organizeMenu = new Menu();
    CheckMenuItem menuItem = new CheckMenuItem("I Like Cats");
    menuItem.setChecked(true);
    organizeMenu.add(menuItem);

    menuItem = new CheckMenuItem("I Like Dogs");
    organizeMenu.add(menuItem);
    organize.setMenu(organizeMenu);

    // The display type button
    displaySplitItem = new SplitButton(" Display");
    displaySplitItem.setIconStyle("icon-display-mosaic");
    displaySplitItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
		public void componentSelected(ButtonEvent ce) {
	        int ord = currentType.ordinal()+1;
	        if (ord >= DisplayType.values().length) {
	          ord = 0;
	        }
	        DisplayType newType = DisplayType.values()[ord];
	        changeDisplay(newType);
		}
    });
    displaySplitItem.addListener(Events.ArrowClick, new Listener<ComponentEvent>() {
      public void handleEvent(ComponentEvent ce) {
        ce.getClientX();
      }
    });

    Menu displayMenu = new Menu();
    MenuItem item;
    item = new MenuItem("Mosaic");
    item.setStyleName("icon-display-mosaic");
    item.addSelectionListener(new SelectionListener<MenuEvent>() {
      public void componentSelected(MenuEvent ceP) {
        changeDisplay(DisplayType.MOSAIC);
      }      
    });
    displayMenu.add(item);
    item = new MenuItem("List");
    item.setStyleName("icon-display-list");
    item.addSelectionListener(new SelectionListener<MenuEvent>() {
      public void componentSelected(MenuEvent ceP) {
        changeDisplay(DisplayType.LIST);
      }      
    });
    displayMenu.add(item);
    item = new MenuItem("Detail");
    item.setStyleName("icon-display-detail");
    item.addSelectionListener(new SelectionListener<MenuEvent>() {
      public void componentSelected(MenuEvent ceP) {
        changeDisplay(DisplayType.DETAIL);
      }      
    });
    displayMenu.add(item);
    displaySplitItem.setMenu(displayMenu);

    tb.add(displaySplitItem);
    
    // New directory button
    Button newDirectory = new Button("New directory");
    newDirectory.setStyleName("icon-newdirectory");
    tb.add(newDirectory);
    */
  }
  
  /**
   * Change the current display
   * @param typeP
   */
  private void changeDisplay(DisplayType typeP) {
    if (typeP == DisplayType.MOSAIC) {
      displaySplitItem.setIconStyle("icon-display-mosaic");
    }
    else if (typeP == DisplayType.LIST) {
      displaySplitItem.setIconStyle("icon-display-list");
    }
    else if (typeP == DisplayType.DETAIL) {
      displaySplitItem.setIconStyle("icon-display-detail");      
    }
    currentType = typeP;
    // Fire the event
    FilesEvents.fireDisplayTypeChanged(typeP);
  }
  
  /**
   * Add a new favorite
   * @param favP
   */
  public void addFavorite(Favorite favP) {
    navPane.addFavorite(favP);
  }
}
