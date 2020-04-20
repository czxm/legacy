package com.intel.cedar.features.IDH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.Environment;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.service.client.feature.model.Variable.VarType;
import com.intel.cedar.storage.IFile;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.tasklet.AbstractTaskRunner;
import com.intel.cedar.tasklet.ITaskItem;
import com.intel.cedar.tasklet.ResultID;
import com.intel.cedar.tasklet.SimpleTaskItem;

public class Builder extends AbstractTaskRunner {
	Logger LOG = LoggerFactory.getLogger(Builder.class);
	
	private static final long serialVersionUID = 733863306707750102L;
	private String resultFolder = ConstHelper.IDH_FOLDER_NAME;
	private String build_script_name = ConstHelper.FNAME_BUILD_SCRIPT;
	//private String rpmBuildLog = ConstHelper.FNAME_RPM_BUILD_LOG;
	//private String pkgBuildLog = ConstHelper.FNAME_PKG_BUILD_LOG;
	private String failureLog = ConstHelper.FNAME_FAILURE_LOG;
	private String RPMSZip = ConstHelper.FNAME_RPMS_ZIP;
	private String pkgFileLocalPath = "";
	private String pkgFileName = "";
	private String pkgFileLocalPath_2 = "";
	private String pkgFileName_2 = "";
	private boolean isRPMBuild = false;
	private boolean isDailyBuild = false;
	private String sharedStoragePath = "";
	private String platform_info_for_repo = "";
	
