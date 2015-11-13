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

If you want to run tests using Intellij, you should set following VM options in 
"Run > Edit Configurations > Edit Defaults > JUnit > VM Options":

    -ea
    -Xms128m
    -Xmx4096m
    -XX:MaxPermSize=350m
    -Didea.system.path=$MODULE_DIR$/../../idea/15.0/test-system
    -Didea.config.path=$MODULE_DIR$/../../idea/15.0/test-config
    -Dplugin.path=$MODULE_DIR$/../../target/plugin
    -Didea.home.path=$MODULE_DIR$/../../idea/15.0
