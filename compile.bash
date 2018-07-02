#!/usr/bin/env bash


###
#checks...
###
if [ -z "$SDK_PATH" ]; then
  echo ""
  echo "ERROR: Need to set SDK_PATH"
  echo "Did you load environment variables? \`export $(grep -v '^#' .env | xargs)\`"
  echo ""
  exit 1
fi








###
#Modifications
###

#Push to sdk directory!
pushd sdk.gradle

echo "Compiling sdk.gradle..."
gradle jar

echo ""
echo ""

echo "Copying .jar file to $SDK_PATH/data/apps"
#Copy most recent jar file:
cp `ls -tr ./build/libs/*.jar | tail -n 1` $SDK_PATH/data/apps/$JAR_NAME

#Pop back to original directory
popd



echo "Done"
