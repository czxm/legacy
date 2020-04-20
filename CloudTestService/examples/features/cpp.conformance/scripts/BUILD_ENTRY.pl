#!/usr/bin/perl
use File::Copy;
sub getTodayDate()
{
	($sec, $min, $hour, $day, $mon, $year) = localtime(time);
	$year = $year + 1900;
	$mon = $mon + 1;
	$mon_alone = $mon;
	$day_alone = $day;
	if ($mon < 10)
	{
	    $monstr = qq(0$mon);
	}
	else
	{
	    $monstr = qq($mon);
	}
	if ($day < 10)
	{
	    $daystr = qq(0$day);
	}
	else
	{
	    $daystr = qq($day);
	}
	return qq($year$monstr$daystr);

}
sub trim
{
        my $str = $_[0];
        $str =~ s/^ *//g;
        $str =~ s/ *$//g;
        return $str;
}

sub help_info()
{
        print "-------------------------------------------------------------------------------------------------\n";
        print "  func:                  perform XMLCORE, CPPAPI, JAVAAPI build according to config file\n";
        print "  usage:                 perl BUILD_ENTRY.pl [config=XXXX] [-help]\n\n";
        print "  The build script will parse the configuration file $CTESTS/config/XXXX.xml first, get the related params and then do the build accordingly. The default configuration file is $CTESTS/config/default.config. \n";
        print "  The meanings of params defined in the configuration file are described as below:\n\n ";
        print "  xmlcore_build:\n";
        print "    turn_on=true|false        #build xmlcore or not\n";
        print "    build_target_platform=as4|as5|...   #the build target platform for xmlcore\n";
        print "    pgo_training_platform=linux|em64|windows|windows64    #the testing platform for PGO build training\n";
        print "    xmlcore_branch=SVN_URL_FOR_XMLCORE	#the svn url for xmlcore source code\n";
        print "    is_pgo_build=true|false      #whether do PGO build or not\n";
        print "    is_cross_build=true|false      #whether do cross build or not\n";
        print "    is_debug_build=true|false      #whether do debug build or not\n";
        print "    is_rebuild=true|false      #whether do rebuild or not\n";
        print "    is_recheckout=true|false      #whether to recheckout xmlcore source code or not\n";
        print "    parallel=SOME_INTEGER      #how many threads will be used when performing build\n";
        print "    upload_xmlcore=true|false      #whether to upload xmlcore libs to ftpserver when build is finished\n";
        print "    ftpserver=FTPSERVER_INFO      #only used when upload_xmlcore=true. The info may include ftp server name, port, username and passwd etc.\n";
        print "    built_date=SOME_DATE      #only used when upload_xmlcore=true. The uploading script will create a new dir with name '$built_date' on ftpserver, and put all the xmlcore libs into it\n";
        print "    mailto=SOME_EMAIL_ADDR      #only used when upload_xmlcore=true.A notification email will be sent to this address when failure occurs during xmlcore libs uploading\n\n";
        print "  api_build:\n";
        print "    turn_on=true|false        #build api or not\n";
        print "    platform=linux|em64|windows|windows64    #the platform for api build\n";
        print "    cpp_branch=SVN_URL_FOR_CPPAPI      #the svn url for cppapi source code\n";
        print "    java_branch=SVN_URL_FOR_JAVAAPI      #the svn url for javaapi source code\n";
        print "    is_debug_build=true|false      #whether do debug build or not\n";
        print "    is_rebuild=true|false      #whether do rebuild or not\n";
        print "    is_recheckout=true|false      #whether to recheckout api source code or not\n";
        print "    parallel=SOME_INTEGER      #how many threads will be used when performing build\n";
        print "    download_xmlcore=true|false      #whether to download xmlcore libs from ftpserver when xmlcore_build is NOT turned on\n";
        print "    ftpserver=FTPSERVER_INFO      #only used when xmlcore_build is NOT turned on and download_xmlcore=true. The info may include ftp server name, port, username and passwd etc.\n";
        print "    core_built_platform=as4|as5|...      #only used when upload_xmlcore=true., And it will indicate which platform's xmlcore libs need to be download.\n";
        print "    core_built_date=SOME_DATE      #only used when download_xmlcore=true. And the downloading script will use it to choose the dir in which the wanted xmlcore libs locate.\n";
        print "    mailto=SOME_EMAIL_ADDR      #only used when download_xmlcore=true. A notification email will be sent to this address when failure occurs during xmlcore libs downloading\n\n";
        print "-------------------------------------------------------------------------------------------------\n\n";
}

