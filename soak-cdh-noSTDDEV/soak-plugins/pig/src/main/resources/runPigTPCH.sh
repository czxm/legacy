cd $2/

sh run_tpch.sh $3/tpch $3/pig/$1/result 1
hadoop fs -rmr $3/pig/$1

