# SQLCipher / Room
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

# Retrofit / Gson model classes used for stock & FX responses
-keep class com.cairnlabworks.mywealth.data.remote.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Retrofit
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Exceptions