sub read_core_config
{
	chdir(qq($ENV{"CTESTS"}/test-entry/config));
	open(CONF, "< $CONFIG_FILE") || die("Can not open config file -- $CONFIG_FILE !!\nPlease use -help for usage!!\n");	
	my $type_start = "false";
	my $type = "xmlcore_build";
	my $line;
	while (defined($line = <CONF>))
	{
		chomp($line);
	        if ($line =~ /<$type>/i)
	        {
        	        $type_start = "true";
                	next;
        	}
        	if ($line =~ /<\/$type>/i)
        	{
                	$type_start = "false";
			last;
		}

		$line=trim($line);

		if ($type_start eq "true")
		{
			if ( $line =~ /^turn_on=:(.*)/ ) 
			{
				$PARAM_CORE__turnon=$1;
				$PARAM_CORE__turnon=trim($PARAM_CORE__turnon);
			}
			elsif( $line =~ /^build_target_platform=:(.*)/ )
			{
				$PARAM_CORE__btplf=$1;
				$PARAM_CORE__btplf=trim($PARAM_CORE__btplf);
			}
                	elsif( $line =~ /^pgo_training_platform=:(.*)/ )
                	{
                       		$PARAM_CORE__testplf=$1;
				$PARAM_CORE__testplf=trim($PARAM_CORE__testplf);
                	}
			elsif( $line =~ /^xmlcore_branch=:(.*)/ )
                	{
                        	$PARAM_CORE__corebranch=$1;
				$PARAM_CORE__corebranch=trim($PARAM_CORE__corebranch);
                                $PARAM_CORE__corebranch =~ s/#TODAY/$TODAY_DATE/;
                	}
                	elsif( $line =~ /^is_pgo_build=:(.*)/ )
                	{
                        	$PARAM_CORE__pgo=$1;
				$PARAM_CORE__pgo=trim($PARAM_CORE__pgo);
                	}
			elsif( $line =~ /^is_cross_build=:(.*)/ )
                	{
                        	$PARAM_CORE__cross=$1;
				$PARAM_CORE__cross=trim($PARAM_CORE__cross);
                	}
                	elsif( $line =~ /^is_debug_build=:(.*)/ )
                	{
                        	$PARAM_CORE__debug=$1;
				$PARAM_CORE__debug=trim($PARAM_CORE__debug);
                	}
                	elsif( $line =~ /^is_rebuild=:(.*)/ )
                	{
                        	$PARAM_CORE__rebuild=$1;
				$PARAM_CORE__rebuild=trim($PARAM_CORE__rebuild);
                	}
                	elsif( $line =~ /^is_recheckout=:(.*)/ )
                	{
                        	$PARAM_CORE__recheckout=$1;
				$PARAM_CORE__recheckout=trim($PARAM_CORE__recheckout);
                	}
                	elsif( $line =~ /^parallel=:(.*)/ )
                	{
                       		$PARAM_CORE__parallel=$1;
				$PARAM_CORE__parallel=trim($PARAM_CORE__parallel);
                	}
                        elsif( $line =~ /^upload_xmlcore=:(.*)/ )
                        {
                                $PARAM_CORE__uploadcore=$1;
                                $PARAM_CORE__uploadcore=trim($PARAM_CORE__uploadcore);
                        }
                	elsif( $line =~ /^ftpserver=:(.*)/ )
                	{
                       		$PARAM_CORE__ftpserver=$1;
				$PARAM_CORE__ftpserver=trim($PARAM_CORE__ftpserver);
                	}
                        elsif( $line =~ /^built_date=:(.*)/ )
                        {
                                $PARAM_CORE__btdate=$1;
				$PARAM_CORE__btdate=trim($PARAM_CORE__btdate);
                                $PARAM_CORE__btdate =~ s/#TODAY/$TODAY_DATE/;
                        }
                	elsif( $line =~ /^mailto=:(.*)/ )
                	{
                        	$PARAM_CORE__mailto=$1;
				$PARAM_CORE__mailto=trim($PARAM_CORE__mailto);
                	}
		}
		next;
	}
		
	close(CONF);
}


