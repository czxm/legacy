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

$ENV{"CTESTS"} = qq($rootdir).$SEP.qq($CTESTS_DIR_NAME);
$ENV{"CSRC"} = qq($rootdir).$SEP.qq($CSRC_DIR_NAME);
$ENV{"CPPAPI"} = qq($rootdir).$SEP.qq($CPPAPI_DIR_NAME);
$ENV{"JAVAAPI"} = qq($rootdir).$SEP.qq($JAVAAPI_DIR_NAME);

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
    $ENV{"PATH"} = qq($SVN_PATH;$PERL_PATH;$ZIP_PATH;$ENV{"ICC_HOME"}/bin;$ENV{"JAVA_HOME"}/bin;$ENV{"ANT_HOME"}/bin;$OLD_PATH;$CYGWIN_PATH);
}
elsif($platform eq "windows64")
{
    $ICC_HOME = "C:\\Program Files (x86)\\Intel\\Compiler\\C++\\10.1.025\\EM64T";
    $SVN_PATH = "C:\\Program Files (x86)\\svn-win32-1.6.13\\bin";
    $PERL_PATH = "C:\\Perl\\site\\bin;C:\\Perl\\bin";
    $ZIP_PATH = "C:\\Program Files\\7-Zip";
    $CYGWIN_PATH = "C:\\cygwin\\bin";
    $ENV{"PATH"} = qq($SVN_PATH;$PERL_PATH;$ZIP_PATH;$ENV{"ICC_HOME"}/bin;$ENV{"JAVA_HOME"}/bin;$ENV{"ANT_HOME"}/bin;$OLD_PATH;$CYGWIN_PATH);
}
else
{
        print("please set platform param correctly!!\n");
        exit;
}

### Run test
chdir(qq($ENV{"CTESTS"}/functionality-test));
$cmd=join(" ",@ARGV);
if( $cmd eq "" )
{
        print("please set running command correctly!!\n");
        exit;
}
print "running command: $cmd\n";
system(qq($cmd));

### Restore system environment variables
$ENV{"CTESTS"} = $OLD_CTESTS;
$ENV{"CSRC"} = $OLD_CSRC;
$ENV{"CPPAPI"} = $OLD_CPPAPI;
$ENV{"JAVAAPI"} = $OLD_JAVAAPI;
$ENV{"PATH"} = $OLD_PATH;
$ENV{"LD_LIBRARY_PATH"} = $OLD_LDPATH;
