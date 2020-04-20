/**
 * 
 */
package com.intel.cedar.service.server.filebrowser;

import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * An interface for class which are able to set a description and icons paths to a fileModel instance
 * @author eric
 */
public interface DescriptionProvider {

	/**
	 * Provide (set) the file description and icons
	 * @param modelP
	 */
	public void provideDescription(FileModel modelP);
	
}