sub read_api_config
{
	chdir(qq($ENV{"CTESTS"}/test-entry/config));
	open(CONF, "< $CONFIG_FILE") || die("Can not open config file -- $CONFIG_FILE !!\nPlease use -help for usage!!\n");

	my $type_start = "false";
	my $type = "api_build";
	my $line;
	while (defined($line = <CONF>))
	{
        	chomp($line);

        	if ($line =~ /<$type>/i)
        	{
                	$type_start = "true";
                	next;
        	}
        	if ($line =~ /<\/$type>/i)
        	{
                	$type_start = "false";
                	last;
        	}

		$line=trim($line);

        	if ($type_start eq "true")
        	{
                	if ( $line =~ /^turn_on=:(.*)/ )
                	{
                        	$PARAM_API__turnon=$1;
				$PARAM_API__turnon=trim($PARAM_API__turnon);
                	}
                        elsif ( $line =~ /^platform=:(.*)/ )
                        {
                                $PARAM_API__testplf=$1;
				$PARAM_API__testplf=trim($PARAM_API__testplf);
                        }
                        elsif ( $line =~ /^cpp_branch=:(.*)/ )
                        {
                                $PARAM_API__cppbranch=$1;
				$PARAM_API__cppbranch=trim($PARAM_API__cppbranch);
				$PARAM_API__cppbranch =~ s/#TODAY/$TODAY_DATE/;
                        }
                        elsif ( $line =~ /^java_branch=:(.*)/ )
                        {
                                $PARAM_API__javabranch=$1;
				$PARAM_API__javabranch=trim($PARAM_API__javabranch);
                                $PARAM_API__javabranch =~ s/#TODAY/$TODAY_DATE/;
                        }
                        elsif ( $line =~ /^is_debug_build=:(.*)/ )
                        {
                                $PARAM_API__debug=$1;
				$PARAM_API__debug=trim($PARAM_API__debug);
                        }
                        elsif( $line =~ /^is_rebuild=:(.*)/ )
                        {
                                $PARAM_API__rebuild=$1;
				$PARAM_API__rebuild=trim($PARAM_API__rebuild);
                        }
                        elsif( $line =~ /^is_recheckout=:(.*)/ )
                        {
                                $PARAM_API__recheckout=$1;
				$PARAM_API__recheckout=trim($PARAM_API__recheckout);
                        }
                        elsif( $line =~ /^parallel=:(.*)/ )
                        {
                                $PARAM_API__parallel=$1;
				$PARAM_API__parallel=trim($PARAM_API__parallel);
                        }
                        elsif( $line =~ /^download_xmlcore=:(.*)/ )
                        {
                                $PARAM_API__downloadcore=$1;
                                $PARAM_API__downloadcore=trim($PARAM_API__downloadcore);
                        }
                        elsif( $line =~ /^ftpserver=:(.*)/ )
                        {
                                $PARAM_API__ftpserver=$1;
				$PARAM_API__ftpserver=trim($PARAM_API__ftpserver);
                        }
                        elsif ( $line =~ /^core_built_platform=:(.*)/ )
                        {
                                $PARAM_API__corebtplf=$1;
				$PARAM_API__corebtplf=trim($PARAM_API__corebtplf);
                        }
                        elsif ( $line =~ /^core_built_date=:(.*)/ )
                        {
                                $PARAM_API__corebtdate=$1;
				$PARAM_API__corebtdate=trim($PARAM_API__corebtdate);
                                $PARAM_API__corebtdate =~ s/#TODAY/$TODAY_DATE/;
                        }
                        elsif( $line =~ /^mailto=:(.*)/ )
                        {
                                $PARAM_API__mailto=$1;
				$PARAM_API__mailto=trim($PARAM_API__mailto);
                        }
		}
		next;
	}

	close(CONF);
}

