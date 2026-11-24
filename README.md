# ELESOMI - La calidad por los aires

Proyecto académico de integración de software. Módulo de gestión de pasajeros.

**Equipo:** Elena Vargas Grisales, Sofía Vásquez Espinal y Miguel.

**Versión:** 1.1.0

## Funcionalidades

- Interfaz responsive de estilo brutalista, negro, blanco y magenta.
- Registro, búsqueda paginada, edición y desactivación lógica de pasajeros.
- API REST JAX-RS de gestión y verificación para integrar otros módulos.
- Validaciones de entrada, códigos HTTP de negocio y persistencia JPA en PostgreSQL.
- Pruebas JUnit 4 y documentos de arquitectura.

## Requisitos

- Docker Desktop para PostgreSQL 16 y para compilar con Maven dentro de contenedor.
- Un servidor Java EE compatible, probado manualmente con Payara Server 5.2022.5.
- PostgreSQL JDBC 42.7.4 para Payara.
- PowerShell en Windows.

## Preparar PostgreSQL

Desde PowerShell, ubicado en la raíz del proyecto, ejecutar por separado:

```powershell
docker compose up -d
```

La base de datos se llama `aerolinea`, usuario `aerolinea`, contraseña local `aerolinea_dev`, puerto local `5433`. Es una configuración exclusiva de desarrollo. No publique esta contraseña en sistemas reales.

En la primera instalación ejecutar el esquema y luego los datos ficticios:

```powershell
Get-Content .\sql\01_esquema_postgresql.sql -Raw | docker exec -i aerolinea-pasajeros-db psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
```

```powershell
Get-Content .\sql\02_datos_demo.sql -Raw | docker exec -i aerolinea-pasajeros-db psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
```

No vuelva a ejecutar el script demo sobre una base que ya tiene esos documentos registrados.

## Compilar WAR con Maven en Docker

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-17 mvn -B clean package
```

El WAR resultante queda en `target/aerolinea-pasajeros.war`.

## Ejecutar JUnit 4

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-17 mvn -B test
```

Las pruebas se ubican en `src/test/java`. La compilación y las pruebas de esta versión se deben verificar nuevamente en el entorno del estudiante. Una salida `BUILD SUCCESS` sin tests ejecutados no equivale a pruebas aprobadas.

## Desplegar con Payara 5 en Docker

El proyecto requiere el recurso JNDI `jdbc/AerolineaDS` apuntando a PostgreSQL. Si es la primera instalación, los pasos son:

1. Crear el contenedor Payara, conectándolo a la red `aerolinea-pasajeros_default` y publicando el puerto 8080.
2. Descargar `postgresql-42.7.4.jar` desde Maven Central y copiarlo a `/opt/payara/appserver/glassfish/domains/domain1/lib/` dentro del contenedor. Reiniciar Payara.
3. Crear el pool JDBC `AerolineaPool` con `org.postgresql.ds.PGSimpleDataSource`, host `aerolinea-pasajeros-db`, puerto `5432`, base `aerolinea` y credenciales de desarrollo.
4. Ejecutar `ping-connection-pool AerolineaPool` y crear `jdbc/AerolineaDS` apuntando al pool.
5. Desplegar el WAR con `asadmin deploy --force=true`.

El procedimiento exacto ya fue verificado manualmente en la versión base. La configuración administrativa del servidor y el driver copiado dentro del contenedor se pierden si ese contenedor se elimina, salvo que se persistan o automaticen.

La interfaz abre en:

`http://localhost:8080/aerolinea-pasajeros/`

La API abre en:

`http://localhost:8080/aerolinea-pasajeros/api/pasajeros`

## Contrato REST

- `GET /api/pasajeros?q=&pagina=1&tamano=10` - Consulta paginada.
- `GET /api/pasajeros/{id}` - Obtiene un pasajero activo por ID.
- `GET /api/pasajeros/verificar?tipoDocumento=CC&numeroDocumento=1000001001` - Consulta existencia de pasajero activo.
- `POST /api/pasajeros` - Crea pasajero.
- `PUT /api/pasajeros/{id}` - Actualiza pasajero.
- `DELETE /api/pasajeros/{id}` - Desactiva pasajero.

El contrato detallado se encuentra en `documentacion/openapi.yaml`.

## Contenido para entregar

- `src/` - Código fuente Java EE, frontend y pruebas.
- `sql/` - Esquema y datos de demostración.
- `diagramas/` - Arquitectura, componentes, entidad-relación, secuencia, despliegue y clases.
- `documentacion/` - Dos documentos de arquitectura en Word y PDF, plan de pruebas y OpenAPI.
- `presentacion/` - Presentación final en PowerPoint y PDF.
- `scripts/` - Scripts auxiliares de configuración y smoke test.

## Evidencia funcional

En una ejecución local de la versión 1.0.0 se comprobó la compilación WAR, el despliegue en Payara 5, la conectividad JDBC con PostgreSQL, la creación, edición, búsqueda y desactivación de pasajeros; la verificación REST con documentos existentes e inexistentes; y una respuesta JSON 404 controlada.

**Pendientes antes de la entrega final:** ejecutar Maven JUnit sobre 1.1.0, volver a desplegar el WAR para validar el frontend nuevo, registrar evidencias reales y confirmar con el profesor qué otros módulos participarán en la integración. No se ha comprobado una integración externa entre grupos.
