# Keep Room-generated code and entities
-keep class com.streakly.app.model.** { *; }
-keep class com.streakly.app.db.** { *; }

# Gson needs field names preserved on backup POJOs
-keepclassmembers class com.streakly.app.backup.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
