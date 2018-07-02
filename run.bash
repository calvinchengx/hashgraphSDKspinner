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


if [ "$MODE" = "container" ]; then
  echo ""
  echo "ERROR: You are in \"$MODE\" mode"
  echo "Run using \`docker-compose\`"
  echo "e.g. \`docker-compose up\`"
  echo ""
  exit 1
fi
if [ "$MODE" = "vm" ]; then
  echo ""
  echo "ERROR: You are in \"$MODE\" mode"
  echo "Run using \`vagrant\`"
  echo "e.g. \`vagrant up\`"
  echo ""
  exit 1
fi


if [ -z "$SDK_PATH" ]; then
  echo ""
  echo "ERROR: Need to set SDK_PATH"
  echo "Did you load environment variables? \`export $(grep -v '^#' .env | xargs)\`"
  echo ""
  exit 1
fi
#check the jar file is in place!
if [ ! -f "$SDK_PATH/data/apps/$JAR_NAME" ]; then
  echo ""
  echo "ERROR: $SDK_PATH/data/apps/$JAR_NAME doesn't exist!"
  echo "-> Did you generate the config.txt (\`./generateConfig.js\`)?"
  echo "-> And did you compile the Java program (\`./compile.bash\`)?"
  echo ""
  exit 1
fi



















###
#Modifications
###

#Push to sdk directory!
pushd sdk

echo "Running swirlds..."
java -jar swirlds.jar

popd


echo "Done."
