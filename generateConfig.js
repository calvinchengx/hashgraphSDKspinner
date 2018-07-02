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

//check for .env file
// try{
//   envfileVarsObj=envfile.parseFileSync(__dirname+"/.env")
// }catch(e){
//   console.error(".env file not found! Try again...")
//   process.exit(0)
// }
//check environment variables set:
if(typeof process.env.MODE == 'undefined'){
  console.log("ERROR: environment variables not set!")
  console.log("Try running: `export $(grep -v '^#' ./.env | xargs)`")
  process.exit()
}
if(typeof process.env.PORT_BASE_INT == 'undefined'){
  console.log("ERROR: environment variables not set!")
  console.log("Try running: `export $(grep -v '^#' ./.env | xargs)`")
  process.exit()
}



//check for cidrs:
result=CIDR.cidr.validate(process.env.CIDR)
if(result==null){
  //OK!
}else{
  console.log("ERROR: Invalid ipv4/ipv6 CIDR...")
  process.exit(0)
}
//check for valid sdk location:
if(!fs.existsSync(process.env.SDK_PATH+"/config.txt")){
  console.log("Invalid sdk path!")
  process.exit(0)
}
if(!fs.existsSync(process.env.SDK_PATH+"/swirlds.jar")){
  console.log("Could not find swirlds.jar at that sdk path!")
  process.exit(0)
}
//check .jar file is valid:
if(!fs.existsSync(process.env.SDK_PATH+"/data/apps/"+process.env.JAR_NAME)){
  console.log("Could not find a jar file at: "+process.env.SDK_PATH+"/data/apps/"+process.env.JAR_NAME+"!")
  process.exit(0)
}









/////
//Modifications
/////
console.log("-->> Generating config for mode: "+process.env.MODE)







//Add a symlink:
cmd="ln -sf "+process.env.SDK_PATH+" .";
result=execSync(cmd)




















//delete any lingering docker-compose.yml and/or Vagrantfile
cmd="rm -rf docker-compose.yml"
result=execSync(cmd)
cmd="rm -rf Vagrantfile"
result=execSync(cmd)









//generate "tmp.json" with any AUTOGEN parameters:
// console.log(JSON.parse(JSON.stringify(CIDR.cidr.ips(process.env.CIDR))).toString())
// //TODO...
process.env.AUTOGEN_CIDRS=CIDR.cidr.ips(process.env.CIDR)   //it's an array - JSON stringify it...
process.env.AUTOGEN_NETMASK=CIDR.cidr.netmask(process.env.CIDR)
process.env.AUTOGEN_SUBNET=CIDR.cidr.min(process.env.CIDR)
//force to end in .255 (relavent only for vm/Vagrantfile case...)
let s1=CIDR.cidr.max(process.env.CIDR).split(".")
process.env.AUTOGEN_BROADCAST=s1[0]+"."+s1[1]+"."+s1[2]+".255"
//Next do some hard-coded logic:
if(process.env.MODE=="container"){
  //-->>container
  process.env.GUI_ONOFF="off"
  process.env.N_NODES=1
}else if(process.env.MODE=="vm"){
  //-->>vm
  process.env.GUI_ONOFF="off"
  process.env.N_NODES=1
}else{
  //-->>local
  //join "127.0.0.1" x times and remove trailing comma...
  process.env.AUTOGEN_CIDRS=Array(Number(process.env.N_NODES)).fill("127.0.0.1")//.replace(/,\s*$/, "")
  //int and ext ports the same...
  process.env.PORT_BASE_EXT=process.env.PORT_BASE_INT
}
//and write back to file:
fs.writeFileSync(__dirname+"/tmp.json",JSON.stringify(process.env))
//fs.writeFileSync(__dirname+"/.env",envfile.stringifySync(envfileVarsObjNew))









//Now generate container/vm files:
if(process.env.MODE=="container"){
  //-->>container
  //Now create a docker-compose.yml
  cmd="./node_modules/.bin/ejs-cli ./ejs.docker-compose.yml --options tmp.json > docker-compose.yml"
  result=execSync(cmd)
  console.log("- Docker \"docker-compose.yml\" generated successfully")
}else if(process.env.MODE=="vm"){
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
if(!fs.existsSync(process.env.SDK_PATH+"/config.orig.txt")){
  cmd="cp "+process.env.SDK_PATH+"/config.txt "+process.env.SDK_PATH+"/config.orig.txt"
  result=execSync(cmd)
}else{
  cmd="cp "+process.env.SDK_PATH+"/config.orig.txt "+process.env.SDK_PATH+"/config.txt"
  result=execSync(cmd)
}
//and generate:
cmd="./node_modules/.bin/ejs-cli ./ejs.config.txt --options tmp.json > "+process.env.SDK_PATH+"/config.txt"
result=execSync(cmd)
console.log("- swirlds sdk \"config.txt\" ("+process.env.SDK_PATH+"/config.txt"+") generated successfully")















//Finally, delete tmp.json
cmd="rm -rf tmp.json"
//result=execSync(cmd)












console.log("Done.")
