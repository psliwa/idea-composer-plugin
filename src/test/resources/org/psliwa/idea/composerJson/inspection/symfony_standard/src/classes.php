<?php

namespace Sensio\Bundle\DistributionBundle\Composer {
    class ScriptHandler {
        public static function buildBootstrap(\Composer\Script\CommandEvent $event) {}
        public static function clearCache(\Composer\Script\CommandEvent $event) {}
        public static function installAssets(\Composer\Script\CommandEvent $event) {}
        public static function installRequirementsFile(\Composer\Script\CommandEvent $event) {}
        public static function removeSymfonyStandardFiles(\Composer\Script\CommandEvent $event) {}
    }
}

namespace Incenteev\ParameterHandler {
    class ScriptHandler {
        public static function buildParameters(\Composer\Script\CommandEvent $event) {}
    }
}

namespace SymfonyStandard {
    class Composer {
        public static function hookRootPackageInstall(\Composer\Script\CommandEvent $event) {}
    }
}