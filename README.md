# Idea Composer Plugin

Adds code completion to composer.json file.

PhpStorm 8.0.2+ and 139+ version Intellij are supported, because in that version json language support is natively in
intellij sdk.

This plugin provides completion for:

* composer.json schema
* package names and version (in require, require-dev etc)
* filepath completion (in bin, autoload etc)

[There][2] you can find plugin homepage.

## This plugin in work

![Screen][1]

## Whats next?

* schema validation and error checker
* inspections
* show current installed version from `composer.lock`
* more completion enhancements
* and more ;)

[1]: https://plugins.jetbrains.com/files/7631/screenshot_14835.png
[2]: https://plugins.jetbrains.com/plugin/7631