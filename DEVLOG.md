# Introduction

This file is intended to act as the log of our contributions towards the VARpedia project. It briefly summarises the work done by each person, as well as some of the key decisions made as a group during meetings.

The progress has been divided up into three "sprints" leading up to each submission date.

# Project Start --> Assignment 3 Submission

*Time Period: 10 Sep 2019 - 30 Sep 2019*

## 2019-09-10 Meeting
#### Discussions
* Initial meeting - introductions
* Discussed plan for project
* Agreed that work would be minimal between now and next meeting due to university assessments

#### Work - Di Kun
* Initial project setup (Gradle etc)

## 2019-09-20 Meeting
#### Discussions
* Drafted up first wireframes of GUI design
* Agreed on general approach to task split
    * Di Kun to handle all GUI design & FXML
    * Tudor to handle more backend code

#### Work - Di Kun
* Design GUI & write FXML of MainScreen, TextEditorScreen, ChunkAssemblerScreen, PlaybackScreen
* Set up controller classes for all GUI screens

#### Work - Tudor
* Implement media player

## 2019-09-23 Meeting
#### Discussions
* Drafted up initial Java class structure
* Agreed on first rough task split for Friday, when we will do all of the work

#### Work - Di Kun
* Refinements to GUI

#### Work - Tudor
* Refinements to media player

## 2019-09-27 Meeting
#### Discussions
* Time to do all of the work!

#### Work - Di Kun
* Create model classes
* Implement playback/deletion of creations
* Implement wikit search
* Figure out easy-to-run jar file compilation

#### Work - Tudor
* Implement FFmpeg video creation
* Implement Festival voice synthesis
* Implement Flickr photo downloading

## 2019-09-29 Meeting

#### Work - Di Kun
* Implement assembly of audio chunks
* Lots of bug fixes

#### Work - Tudor
* Implement FFmpeg combination of audio chunks
* Lots of bug fixes

# Assignment 3 Submission --> Beta Submission

*Time Period: 8 Oct 2019 - 14 Oct 2019*

## 2019-10-08 Meeting
#### Discussions
* Selected target audience of 18-25 year old second language learners
* Decided to implement creation sorting instead of a quiz component for the active learning
* Set goals for Beta submission - GUI theming, photo selection & reordering, active learning component

#### Work - Di Kun
* Design GUI & write FXML for PhotoPickerScreen, RatingDialog
* Refactor Alert system

#### Work - Tudor
* Implement optional addition of background music
* Move creation logic over to PhotoPicker as needed

## 2019-10-11 Meeting

#### Work - Di Kun
* Theming/redesign of GUI via CSS
* Refactor to use Audio object

#### Work - Tudor
* Replace MainScreen ListView with TableView
* Serialisation of creation metadata
* Sorting of creations for active learning component

## 2019-10-13 Meeting

#### Work - Di Kun
* Binding for many GUI components
* Serialisation of audio chunks
* Lots of bug fixes

#### Work - Tudor
* Fix FFmpeg bugs
* Lots of other bug fixes too

# Beta Submission --> Final Submission

*Time Period: 16 Oct 2019 - 27 Oct 2019*

## 2019-10-24 Meeting
#### Discussions
* Reviewed peer feedback - mostly minor GUI fixes/theming improvements and a couple of bugs
    * Agreed to respond by implementing a dark mode (Di Kun) and fixing the bugs (Tudor)
* Set additional final submission goals - thumbnails, final audio preview, background music volume control

#### Work - Di Kun
* More CSS theming work + consistency fixes
* More bindings for GUI components

#### Work - Tudor
* Fix Festival and serialisation bugs from peer review
* Implement creation thumbnails
* Add serialisation to retain some user data between screens

## 2019-10-25 Meeting

#### Work - Di Kun
* Add dark mode
* Lots of final fixes and polish

#### Work - Tudor
* Implement final audio previewing
* Add background music volume control in FFmpeg
* Even more bindings for GUI components
* Lots of final fixes and polish