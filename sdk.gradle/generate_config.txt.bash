#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Must specify path to swirlds sdk and name of your .jar!"
    echo "e.g."
    echo "\`./generate_config.txt.bash /path/to/sdk SharedWorld.jar\`"
    exit
fi

SDK="$1"
FN="$2"


ln -sf $SDK ../sdk

RESULT=`gradle jar`
if [[ ${RESULT} != *"BUILD SUCCESSFUL"* ]];then
  echo "gradle jar failed!"
  exit
fi









#Get the most up-to-date .jar:
###JARFILENAME=`ls -tr ./build/libs/*.jar | tail -n 1`
#Just filename...
###JARFILENAME=$(basename "$JARFILENAME")
#Copy to swirlds sdk directory:
###cp ./build/libs/$JARFILENAME $SDK/data/apps/
#Copy most recent jar file:
cp `ls -tr ./build/libs/*.jar | tail -n 1` $SDK/data/apps/$FN

echo "Built successfully."
echo ""







#Push to sdk directory!
pushd $SDK

#First comment out ALL apps from config.txt
sed -i '' '/^ *app./s/^/#/g' config.txt

#Add our app to official sdk:
#First get linenumnber of last occurance of "app,"
LINENUMBER=`cat config.txt | grep -n 'app,' | tail -n1 | cut -d: -f1`
#Add new line at LINENUMBER:
awk "NR==$LINENUMBER{print \"app,          $FN\"}1" config.txt > tmp && mv tmp config.txt
#sed $LINENUMBER"a\
#app,          $JARFILENAME" < config.txt

#Optional...
java -jar swirlds.jar

#Pop back to original directory
popd

# echo ""
# echo "Success."
# echo "sdk configured."
# echo "Changes made to: \"config.txt\" and \"$FN\""
# echo ""
# echo "***Now run with:***"
# echo "cd ../sdk"
# echo "----->>>     java -jar swirlds.jar"
# echo ""