$CONFIG_FILE = qq(default.config);
$TODAY_DATE = getTodayDate();
$SVN_USERNAME = "lab_xmldev";
$SVN_PASSWORD = "qnn8S*NP";
foreach (@ARGV)
{
        if (/^-help$/)
        {
                help_info();
                exit;
        }
        elsif (/^config=(.*)/)
        {
                $CONFIG_FILE=qq($1.config);
        }
        elsif (/^today=(.*)/)
        {
                $TODAY_DATE=$1;
        }
        elsif (/^username=(.*)/)
        {
                $SVN_USERNAME=$1;
        }
        elsif (/^password=(.*)/)
        {
                $SVN_PASSWORD=$1;
        }

}

### default common params
$PARAM_COMMON__iswindows = "false";

### default xmlcore build params
$PARAM_CORE__turnon = "true";
$PARAM_CORE__btplf = "as5";
$PARAM_CORE__testplf = "linux";
$PARAM_CORE__corebranch = "https://sh-ssvn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/trunk/xmlcore-src";
$PARAM_CORE__pgo = "true";
$PARAM_CORE__cross = "true";
$PARAM_CORE__debug = "false";
$PARAM_CORE__rebuild = "true";
$PARAM_CORE__recheckout = "true";
$PARAM_CORE__parallel = "4";
$PARAM_CORE__uploadcore = "false";
$PARAM_CORE__ftpserver = "";
$PARAM_CORE__btdate = "TODAY";
$PARAM_CORE__mailto = "";

### default API build params
$PARAM_API__turnon = "true";
$PARAM_API__testplf = "linux";
$PARAM_API__cppbranch = "https://sh-ssvn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/trunk/cpp_api";
$PARAM_API__javabranch = "https://sh-ssvn.sh.intel.com/ssg_repos/svn_xmldev/xmldev/penngrove/trunk/java-api";
$PARAM_API__debug = "false";
$PARAM_API__rebuild = "true";
$PARAM_API__recheckout = "true";
$PARAM_API__parallel = "4";
$PARAM_API__downloadcore = "false";
$PARAM_API__ftpserver = "";
$PARAM_API__corebtplf = "as5";
$PARAM_API__corebtdate = "TODAY";
$PARAM_API__mailto = "";

read_core_config();
read_api_config();

print "----CORE BUILD CONFIG-------\n";
print $PARAM_CORE__turnon."\n";
print $PARAM_CORE__btplf."\n";
print $PARAM_CORE__testplf."\n";
print $PARAM_CORE__corebranch."\n";
print $PARAM_CORE__pgo."\n";
print $PARAM_CORE__cross."\n";
print $PARAM_CORE__debug."\n";
print $PARAM_CORE__rebuild."\n";
print $PARAM_CORE__recheckout."\n";
print $PARAM_CORE__parallel."\n";
print $PARAM_CORE__uploadcore."\n";
print $PARAM_CORE__ftpserver."\n";
print $PARAM_CORE__btdate."\n";
print $PARAM_CORE__mailto."\n";
print "\n";
print "----API BUILD CONFIG--------\n";
print $PARAM_API__turnon."\n";
print $PARAM_API__testplf."\n";
print $PARAM_API__cppbranch."\n";
print $PARAM_API__javabranch."\n";
print $PARAM_API__debug."\n";
print $PARAM_API__rebuild."\n";
print $PARAM_API__recheckout."\n";
print $PARAM_API__parallel."\n";
print $PARAM_API__downloadcore."\n";
print $PARAM_API__ftpserver."\n";
print $PARAM_API__corebtplf."\n";
print $PARAM_API__corebtdate."\n";
print $PARAM_API__mailto."\n";
print "\n";

