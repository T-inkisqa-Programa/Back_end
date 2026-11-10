# T'inkisqa — Backend (microservicios)

Backend del proyecto de voluntariado **T'inkisqa**, construido como un conjunto de
microservicios **Spring Boot** con **DDD + Arquitectura Limpia**, base de datos
independiente por servicio y un **API Gateway** como único punto de entrada del
frontend (Expo/React Native).

> Fase actual: **Fase 0 (esqueleto) + Fase 1 (IAM Service implementado)**.
> Los demás servicios existen como esqueleto ejecutable (health + ping) y se
> completan en fases posteriores.

---

## 1. Arquitectura

```
                         ┌──────────────────────────────┐
   Frontend (Expo)  ───► │        API Gateway :8080     │  único puerto público
                         │  Spring Cloud Gateway + JWT   │  CORS + seguridad
                         └───────────────┬──────────────┘
                                         │ red interna (nombres de contenedor)
        ┌───────────────┬───────────────┼───────────────┬───────────────┐
        ▼               ▼               ▼               ▼               ▼
   ┌─────────┐    ┌──────────┐   ┌───────────┐   ┌───────────┐   ┌────────────┐
   │   IAM   │    │  Social  │   │  Content  │   │  Service  │   │ Engagement │
   │  :8081  │    │  :8082   │   │   :8083   │   │   :8084   │   │   :8085    │
   └────┬────┘    └────┬─────┘   └─────┬─────┘   └─────┬─────┘   └─────┬──────┘
        │              │                │               │               │
        ▼              ▼                ▼               ▼               ▼
   tinkisqa_iam   tinkisqa_social  tinkisqa_content tinkisqa_service tinkisqa_engagement
        └──────────────┴────────────────┴───────────────┴───────────────┘
                        PostgreSQL 16 (una instancia, 5 bases independientes)
```

**Reglas de aislamiento**

- Cada servicio tiene su **propia base de datos**; **nunca** se consulta el
  esquema de otro servicio (sin SQL cruzado).
- La comunicación entre dominios es por **API REST** (eventos internos de dominio
  ya modelados; el bus de mensajería queda para una fase posterior).
- La **verificación de identidad** no simula integraciones reales: el IAM deriva
  siempre a **revisión humana**.

### Puertos

| Servicio            | Puerto | Base de datos         | Responsabilidad                                            |
|---------------------|:------:|-----------------------|------------------------------------------------------------|
| `gateway`           | 8080   | —                     | Enrutamiento, CORS, validación JWT transversal             |
| `iam-service`       | 8081   | `tinkisqa_iam`        | Auth, registro, JWT, verificación de correo/DNI, roles     |
| `social-service`    | 8082   | `tinkisqa_social`     | Comunidad, foros, publicaciones, comentarios, testimonios  |
| `content-service`   | 8083   | `tinkisqa_content`    | Artículos, recursos, biblioteca de bienestar               |
| `service-service`   | 8084   | `tinkisqa_service`    | Red de apoyo, psicólogas, voluntariado, postulaciones      |
| `engagement-service`| 8085   | `tinkisqa_engagement` | Rachas, notificaciones, métricas, actividad                |

---

## 2. Estructura del repositorio

```
Back_end/
├── docker-compose.yml          # Orquesta PostgreSQL + 5 servicios + gateway
├── .env.example                # Variables de entorno (copiar a .env)
├── init-db/
│   └── 01-create-databases.sql # Crea las 5 bases al inicializar PostgreSQL
├── gateway/                    # Proyecto Gradle independiente
└── services/
    ├── iam-service/            # Fase 1 (implementado)
    ├── social-service/         # Fase 2 (comunidades, publicaciones, interacciones)
    ├── content-service/        # Esqueleto
    ├── service-service/        # Esqueleto
    └── engagement-service/     # Esqueleto
```

Cada servicio es un **proyecto Gradle independiente** (`settings.gradle` +
`build.gradle` propios, sin wrapper) y sigue la estructura de Arquitectura Limpia:

