# PHP composer.json support

Adds code completion and inspections to composer.json file.

PhpStorm 8.0.2+ and 139+ version Intellij are supported, because in that version json language support is natively in
intellij sdk.

This plugin provides:

* completion for:
    * composer.json schema
    * package names and version (in require, require-dev etc)
    * filepath completion (in bin, autoload etc)

* inspections for:
    * composer.json schema + quick fixes (remove entry / property, create property)
    * filepath existence (in bin, autoload etc) + quick fixes (remove entry, create file / directory)
    * misconfiguration + quick fixes

[There][2] you can find plugin homepage.

## This plugin in work

![Screen][1]

## Whats next?

* improve completion (support for custom repositories, namespace completion in autoload, class completion for scripts)
* improve inspections (detect doubled properties, detect misconfiguration, detect packages that are not installed yet + install as quick fix)
* show current installed version from `composer.lock`
* and more ;)

[1]: https://plugins.jetbrains.com/files/7631/screenshot_14847.png
[2]: https://plugins.jetbrains.com/plugin/7631