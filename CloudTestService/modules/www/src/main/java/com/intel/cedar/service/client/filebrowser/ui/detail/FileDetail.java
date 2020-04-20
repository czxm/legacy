/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui.detail;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.intel.cedar.service.client.filebrowser.controller.FileUI;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * This panel shows file details (it uses a grid to display files
 * information)
 * 
 * @author Eric Taix
 */
public class FileDetail extends LayoutContainer implements FileUI {

  private Grid<BeanModel> grid;
  private ColumnModel cm;

  /**
   * Default constructor
   */
  public FileDetail() {
    initUI();
  }

  /**
   * Initialize the user interface
   */
  protected void initUI() {
    setId("file-detail-id");
    setLayout(new FitLayout());
    setBorders(true);

    // Define columns config
    List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
    ColumnConfig column = new ColumnConfig("icon16x16", "", 22);
    column.setRenderer(new GridCellRenderer<BeanModel>() {

      /* (non-Javadoc)
       * @see com.extjs.gxt.ui.client.widget.grid.GridCellRenderer#render(com.extjs.gxt.ui.client.data.ModelData, java.lang.String, com.extjs.gxt.ui.client.widget.grid.ColumnData, int, int, com.extjs.gxt.ui.client.store.ListStore)
       */
      public String render(BeanModel modelP, String propertyP, ColumnData configP, int rowIndexP, int colIndexP,
              ListStore<BeanModel> storeP, Grid<BeanModel> gridP) {
        FileModel fileModel = modelP.getBean();
        return "<img src='"+fileModel.getIcon16x16()+"'/>";
      }
      
    });
    configs.add(column);
    column = new ColumnConfig("name", "Name", 200);
    configs.add(column);
    column = new ColumnConfig("date", "Last modified", 100);
    column.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
    configs.add(column);
    column = new ColumnConfig("bytes", "Size", 75);
    configs.add(column);
    column = new ColumnConfig("description", "Type", 100);
    configs.add(column);

    cm = new ColumnModel(configs);
    grid = new Grid<BeanModel>(null, cm);
    grid.setAutoExpandColumn("name");
    grid.setBorders(false);

    // Handle the DoubleClick event
    grid.addListener(Events.RowDoubleClick, new Listener<GridEvent>() {
			public void handleEvent(GridEvent be) {
				BeanModel model = grid.getSelectionModel().getSelectedItem();
				FileModel fileModel = null;
				if (model != null) {
					fileModel = model.getBean();
					if (fileModel != null) {
						// If it's a directory then change the current
						if (fileModel.isDirectory()) {
							FilesEvents.fireDirectoryChanged(fileModel);
						}
						// It's a file, so fire onSuccess event
						else {

						}
					}
				}
			}    	
    });
    
    // Handle the click event (file selection)
    grid.addListener(Events.RowClick, new Listener<GridEvent>() {
			public void handleEvent(GridEvent be) {
				BeanModel model = grid.getSelectionModel().getSelectedItem();
				FileModel fileModel = null;
				if (model != null) {
					fileModel = model.getBean();
				}
				FilesEvents.fireCurrentFileChanged(fileModel);				
			}    	
    });
    
    add(grid);
  }

  /* (non-Javadoc)
   * @see ext.ux.files.client.dialog.controler.FileUI#setStore(com.extjs.gxt.ui.client.store.Store)
   */
  public void setStore(ListStore<BeanModel> storeP) {
    grid.reconfigure(storeP, cm);
  }


}
