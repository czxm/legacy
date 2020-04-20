/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui.detail;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.intel.cedar.service.client.filebrowser.controller.FileUI;

/**
 * This panel shows file details (it uses a grid to display files
 * information)
 * 
 * @author Eric Taix
 */
public class FileList extends LayoutContainer implements FileUI {

  private ColumnModel cm;
  private Grid<BeanModel> grid;

  /**
   * Default constructor
   */
  public FileList() {
    initUI();
  }

  /**
   * Initialize the user interface
   */
  protected void initUI() {
    setId("file-list-id");
    setLayout(new FitLayout());
    setBorders(true);

    // Define columns config
    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
    ColumnConfig column = new ColumnConfig("name", "Name", 200);
    configs.add(column);
    column = new ColumnConfig("date", "Last Updated", 100);
    column.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
    configs.add(column);
    column = new ColumnConfig("size", "Size", 75);
    configs.add(column);
    column = new ColumnConfig("type", "Type", 100);
    configs.add(column);


    cm = new ColumnModel(configs);
    grid = new Grid<BeanModel>(null, cm);
    grid.setAutoExpandColumn("name");
    grid.setBorders(false);

    add(grid);
  }

  /* (non-Javadoc)
   * @see ext.ux.files.client.dialog.controler.FileUI#setStore(com.extjs.gxt.ui.client.store.ListStore)
   */
  public void setStore(ListStore<BeanModel> storeP) {
    grid.reconfigure(storeP, cm);
  }


}
