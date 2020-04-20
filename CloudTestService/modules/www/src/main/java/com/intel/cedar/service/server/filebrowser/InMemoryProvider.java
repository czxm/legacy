/**
 * 
 */
package com.intel.cedar.service.server.filebrowser;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intel.cedar.service.client.filebrowser.model.FileModel;

/**
 * This provider is able to provide<br/>
 * - icons for a file according to its file extension - description for a file according to its extension
 * 
 * @author eric
 */
public class InMemoryProvider implements DescriptionProvider {

	// The directory base for images
	private String baseImage;
	// Directories datas
	private ExtensionDefinition directoriesDefinition;
	// Files extensions
	private HashMap<Pattern, ExtensionDefinition> extensionsDefinition = new HashMap<Pattern, ExtensionDefinition>();

	// ========== Inner class ========
	private class ExtensionDefinition {
		public String icon16x16;
		public String icon48x48;
		public String description;
	}

	/**
	 * @return the baseImage
	 */
	public String getBaseImage() {
		return baseImage;
	}

	/**
	 * @param baseImage
	 *          the baseImage to set
	 */
	public void setBaseImage(String baseImageP) {
		baseImage = baseImageP;
	}

	/**
	 * Set the extensions mapping<br/>
	 * Extension mapping is written in a string with comma separator:<br/>
	 * <code>
	 * &lt;regexp extension&gt;,&lt;16x16 icon path&gt;,&lt;48x48 icon path&gt;,&lt;file description&gt;<br/>
	 * <br/>
	 * For example:<br/>
	 * <code>doc,
	 * </code> This provider tries to match the file extension with one of the regexp in the declared order. So it's very important to declare your extension in
	 * the correct order, and better to use '\.' to match all other extension that you don't mind
	 * 
	 * @param extensionsP
	 */
	public void setExtensionsMapping(List<String> extensionsP) {		             
		for (Object object : extensionsP) {
			String definitionLine  = (String)object;
			// Extract the regexp
			int index = definitionLine.indexOf(",");
			String regStr = definitionLine.substring(0, index);
			String other = definitionLine.substring(index+1);
			ExtensionDefinition def = getExtension(other);
			// Add to the extensions definition
			Pattern pattern = Pattern.compile(regStr);
			extensionsDefinition.put(pattern, def);
		}
	}

	/**
	 * Set the directories mapping
	 * 
	 * @param directoriesP
	 */
	public void setDirectoriesMapping(String directoriesP) {
		ExtensionDefinition def = getExtension(directoriesP);
		directoriesDefinition = def;
	}


	/**
	 * Add a extension definition
	 * 
	 * @param extensionDefP
	 */
	private ExtensionDefinition getExtension(String extensionDefP) {
		ExtensionDefinition def = new ExtensionDefinition();
		StringTokenizer tok = new StringTokenizer(extensionDefP, ",");
		def.icon16x16 = tok.nextToken();
		def.icon48x48 = tok.nextToken();
		def.description = tok.nextToken();
		return def;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ext.ux.files.server.DescriptionProvider#provideDescription(ext.ux.files.client.dialog.model.FileModel)
	 */
	public void provideDescription(FileModel modelP) {
		if (modelP.isDirectory()) {
			modelP.setIcon16x16(baseImage + directoriesDefinition.icon16x16);
			modelP.setIcon48x48(baseImage + directoriesDefinition.icon48x48);
			modelP.setDescription(directoriesDefinition.description);
		}
		else {
			String fileName = modelP.getAbsoluteName();
			int index = fileName.lastIndexOf(".");
			if (index != -1) {
				String ext = fileName.substring(index+1);
				// Try to match with all extensionMapping
				for (Entry<Pattern, ExtensionDefinition> entry : extensionsDefinition.entrySet()) {
					Pattern pattern = entry.getKey();
					Matcher matcher = pattern.matcher(ext);
					if (matcher.find()) {
						modelP.setIcon16x16(baseImage + entry.getValue().icon16x16);
						modelP.setIcon48x48(baseImage + entry.getValue().icon48x48);
						modelP.setDescription(entry.getValue().description);
						break;
					}
				}
			}
		}
	}

}