#exit;

### check whether env is set correctly
if ( ($ENV{"CSRC"} eq "") || ( (! -e $ENV{"CSRC"}) && ($PARAM_CORE__recheckout eq "false") ) )
{
       	print("please set CSRC env var correctly!!\n");
       	exit;
}
if ( $PARAM_API__turnon eq "true" )
{
	if ( ($ENV{"CPPAPI"} eq "") || ( (! -e $ENV{"CPPAPI"}) && ($PARAM_API__recheckout eq "false") ) )
	{
        	print("please set CPPAPI env var correctly!!\n");
        	exit;
	}
	if ( ($ENV{"JAVAAPI"} eq "") || ( (! -e $ENV{"JAVAAPI"}) && ($PARAM_API__recheckout eq "false") ) )
	{
        	print("please set JAVAAPI env var correctly!!\n");
        	exit;
	}
}
if ( ($ENV{"CTESTS"} eq "") || (! -e $ENV{"CTESTS"}) )
{
        print("please set CTESTS env var correctly!!\n");
        exit;
}

if ( $PARAM_CORE__turnon eq "true" )
{
	if ( $PARAM_CORE__btplf eq "" )
        {
		print("please set param 'build_target_platform' for xmlcore build correctly!!\n");
        	exit;
	}
	if ( ($PARAM_CORE__pgo eq "true") && ($PARAM_CORE__testplf eq "")  )
        {
		print("please set param 'pgo_traning_platform' for xmlcore build correctly!!\n");
        	exit;
	}

}
elsif ( $PARAM_API__turnon eq "true" )
{
	if ( $PARAM_API__downloadcore eq "true" )
	{
		if ( $PARAM_API__ftpserver eq "" )
		{
			print("please set param 'ftpserver' for downloading xmlcore libs correctly!!\n");
        		exit;		
		}
		if ( $PARAM_API__corebtplf eq "" )
		{
			print("please set param 'core_built_platform' for downloading xmlcore libs correctly!!\n");
        		exit;		
		}
		if ( $PARAM_API__corebtdate eq "" )
		{
			print("please set param 'core_built_date' for downloading xmlcore libs correctly!!\n");
        		exit;		
		}
	}
}
if( $PARAM_CORE__recheckout eq "true" )  ### NOTE: could recheckout xmlcore even when $PARAM_CORE__turnon eq "false".
{
	if($PARAM_CORE__corebranch eq "")
	{
		print("please set param 'xmlcore_branch' correctly!!\n");
        	exit;			
	}
	
        if(! -e $ENV{"CSRC"})
        {
                system(qq(mkdir -p $ENV{"CSRC"}));
        }
        chdir qq($ENV{"CSRC"}/..);
        system(qq(rm -rf xmlcore-src));
        system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_CORE__corebranch));
        system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_CORE__corebranch xmlcore-src));
        system(qq(rm -f xmlcore-src/proj-tools/bin/windows/ia32/vc8/bin/cygwin1.dll));
        system(qq(rm -f xmlcore-src/proj-tools/bin/windows/em64t/vc8/bin/cygwin1.dll));
}

