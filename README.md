# ReportesCiudadanos

Aplicacion Android para reportes ciudadanos con enfoque en seguridad, infraestructura, mascotas, emergencias medicas y comunidad. Permite crear, visualizar, editar y moderar reportes geolocalizados, con soporte de imagenes multiples, votacion de importancia, comentarios, filtros en mapa y perfil de usuario con foto.

## Descripcion del proyecto

`ReportesCiudadanos` busca facilitar que cualquier persona reporte situaciones de su ciudad de forma rapida y organizada. La app centraliza reportes por categoria, muestra ubicaciones en mapa, y promueve la participacion comunitaria con reputacion, insignias y moderacion de contenido.

Casos de uso principales:
- Ciudadano crea un reporte con titulo, descripcion, categoria, direccion, ubicacion y fotos.
- Comunidad consulta reportes en inicio o mapa, filtra por categoria y vota importancia.
- Usuario ve detalles, comentarios y mapa exacto del reporte.
- Moderador revisa reportes y decide verificar/rechazar/resolver.

## Funcionalidades implementadas

### 1) Reportes y multimedia
- Creacion de reportes con validacion de campos obligatorios.
- Carga de imagenes desde galeria y camara.
- Soporte de multiples imagenes por reporte (maximo `5`).
- Contador visual de fotos en crear reporte (`X / 5 fotos`) y bloqueo al llegar al maximo.
- Vista tipo carrusel (HorizontalPager) en:
  - Crear reporte
  - Editar reporte
  - Detalle del reporte

### 2) Geolocalizacion y mapas
- Busqueda de direccion con sugerencias (Geocoder).
- Seleccion de ubicacion y marcador en crear reporte.
- Vista de mapa general con marcadores por categoria.
- Boton flotante independiente para filtrar por categorias en mapa (no en barra de busqueda).
- En detalle del reporte, render de mapa de ubicacion del reporte cuando hay latitud/longitud.

### 3) Perfil y cuenta
- Perfil con datos del usuario, nivel, progreso y puntos.
- Soporte para foto de perfil editable desde galeria.
- Flujo de cambio de contrasena y eliminacion de cuenta.

### 4) Interaccion social
- Votacion "Es importante" por reporte.
- Comentarios en detalle de reporte.
- Compartir reporte por intent nativo.

### 5) Moderacion
- Panel de moderacion por estados (pendiente, verificado, rechazado).
- Acciones directas: verificar, rechazar, marcar resuelto.

### 6) Internacionalizacion (i18n)
- Recursos de texto en:
  - `app/src/main/res/values/strings.xml` (espanol)
  - `app/src/main/res/values-en/strings.xml` (ingles)
- Android cambia automaticamente el idioma segun la configuracion del dispositivo.

## Arquitectura

La app sigue una separacion por capas + MVVM:

- **UI/Features (Jetpack Compose):** pantallas y componentes (`features/*`, `ui/*`).
- **ViewModel:** estado y logica de presentacion por pantalla.
- **Domain:** modelos y contratos de repositorio (`domain/model`, `domain/repository`).
- **Data:** implementaciones de repositorios y DataStore (`data/repository`, `data/datastore`).
- **DI:** inyeccion de dependencias con Hilt (`di/RepositoryModule.kt`).

Patrones usados:
- State hoisting y `StateFlow` para estado reactivo.
- Navegacion tipada con `navigation-compose` y `kotlinx-serialization`.
- Inyeccion de dependencias con `@HiltViewModel` y modulo Hilt.

## Stack tecnologico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Arquitectura:** MVVM
- **DI:** Hilt
- **Persistencia ligera:** DataStore Preferences (sesion)
- **Imagenes:** Coil 3
- **Mapas:** Google Maps Compose + Play Services Maps
- **Navegacion:** Navigation Compose
- **Build:** Gradle Kotlin DSL

Versiones relevantes (archivo `gradle/libs.versions.toml`):
- Kotlin `2.2.21`
- AGP `8.13.1`
- Compose BOM `2026.02.00`
- Navigation Compose `2.9.7`
- Hilt `2.57.2`

## Estructura del proyecto (resumen)

```text
ReportesCiudadanos/
  app/
    src/main/java/com/uniquindio/reportes/
      core/          # navegacion, sesion, tema, utilidades
      data/          # datastore y repositorios
      di/            # modulos Hilt
      domain/        # modelos y contratos
      features/      # pantallas por feature
      ui/            # componentes reutilizables
      MainActivity.kt
      ReportesCiudadanosApp.kt
    src/main/res/
      values/strings.xml
      values-en/strings.xml
```

## Repositorios y estado actual de datos

Actualmente el proyecto usa repositorios en memoria para autenticacion, reportes, comentarios y notificaciones (`InMemory*Repository`).

Implicaciones:
- Los datos de reportes/usuarios son de demostracion en runtime.
- Al reiniciar proceso se pierde estado en memoria.
- La sesion si se persiste localmente con DataStore.

## Credenciales de prueba

Definidas en `InMemoryAuthRepository`:

- Usuario:
  - correo: `demo@ciudad.com`
  - contrasena: `demo1234`
- Moderador:
  - correo: `mod@ciudad.com`
  - contrasena: `mod12345`

## Requisitos

- Android Studio reciente (con soporte para Kotlin 2.2.x / AGP 8.13.x).
- JDK 11.
- SDK Android:
  - `minSdk 24`
  - `targetSdk 36`
- API key de Google Maps.

## Configuracion y ejecucion

1. Crear/editar `local.properties` en la raiz del proyecto y agregar:

```properties
MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
```

2. Sincronizar Gradle y ejecutar la app desde Android Studio.

Opcional por terminal:

```sh
cd "/Users/sebastianagudelo/Documents/GitHub/ReportesCiudadanos"
./gradlew :app:assembleDebug
```

## Flujo funcional de alto nivel

1. Inicio de sesion / registro.
2. Navegacion principal con barra inferior: Inicio, Mapa, Reportar, Datos, Perfil.
3. Creacion de reporte con categoria, direccion, mapa y fotos.
4. Consulta de reportes por lista o mapa con filtros.
5. Revision de detalle con carrusel, importancia, comentarios y mapa de ubicacion.
6. Moderacion de reportes (si el usuario tiene rol moderador).

## Calidad y pruebas

El proyecto incluye pruebas base de ejemplo (`ExampleUnitTest`, `ExampleInstrumentedTest`).

Siguiente paso recomendado para robustez:
- pruebas unitarias de ViewModels,
- pruebas de repositorios,
- pruebas UI Compose para flujos criticos (crear reporte, filtro de mapa, detalle).

## Mejoras recomendadas

- Migrar repositorios en memoria a backend real (REST/Firebase).
- Subida de imagenes a almacenamiento remoto y manejo de cache/compresion.
- Mejorar accesibilidad (TalkBack, contraste, tamanos de fuente).
- Agregar telemetria y metricas de uso.
- Endurecer seguridad (gestion de tokens, cifrado y validaciones de servidor).

## Autores / contexto academico

Proyecto academico orientado a fortalecer competencias en desarrollo Android moderno (Compose, Hilt, MVVM, mapas, i18n y arquitectura por capas), resolviendo una necesidad real de participacion ciudadana y reporte comunitario.