```
src/main/java/com/tinkisqa/<servicio>/
├── domain/           # Entidades, reglas de negocio puras y puertos (sin framework)
│   ├── model/
│   ├── service/
│   ├── port/
│   └── exception/
├── application/      # Casos de uso (servicios), DTOs y configuración de aplicación
│   ├── config/
│   ├── dto/
│   └── service/
├── infrastructure/   # Adaptadores: JPA, JWT, notificaciones, eventos
│   ├── persistence/jpa/
│   ├── security/
│   ├── notification/
│   ├── verification/
│   ├── event/
│   └── config
└── presentation/      # Controladores REST, seguridad web y manejo de errores
    ├── security/
    └── web/
```

El **dominio no depende de frameworks**: los puertos (`domain/port`) se
implementan en `infrastructure` y se orquestan desde `application`.

---

## 3. Puesta en marcha

Requisitos: **Docker** (Docker Desktop / Engine) y Docker Compose v2.

```bash
cd Back_end
cp .env.example .env          # ajustar JWT_SECRET y POSTGRES_PASSWORD

docker compose up -d --build
```

- El **único puerto publicado** es el del Gateway: **http://localhost:8080**.
- PostgreSQL y los microservicios quedan en la red interna `tinkisqa-net`.
- El arranque del Gateway **espera** a que los 5 servicios estén `healthy`.

### Verificación rápida

```bash
# Estado del Gateway y de cada servicio
curl http://localhost:8080/actuator/health

# Ping del IAM a través del Gateway
curl http://localhost:8080/api/v1/auth/ping

# Documentación interactiva (por servicio, vía puerto interno)
#   IAM:     http://localhost:8081/swagger-ui.html
#   Gateway: http://localhost:8080/swagger-ui.html  (agrupa los servicios)
```

Logs de un servicio:

```bash
docker compose logs -f iam-service
```

---

## 4. Flujo de autenticación (IAM)

El flujo replica el contrato del frontend.

```bash
# 1) Registro -> envía un código OTP de 6 dígitos al correo
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@ejemplo.com","password":"Segura12345!","nombreCompleto":"Ana Quispe Ramos","acceptTerms":true}'

# 2) Obtener el código (solo en desarrollo, aparece en el log del IAM)
docker compose logs iam-service | Select-String "Código de verificación"

# 3) Verificar correo -> devuelve accessToken + refreshToken
curl -X POST http://localhost:8080/api/v1/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@ejemplo.com","code":"123456"}'

# 4) Iniciar sesión
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@ejemplo.com","password":"Segura12345!"}'

# 5) Perfil (requiere token)
curl http://localhost:8080/api/v1/users/me -H "Authorization: Bearer <ACCESS_TOKEN>"
```

### Endpoints del IAM

| Método | Ruta                                        | Público | Descripción                                  |
|:------:|---------------------------------------------|:-------:|----------------------------------------------|
| POST   | `/api/v1/auth/register`                     | ✅      | Registro de cuenta + OTP de correo           |
| POST   | `/api/v1/auth/verify-email`                 | ✅      | Verificar correo con OTP                     |
| POST   | `/api/v1/auth/resend-verification`          | ✅      | Reenviar OTP (con tiempo de espera)          |
| POST   | `/api/v1/auth/login`                        | ✅      | Inicio de sesión (bloqueo por intentos)      |
| POST   | `/api/v1/auth/refresh`                      | ✅      | Renovar sesión (rotación de refresh token)   |
| POST   | `/api/v1/auth/logout`                       | ✅      | Revocar el refresh token                     |
| GET    | `/api/v1/auth/ping`                         | ✅      | Health de enrutamiento                       |
| GET    | `/api/v1/terms`                             | ✅      | Términos y condiciones vigentes              |
| GET    | `/api/v1/users/me`                          | 🔒      | Perfil de la cuenta                          |
| PUT    | `/api/v1/users/me`                          | 🔒      | Actualizar perfil                            |
| GET    | `/api/v1/users/me/permissions`              | 🔒      | Permisos efectivos                           |
| POST   | `/api/v1/identity/verifications`            | 🔒      | Enviar verificación de DNI (→ revisión humana)|
| GET    | `/api/v1/identity/verifications/me`         | 🔒      | Estado de la última verificación             |
| POST   | `/api/v1/identity/verifications/{id}/review`| 🔒 Admin | Resolver verificación de la cola              |
| POST   | `/api/v1/identity/phone/otp`                | 🔒      | Enviar OTP por SMS                           |
| POST   | `/api/v1/identity/phone/confirm`            | 🔒      | Confirmar OTP de teléfono                    |
| GET    | `/api/v1/identity/access`                   | 🔒      | Estado de admisión a la comunidad            |
| POST   | `/api/v1/identity/access/accept-norms`      | 🔒      | Aceptar normas de la comunidad               |
| GET    | `/api/v1/security/events`                   | 🔒      | Bitácora de eventos de seguridad             |
| POST   | `/api/v1/security/reports`                  | 🔒      | Reportar a una usuaria (denuncia persistida) |
| POST   | `/api/v1/security/blocks`                   | 🔒      | Bloquear a una usuaria                       |

