# se206-a3
Assignment 3 for SOFTENG 206 (VARpedia)

## Building the JAR file

Execute the following command:

`gradlew.bat shadowJar`

This will produce a runnable JAR file in /build/libs.

Note that there's a weird issue where Gradle can't find your JDK install - if this happens, you need to
manually set it by creating a `gradle.properties` file in the root directory, with this line in it:

`org.gradle.java.home=C:\\Program Files\\Java\\jdk1.8.0_202`

where `HOME_DIRECTORY` is your JDK install directory e.g. `C:\\Program Files\\Java\\jdk1.8.0_202`

## Running the JAR file

Move the JAR file from /build/libs to the root directory, then execute the following command:

#### On Windows

`java -jar se206-a3-1.0-SNAPSHOT-all.jar`

#### On Linux (SOFTENG 206 VirtualBox Image ONLY)

A script file has been provided for running the JAR. It assumes that the JAR file has been moved out of /build/libs and into the root
directory (this is so dev builds in IntelliJ and the production JAR access the same /creations folder).

`./run_varpedia.sh`

Alternatively, you may navigate to the location of the JAR and manually run the following command in Bash:

`/usr/lib/jvm/jdk-13/bin/java --module-path /home/student/Downloads/openjfx-13-rc+2_linux-x64_bin-sdk/javafx-sdk-13/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media -jar se206-a3-1.0-SNAPSHOT-all.jar`