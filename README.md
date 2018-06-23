# hashgraphSDKgradle
Gradle version of hashgraph SDK

Stripped down version of the Swirlds SDK. Featuring:

- Gradle build (instead of Maven)
- simplified project structure - focus on state and application
- copy and paste generated .jar into official Swirls SDK
- experiments with Google protobuf for state storage

Focus on designing your Main class and State class. Built with Gradle/IntelliJ IDE in mind.

Simply provide a reference to the official SDK.

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



**Step 1: Build your hashgraph program**

`cd sdk.gradle`

`gradle jar`

**Step 2: Generate config.txt**

`./generate_config.txt.bash ../sdk SharedWorld.jar`   

, where "../sdk" is the official sdk path and "SharedWorld.jar" is the name of the generated .jar file

**Step 3: Run your program**

`cd sdk`

`java -jar swirlds.jar`



### Interacting with the hashgraph network

When you run the program, a number of windows appear

- write to the distributed ledger by entering strings in any one of the windows
- or, write to the ledger by telnet'ing into any one of the windows e.g. `telnet 127.0.0.1 51206`