### Endpoints de administración (rol `ROLE_ADMIN`)

| Método | Ruta                                     | Descripción                                              |
|:------:|------------------------------------------|----------------------------------------------------------|
| GET    | `/api/v1/admin/users`                    | Listar/buscar cuentas (`query`, `status`, `page`, `size`)|
| GET    | `/api/v1/admin/users/{id}`               | Detalle de una cuenta                                    |
| POST   | `/api/v1/admin/users/{id}/suspend`       | Suspender cuenta (revoca sus refresh tokens)             |
| POST   | `/api/v1/admin/users/{id}/reactivate`    | Reactivar y recalcular el acceso                         |
| POST   | `/api/v1/admin/users/{id}/role`          | Cambiar rol (`ROLE_USER` / `ROLE_ADMIN`)                 |
| GET    | `/api/v1/admin/verifications`            | Cola de verificación de identidad (`state` opcional)     |
| GET    | `/api/v1/admin/reports`                  | Listar denuncias (`status` opcional)                     |
| POST   | `/api/v1/admin/reports/{id}/resolve`     | Resolver denuncia (`REVISADO` / `DESCARTADO`)            |

> `GET /api/v1/users/me` incluye `roles` (p. ej. `["ROLE_USER"]`) para que el
> frontend sepa si la cuenta puede acceder al panel de administración.

### Cuentas de prueba y panel de administración (solo desarrollo)

Con `IAM_SEED_ENABLED=true` (valor por defecto en `docker compose`) el IAM crea
de forma **idempotente** tres cuentas ya verificadas y con acceso aprobado:

| Rol          | Correo                    | Contraseña          |
|--------------|---------------------------|---------------------|
| `ROLE_ADMIN` | `admin@tinkisqa.dev`      | `AdminSegura123!`   |
| `ROLE_USER`  | `usuaria.a@tinkisqa.dev`  | `UsuariaSegura123!` |
| `ROLE_USER`  | `usuaria.b@tinkisqa.dev`  | `UsuariaSegura123!` |

Las dos usuarias tienen **exactamente los mismos permisos**. Nadie puede
autoaprobarse: la revisión de identidad exige `ROLE_ADMIN`.

```powershell
# 1) Iniciar sesión como ADMIN
$admin = Invoke-RestMethod -Method Post -ContentType 'application/json' `
  -Uri 'http://localhost:8080/api/v1/auth/login' `
  -Body (@{ email='admin@tinkisqa.dev'; password='AdminSegura123!' } | ConvertTo-Json)
$ah = @{ Authorization = "Bearer $($admin.accessToken)" }

# 2) Listar cuentas
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/admin/users' -Headers $ah

# 3) Cola de verificación pendiente
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/admin/verifications' -Headers $ah
```

En **Swagger UI** (`http://localhost:8080/swagger-ui.html`) selecciona la
definición *IAM Service*, pulsa **Authorize** y pega el `accessToken`; así puedes
probar los endpoints protegidos sin salir del navegador.

> El rol y los permisos viajan dentro del JWT: tras un cambio de rol o una
> suspensión, la cuenta debe volver a autenticarse (el IAM revoca sus refresh
> tokens) y, en el peor caso, el access token anterior sigue siendo válido hasta
> 60 minutos. Las denuncias de publicaciones y comentarios se gestionarán en el
> Social Service (ver roadmap).

---

## 5. Social Service (comunidades y publicaciones)

Todas las rutas se publican por el **API Gateway** (`:8080`) y exigen un access
token del IAM. Igual que en el IAM, cada operación valida el permiso funcional
con `@PreAuthorize`:

