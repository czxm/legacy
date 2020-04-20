package com.intel.cedar.features.IDH;

public class ConstHelper {
	//General text
	public static final String TEXT_NA	= "N/A";
	public static final String HEAD	= "HEAD";
	public static final String YES	= "YES";
	public static final String NO	= "NO";
	
	//OS type
	public static final String OS_VERSION_CENTOS_6d2	= "CentOS 6.2";
	public static final String OS_VERSION_CENTOS_6d3	= "CentOS 6.3";
	public static final String OS_VERSION_CENTOS_5d7	= "CentOS 5.7";
	public static final String OS_VERSION_SLES_11_SP1	= "SLES11 SP1";
	public static final String OS_VERSION_SLES_11_SP2	= "SLES11 SP2";
	
	//build type
	public static final String BUILD_TYPE_TGZ	= "TGZ";
	public static final String BUILD_TYPE_ISO	= "ISO";
	public static final String BUILD_TYPE_ALL	= "ALL";
	public static final String BUILD_TYPE_RPM	= "RPM";
	
	public static final String SUFFIX_TGZ	= "tar.gz";
	public static final String SUFFIX_ISO	= "iso";
	
	//iso build related
	public static final String ISO_RELEASE_VERSION_2	= "2.";
	public static final String ISO_RELEASE_VERSION_3	= "3.";
	public static final String ISO_LANGUAGE_CH	= "CH";
	public static final String ISO_LANGUAGE_EN	= "EN";
	public static final String LICENSE_TYPE_EVALUATION	= "Evaluation";
	public static final String LICENSE_TYPE_COMMERCIAL	= "Commercial";
	public static final String WITHOUT_CALLHOME_STR	= "without-chreg";
	public static final String WITH_CALLHOME_STR	= "with-chreg";
	public static final String DEFAULT_ISO_NAME_PREFIX	= "intelhadoop";

	//General text
	public static final String DEFAULT_SVN_USERNAME	= "lab_xmldev";
	public static final String DEFAULT_SVN_PASSWORD	= "qnn8S*NP";
	
	//local folder for build
	public static final String IDH_FOLDER_NAME	= "IDH";
	public static final String BUILDTOOLS_FOLDER_NAME	= "BUILD_TOOLS";
	public static final String ISOBUILDSCRIPT_FOLDER_NAME	= "gadget";
	
	//build tool & build result
	public static final String FNAME_BUILD_SCRIPT	= "autobuild.sh";
	public static final String FNAME_RPM_BUILD_LOG	= "rpm_build.log";
	public static final String FNAME_PKG_BUILD_LOG	= "package_build.log";
	public static final String FNAME_FAILURE_LOG	= "failure.log";	
	public static final String FNAME_RPMS_ZIP	= "RPMS.zip";
	public static final String FNAME_TGZ_BUILD_COMPLETE_TAG	= "tgz_build_complete_tag.log";
	public static final String FNAME_ISO_BUILD_COMPLETE_TAG	= "iso_build_complete_tag.log";
	public static final String FNAME_BUILD_COMPLETE_TAG	= "build_complete_tag.log";
	public static final String FNAME_TGZ_NAME_LOCAL_LOG = "tgz_name.log";
	public static final String FNAME_ISO_NAME_LOCAL_LOG = "iso_name.log";
	public static final String FNAME_BUILD_COMPLETE_LOCAL_LOG = "build_complete.log";

	//report label
	public static final String REPORT_LABEL_IDH_RPMS	= "IDH RPMs";
	public static final String REPORT_LABEL_IDH_PKG	= "IDH Package";
	
	//daily build dir label
	public static final String DIR_DAILY_BUILD	= "dailybuild";
	public static final String DIR_LATEST_BUILD	= "latest";
	
}
