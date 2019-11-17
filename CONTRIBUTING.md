# Contributing

This plugin uses SBT as build tool. At the beginning, Intellij Ultimate
Edition SDK will be downloaded (into `idea` directory), so be patient.
Intellij Ultimate Edition license is nice to have.

Following custom SBT commands are defined:

* `sbt runIDE` - run testing Intellij with the plugin installed - on
  first startup you have to install php plugin manually and restart
  testing Intellij
* `sbt release` - compile plugin from scratch, prepare package, create
  zip and shrink it
* `sbt createRunConfiguration` - creates run configuration in IntelliJ.
  Thanks to that you can run IntelliJ with the plugin installed using
  `ideaComposerPlugin` run configuration.
