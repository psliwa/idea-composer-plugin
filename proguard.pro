-dontobfuscate
-dontoptimize
-dontwarn scala.**
-dontwarn com.intellij.uiDesigner.core.**

-keep class org.psliwa.idea.composerJson.completion.CompletionContributor
-keep class org.psliwa.idea.composerJson.completion.PackagesLoader
-keep class org.psliwa.idea.composerJson.inspection.SchemaInspection
-keep class org.psliwa.idea.composerJson.inspection.FilePathInspection
-keep class org.psliwa.idea.composerJson.inspection.MisconfigurationInspection
-keep class org.psliwa.idea.composerJson.inspection.PackageVersionAnnotator
-keep class org.psliwa.idea.composerJson.inspection.NotInstalledPackageInspection
-keep class org.psliwa.idea.composerJson.reference.FilePathReferenceContributor
-keep class org.psliwa.idea.composerJson.reference.UrlReferenceContributor
-keep class org.psliwa.idea.composerJson.reference.php.PhpReferenceContributor
-keep class org.psliwa.idea.composerJson.settings.ComposerJsonSettings
-keep class org.psliwa.idea.composerJson.settings.ComposerJsonSettingsConfigurable
-keepclassmembers class org.psliwa.idea.composerJson.settings.ComposerJsonSettingsConfigurable {
    public <init>(***);
}
-keep class org.psliwa.idea.composerJson.composer.InstalledPackagesWatcher
