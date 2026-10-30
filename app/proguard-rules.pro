# Keep Room-generated code and entities
-keep class com.apptech.streakly.model.** { *; }
-keep class com.apptech.streakly.db.** { *; }

# Gson needs field names preserved on backup POJOs
-keepclassmembers class com.apptech.streakly.backup.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
