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
$SVN_USERNAME = qq(lab_xmldev);
$SVN_PASSWORD = qq(qnn8S*NP);
$FIND = qq(find);
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

$platform = $ARGV[0];
shift;

### Backup/reset system environment variables
$OLD_CTESTS = $ENV{"CTESTS"};
$OLD_CSRC = $ENV{"CSRC"};
$OLD_CPPAPI = $ENV{"CPPAPI"};
$OLD_JAVAAPI = $ENV{"JAVAAPI"};
$OLD_PATH = $ENV{"PATH"};
$OLD_LDPATH = $ENV{"LD_LIBRARY_PATH"};

if($platform eq "linux")
{
    $ENV{"PATH"} = qq($ENV{"ICC_HOME"}/bin/ia32:$ENV{"JAVA_HOME"}/bin:$ANT_HOME/bin:$OLD_PATH);
    $ENV{"LD_LIBRARY_PATH"} = qq($ENV{"CPPAPI"}/bin/ia32:$ENV{"CSRC"}/lib/libt:$ENV{"ICC_HOME"}/bin/ia32:$OLD_LDPATH);
}
elsif($platform eq "em64")
{
    $ENV{"PATH"} = qq($ENV{"ICC_HOME"}/bin/intel64:$ENV{"JAVA_HOME"}/bin:$ANT_HOME/bin:$OLD_PATH);
    $ENV{"LD_LIBRARY_PATH"} = qq($ENV{"CPPAPI"}/bin/intel64:$ENV{"CSRC"}/lib/libt:$ENV{"ICC_HOME"}/bin/intel64:$OLD_LDPATH);
}
elsif($platform eq "windows")
{
    $ICC_HOME = "C:\\Program Files\\Intel\\Compiler\\C++\\10.1.025\\IA32";
    $SVN_PATH = "C:\\Program Files\\svn-win32-1.6.13\\bin";
    $PERL_PATH = "C:\\Perl\\site\\bin;C:\\Perl\\bin";
    $ZIP_PATH = "C:\\Program Files\\7-Zip";
    $CYGWIN_PATH = "C:\\cygwin\\bin";
    $ENV{"PATH"} = qq($SVN_PATH;$PERL_PATH;$ZIP_PATH;$ENV{"ICC_HOME"}/bin;$ENV{"JAVA_HOME"}/bin;$ENV{"ANT_HOME"}/bin;$CYGWIN_PATH;$OLD_PATH);
}
elsif($platform eq "windows64")
{
    $ICC_HOME = "C:\\Program Files (x86)\\Intel\\Compiler\\C++\\10.1.025\\EM64T";
    $SVN_PATH = "C:\\Program Files (x86)\\svn-win32-1.6.13\\bin";
    $PERL_PATH = "C:\\Perl\\site\\bin;C:\\Perl\\bin";
    $ZIP_PATH = "C:\\Program Files\\7-Zip";
    $CYGWIN_PATH = "C:\\cygwin\\bin";
    $ENV{"PATH"} = qq($SVN_PATH;$PERL_PATH;$ZIP_PATH;$ENV{"ICC_HOME"}/bin;$ENV{"JAVA_HOME"}/bin;$ENV{"ANT_HOME"}/bin;$CYGWIN_PATH;$OLD_PATH);
}
else
{
        print("please set platform param correctly!!\n");
        exit;
}
$ENV{"CTESTS"} = qq($rootdir).$SEP.qq($CTESTS_DIR_NAME);
$ENV{"CSRC"} = qq($rootdir).$SEP.qq($CSRC_DIR_NAME);
$ENV{"CPPAPI"} = qq($rootdir).$SEP.qq($CPPAPI_DIR_NAME);
$ENV{"JAVAAPI"} = qq($rootdir).$SEP.qq($JAVAAPI_DIR_NAME);

### Check CTESTS status 
chdir(qq($rootdir));
if ( -e $ENV{"CTESTS"} )
{
    system(qq(rm -rf $CTESTS_DIR_NAME));
}
system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $CTESTS_SVN_URL));
system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $CTESTS_SVN_URL $CTESTS_DIR_NAME));

### Perform XML library build
if ( -e qq($rootdir/CT_demo.config))
{
    copy(qq($rootdir/CT_demo.config),qq($ENV{"CTESTS"}/test-entry/config/CT_demo.config));
}
else
{
    print("No build config file generated!!\n");
    exit;    
}
system(qq(rm -f qq($ENV{"CTESTS"}/test-entry/BUILD_ENTRY.pl)));
copy(qq($rootdir/BUILD_ENTRY.pl), qq($ENV{"CTESTS"}/test-entry/BUILD_ENTRY.pl));
chdir( qq($ENV{"CTESTS"}/test-entry) );
system(qq(perl BUILD_ENTRY.pl config=CT_demo));

### Zip XML source and library
chdir(qq($rootdir));
system(qq(rm -rf package.zip));
system(qq($FIND xmlcore-src cpp_api java-api -not -path "*.svn*" -and -not -path "*.dep*" >list));
guarded_system("xargs --arg-file=list zip package.zip");

### Restore system environment variables
$ENV{"CTESTS"} = $OLD_CTESTS;
$ENV{"CSRC"} = $OLD_CSRC;
$ENV{"CPPAPI"} = $OLD_CPPAPI;
$ENV{"JAVAAPI"} = $OLD_JAVAAPI;
$ENV{"PATH"} = $OLD_PATH;
$ENV{"LD_LIBRARY_PATH"} = $OLD_LDPATH;
