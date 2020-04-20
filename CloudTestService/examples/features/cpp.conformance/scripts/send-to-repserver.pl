#!/usr/bin/perl
#name: send-to-repserver.pl
#transfer a specific file to the FTP server
#user needs to specify the source file and destination directory
#perl ftpscript.pl file=xxx dest=xxx
#outer script can capture the standard output from this file

use Cwd;
$FLAG="";
$tagfile="";
$transfile="";
$directory="";
$uploadraw="true";

if(!@ARGV){
    helpinfo();
    exit();
}

foreach (@ARGV){
    if(/-help/)
    {
        helpinfo();
        exit;
    }
    elsif(/^tag=(.*)/)
    {
        $tagfile=$1;
        $FLAG="tag";
    }
    elsif(/timetag=(.*)/)
    {
        $TIME_TAG_NIGHTLY=$1;
    }
    elsif(/file=(.*)/)
    {
        $transfile=$1;
        $FLAG="data";
    }
    elsif(/uploadraw=(.*)/)
    {
	$uploadraw=$1;
    }
    elsif(/dest=(.*)/)
    {
        $directory=$1;
    }
}

# if don't want to upload raw data file
if($uploadraw eq "false")
{
    print "We don't want to upload the raw data files!\n";
}else{
    # change the current working directory
    $current=getcwd();
    print "current working dirctory: $current\n";
    chdir(qq($ENV{"CTESTS"}/test-entry));
    $changed=getcwd();
    print "change to directory: $changed\n";
    
    if($FLAG eq "tag")
    {
        put_tag();
    }
    if($FLAG eq "data")
    {
        put_data();
    }
    
    # restore the working directory
    chdir($current);
    $changed=getcwd();
    print "restore the working directory $changed\n";
}

sub put_tag{
    unlink glob "*.COMPLETE";
    unlink glob "*.START";
    print "$tagfile\n";
    $tagfile =~ s/(.*)\./$1#$TIME_TAG_NIGHTLY\./;
    $transfile = $tagfile;
    $directory = "res/test_tags";
    print "put tag file: "."$transfile\n";
    open TAG, "> $transfile";
    if($transfile =~ /COMPLETE/){
        $plat=qq($ENV{"CTESTS"}/test-entry/plat.pl);
        $sysinfo=`perl $plat`;
        print TAG $sysinfo;
    }
    put_data();
}
sub put_data{
    print STDOUT "$transfile\n";
    @parts=split("/", $transfile);
    $len=@parts;
    $TNAME=$parts[$len-1];
    $FNAME="FINISHED-".$parts[$len-1];
    

    # remove the leading path if exists
    print STDOUT qq($ENV{"CTESTS"}\n);
    $leadpath=$ENV{"CTESTS"};
    $slen=length $leadpath;
    $pos=index $transfile, $leadpath;
    if($pos==-1){
        $path=$transfile;
    }elsif($pos==0){
        $path=substr $transfile, $slen;
	$path="..$path";
    }
    print STDOUT "$path\n";

    open FTPHANDLE, "> ftp-command.ftp";
    select FTPHANDLE;
    print "cd xml-report-server_dev/siteroot/"."$directory";
    print STDOUT "\ncd xml-report-server_dev/siteroot/".$directory;
    print "\n";
    print "put $path";
    print STDOUT "\nput $transfile\n";
    print "\n";
    print "chmod 777 $TNAME";
    print STDOUT "$TNAME\n";
    print "\n";
    print "mv $TNAME $FNAME\n";
    print STDOUT "renamed $TNAME to $FNAME\n";
    print "\n";
    
    system(qq(lftp repserver:repserver\@xmlqa-c2d2.sh.intel.com <ftp-command.ftp));
    unlink("ftp-command.ftp"); # on windows platform, this would not remove the newly-created tag file
    print STDOUT "$FLAG\n";
    if($FLAG eq "tag")
    {
	print STDOUT "unlink $transfile\n";
        unlink(qq($transfile)) or print STDOUT "unlink failure: $!\n";
    }
    select STDOUT;
}
sub helpinfo(){
    print "\n    this script is used to send tag and raw data file to web report server. You can invoke it as the following:\n \
    perl send-to-repserver.pl -help\t\t\t\tto get help info.\n \
    perl tag=<tag> timetag=<time>\t\t\t\tsample: perl tag=windows#conformance#validator.START timetag=20090413\n\
    perl file=<raw data file> dest=<destination directory>\tsample: perl tag=xxx.xml dest=data/performance/transform\n"; 
}
