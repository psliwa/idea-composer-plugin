# Contributing

To be able setup environment for this plugin, you need:

- Idea Intellij 14+ (community edition should be enough) with [scala plugin][1]
- Follow the [instructions][2] to setup general plugin development sdk (IntelliJ 141.+ SDK is required)
- checkout plugin repository
- create new project choosing composer-json-plugin directory, select "Intellij Platform Plugin" project type and choose
"Scala" as additional library (select 2.10.2 version or eventually any 2.10.x).
- Intellij asks you to overwrite composer-json-plugin.iml file - click yes, but when project will open, you should revert
that file, using for instance `git checkout composer-json-plugin.iml` command
- add following libraries in "File -> Project Structure -> Project Settings -> Libraries":

    - click "+" and add "Scala SDK" - required 2.10.x version, 2.10.2 is preferred
    - &lt;PhpStorm-dir&gt;/plugins/php/lib/php.jar
    - &lt;PhpStorm-dir&gt;/plugins/php/lib/php-openapi.jar

- *(optional - required to run tests)* add following libraries in "File -> Project Structure -> Project Settings -> Libraries"

    - &lt;PhpStorm-dir&gt;/plugins/php/lib/resources_en.jar
    - &lt;PhpStorm-dir&gt;/plugins/CSS/lib/css.jar
    - &lt;PhpStorm-dir&gt;/plugins/CSS/lib/css-openapi.jar
    - &lt;Intellij-dir&gt;/plugins/java-i18n/lib/java-i18n.jar
    - &lt;Intellij-dir&gt;/plugins/properties/lib/properties.jar


[1]: https://plugins.jetbrains.com/plugin/?id=1347
[2]: https://confluence.jetbrains.com/display/IntelliJIDEA/Prerequisites