#!/usr/bin/perl

use File::Path;
use File::Copy;

sub format_slash
{
    $STR_TO_FMT = $_[0];
    $STR_SEP = $_[1];
    $STR_TO_FMT =~ s/\\+/\\/g;
    $STR_TO_FMT =~ s/\/+/\//g;
    if($STR_SEP eq "\\")
    {
    	$STR_TO_FMT =~ s/\//\\/g;
    }
    else
    {
    	$STR_TO_FMT =~ s/\\/\//g;
    }
    return $STR_TO_FMT;
}

sub guarded_system(@ARGS)
{
  $command=shift;
  print "$command\n";
  $ret=system("$command > log");
  $ret==0 || die;
}

$CSRC_DIR_NAME = qq(xmlcore-src);
$CPPAPI_DIR_NAME = qq(cpp_api);
$JAVAAPI_DIR_NAME = qq(java-api);
$CTESTS_DIR_NAME = qq(xml-products-tests);
$CTESTS_SVN_URL = qq(https://sh-ssvn.sh.intel.com/ssg_repos/svn_xtt/xtt/xmlcore-tests/xml-products-tests);
$XCHECK_SVN_URL = qq(https://sh-ssvn.sh.intel.com/ssg_repos/svn_xtt/xtt/performance-test/xpath-v1/xcheck/trunk);
$XCHECK_DIR_NAME = qq(xcheck);
$SVN_USERNAME = qq(lab_xmldev);
$SVN_PASSWORD = qq(qnn8S*NP);
$SEP = "/";
if($platform eq "windows" || $platform eq "windows64")
{
    $SEP = "\\";
}

$param_rootdir=$ARGV[0];
shift;
$rootdir = format_slash($param_rootdir, $SEP);
if ( ($rootdir eq "") || (! -e $rootdir) )
{
        print("please set root dir correctly!!\n");
        exit;
}

### Backup/reset system environment variables
$OLD_CTESTS = $ENV{"CTESTS"};
$ENV{"CTESTS"} = qq($rootdir/xml-products-tests);

### Check CTESTS status
print(qq(rootdir----$rootdir\n)); 
chdir(qq($rootdir));
if ( -e $ENV{"CTESTS"} )
{
    print(qq($CTESTS_DIR_NAME));     
    system(qq(rm -rf $CTESTS_DIR_NAME));
}
system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $CTESTS_SVN_URL));
system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $XCHECK_SVN_URL));
system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $CTESTS_SVN_URL $CTESTS_DIR_NAME));
system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $XCHECK_SVN_URL $XCHECK_DIR_NAME));
### Unzip XML source and library
chdir(qq($rootdir));
if( -e "package.zip")
{
	system(qq(rm -rf $CSRC_DIR_NAME $CPPAPI_DIR_NAME $JAVAAPI_DIR_NAME));
	guarded_system(qq(unzip package.zip));
}
else
{
        print("No package of XML source and library found during unzip process!!\n");
        exit;
}
system(qq(rm -f "$ENV{'CTESTS'}/test-entry/send-to-repserver.pl"));
copy(qq($rootdir/send-to-repserver.pl), qq($ENV{"CTESTS"}/test-entry/send-to-repserver.pl));
### Restore system environment variables
$ENV{"CTESTS"} = $OLD_CTESTS;
