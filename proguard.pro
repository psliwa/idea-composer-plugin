-dontobfuscate
-dontoptimize
-dontwarn scala.**
-dontwarn com.intellij.uiDesigner.core.**

-keep class org.psliwa.idea.composerJson.intellij.codeAssist.schema.CompletionContributor
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.CompletionContributor
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.PackagesLoader
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer.PackageVersionInspection
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer.VersionOverlay
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.schema.SchemaInspection
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.file.FilePathInspection
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.MisconfigurationInspection
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.PackageVersionAnnotator
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.composer.NotInstalledPackageInspection
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.file.FilePathReferenceContributor
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.file.UrlReferenceContributor
-keep class org.psliwa.idea.composerJson.intellij.codeAssist.php.PhpReferenceContributor
-keep class org.psliwa.idea.composerJson.settings.ComposerJsonSettings
-keep class org.psliwa.idea.composerJson.settings.ComposerJsonSettingsConfigurable
-keepclassmembers class org.psliwa.idea.composerJson.settings.ComposerJsonSettingsConfigurable {
    public <init>(***);
}
-keep class org.psliwa.idea.composerJson.composer.InstalledPackagesWatcher
