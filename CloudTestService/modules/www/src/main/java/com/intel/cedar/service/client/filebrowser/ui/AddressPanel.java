/**
 * 
 */
package com.intel.cedar.service.client.filebrowser.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.intel.cedar.service.client.filebrowser.controller.AddressControler;
import com.intel.cedar.service.client.filebrowser.controller.FilesEvents;
import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * The panel which contains the adress bar, history's buttons
 * 
 * @author Eric Taix
 */
public class AddressPanel extends ToolBar {

	// The history list
	private List<FileModel> history = new ArrayList<FileModel>();
	// The current index
	private int currentHistory;

	// Adress field
	private TextField<String> address;
	// Previous button
	private Button prev;
	// Next button
	private Button next;
	// Up button
	private Button up;
	// Go button
	private Button go;
	// Flag to prevent from adding to history
	private boolean fromNavigation = false;
	// current File
	private FileModel current;
	/**
	 * Constructor
	 */
	public AddressPanel() {
		initUI();
		// 
		Dispatcher dispatcher = Dispatcher.get();
		dispatcher.addController(new AddressControler(this));
	}

	/**
	 * Initialize the user interface
	 */
	protected void initUI() {
		addStyleName("address-tb");
		prev = new Button();
		prev.setIconStyle("icon-previous");
		prev.setEnabled(false);
		prev.addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				FileModel model = history.get(currentHistory - 1);
				currentHistory--;
				updateHistory();
				fromNavigation = true;
				FilesEvents.fireDirectoryChanged(model);
			}
		});
		add(prev);
		next = new Button();
		next.setIconStyle("icon-next");
		next.setEnabled(false);
		next.addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				FileModel model = history.get(currentHistory + 1);
				currentHistory++;
				updateHistory();
				fromNavigation = true;
				FilesEvents.fireDirectoryChanged(model);
			}
		});
		add(next);
		up = new Button();
		up.setIconStyle("icon-next");
		up.setEnabled(true);
		up.addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				String path = current.getAbsoluteName();
				if(current.isDirectory() && path.endsWith("/")){
					path = path.substring(0, path.length() - 1);
					path = path.substring(0, path.lastIndexOf("/") + 1);
					if(!path.endsWith("//")){
						FileModel model = new FileModel();
						model.setAbsoluteName(path);
						model.setDirectory(true);
						currentHistory++;
						FilesEvents.fireDirectoryChanged(model);
					}
				}
			}
		});
		add(up);		
		// Address
		add(new SeparatorToolItem());
		address = new TextField<String>();
		address.setReadOnly(false);
		address.setWidth(500);
		address.setValue("/");
		add(address);
		go = new Button();
		go.setIconStyle("icon-next");
		go.setEnabled(true);
		go.addListener(Events.Select, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				FileModel model = new FileModel();
				String path = address.getValue();
				if(path.length() > 8){
					model.setAbsoluteName(path);
					currentHistory++;
					FilesEvents.fireDirectoryChanged(model);
				}
			}
		});
		add(go);
		// initialize the history
		initHistory();
	}

	/**
	 * Initialize (clear) the history list
	 */
	public void initHistory() {
		history = new ArrayList<FileModel>();
		next.setEnabled(false);
		prev.setEnabled(false);
	}

	/**
	 * Add a new history
	 * 
	 * @param historyP
	 */
	private void addHistory(FileModel modelP) {
		if (!fromNavigation) {
			history.add(modelP);
			currentHistory = history.size() - 1;
			updateHistory();
		}
		fromNavigation = false;
	}

	/**
	 * Update navigation buttons state and tips
	 */
	private void updateHistory() {
		if (currentHistory < (history.size() - 1)) {
			next.setEnabled(true);
			next.setTitle(history.get(currentHistory + 1).getAbsoluteName());
		}
		else {
			next.setEnabled(false);
			next.setTitle("");
		}
		if (currentHistory > 0) {
			prev.setEnabled(true);
			prev.setTitle(history.get(currentHistory - 1).getAbsoluteName());
		}
		else {
			prev.setEnabled(false);
			prev.setTitle("");
		}
	}

	/**
	 * Change the current history
	 */
	public void changeDirectory(FileModel newDirP) {
		if (newDirP == null) {
			address.setValue("/");
		}
		else {
			String dir = newDirP.getAbsoluteName();
			//dir = dir.replace("/", " / ");
			address.setValue(dir);
			this.current = newDirP;
			addHistory(newDirP);
		}
	}

}
