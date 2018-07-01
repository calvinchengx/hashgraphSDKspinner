#!/usr/bin/env node

const fs = require('fs')
const execSync = require('child_process').execSync
const CIDR = require("node-cidr")
//const envfile = require('envfile')
//onst bases = require('bases')

let cmd=null
let result=null




/////
//checks
/////
let cidr=""
let envfileVarsObj=null

//check for .env file
try{
  envfileVarsObj=envfile.parseFileSync(__dirname+"/.env")
}catch(e){
  console.error(".env file not found! Try again...")
  process.exit(0)
}
//check for cidrs:
result=CIDR.cidr.validate(envfileVarsObj.CIDR)
if(result==null){
  //OK!
}else{
  console.log("ERROR: Invalid ipv4/ipv6 CIDR...")
  process.exit(0)
}
//check for valid sdk location:
if(!fs.existsSync(envfileVarsObj.SDK_PATH+"/config.txt")){
  console.log("Invalid sdk path!")
  process.exit(0)
}
if(!fs.existsSync(envfileVarsObj.SDK_PATH+"/swirlds.jar")){
  console.log("Could not find swirlds.jar at that sdk path!")
  process.exit(0)
}
//check .jar file is valid:
if(!fs.existsSync(envfileVarsObj.SDK_PATH+"/data/apps/"+envfileVarsObj.JAR_NAME)){
  console.log("Could not find a jar file at: "+envfileVarsObj.SDK_PATH+"/data/apps/"+envfileVarsObj.JAR_NAME+"!")
  process.exit(0)
}










//Add a symlink:
cmd="ln -sf "+envfileVarsObj.SDK_PATH+".";
result=execSync(cmd)




















//delete any lingering docker-compose.yml and/or Vagrantfile
cmd="rm -rf docker-compose.yml"
result=execSync(cmd)
cmd="rm -rf Vagrantfile"
result=execSync(cmd)









//generate "tmp.json" with any AUTOGEN parameters:
envfileVarsObj.AUTOGEN_CIDRS=CIDR.cidr.ips(envfileVarsObj.CIDR)
envfileVarsObj.AUTOGEN_NETMASK=CIDR.cidr.netmask(envfileVarsObj.CIDR)
envfileVarsObj.AUTOGEN_SUBNET=CIDR.cidr.min(envfileVarsObj.CIDR)
//force to end in .255 (relavent only for vm/Vagrantfile case...)
let s1=CIDR.cidr.max(envfileVarsObj.CIDR).split(".")
envfileVarsObj.AUTOGEN_BROADCAST=s1[0]+"."+s1[1]+"."+s1[2]+".255"
//Next do some hard-coded logic:
if(envfileVarsObj.MODE=="container"){
  //-->>container
  envfileVarsObj.GUI_ONOFF="off"
  envfileVarsObj.N_NODES=1
}else if(envfileVarsObj.MODE=="vm"){
  //-->>vm
  envfileVarsObj.GUI_ONOFF="off"
  envfileVarsObj.N_NODES=1
}else{
  //-->>local
  //join "127.0.0.1" x times and remove trailing comma...
  envfileVarsObj.AUTOGEN_CIDRS=Array(Number(envfileVarsObj.N_NODES)).fill("127.0.0.1")//.replace(/,\s*$/, "")
  //int and ext ports the same...
  envfileVarsObj.PORT_BASE_EXT=envfileVarsObj.PORT_BASE_INT
}
let envfileVarsObjNew=envfile.parseSync(JSON.stringify(envfileVarsObj))
//and write back to file:
fs.writeFileSync(__dirname+"/tmp.json",JSON.stringify(envfileVarsObjNew))
//fs.writeFileSync(__dirname+"/.env",envfile.stringifySync(envfileVarsObjNew))









//Now generate container/vm files:
if(envfileVarsObj.MODE=="container"){
  //-->>container
  //Now create a docker-compose.yml
  cmd="./node_modules/.bin/ejs-cli ./ejs.docker-compose.yml --options tmp.json > docker-compose.yml"
  result=execSync(cmd)
  console.log("- Docker \"docker-compose.yml\" generated successfully")
}else if(envfileVarsObj.MODE=="vm"){
  //-->>vm
  //Now create a Vagrantfile
  cmd="./node_modules/.bin/ejs-cli ./ejs.Vagrantfile --options tmp.json > Vagrantfile"
  result=execSync(cmd)
  console.log("- Docker \"Vagrantfile\" generated successfully")
}else{
  //-->>local
  //assume "local"...

}











//Now generate the config.txt file:
//backup config.txt
if(!fs.existsSync(envfileVarsObj.SDK_PATH+"/config.orig.txt")){
  cmd="cp "+envfileVarsObj.SDK_PATH+"/config.txt "+envfileVarsObj.SDK_PATH+"/config.orig.txt"
  result=execSync(cmd)
}else{
  cmd="cp "+envfileVarsObj.SDK_PATH+"/config.orig.txt "+envfileVarsObj.SDK_PATH+"/config.txt"
  result=execSync(cmd)
}
//and generate:
cmd="./node_modules/.bin/ejs-cli ./ejs.config.txt --options tmp.json > "+envfileVarsObj.SDK_PATH+"/config.txt"
result=execSync(cmd)
console.log("- swirlds sdk \"config.txt\" ("+envfileVarsObj.SDK_PATH+"/config.txt"+") generated successfully")















//Finally, delete tmp.json
cmd="rm -rf tmp.json"
//result=execSync(cmd)












console.log("Done.")