	@Override
	public List<ITaskItem> getTaskItems(Environment env) {
		List<ITaskItem> items = new ArrayList<ITaskItem>();
		try{
			String username = checkEnvVar(env, "svn_username", ConstHelper.DEFAULT_SVN_USERNAME);
			String password = checkEnvVar(env, "svn_password", ConstHelper.DEFAULT_SVN_PASSWORD);
			String url = checkEnvVar(env, "svn_url", "");
			String rev = checkEnvVar(env, "svn_rev", ConstHelper.HEAD);
			String BUILDTOOLS_url = checkEnvVar(env, "BUILDTOOLS_svn_url", "");
			String BUILDTOOLS_rev = checkEnvVar(env, "BUILDTOOLS_svn_rev", ConstHelper.HEAD);
			String ISOBUILD_url = checkEnvVar(env, "ISOBUILD_svn_url", "");
			String ISOBUILD_rev = checkEnvVar(env, "ISOBUILD_svn_rev", ConstHelper.HEAD);
			String build_type = checkEnvVar(env, "build_type", ConstHelper.BUILD_TYPE_TGZ);
			String iso_name_prefix = checkEnvVar(env, "iso_name_prefix", ConstHelper.DEFAULT_ISO_NAME_PREFIX);
			String iso_release_version = checkEnvVar(env, "iso_release_version", "2.0");
			String iso_language = checkEnvVar(env, "iso_language", ConstHelper.ISO_LANGUAGE_EN);
			String osVersionStr = checkEnvVar(env, "target", ConstHelper.OS_VERSION_CENTOS_6d2);
			String build_script_zip_url_2 = checkEnvVar(env, "build_script_zip_url_2d0", "");
			String build_script_zip_url_3 = checkEnvVar(env, "build_script_zip_url_3d0", "");
			String isDailyBuildStr = checkEnvVar(env, "isDailyBuild", ConstHelper.NO);
			String sharedStoragePathStr = checkEnvVar(env, "sharedStoragePath", "");
			String IM_url = "";
			String IM_rev ="";
			String IDHTOOLS_url = "";
			String IDHTOOLS_rev = "";
			String enable_resmon = ConstHelper.YES;
			String enable_callhome = ConstHelper.NO;
			String license_type = ConstHelper.LICENSE_TYPE_EVALUATION;
			if(!build_type.equals(ConstHelper.BUILD_TYPE_RPM)){
				IM_url = checkEnvVar(env, "IM_svn_url", "");
				IM_rev = checkEnvVar(env, "IM_svn_rev", ConstHelper.HEAD);
				IDHTOOLS_url = checkEnvVar(env, "IDHTOOLS_svn_url", "");
				IDHTOOLS_rev = checkEnvVar(env, "IDHTOOLS_svn_rev", ConstHelper.HEAD);
				enable_resmon = checkEnvVar(env, "enable_resmon", ConstHelper.YES);
				license_type = checkEnvVar(env, "license_type", ConstHelper.LICENSE_TYPE_EVALUATION);
				enable_callhome = checkEnvVar(env, "enable_callhome", ConstHelper.NO);
				if(iso_language.equalsIgnoreCase(ConstHelper.ISO_LANGUAGE_CH)){
					enable_callhome = ConstHelper.NO;
				}else if(iso_language.equalsIgnoreCase(ConstHelper.ISO_LANGUAGE_EN) && license_type.equalsIgnoreCase(ConstHelper.LICENSE_TYPE_EVALUATION)){
					enable_callhome = ConstHelper.YES;
				}
			}
			
			SimpleTaskItem item = new SimpleTaskItem();
			item.setProperty("svn_url", url);
			item.setProperty("svn_rev", rev);
			item.setProperty("BUILDTOOLS_svn_url", BUILDTOOLS_url);
			item.setProperty("BUILDTOOLS_svn_rev", BUILDTOOLS_rev);
			item.setProperty("ISOBUILD_svn_url", ISOBUILD_url);
			item.setProperty("ISOBUILD_svn_rev", ISOBUILD_rev);
			item.setProperty("svn_username", username);
			item.setProperty("svn_password", password);
			if(!build_type.equals(ConstHelper.BUILD_TYPE_RPM)){
				item.setProperty("IM_svn_url", IM_url);
				item.setProperty("IM_svn_rev", IM_rev);
				item.setProperty("IDHTOOLS_svn_url", IDHTOOLS_url);
				item.setProperty("IDHTOOLS_svn_rev", IDHTOOLS_rev);
				item.setProperty("enable_resmon", enable_resmon);
				item.setProperty("license_type", license_type);
				item.setProperty("enable_callhome", enable_callhome);
			}
			
			item.setProperty("build_type", build_type);
			item.setProperty("iso_name_prefix", iso_name_prefix);
			item.setProperty("iso_release_version", iso_release_version);
			item.setProperty("iso_language", iso_language);
			String iso_os_version = "el6.x86_64";
			String platform_info_for_repo_str="cent6.2";
			if(osVersionStr.equalsIgnoreCase(ConstHelper.OS_VERSION_CENTOS_5d7)){
				iso_os_version = "el5.x86_64";
				platform_info_for_repo_str="cent5.7";
			}
			if(osVersionStr.equalsIgnoreCase(ConstHelper.OS_VERSION_SLES_11_SP1)){
				iso_os_version = "sles11.x86_64";
				platform_info_for_repo_str="sles11.1";
			}
			if(osVersionStr.equalsIgnoreCase(ConstHelper.OS_VERSION_SLES_11_SP2)){
				iso_os_version = "sles11.x86_64";
				platform_info_for_repo_str="sles11.2";
			}
			item.setProperty("iso_os_version", iso_os_version);
			item.setProperty("platform_info_for_repo", platform_info_for_repo_str);
			Variable v = new Variable("platform_info_for_repo",VarType.LOCAL_V);
			v.setValue(platform_info_for_repo_str);
			env.setVariable(v);
			
			if(iso_release_version.substring(0, 2).equals(ConstHelper.ISO_RELEASE_VERSION_2)){
				item.setProperty("build_script_zip_url", build_script_zip_url_2);
			}else if(iso_release_version.substring(0, 2).equals(ConstHelper.ISO_RELEASE_VERSION_3)){
				item.setProperty("build_script_zip_url", build_script_zip_url_3);
			}else{
				//default to 2.0
				item.setProperty("build_script_zip_url", build_script_zip_url_2);
			}
			item.setProperty("isDailyBuild", isDailyBuildStr);
			item.setProperty("sharedStoragePath", sharedStoragePathStr);
			items.add(item);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return items;
	}

	private String checkEnvVar(Environment env, String varName, String defaultValue) {
		String value = "";
		try {
			value = env.getVariable(varName).getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(value.isEmpty())
			return defaultValue;
		else
			return value;
	}

	@Override
	public void onStart(Environment env){
		try {
			/*
			// fetch auto build script
			env.extractResource("scripts/" + build_script_name);
			env.execute("chmod +x " + build_script_name);*/
			
			//copy folders (.m2 .ivy2 .ant)
			/*String src_root_dir = "/home/user/";
			String dst_root_dir = "/root/";
			String m2_dir = ".m2";
			String ivy2_dir = ".ivy2";
			String ant_dir = ".ant";
			
			File oldFile1 = new File(src_root_dir + m2_dir);
			File oldFile2 = new File(src_root_dir + ivy2_dir);
			File oldFile3 = new File(src_root_dir + ant_dir);
			
			File newFile1 = new File(dst_root_dir + m2_dir);
			File newFile2 = new File(dst_root_dir + ivy2_dir);
			File newFile3 = new File(dst_root_dir + ant_dir);
			
			oldFile1.renameTo(newFile1);
			oldFile2.renameTo(newFile2);
			oldFile3.renameTo(newFile3);*/

			String build_type = env.getVariable("build_type").getValue();
			isRPMBuild = this.isRPMBuildType(build_type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public ResultID run(ITaskItem ti, final Writer output, final Environment env) {
		SimpleTaskItem item = (SimpleTaskItem)ti;
		String url = item.getProperty("svn_url");
		String rev = item.getProperty("svn_rev");
		String BUILDTOOLS_url = item.getProperty("BUILDTOOLS_svn_url");
		String BUILDTOOLS_rev = item.getProperty("BUILDTOOLS_svn_rev");
		String ISOBUILD_url = item.getProperty("ISOBUILD_svn_url");
		String ISOBUILD_rev = item.getProperty("ISOBUILD_svn_rev");
		String username = item.getProperty("svn_username");
		String password = item.getProperty("svn_password");
		String build_type = item.getProperty("build_type");
		String iso_name_prefix = item.getProperty("iso_name_prefix");
		String iso_release_version = item.getProperty("iso_release_version");
		String iso_language = item.getProperty("iso_language");
		String iso_os_version = item.getProperty("iso_os_version");
		String build_script_zip_url = item.getProperty("build_script_zip_url");
		isDailyBuild = isDailyBuildType(item.getProperty("isDailyBuild"));
		sharedStoragePath = item.getProperty("sharedStoragePath");
		platform_info_for_repo = item.getProperty("platform_info_for_repo");
		String IM_url = "";
		String IM_rev = "";
		String IDHTOOLS_url = "";
		String IDHTOOLS_rev = "";
		String enable_resmon = ConstHelper.YES;
		String license_type = ConstHelper.LICENSE_TYPE_EVALUATION;
		String enable_callhome = ConstHelper.NO;
		String callhome_str = ConstHelper.WITHOUT_CALLHOME_STR;
		String src_location_param_str = "";
		
		if(!build_type.equals(ConstHelper.BUILD_TYPE_RPM)){
			IM_url = item.getProperty("IM_svn_url");
			IM_rev = item.getProperty("IM_svn_rev");
			IDHTOOLS_url = item.getProperty("IDHTOOLS_svn_url");
			IDHTOOLS_rev = item.getProperty("IDHTOOLS_svn_rev");
			enable_resmon = item.getProperty("enable_resmon");
			license_type = item.getProperty("license_type");
			enable_callhome = item.getProperty("enable_callhome");
			if(enable_callhome.equalsIgnoreCase(ConstHelper.YES)){
				callhome_str = ConstHelper.WITH_CALLHOME_STR;
			}
			src_location_param_str += genParamFromSvnUrl(IM_url,IM_rev, "-im");
			src_location_param_str += genParamFromSvnUrl(ISOBUILD_url,ISOBUILD_rev, "-build");
			src_location_param_str += genParamFromSvnUrl(IDHTOOLS_url,IDHTOOLS_rev, "-tools");
			src_location_param_str += "-lic " + license_type + " ";
			src_location_param_str += "-chreg " + callhome_str + " ";
			if(enable_resmon.equalsIgnoreCase(ConstHelper.NO)){
				src_location_param_str += "-noresmon";
			}
			
			
		}
		
		boolean result = true;
		try {
			// check out autobuild entry script
			File autoScriptFile = new File(build_script_name);
			if(autoScriptFile.exists()){
				deleteFolderAndContents(autoScriptFile);
			}
			IFile buildScriptZipFile = env.getFileByURI(URI.create(build_script_zip_url));
			this.extractZipBundle(buildScriptZipFile, ".");
			env.execute("chmod +x " + build_script_name);
			
			// check out source code
			fetchCode(env, url, rev, username, password, resultFolder);
			
			// check out build tools
			fetchCode(env, BUILDTOOLS_url, BUILDTOOLS_rev, username, password, ConstHelper.BUILDTOOLS_FOLDER_NAME);
			
			// check out iso build script
			fetchCode(env, ISOBUILD_url + "/" + ConstHelper.ISOBUILDSCRIPT_FOLDER_NAME, ISOBUILD_rev, username, password, ConstHelper.ISOBUILDSCRIPT_FOLDER_NAME);
			
			// generate version info for each component, and save it into shared storage
			IFile sources = env.getStorageRoot().getFile("sources.txt");
			if(!sources.exist()){
    			sources.create();
    			StringWriter sw = new StringWriter();
    			for(String s : new File(resultFolder + File.separator + "sources").list()){
    			    if(s.startsWith(".")) // skip .svn
    			        continue;
    			    sw.write(s);
    			    sw.write("\n");
    			}
    			sw.close();
    			ByteArrayInputStream is = new ByteArrayInputStream(sw.toString().getBytes());
    			sources.setContents(is);
    			is.close();
			}
			
			// build rpm
			int ret = 1;
			List<String> commands = getRequiredCommands(env);
			commands.add("sh " + build_script_name + " " + "RPM" + " -l " + iso_language + " -n " + iso_name_prefix + " -v " + iso_release_version + " -o " + iso_os_version + " -r " + rev); // + " >run_autobuild_script.log 2>&1"
			/*IFile rpmBuildLogFile = env.getStorageRoot().getFile(rpmBuildLog);
			if(!rpmBuildLogFile.exist()){
				if(!rpmBuildLogFile.create())
					LOG.error("rpmBuildLogFile (on shared storage) creation failed!");
			}*/
			ret = env.executeAs("root", commands.toArray(new String[]{}), output);
				

			IFile failureLogFile = env.getStorageRoot().getFile(failureLog);
			if(!failureLogFile.exist()){
				if(!failureLogFile.create()){
					LOG.error("failureLogFile (on shared storage) creation failed!");
				}
			}
		
			if(ret == 0){
				//check RPMS output
				String RPMSFolderName = resultFolder + java.io.File.separator + "output";
				if(!(new File(RPMSFolderName).exists())){
					writeLocalLogFile(failureLog, "Could NOT find output folder " + RPMSFolderName + " for RPMs!");
					failureLogFile.setContents(new FileInputStream(failureLog));
					return ResultID.Failed;
				}
				
				List<File> files = new ArrayList<File>();
				File RPMSFolder = new File(RPMSFolderName);
				File[] allFile = RPMSFolder.listFiles();
				for (int i = 0; i < allFile.length; i++){
					 files.add(allFile[i]);					 
				}
				//files.add(new File(RPMSFolderName));
				this.createZipBundle(RPMSZip, files);	
				if(!(new File(RPMSZip).exists())){
					writeLocalLogFile(failureLog, "Could NOT generate zip file " + RPMSZip + " for RPMs successfully!");
					failureLogFile.setContents(new FileInputStream(failureLog));
					return ResultID.Failed;
				}

			}else{
				result = false;
			}
			
			if(!isRPMBuild && (true == result) ){
				// build full package
				int ret2 = 1;
				List<String> commands2 = getRequiredCommands(env);
				commands2.add("sh " + build_script_name + " " + build_type + " -s " + " -l " + iso_language + " -n " + iso_name_prefix + " -v " + iso_release_version + " -o " + iso_os_version + " -r " + rev + " " + src_location_param_str); // + " >run_autobuild_script.log 2>&1"
				writeLocalLogFile("cmd.txt", "sh " + build_script_name + " " + "ISO -s " + " -l " + iso_language + " -n " + iso_name_prefix + " -v " + iso_release_version + " -o " + iso_os_version + " -r " + rev + " " + src_location_param_str);
				/*IFile packageBuildLogFile = env.getStorageRoot().getFile(pkgBuildLog);
				if(!packageBuildLogFile.exist()){
					if(!packageBuildLogFile.create())
						LOG.error("packageBuildLogFile (on shared storage) creation failed!");
				}*/
				ret2 = env.executeAs("root", commands2.toArray(new String[]{}), output);
			
			
				if(ret2 == 0){
					//check package build output	
					String packageFileFolder = "gadget/" + iso_name_prefix;
					if(!(new File(packageFileFolder).exists())){
						writeLocalLogFile(failureLog, "Could NOT find output folder " + packageFileFolder + " for IDH package!");
						failureLogFile.setContents(new FileInputStream(failureLog));
						return ResultID.Failed;
					}
					
					if(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_TGZ) || build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ALL)){
						pkgFileLocalPath = getPackageFile(packageFileFolder, iso_name_prefix, ConstHelper.SUFFIX_TGZ);
						if(pkgFileLocalPath.isEmpty()){
							writeLocalLogFile(failureLog, "Could NOT find IDH package (." + ConstHelper.SUFFIX_TGZ + ")  in output folder " + packageFileFolder + "!");
							failureLogFile.setContents(new FileInputStream(failureLog));
							return ResultID.Failed;
						}else{
							pkgFileName = pkgFileLocalPath.substring(pkgFileLocalPath.lastIndexOf("/") + 1);
							IFile tagFile = env.getStorageRoot().getFile(ConstHelper.FNAME_TGZ_BUILD_COMPLETE_TAG);
							if(!tagFile.exist()){
								if(!tagFile.create())
									LOG.error("tagFile (on shared storage) creation failed!");
							}
							writeLocalLogFile(ConstHelper.FNAME_TGZ_NAME_LOCAL_LOG, pkgFileName);
							try {
								tagFile.setContents(new FileInputStream(new File(ConstHelper.FNAME_TGZ_NAME_LOCAL_LOG)));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					if(build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ISO) || build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_ALL)){
						pkgFileLocalPath_2 = getPackageFile(packageFileFolder, iso_name_prefix, ConstHelper.SUFFIX_ISO);
						if(pkgFileLocalPath_2.isEmpty()){
							writeLocalLogFile(failureLog, "Could NOT find IDH package (." + ConstHelper.SUFFIX_ISO + ") in output folder " + packageFileFolder + "!");
							failureLogFile.setContents(new FileInputStream(failureLog));
							return ResultID.Failed;
						}else{
							pkgFileName_2 = pkgFileLocalPath_2.substring(pkgFileLocalPath_2.lastIndexOf("/") + 1);
							IFile tagFile_2 = env.getStorageRoot().getFile(ConstHelper.FNAME_ISO_BUILD_COMPLETE_TAG);
							if(!tagFile_2.exist()){
								if(!tagFile_2.create())
									LOG.error("tagFile_2 (on shared storage) creation failed!");
							}
							writeLocalLogFile(ConstHelper.FNAME_ISO_NAME_LOCAL_LOG, pkgFileName_2);
							try {
								tagFile_2.setContents(new FileInputStream(new File(ConstHelper.FNAME_ISO_NAME_LOCAL_LOG)));
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							
						}
					}

				}else{
					result = false;
				}
			}
			
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			result = false;
		}
		if(true == result){
			//write tag file to indicate build complete
			IFile buildCompleteTagFile = env.getStorageRoot().getFile(ConstHelper.FNAME_BUILD_COMPLETE_TAG);
			if(!buildCompleteTagFile.exist()){
				if(!buildCompleteTagFile.create())
					LOG.error("buildCompleteTagFile (on shared storage) creation failed!");
			}			
			writeLocalLogFile(ConstHelper.FNAME_BUILD_COMPLETE_LOCAL_LOG, "Build Complete!");
			try {
				buildCompleteTagFile.setContents(new FileInputStream(new File(ConstHelper.FNAME_BUILD_COMPLETE_LOCAL_LOG)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return ResultID.Passed;
		}
		return ResultID.Failed;
	}
	
	
	private String genParamFromSvnUrl(String url, String rev, String paramName) {
		String result = "";
		if(!url.isEmpty() && !rev.isEmpty() && !paramName.isEmpty()){
			result = paramName + " " + url + "@" + rev + " ";
		}
		return result;
	}

	protected List<String> getRequiredCommands(Environment env){
		List<String> commands = new ArrayList<String>();
		return commands;
	}
	
	protected void fetchCode(Environment env, String url, String rev, String username, String password, String localFolder) throws Exception{
		String archive_url = null;
		try{
			archive_url = env.getVariable(localFolder + "_archive").getValue();
		}
		catch(Exception e){			
		}
		
		File f = new File(localFolder);
		if(f.exists()){
			deleteFolderAndContents(f);
		}
		
		if(archive_url != null){
			IFile archiveFile = env.getFileByURI(URI.create(archive_url));
			if(archiveFile.exist()){
				this.extractZipBundle(archiveFile, ".");
				this.svnUpdate(url, username, password, rev, localFolder);
			}
		}
		else{
			this.svnCheckOut(url, username, password, rev, localFolder);
		}
	}
	
	private void deleteFolderContents(File folder) {
		if (folder.exists() && folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (!file.isDirectory()) {
					file.delete();
				} else {
					deleteFolderContents(file);
					file.delete();
				}
			}
		}
	}

	private void deleteFolderAndContents(File folder) {
		deleteFolderContents(folder);
		folder.delete();
	}

	protected boolean isRPMBuildType(String build_type) {
		if( (!build_type.isEmpty()) && (build_type.equalsIgnoreCase(ConstHelper.BUILD_TYPE_RPM))){
			return true;
		}else{
			return false;
		}
	}
	
	protected boolean isDailyBuildType(String isDailyBuildStr) {
		if( (!isDailyBuildStr.isEmpty()) && (isDailyBuildStr.equalsIgnoreCase(ConstHelper.YES))){
			return true;
		}else{
			return false;
		}
	}

	protected String getPackageFile(String fileFolder, String fileNamePrefix, String fileNameSuffix) {
		String result = "";
		File folder = new File(fileFolder);
		if(folder.exists() && folder.isDirectory()){
			File[] allFile = folder.listFiles();
			 for (int i = 0; i < allFile.length; i++){
				 File file = allFile[i];
				 String fileName = file.getName();
				 if(file.isFile() && fileName.startsWith(fileNamePrefix + "-") && fileName.endsWith("." + fileNameSuffix)){
					 result = fileFolder + "/" + fileName;
				 }
			 }
		}

		return result;
	}
	
	protected void writeLocalLogFile(String localLog, String errorMessage) {
		FileWriter fw;
		try {
			fw = new FileWriter(localLog);
			fw.write(errorMessage);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	@Override
	public void onFinish(Environment env) {
		try{
			if(isRPMBuild){
				if(new File(RPMSZip).exists()){
					IFile RPMSZipFile = env.getStorageRoot().getFile(RPMSZip);
					if(!RPMSZipFile.exist()){
						if(!RPMSZipFile.create())
							LOG.error("RPMSZipFile (on shared storage) creation failed!");
					}
					RPMSZipFile.setContents(new FileInputStream(RPMSZip));
				}
			}
			
			if(!isRPMBuild){
				if( (!pkgFileLocalPath.isEmpty()) && (new File(pkgFileLocalPath).exists())){
					
					//String release = checkEnvVar(env, "iso_release_version", "");
					//String product = release.startsWith(ConstHelper.ISO_RELEASE_VERSION_3)?"IM3":"IM2";
					//env.setFeatureProperty("package_file_full_name", pkgFileName, product);
					Variable v = new Variable("package_file_full_name",VarType.LOCAL_V);
					v.setValue(pkgFileName);
					env.setVariable(v);
					IFile tgzFile = env.getStorageRoot().getFile(pkgFileName);
					if(!tgzFile.exist()){
						if(!tgzFile.create())
							LOG.error("TgzFile (on shared storage) creation failed!");
					}
					tgzFile.setContents(new FileInputStream(pkgFileLocalPath));
					if(isDailyBuild){
						uploadToDailyBuildStorage(env, pkgFileName, pkgFileLocalPath);
					}
					
				}
				if( (!pkgFileLocalPath_2.isEmpty()) && (new File(pkgFileLocalPath_2).exists())){
					IFile ISOFile = env.getStorageRoot().getFile(pkgFileName_2);
					if(!ISOFile.exist()){
						if(!ISOFile.create())
							LOG.error("ISOFile (on shared storage) creation failed!");
					}
					ISOFile.setContents(new FileInputStream(pkgFileLocalPath_2));
					
					if(isDailyBuild){
						uploadToDailyBuildStorage(env, pkgFileName_2, pkgFileLocalPath_2);
					}
				}		
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void writeMyFile(Environment env, String fileName, String log) {
		IFile tagFile_2 = env.getStorageRoot().getFile(fileName);
		if(!tagFile_2.exist()){
			if(!tagFile_2.create())
				LOG.error("tagFile_2 (on shared storage) creation failed!");
		}
		writeLocalLogFile(fileName, log);
		try {
			tagFile_2.setContents(new FileInputStream(new File(fileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void uploadToDailyBuildStorage(Environment env, String pkgFileName, String pkgFileLocalPath) {
		IFolder sharedStorageIFolder = null;
		IFolder dailybuildIFolder = null;
		IFolder datetimeIFolder = null;
		IFolder dailyPlfIFolder = null;
		IFile dailyIFile = null;

		IFolder latestIFolder = null;
		IFolder latestPlfIFolder = null;
		IFile latestIFile = null;
		String datetimeStr = DateTimeUtils.getCurrentTimeStringByFormat("yyyyMMdd");
		try{
			if(null != sharedStoragePath && !sharedStoragePath.isEmpty()){
				sharedStorageIFolder = env.getFolderByURI(URI.create(sharedStoragePath));
				dailybuildIFolder = sharedStorageIFolder.getFolder(ConstHelper.DIR_DAILY_BUILD);
				if(!dailybuildIFolder.exist()){
					dailybuildIFolder.create();
				}
				datetimeIFolder = dailybuildIFolder.getFolder(datetimeStr);
				if(!datetimeIFolder.exist()){
					datetimeIFolder.create();
				}
				dailyPlfIFolder = datetimeIFolder.getFolder(platform_info_for_repo);
				if(!dailyPlfIFolder.exist()){
					dailyPlfIFolder.create();
				}
				dailyIFile = dailyPlfIFolder.getFile(pkgFileName);
				if(!dailyIFile.exist()){
					dailyIFile.create();
				}
				dailyIFile.setContents(new FileInputStream(pkgFileLocalPath));

				latestIFolder = dailybuildIFolder.getFolder(ConstHelper.DIR_LATEST_BUILD);
				if(!latestIFolder.exist()){
					latestIFolder.create();
				}
				latestPlfIFolder = latestIFolder.getFolder(platform_info_for_repo);
				if(latestPlfIFolder.exist()){
					latestPlfIFolder.delete();
				}
				if(!latestPlfIFolder.exist()){
					latestPlfIFolder.create();
				}
				latestIFile = latestPlfIFolder.getFile(pkgFileName);
				if(!latestIFile.exist()){
					latestIFile.create();
				}
				latestIFile.setContents(new FileInputStream(pkgFileLocalPath));
			}
			
		}catch(Exception e){
			LOG.error("Exception occurs during uploading package to daily build storage!");
		}
		
	}

}