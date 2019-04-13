-dontobfuscate
-dontoptimize
-dontwarn scala.**
-dontwarn com.intellij.uiDesigner.core.**
-dontwarn org.jetbrains.**
-dontwarn com.intellij.memory.**
-dontwarn javax.xml.bind.ModuleUtil

-keep class org.psliwa.idea.composerJson.**
-keepclassmembers class org.psliwa.idea.composerJson.** {
    public <init>(...);
}
