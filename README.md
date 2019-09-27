# se206-a3
Assignment 3 for SOFTENG 206 (VARpedia)

## Building the JAR file

Execute the following command:

`gradlew.bat jar`

This will produce a runnable JAR file in /build/libs.

Note that there's a weird issue where Gradle can't find your JDK install - if this happens, you need to
manually set it by creating a `gradle.properties` file, with this line in it:

`org.gradle.java.home=C:\\Program Files\\Java\\jdk1.8.0_202`

Substitute your JDK install directory in for the last part.

## Running the JAR file

Move the JAR file from /build/libs to the root directory, then execute the following command:

`java -jar se206-a3-1.0-SNAPSHOT.jar`