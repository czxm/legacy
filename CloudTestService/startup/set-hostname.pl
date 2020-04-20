#!/usr/bin/perl -w
use strict;

sub randomHostname {
my $hostname;
my $_rand;

my $hostname_length = $_[0];
    if (!$hostname_length) {
        $hostname_length = 10;
    }

my @chars = split(" ",
    "a b c d e f g h i j k l m n o p q r s t u v w x y z 0 1 2 3 4 5 6 7 8 9");

srand;

for (my $i=0; $i <= $hostname_length ;$i++) {
    $_rand = int(rand 36);
    $hostname .= $chars[$_rand];
}
return $hostname;
}

my $hostname = 'L'.randomHostname(7);
system("perl -i -np -e \"s/HOSTNAME=.*/HOSTNAME=$hostname/\" /etc/sysconfig/network-scripts/ifcfg-eth0") == 0
    or die "Could not change hostname: $?";
system("perl -i -np -e \"s/DHCP_HOSTNAME=.*/DHCP_HOSTNAME=$hostname/\" /etc/sysconfig/network-scripts/ifcfg-eth0") == 0
    or die "Could not change hostname: $?";
system("perl -i -np -e \"s/HOSTNAME=.*/HOSTNAME=$hostname/\" /etc/sysconfig/network") == 0
    or die "Could not change hostname: $?";
