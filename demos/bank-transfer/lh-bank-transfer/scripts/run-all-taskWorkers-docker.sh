#!/bin/sh

echo "Starting fetch-account worker"
java -jar /lh/app.jar fetch-account &

echo "Starting initiate-transfer worker"
java -jar /lh/app.jar initiate-transfer &

echo "Starting check-transfer worker"
java -jar /lh/app.jar check-transfer &

wait -n

exit $?

