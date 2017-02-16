# Contributing

This plugin uses SBT as build tool. At the beginning, Intellij Ultimate Edition SDK will be downloaded 
(into `idea` directory), so be patient. Intellij Ultimate Edition license is nice to have.

Following custom SBT commands are defined:

* `sbt pluginRun` - run testing Intellij with plugin installed - on first startup you have to install php plugin 
manually and restart testing Intellij
* `sbt pluginPack` - prepare plugin for packaging, move all required jars to one directory
* `sbt pluginCompress` - make zip from `pluginPack` result - output `zip` is a working plugin
* `sbt pluginProguard` - shrink plugin zip using proguard - result should be ~5 times smaller than raw zip
* `sbt release` - compile plugin from scratch, prepare package, create zip and shrink it