| Permiso          | Alcance                                                                  |
|------------------|--------------------------------------------------------------------------|
| `PERM_FOROS`     | ver comunidades, publicaciones y comentarios; crear comunidades; solicitar ingreso |
| `PERM_PUBLICAR`  | publicar y editar/eliminar publicaciones propias                          |
| `PERM_COMENTAR`  | comentar, dar «me gusta» y guardar                                       |

### Comunidades y solicitudes de ingreso

| Método | Ruta                                                            | Descripción                                                  |
|--------|-----------------------------------------------------------------|--------------------------------------------------------------|
| GET    | `/api/v1/communities`                                           | Listar y buscar (`q`, `ownerId`, `page`, `size`)             |
| GET    | `/api/v1/communities/{id}`                                      | Detalle (`member`, `pendingRequest`, `owner`, `memberCount`) |
| POST   | `/api/v1/communities`                                           | Crear comunidad (quien crea queda como dueña e integrante)    |
| PATCH  | `/api/v1/communities/{id}`                                      | Editar (dueña o `ROLE_ADMIN`)                                |
| DELETE | `/api/v1/communities/{id}`                                      | Eliminar (dueña o `ROLE_ADMIN`)                              |
| POST   | `/api/v1/communities/{id}/join`                                 | Solicitar ingreso (`202`); el nombre sale del JWT            |
| DELETE | `/api/v1/communities/{id}/join`                                 | Cancelar la solicitud pendiente                              |
| GET    | `/api/v1/communities/{id}/requests`                             | Solicitudes pendientes (dueña o `ROLE_ADMIN`)                |
| POST   | `/api/v1/communities/{id}/requests/{requestId}/approve`         | Aprobar (crea la membresía)                                  |
| POST   | `/api/v1/communities/{id}/requests/{requestId}/reject`          | Rechazar                                                     |
| DELETE | `/api/v1/communities/{id}/membership`                           | Salir de la comunidad                                        |

### Publicaciones, comentarios e interacciones

| Método         | Ruta                                     | Descripción                                                    |
|----------------|------------------------------------------|----------------------------------------------------------------|
| GET            | `/api/v1/posts`                          | Muro paginado (`q`, `communityId`, `authorId`, `mine`, `saved`) |
| GET            | `/api/v1/posts/{id}`                     | Detalle con contadores y marcas de quien consulta              |
| POST           | `/api/v1/posts`                          | Publicar (general o en una comunidad donde se pertenezca)      |
| PATCH          | `/api/v1/posts/{id}`                     | Editar (autora o `ROLE_ADMIN`)                                 |
| DELETE         | `/api/v1/posts/{id}`                     | Eliminar (autora o `ROLE_ADMIN`)                               |
| POST / DELETE  | `/api/v1/posts/{id}/likes`               | Dar o quitar «me gusta»                                        |
| POST / DELETE  | `/api/v1/posts/{id}/saves`               | Guardar o quitar de guardados                                  |
| GET            | `/api/v1/posts/{id}/comments`            | Comentarios de la publicación                                  |
| POST           | `/api/v1/posts/{id}/comments`            | Comentar                                                       |
| DELETE         | `/api/v1/comments/{commentId}`           | Eliminar comentario (autora o `ROLE_ADMIN`)                    |

> El muro de guardados es `GET /api/v1/posts?saved=true` y el de publicaciones
> propias `GET /api/v1/posts?mine=true`. Las respuestas incluyen las
> instantáneas de autora (`author`, `avatar`) y los datos resueltos para quien
> consulta (`likes`, `comments`, `saves`, `liked`, `saved`, `mine`), de modo que
> el frontend no necesita consultar el IAM para pintar el muro.

### Reglas de negocio

- **Publicar en una comunidad exige pertenecer a ella**; la dueña y `ROLE_ADMIN`
  también pueden. Las publicaciones generales (`communityId` nulo) no lo exigen.
- **Solo la autora o `ROLE_ADMIN`** editan o eliminan una publicación o un
  comentario.
- **Moderar una comunidad** (editar, eliminar, resolver solicitudes) es de la
  dueña o de `ROLE_ADMIN`. Las **comunidades de plataforma** (`ownerId` nulo) solo
  las modera `ROLE_ADMIN`.
- La dueña **no puede salir** de su propia comunidad; quien ya es integrante no
  puede volver a solicitar ingreso.
