#!/usr/bin/perl

my $third_start = 200;
my $fouth_start = 1;
my $third_end = 203;
my $fouth_end = 254;
my $nscmd = "/tmp/$$";
for($third=$third_start;$third<=$third_end;$third++){
  for($fouth=$fouth_start;$fouth<=$fouth_end;$fouth++){
    open(FH,">$nscmd") || die $!;
    printf FH ("update add ec2-192-168-%d-%d.cloudtest.intel.com 86400 A 192.168.%d.%d\n", $third, $fouth, $third, $fouth);
    print FH ("send\n");
    close FH;
    system("nsupdate $nscmd");

    open(FH,">$nscmd") || die $!;
    printf FH ("update add %d.%d.168.192.in-addr.arpa 86400 in ptr ec2-192-168-%d-%d.cloudtest.intel.com\n", $fouth, $third, $third, $fouth);
    print FH ("send\n");
    close FH;
    system("nsupdate $nscmd");
  }
}
unlink($nscmd);
