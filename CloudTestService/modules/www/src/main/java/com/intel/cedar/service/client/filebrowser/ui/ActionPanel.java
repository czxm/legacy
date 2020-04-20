/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * The panel which contains selected file, buttons, file extension filter
 * @author Eric Taix
 */
public class ActionPanel extends LayoutContainer {

  /**
   * Constructor
   */
  public ActionPanel() {
    setLayout(new RowLayout(Orientation.HORIZONTAL));
    setBorders(false);
    setTitle("action");
    setLayout(new FitLayout());
  }
  
}
