# Media3 and Room are mostly covered by their consumer rules.
# Keep Hilt generated entry points and Room schema metadata during minification.
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
