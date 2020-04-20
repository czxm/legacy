/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui.detail;

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.intel.cedar.service.client.filebrowser.controller.FileUI;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * This panel shows file details (it uses a grid to display files information)
 * 
 * @author Eric Taix
 */
public class FileMosaic extends LayoutContainer implements FileUI {

	private ListView<BeanModel> view;

	/**
	 * Default constructor
	 */
	public FileMosaic() {
		initUI();
	}

	/**
	 * Initialize the user interface
	 */
	protected void initUI() {
		setId("file-mosaic-id");
		setLayout(new FitLayout());
		setBorders(true);

		view = new ListView<BeanModel>() {
			@Override
			protected BeanModel prepareData(BeanModel model) {
				FileModel fileModel = model.getBean();
				long size = fileModel.getBytes() / 1000;
				model.set("shortName", Format.ellipse(fileModel.getName(), 15));
				model.set("size", NumberFormat.getFormat("#0").format(size) + "k");
				if (fileModel.getLastModified() == null) {
					model.set("modified", "");
				}
				else {
					model.set("modified", DateTimeFormat.getMediumDateTimeFormat().format(fileModel.getLastModified()));
				}
				return model;
			}
		};
		view.setId("file-mosaic-view");
		view.setTemplate(getTemplate());
		view.setBorders(false);
		view.setStore(null);
		view.setItemSelector("div.thumb-wrap");
		view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// Handle the selection event
		view.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				BeanModel model = view.getSelectionModel().getSelectedItem();
				FileModel fileModel = null;
				if (model != null) {
					fileModel = model.getBean();
				}
				FilesEvents.fireCurrentFileChanged(fileModel);
			}
		});
		// Handle DoubleClick event
		view.addListener(Events.DoubleClick, new Listener<ListViewEvent>() {
			public void handleEvent(ListViewEvent be) {
				BeanModel model = view.getSelectionModel().getSelectedItem();
				FileModel fileModel = null;
				if (model != null) {
					fileModel = model.getBean();
					if (fileModel != null) {
						// If it's a directory then change the current
						if (fileModel.isDirectory()) {
							FilesEvents.fireDirectoryChanged(fileModel);
						}
						// It's a file, so fire onSucess event
						else {

						}
					}
				}
			}

		});
		add(view);
	}

	private native String getTemplate() /*-{ 
	            return ['<tpl for=".">', 
	            '<div class="thumb-wrap" id="{namex}" style="border: 1px solid white">', 
	            '<div class="thumb" align="center"><img src="{icon48x48}" title="{name}"></div>', 
	            '<span class="x-editable">{shortName}</span></div>', 
	            '</tpl>', 
	            '<div class="x-clear"></div>'].join(""); 
	             
	            }-*/;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ext.ux.files.client.dialog.controler.FileUI#setStore(com.extjs.gxt.ui .client.store.ListStore)
	 */
	public void setStore(ListStore<BeanModel> storeP) {
		view.setStore(storeP);
	}

}
