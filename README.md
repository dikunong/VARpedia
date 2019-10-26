# VARpedia - SOFTENG 206 Project
### Group 12 - Di Kun Ong (dngo711) & Tudor Zagreanu (tzag747)

*Check out the VARpedia GitHub: https://github.com/dikunong/VARpedia*

VARpedia is an encyclopedia tool that provides visual, aural and reading capabilities for **a second language learner, young adult (18-25 years).**

## Running the JAR file

#### On Windows AND On Linux (incl. Beta Linux in UoA labs)

_For marking purposes, this should be tested on the Beta Linux image on the lab computers._

_As stated above, the target audience of this app is a second language learner, young adult (18-25 years)._

**_NOTE: VARpedia is dependent on an_** `ffmpeg` **_version of at least 4.x. Some lab computers have an older version 2.x, which will not work. Before marking, please check your computer's_** `ffmpeg` **_version using the following command:_**

`ffmpeg -version`

Execute the following command:

`java -jar VARpedia.jar`

#### On SOFTENG 206 VirtualBox Image ONLY

A script file has been provided for running the JAR in the VirtualBox. It assumes that the JAR file has been moved out of /build/libs and into the root directory.

`./run_varpedia_vbox.sh`

*NOTE: If you are building your own JAR and wish for dev builds in IntelliJ and the production JAR to access the same /creations folder, move the JAR file from /build/libs to the root directory.*

## Building the JAR file

Execute the following command:

`gradlew.bat shadowJar`

This will produce a runnable JAR file in /build/libs.

Note that there's a weird issue where Gradle can't find your JDK install - if this happens, you need to
manually set it by creating a `gradle.properties` file in the root directory, with this line in it:

`org.gradle.java.home=HOME_DIRECTORY`

where `HOME_DIRECTORY` is your JDK install directory e.g. `C:\\Program Files\\Java\\jdk1.8.0_202`

## Attribution

#### src/main/resources/varpedia/music/perspective.mp3

Music "another perspective" by panu featuring airtone, onlymeith  
Available at ccMixter.org http://ccmixter.org/files/panumoon/60396  
Under CC BY NC license http://creativecommons.org/licenses/by-nc/3.0/  

#### src/main/resources/varpedia/music/chinese.mp3

Music "mandolin chinese (moscardo remix)" by moscardo  
Available at ccMixter.org http://ccmixter.org/files/moscardo/60170  
Under CC BY NC license http://creativecommons.org/licenses/by-nc/3.0/  

#### src/main/resources/varpedia/music/sirius.mp3

Music "Sirius Crystal" by Speck featuring Sascha Ende, Apoxode  
Available at ccMixter.org http://ccmixter.org/files/speck/60126  
Under CC BY license http://creativecommons.org/licenses/by/3.0/  
