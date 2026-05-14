# Proyecto: Reportes Ciudadanos

App Android en Kotlin/Java para reportes ciudadanos.  
**Package:** `com.uniquindio.reportes`  
**Ruta:** `~/Developer/ReportesCiudadanos`

## Arquitectura
MVVM (Model-View-ViewModel)

## Estructura de paquetes
```
com.uniquindio.reportes/
├── core/        # Utilidades base, constantes, extensiones
├── data/        # Repositorios, fuentes de datos, modelos de datos
├── di/          # Inyección de dependencias
├── domain/      # Casos de uso, modelos de dominio
├── features/    # Pantallas/features de la app
├── images/      # Manejo de imágenes
└── ui/          # Componentes UI compartidos
```

## Ya implementado
- ✅ Google Maps
- ✅ Login y Registro
- ✅ CRUD de reportes

## Por implementar (entrega final)
- [ ] Firebase conectado y configurado (verificar google-services.json)
- [ ] Firebase Auth (reemplazar o integrar con login/registro existente)
- [ ] Recuperación de contraseña con Firebase
- [ ] Firebase Firestore con caché offline
- [ ] Firebase Storage para carga y almacenamiento de imágenes
- [ ] Firebase Cloud Messaging (FCM) para notificaciones push
- [ ] Funcionalidad con IA (clasificación o descripción de reportes)
- [ ] Publicación en Firebase App Distribution

## Convenciones
- Respetar arquitectura MVVM existente
- Nuevas clases en el paquete correspondiente según su responsabilidad
- Comentarios y nombres de variables en español
- Un ViewModel por feature/pantalla
- Repositorios en `data/`, casos de uso en `domain/`

## Notas importantes
- Antes de implementar cualquier cosa, revisar si ya existe código relacionado
- No romper el login/registro existente al integrar Firebase Auth
- Mantener consistencia con el estilo de código actual del proyecto
