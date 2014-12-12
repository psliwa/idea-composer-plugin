# Idea Composer Plugin

Adds code completion to composer.json file.

PhpStorm 8.0.2+ and 139+ version Intellij are supported, because in that version json language support is natively in
intellij sdk.

Code completion is based on composer.json schema, but there is also package names and version completion in require,
require-dev etc properties.

## This plugin in work

![Screen][1]

## Whats next?

* schema validation and error checker
* inspections
* show current installed version from `composer.lock`
* completion enhancements

[1]: doc/screen.gif