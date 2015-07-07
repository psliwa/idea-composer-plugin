# PHP composer.json support
[![Version](http://phpstorm.espend.de/badge/7631/version)](https://plugins.jetbrains.com/plugin/7631)
[![Downloads](http://phpstorm.espend.de/badge/7631/downloads)](https://plugins.jetbrains.com/plugin/7631)
[![Downloads last month](http://phpstorm.espend.de/badge/7631/last-month)](https://plugins.jetbrains.com/plugin/7631)


Adds code completion, inspections and more to composer.json file.

PhpStorm 8.0.2+ and 139+ version Intellij are supported, because in that version json language support is natively in
intellij sdk.

This plugin provides:

* completion for:
    * composer.json schema
    * package names and version (in require, require-dev etc) from packagist repository and custom repositories defined in composer.json file (only "composer" repository type is supported right now)
    * filepath completion (in bin, autoload etc)
    * class and static method names in "scripts" properties
    * namespaces eg. in "autoload.psr-0" property

* inspections for:
    * composer.json schema + quick fixes (remove entry / property, create property etc.)
    * filepath existence (in bin, autoload etc) + quick fixes (remove entry, create file / directory)
    * misconfiguration + quick fixes
    * version constraints misconfiguration + quick fixes
    * not installed packages + install quick fix
    * scripts callbacks (class names and method signature)

* navigation for (eg. by Ctrl+LMB):
    * class and method names in "scripts" properties
    * files and directories in properties that store file path (eg. "bin")
    * package directory (eg. in "require", "require-dev")
    * urls and emails (eg. in "homepage")

* documentation:
    * external documentation (`shift+f1`) for packages
    * quick docs (`ctrl+q`) and external docs (`shift+f1`) for properties

* others:
    * show current installed version from `composer.lock`

[There][2] you can find plugin homepage.

## This plugin in work

![Screen][1]

## Whats next?

* improve support for custom repositories - support for inline repositories
* improve inspections (detect misconfiguration, detect not existing namespaces in "autoload.psr-0")
* and more ;)

[1]: https://plugins.jetbrains.com/files/7631/screenshot_14847.png
[2]: https://plugins.jetbrains.com/plugin/7631
