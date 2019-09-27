# se206-a3
Assignment 3 for SOFTENG 206 (VARpedia)

## Building the JAR file

Execute the following command:

`gradlew.bat jar`

This will produce a runnable JAR file in /build/libs.

Note that there's a weird issue where Gradle can't find your JDK install - if this happens, you need to
manually set it by changing the java.home line in the gradle.properties file so that it points to your
JDK install directory.

## Running the JAR file

Move the JAR file from /build/libs to the root directory, then execute the following command:

`java -jar se206-a3-1.0-SNAPSHOT.jar`