- Cada publicación y comentario guarda una **instantánea** del nombre y del
  avatar de su autora (tomados del JWT) para no depender del IAM al pintar.
- `community_members`, `community_join_requests`, `post_people`,
  `post_interactions` y `post_comments` caen en cascada con su comunidad o
  publicación. Al eliminar una comunidad sus publicaciones **no se borran**:
  quedan como generales (`community_id` → `NULL`).

### Contenido de demostración (solo desarrollo)

Con `SOCIAL_SEED_ENABLED=true` (por defecto en `docker compose`) el Social Service
siembra de forma **idempotente** seis comunidades de plataforma y cuatro
publicaciones con sus comentarios. Las autoras del muro son identidades de
demostración: no existen en el IAM y solo viven como instantánea en el contenido.

```powershell
# Comunidades y muro (con el token de cualquiera de las cuentas de prueba)
$me = Invoke-RestMethod -Method Post -ContentType 'application/json' `
  -Uri 'http://localhost:8080/api/v1/auth/login' `
  -Body (@{ email='usuaria.a@tinkisqa.dev'; password='UsuariaSegura123!' } | ConvertTo-Json)
$h = @{ Authorization = "Bearer $($me.accessToken)" }

Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/communities' -Headers $h
$post = (Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/posts' -Headers $h).items[0]

# Guardar una publicación y ver el muro de guardados
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/posts/$($post.id)/saves" -Headers $h
Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/posts?saved=true' -Headers $h
```

En **Swagger UI** (`http://localhost:8080/swagger-ui.html`) selecciona la
definición *Social Service* para probar estos endpoints con el mismo botón
**Authorize**.

---

## 6. Seguridad

- **JWT HS256** firmado por el IAM con un secreto compartido
  (`JWT_SECRET`, **mínimo 32 caracteres**). Claims: `sub`, `email`, `roles`,
  `permissions`, `type` (`access`/`refresh`) y, para pintar contenido sin
  consultar al IAM, `nombreCompleto` y —si existe— `avatarUri`.
- **El Gateway** valida el token en cada ruta protegida, **elimina** cualquier
  cabecera `X-User-*` enviada por el cliente y propaga la identidad en
  `X-User-Id`, `X-User-Email`, `X-User-Roles`, `X-User-Permissions` + `X-Request-Id`.
- **Cada microservicio** vuelve a validar el JWT (resource server) y traduce
  roles/permisos a autoridades de Spring Security (`ROLE_ADMIN`,
  `PERM_PUBLICAR`, …). No confía ciegamente en el Gateway.
- **Roles globales**: exactamente dos, `ROLE_USER` y `ROLE_ADMIN`; Compose crea
  una sola cuenta administrativa de desarrollo. La API no permite promover
  otras cuentas a `ROLE_ADMIN`. La administración de una comunidad se resuelve
  con su pertenencia (Social Service). Toda cuenta activa tiene los permisos
  funcionales de la app mientras la identidad espera revisión; una cuenta
  suspendida solo conserva `VER_ESTADO_SOLICITUD`.
- **Reglas del IAM**: bloqueo tras 5 intentos fallidos / 15 min, contraseña ≥ 10
  caracteres con al menos 3 comprobaciones, OTP de 6 dígitos (10 min de validez,
  5 intentos, 60 s de espera entre reenvíos), refresh con rotación.
- **Privacidad del DNI**: solo se almacena una **huella HMAC no reversible**
  (`dni_fingerprint`, única) para impedir que el mismo documento se use en dos
  cuentas; nunca el número en claro ni fotografías.
- **Verificación humana**: `ManualReviewIdentityVerificationAdapter` jamás marca
  una identidad como verificada por sí mismo; la decisión es de moderación.

---

## 7. Estructura interna (Arquitectura Limpia)

```
domain          →  modelos, reglas y puertos (sin Spring, sin JPA)
application     →  casos de uso (servicios) y DTOs; depende solo de domain
infrastructure  →  adaptadores (JPA, BCrypt, JWT, OTP, notificaciones, eventos)
presentation    →  controladores REST, seguridad y manejo de errores (RFC 7807)
```

Las dependencias apuntan **hacia dentro**: `presentation` → `application` →
`domain`. La infraestructura implementa los puertos del dominio.

---

## 8. Variables de entorno

