# hashgraphSDKspinner

Spin up a hashgraph private network in one of three modes:

- local
- container (Docker)
- virtual machine (Vagrant/Virtualbox)

Also comes with a stripped down version of the Swirlds SDK. Featuring:

- Gradle build (instead of Maven). Built with Gradle/IntelliJ IDE in mind
- Simplified project structure - focus on state and application
- Google protobuf for state storage and messaging
- `telnet` interface to nodes

Simply provide a reference to the official SDK (Download from: https://www.swirlds.com/download).

**Using Swirlds sdk version: 18.05.23**

### Prerequisites

- Java 8 (Oracle)
- Gradle
- *Optional: IntelliJ*
- Google protobuf

Java 8 (Oracle) and Gradle can be conveniently installed using [sdkman](http://sdkman.io/).

`sdk list java`

`sdk install java 8.0.171-oracle`

`sdk list gradle`

`sdk install gradle 4.7`

Google protobuf can be installed on OSX with:

`brew install protobuf`





### Getting started

**Step 0: clone repo and link sdk**

Clone repo:

`git clone https://github.com/hynese/hashgraphSDKgradle.git`

Link hashgraph SDK:

`ln -s /path/to/hashgraph/sdk ./sdk`

, where "/path/to/hashgraph/sdk" is the path to the official Hashgraph sdk




**Step 1: Generate configuration**

Edit .env and set environment variables as appropriate

Load all environment variables:

`export $(grep -v '^#' .env | xargs)`

And generate config:

`./generateConfig.js`

There are three modes of operation:

**i) local**
All nodes run on 127.0.0.1 with a unique port number. Number of nodes to start up is specified by `N_NODES` environment variable. Port numbers are specified via `PORT_BASE_EXT` and `TELNET_PORT_OFFSET`. Telnet connect to each node via 127.0.0.1 and the telnet port. e.g. `telnet 127.0.0.1 4001`.

**ii) container**
In this mode all nodes run inside their own Docker container. IP address of each container is specified by the `CIDR` environment variable. Ports are specified in environment variable. `./generateConfig.js` will generate a `docker-compose.yml` file.

**iii) vm**
In this mode all nodes run inside their own virtual machine. IP address of each vm is specified by the `CIDR` environment variable. Ports are specified in environment variable. `./generateConfig.js` will generate a `Vagrantfile` configuration file.



**Step 2: compile hashgraph sdk (Gradle version of sdk)**

Generate your .jar file with:

`./compile`

Recommend using `IntelliJ` to edit the `SharedWorldMain.java` and `SharedWorldState.java`

protobuf definition in "./sdk.gradle/src/main/proto/Hashgraph.proto"

**Step 3: start your private hashgraph network**

`./run.bash`

To start a Docker network:

`docker-compose up`

To start a network in Virtualbox (*~ experimental at present ~*):

`vagrant up`




### Interacting with the hashgraph network

When you run the program in `local` mode, a number of windows appear:

- write to the distributed ledger by entering strings in any one of the windows (stdin)

In all modes (`local`, `container` and `vm`) you can connect via telnet:

- or, write to the ledger by telnet'ing into any one of the windows e.g. `telnet 127.0.0.1 51206`
