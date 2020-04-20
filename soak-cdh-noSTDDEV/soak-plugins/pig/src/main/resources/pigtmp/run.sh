hadoop fs -mkdir $1
hadoop fs -copyFromLocal excite.log.bz2 $1/
hadoop fs -copyFromLocal excite-small.log $1/

pig -param output=$1 script1-hadoop.pig
pig -param output=$1 script2-hadoop.pig

hadoop fs -rmr $1