Ver [`.env.example`](.env.example). Las principales:

| Variable                 | Descripción                                        |
|--------------------------|----------------------------------------------------|
| `POSTGRES_USER/PASSWORD` | Credenciales de PostgreSQL                         |
| `JWT_SECRET`             | Secreto HS256 (≥ 32 caracteres)                    |
| `JWT_ISSUER`             | Emisor esperado del token (`tinkisqa-iam`)         |
| `JWT_ACCESS_TTL_MINUTES` | Vida del access token (por defecto 60)             |
| `JWT_REFRESH_TTL_DAYS`   | Vida del refresh token (por defecto 7)             |
| `CORS_ALLOWED_ORIGINS`   | Orígenes permitidos para el frontend               |
| `GATEWAY_PORT`           | Puerto público del Gateway (por defecto 8080)      |
| `IAM_SEED_ENABLED`       | Crea las 3 cuentas de prueba (**solo desarrollo**) |
| `SOCIAL_SEED_ENABLED`    | Crea comunidades y publicaciones demo (**solo desarrollo**) |

---

## 9. Despliegue en Render

1. Crear **un servicio por contenedor** (5 servicios + Gateway) usando cada
   `Dockerfile` (build context = carpeta del servicio).
2. Definir `JWT_SECRET`, `JWT_ISSUER`, `POSTGRES_*` y `CORS_ALLOWED_ORIGINS`.
3. En el **Gateway**, definir `IAM_URL`, `SOCIAL_URL`, `CONTENT_URL`,
   `SERVICE_URL`, `ENGAGEMENT_URL` con las URLs públicas de cada servicio; el
   Gateway deja de usar los nombres de contenedor.
4. Exponer públicamente **solo el Gateway**; el resto puede quedar privado.
5. Base de datos: usar un PostgreSQL gestionado y crear las 5 bases (o adaptar
   `init-db/01-create-databases.sql`). El `docker-compose.yml` es para local.

> En Render cada servicio corre en su propio contenedor; el health check es
> `/actuator/health` y el tráfico interno va por HTTPS.

---

## 10. Estado y roadmap

| Fase | Alcance                                                        | Estado |
|:----:|----------------------------------------------------------------|:------:|
| 0    | Esqueleto: 5 servicios + Gateway + PostgreSQL + Docker         | ✅     |
| 1    | **IAM**: auth, JWT, verificación, roles/permisos, perfil, seguridad | ✅ |
| 1.1  | **IAM — autorización**: 2 roles globales (`ROLE_USER`/`ROLE_ADMIN`), panel de administración, denuncias persistidas, cuentas de prueba | ✅ |
| 2    | **Social**: comunidades, solicitudes de unión, publicaciones, comentarios, «me gusta» y guardados | ✅ |
| 2.1  | **Social — contenido y moderación**: testimonios, promociones y denuncias de publicaciones/comentarios | ⏳ |
| 2.2  | **Frontend Social**: conectar el muro y las comunidades del front con el API Social | ⏳ |
| 3    | **Content**: artículos y biblioteca de recursos                | ⏳     |
| 4    | **Service**: red de apoyo, psicólogas, voluntariado            | ⏳     |
| 5    | **Engagement**: rachas, notificaciones, métricas               | ⏳     |
| 6    | **Frontend**: conectar los módulos al Gateway en modo API      | ⏳     |

> Pendiente conocido: al viajar los permisos dentro del JWT, una cuenta
> suspendida conserva sus permisos hasta que expira el access token (60 min). Se
> mitiga revocando los refresh tokens; la solución completa (introspección o
> lista de denegación en el Gateway) queda para una fase posterior.

---

## 11. Desarrollo local sin Docker (opcional)

Cada servicio es independiente. Con un JDK 21 y Gradle 8.x:

```bash
cd services/iam-service
gradle bootRun   # requiere PostgreSQL y las variables de entorno
```

---

## 12. Notas

- `ddl-auto: none` en todos los servicios: el esquema lo gestiona **Flyway**
  (`src/main/resources/db/migration`).
- Sin broker de mensajería en esta fase: los eventos de dominio se publican en el
  log y el diseño queda listo para conectar un bus más adelante.
- Ningún servicio simula integraciones reales (correo/SMS se registran en el log
  en desarrollo; la verificación de identidad es siempre humana).
