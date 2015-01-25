# PHP composer.json support

Adds code completion, inspections and more to composer.json file.

PhpStorm 8.0.2+ and 139+ version Intellij are supported, because in that version json language support is natively in
intellij sdk.

This plugin provides:

* completion for:
    * composer.json schema
    * package names and version (in require, require-dev etc)
    * filepath completion (in bin, autoload etc)
    * class and static method names in "scripts" properties
    * namespaces eg. in "autoload.psr-0" property

* inspections for:
    * composer.json schema + quick fixes (remove entry / property, create property etc.)
    * filepath existence (in bin, autoload etc) + quick fixes (remove entry, create file / directory)
    * misconfiguration + quick fixes
    * version constraints misconfiguration + quick fixes
    * not installed packages + install quick fix

* go to declaration for (eg. by Ctrl+LMB):
    * class and method names in "scripts" properties
    * files and directories in properties that store file path (eg. "bin")
    * package directory (eg. in "require", "require-dev")
    * urls and emails (eg. in "homepage")

[There][2] you can find plugin homepage.

## This plugin in work

![Screen][1]

## Whats next?

* improve completion (support for custom repositories)
* improve inspections (detect doubled properties, detect misconfiguration, detect not existing namespaces in "autoload.psr-0", detect invalid callback in "scripts")
* show current installed version from `composer.lock`
* and more ;)

[1]: https://plugins.jetbrains.com/files/7631/screenshot_14847.png
[2]: https://plugins.jetbrains.com/plugin/7631