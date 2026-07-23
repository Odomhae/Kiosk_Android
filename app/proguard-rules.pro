# Stack traces
#  크래시 스택트레이스에서 파일명·라인 확인 가능
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Food - Gson fromJson 역직렬화 대상 (MenuJsonParser)
-keep class com.odom.orderkiosk.model.Food { *; }
-keep class com.odom.orderkiosk.model.Food$Type { *; }