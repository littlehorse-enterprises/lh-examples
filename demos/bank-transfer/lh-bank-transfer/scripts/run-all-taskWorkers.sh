#!/bin/env bash

java -jar /lh/app.jar fetch-account &
java -jar /lh/app.jar initiate-transfer &
java -jar /lh/app.jar check-transfer &