if( $PARAM_API__turnon eq "true" && $PARAM_API__recheckout eq "true" )
{
	if($PARAM_API__cppbranch eq "")
	{
		print("please set param 'cppapi_branch' correctly!!\n");
        	exit;			
	}

	if($PARAM_API__javabranch eq "")
	{
		print("please set param 'javaapi_branch' correctly!!\n");
        	exit;			
	}
        if(! -e $ENV{"CPPAPI"})
        {
                system(qq(mkdir -p $ENV{"CPPAPI"}));
        }
        chdir qq($ENV{"CPPAPI"}/..);
        system(qq(rm -rf cpp_api));
        system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_API__cppbranch));
        system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_API__cppbranch cpp_api));
        if(! -e $ENV{"JAVAAPI"})
        {
                system(qq(mkdir -p $ENV{"JAVAAPI"}));
        }
        chdir qq($ENV{"JAVAAPI"}/..);
        system(qq(rm -rf java-api));
        system(qq(echo p | svn ls --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_API__javabranch));
        system(qq(svn co --non-interactive --username=$SVN_USERNAME --password=$SVN_PASSWORD $PARAM_API__javabranch java-api));
}


if( $PARAM_CORE__turnon eq "true" ) ### build xmlcore libs and tools, upload them to ftpserver if needed
{
        chdir qq($ENV{"CTESTS"}/test-entry);
	print "---- Start XMLCORE build ----\n";
	system qq(perl xmlcore_build.pl branch=$PARAM_CORE__corebranch btplf=$PARAM_CORE__btplf testplf=$PARAM_CORE__testplf pgo=$PARAM_CORE__pgo cross=$PARAM_CORE__cross debug=$PARAM_CORE__debug rebuild=$PARAM_CORE__rebuild recheckout=false -j=$PARAM_CORE__parallel);

	### upload xmlcore libs and header files if needed
	if( ($PARAM_CORE__uploadcore eq "true") && ($PARAM_CORE__ftpserver ne "") )
	{

	        if($PARAM_CORE__debug eq "true")
        	{
                	$XMLCORE_LIB_DIR_NAME = "libdt";
                	$DEBUG_OR_RELEASE_DIR = "Debug";
        	}
        	else
        	{
                	$XMLCORE_LIB_DIR_NAME = "libt";
                	$DEBUG_OR_RELEASE_DIR = "Release";
        	}

        	chdir(qq($ENV{"CTESTS"}/test-result/binary));
        	system(qq(mkdir -p Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));

        	if(($PARAM_CORE__btplf eq "win32") or ($PARAM_CORE__btplf eq "win64"))
        	{
                	system(qq(cp $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME/*.lib $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
                	system(qq(cp $ENV{"CSRC"}/bin/*.exe $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			system(qq(cp $ENV{"CSRC"}/xmlcore_headers.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			chdir(qq($ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
			system(qq(tar czvf xmlcore_lib.tgz *.lib build_revision.txt));
                        system(qq(mv xmlcore_lib.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			chdir(qq($ENV{"CSRC"}/bin));
			system(qq(tar czvf xmlcore_bin.tgz *.exe));

			system(qq(mv xmlcore_bin.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
		}
        	else
        	{
                	system(qq(cp $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME/*.so $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
                	system(qq(cp $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME/*.a $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
                	system(qq(cp $ENV{"CSRC"}/bin/*.exe $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			system(qq(cp $ENV{"CSRC"}/xmlcore_headers.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			chdir(qq($ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
                        system(qq(tar czvf xmlcore_lib.tgz *.so *.a build_revision.txt));
                        system(qq(mv xmlcore_lib.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
			chdir(qq($ENV{"CSRC"}/bin));
                        system(qq(tar czvf xmlcore_bin.tgz *.exe));
                        system(qq(mv xmlcore_bin.tgz $ENV{"CTESTS"}/test-result/binary/Upload/$PARAM_CORE__btdate/$PARAM_CORE__btplf/Package/$DEBUG_OR_RELEASE_DIR));
        	}
        	chdir(qq($ENV{"CTESTS"}/test-result/binary));
		print "---- Start uploading xmlcore libs and tools ----\n";
        	system(qq(perl ../../test-entry/ftptool.pl server=$PARAM_CORE__ftpserver operation=upload date=$PARAM_CORE__btdate plf=$PARAM_CORE__btplf comp=xmlcore debug=$PARAM_CORE__debug mailto=$PARAM_CORE__mailto testname=CPP));


		
	}
}
else  ### not build xmlcore, download xmlcore libs and tools from ftpserver if needed
{
        if( ($PARAM_API__downloadcore eq "true") && ($PARAM_API__ftpserver ne "") )
        {
        	if($PARAM_API__debug eq "true")
        	{
                	$XMLCORE_LIB_DIR_NAME = "libdt";
	                $DEBUG_OR_RELEASE_DIR = "Debug";
        	}
        	else
        	{
                	$XMLCORE_LIB_DIR_NAME = "libt";
                	$DEBUG_OR_RELEASE_DIR = "Release";
        	}

        	chdir(qq($ENV{"CTESTS"}/test-result/binary));
		print "---- Start downloading xmlcore libs and tools ----\n";
        	$DOWNLOAD_XMLCORE_RESULT = system(qq(perl ../../test-entry/ftptool.pl server=$PARAM_API__ftpserver operation=download date=$PARAM_API__corebtdate plf=$PARAM_API__corebtplf comp=xmlcore debug=$PARAM_API__debug mailto=$PARAM_API__mailto testname=CPP));
		if ( $DOWNLOAD_XMLCORE_RESULT != 0 )
                {
			exit(1);
                }
        	### copy xmlcore libs and tools to $CSRC
        	if(($PARAM_API__corebtplf ne "win32") and ($PARAM_API__corebtplf ne "win64"))
        	{
               		system(qq(mkdir -p $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
               		system(qq(mkdir -p $ENV{"CSRC"}/bin));
               		system(qq(cp $ENV{"CTESTS"}/test-result/binary/Download/$PARAM_API__corebtdate/$PARAM_API__corebtplf/Package/$DEBUG_OR_RELEASE_DIR/xmlcore/lib/*.so $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
               		system(qq(cp $ENV{"CTESTS"}/test-result/binary/Download/$PARAM_API__corebtdate/$PARAM_API__corebtplf/Package/$DEBUG_OR_RELEASE_DIR/xmlcore/lib/*.a $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
               		system(qq(cp $ENV{"CTESTS"}/test-result/binary/Download/$PARAM_API__corebtdate/$PARAM_API__corebtplf/Package/$DEBUG_OR_RELEASE_DIR/xmlcore/bin/*.exe $ENV{"CSRC"}/bin));
        	}
        	else
        	{
                	system(qq(mkdir -p $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
                	system(qq(mkdir -p $ENV{"CSRC"}/bin));
                	system(qq(cp $ENV{"CTESTS"}/test-result/binary/Download/$PARAM_API__corebtdate/$PARAM_API__corebtplf/Package/$DEBUG_OR_RELEASE_DIR/xmlcore/lib/*.lib $ENV{"CSRC"}/lib/$XMLCORE_LIB_DIR_NAME));
                	system(qq(cp $ENV{"CTESTS"}/test-result/binary/Download/$PARAM_API__corebtdate/$PARAM_API__corebtplf/Package/$DEBUG_OR_RELEASE_DIR/xmlcore/bin/*.exe $ENV{"CSRC"}/bin));

        	}
                chdir(qq($ENV{"CSRC"}));
                system(qq(chmod -R 755 bin lib));

	}

}
if( $PARAM_API__turnon eq "true" )
{
	chdir(qq($ENV{"CTESTS"}/test-entry));
	print "---- Start API build ----\n";
	system(qq(perl api_build.pl cppbranch=$PARAM_API__cppbranch javabranch=$PARAM_API__javabranch plf=$PARAM_API__testplf debug=$PARAM_API__debug rebuild=$PARAM_API__rebuild recheckout=false -j=$$PARAM_API__parallel));
}

