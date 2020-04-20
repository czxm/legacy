package com.intel.cedar.service.server.filebrowser;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.intel.cedar.service.client.filebrowser.model.FileModel;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.IStorage;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.util.Utils;

public class CedarStorage implements FileSystem {
	
	private IFolder root;
	
	// An icon provider
	private DescriptionProvider descriptionProvider;
	
	public CedarStorage(){
		root = StorageFactory.getInstance().getStorage().getRoot();
	}

	/**
	 * @return the iconProvider
	 */
	public DescriptionProvider getDescriptionProvider() {
		return descriptionProvider;
	}

	/**
	 * @param iconProviderP
	 *            the iconProvider to set
	 */
	public void setDescriptionProvider(DescriptionProvider descriptionProviderP) {
		descriptionProvider = descriptionProviderP;
	}

	public List<FileModel> getFiles(FileModel directoryP) {
		// Si root directories requested
		if (directoryP == null) {
			return getRootDirectories();
		}

		List<FileModel> result = new ArrayList<FileModel>();
		URI cedarURI = null;
		String path = directoryP.getAbsoluteName();
		if(path == null || path.equals("") || path.equals("/")){
			cedarURI = root.getURI();
		}
		else{
			cedarURI = URI.create(path);
		}
		IFolder folder = root.getFolder(cedarURI);
		if(folder.exist()){
			for(IStorage file : folder.list()){
				FileModel model = new FileModel();
				model.setName(file.getName());
				model.setBytes(file instanceof IFile ? ((IFile)file).length() : -1);
				model.setDirectory(file instanceof IFolder);
				model.setLastModified(new Date(file.lastModified()));
				model.setAbsoluteName(Utils.decodeURL(file.getURI().toString()));
				if (descriptionProvider != null) {
					descriptionProvider.provideDescription(model);
				}
				result.add(model);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ext.ux.files.server.FileSystem#getRootDirectories()
	 */
	public List<FileModel> getRootDirectories() {
		List<FileModel> result = new ArrayList<FileModel>();

		FileModel model = new FileModel();
		model.setName(root.getName());
		model.setBytes(0);
		model.setDirectory(true);
		model.setAbsoluteName(root.getURI().toString());
		if (descriptionProvider != null) {
			descriptionProvider.provideDescription(model);
		}
		result.add(model);
		
		return result;
	}
}
