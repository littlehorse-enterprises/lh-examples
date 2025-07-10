#!/usr/bin/env bash

root_dir=`pwd`
api_dir="accountsApi"
api_docker_name="accountsapi"
wf_dir="lh-bank-transfer"
wf_task_worker_name="bank-transfer"


# verify that `lhctl` is installed
if [[ x`which lhctl` == "x" ]]; then
    echo "Missing lhctl.  Please install, https://littlehorse.dev/docs/developer-guide/install/#littlehorse-cli"
fi

# verify that the project directories exist
if [ ! -d "$root_dir/$api_dir" ] || [ ! -d "$root_dir/$wf_dir" ]; then
    echo "Could not find project directories.  Exiting...."
    exit 1
fi 

#check for prerequisites
# verify that lh-server is up and connectable
lhctl whoami 2> /dev/null
lh_ret=`echo $?`

if [[ $lh_ret -ne 0 ]]; then
    echo "Please start lh-server standalone image"
    echo "docker run --name lh-user-tasks-standalone --rm -d -p 2023:2023 -p 8080:8080 -p 8888:8888 -p 8089:8089 -p 3000:3000 ghcr.io/littlehorse-enterprises/lh-user-tasks-api/lh-user-tasks-standalone:main"
fi
# docker
 if [[ x`docker --version` == "x" ]]; then
    echo "missing docker.  Please check your \$PATH or install docker.  https://docs.docker.com/engine/install/"
    exit 1
fi
# check for java and the correct version
if ! command -v java &> /dev/null; then
    echo "Java is not installed."
    exit 1
fi
# Extract the Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')

# Parse the major version number
MAJOR_VERSION=$(echo "$JAVA_VERSION" | awk -F. '{if ($1 == "1") print $2; else print $1}')
# Check if the version >= 11
if [[ ! "$MAJOR_VERSION" -ge 11 ]]; then
    echo "Java version $JAVA_VERSION is not above 11."
    exit 1
fi
# Verify that lh is up and working

### Build the docker images
cd "$root_dir/$api_dir"
./gradlew clean 2>/dev/null
echo "Building API"
./gradlew build 2>/dev/null

if [[ `echo $?` -ne 0 ]]; then
    echo "Build of accountsApi failed.  Please see logs or manually try the build.  cd $api_dir ; ./gradlew build.  Exiting..."
    exit 1
fi
docker build -t littlehorse/demo-accountsapi .
if [[ `echo $?` -ne 0 ]]; then
    echo "Docker build of accountsApi failed.  Please see logs or manually try the build."
    exit 1
fi

# start the banking API
echo "Starting the banking API"
docker run -d -p 7070:7070 littlehorse/demo-accountsapi
sleep 5
if [[ x`docker ps |grep $api_docker_name` == "x" ]]; then
    echo "failed to start API docker.  Exiting...."
    exit 1
fi

# workflow build and run
cd "$root_dir"
cd "$root_dir/$wf_dir"

./gradlew clean 1> /dev/null 2> /dev/null
echo "building LittleHorse task workers and registering workflow"

./gradlew build 1> /dev/null 2> /dev/null
if [[ `echo $?` -ne 0 ]]; then
    echo "Build of lh-bank-transfer  failed.  Please see logs or manually try the build.  cd $wf_dir ; ./gradlew build.  Exiting..."
    exit 1
fi
### need to register workflow before we can start the task workers
./gradlew run --args register

docker build -t littlehorse/demo-bank-transfer . 1>/dev/null 2>/dev/null
if [[ `echo $?` -ne 0 ]]; then
    echo "Docker build of bank-transfer failed.  Please see logs or manually try the build."
    exit 1
fi
docker run --network host -d littlehorse/demo-bank-transfer 
sleep 5
if [[ x`docker ps |grep $wf_task_worker_name` == "x" ]]; then
    echo "failed to start Bank transfer task worker docker image.  Exiting...."
    exit 1
fi

echo "All processes are up and running"

echo "You can now try out the bank transfer workflow with:"
cat << EOF
 lhctl run initiate-transfer transferDetails '{
        "fromAccountId": "1234",
        "toAccountId": "4564",
        "amount": 500.23,
        "currency": "USD",
        "description": "1234 to 4564"
}'
EOF