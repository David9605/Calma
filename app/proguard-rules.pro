# Reglas para la versión de lanzamiento (R8)

# kotlinx.serialization: conservar los serializadores de nuestros modelos
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.calmaceleste.app.data.**$$serializer { *; }
-keepclassmembers class com.calmaceleste.app.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.calmaceleste.app.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Receptores declarados en el manifiesto
-keep class com.calmaceleste.app.system.ReminderReceiver
-keep class com.calmaceleste.app.system.BootReceiver
