#!/bin/bash

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 start-number count" >&2
  exit 1
fi

start=$1
count=$2

if [ "$start" -eq "$start" ]; then
  :
else
  echo $start is not a number
  exit 1
fi

if [ "$count" -eq "$count" ]; then
  :
else
  echo $count is not a number
  exit 1
fi


echo Putting $count records starting at $start into Kinesis stream 'transactions'

end=`expr $start + $count`

for (( c=$start; c<$end; c++ ))
do
    random=`jot -r 1 1 9999`
    data=$c,user_$c,$random
    echo will write $data
    aws kinesis put-record --stream-name transactions --partition-key $random --data "$data"
done
