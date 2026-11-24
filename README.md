# ELESOMI

**La calidad por los aires**

**Proyecto de integración de software - Módulo de Gestión de Pasajeros**

**Integrantes**
- Elena Vargas Grisales
- Sofía Vásquez Espinal
- Miguel Ángel Marín Otálvaro


## Descripción

ELESOMI es una aplicación web para gestionar pasajeros de una aerolínea. Permite registrar, consultar, buscar, editar y desactivar pasajeros. Expone servicios REST para que otros módulos puedan consultar los registros y verificar si un pasajero se encuentra activo.

La solución utiliza Java EE 7, JAX-RS, JPA, PostgreSQL 16 y una interfaz web responsive con identidad visual propia. Se distribuye como archivo WAR para un servidor de aplicaciones compatible con Java EE.

## Funcionalidades

- Registro de pasajeros con validación de datos.
- Consulta y búsqueda paginada de pasajeros.
- Edición de datos personales.
- Desactivación lógica sin eliminar el registro de la base de datos.
- Verificación de pasajeros activos mediante tipo y número de documento.
- API REST con respuestas JSON y códigos HTTP para errores de negocio.
- Interfaz responsive ELESOMI con iconografía SVG.

## Tecnologías

- Java EE 7 y Java 8 como versión objetivo de compilación.
- JAX-RS para la API REST.
- JPA y transacciones JTA para persistencia.
- PostgreSQL 16.
- Payara Server 5.2022.5 como servidor de aplicaciones utilizado en las pruebas locales.
- Maven para compilación y JUnit 4 para pruebas automatizadas.
- Docker Desktop y Docker Compose para el entorno local.

## Estructura del repositorio

- `src/main/java/` - Código Java, servicios, modelo y API REST.
- `src/main/webapp/` - Interfaz web, estilos, JavaScript y recursos SVG.
- `src/test/java/` - Pruebas JUnit.
- `sql/` - Esquema PostgreSQL y datos de demostración.
- `diagramas/` - Diagramas de arquitectura, componentes, entidad-relación, secuencia, despliegue y clases.
- `documentacion/` - Diseño arquitectural, especificación arquitectural, plan de pruebas y contrato OpenAPI.
- `presentacion/` - Presentación del proyecto en PowerPoint y PDF.
- `scripts/` - Utilidades para configuración, despliegue y comprobaciones de la API.

## Instalación local

Los comandos siguientes se ejecutan en PowerShell desde la carpeta raíz del repositorio. Docker Desktop debe estar en ejecución.

### 1. Iniciar PostgreSQL

```powershell
docker compose up -d
```

La configuración de desarrollo utiliza:

- Base de datos: `aerolinea`
- Usuario: `aerolinea`
- Contraseña local: `aerolinea_dev`
- Puerto del equipo: `5433`
- Puerto dentro de Docker: `5432`

Estas credenciales son exclusivamente de demostración y no deben utilizarse en entornos públicos.

### 2. Crear el esquema y cargar datos de ejemplo

Ejecutar los scripts en el siguiente orden sobre una base de datos nueva:

```powershell
Get-Content .\sql\01_esquema_postgresql.sql -Raw | docker exec -i aerolinea-pasajeros-db psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
```

```powershell
Get-Content .\sql\02_datos_demo.sql -Raw | docker exec -i aerolinea-pasajeros-db psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
```

El script de datos de demostración está previsto para la carga inicial.

### 3. Compilar y ejecutar las pruebas

No es necesario instalar Maven globalmente si se utiliza su imagen Docker.

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-17 mvn -B clean package
```

El archivo generado queda en `target/aerolinea-pasajeros.war`.

Para ejecutar solamente las pruebas automatizadas:

```powershell
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.11-eclipse-temurin-17 mvn -B test
```

Los reportes de pruebas se generan en `target/surefire-reports/`.

### 4. Configurar Payara Server y PostgreSQL JDBC

Se utilizó la imagen Docker `payara/server-full:5.2022.5`. El servidor debe estar conectado a la red Docker creada por Compose, `aerolinea-pasajeros_default`, y tener publicado el puerto `8080`.

Ejemplo de inicio del contenedor desde la raíz del proyecto:

```powershell
docker run -d --name aerolinea-pasajeros-app --network aerolinea-pasajeros_default -p 8080:8080 payara/server-full:5.2022.5
```

Descargar el controlador JDBC de PostgreSQL y copiarlo al directorio de librerías del dominio:

```powershell
curl.exe -L --fail -o postgresql-42.7.4.jar https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar
```

```powershell
docker cp .\postgresql-42.7.4.jar aerolinea-pasajeros-app:/opt/payara/appserver/glassfish/domains/domain1/lib/postgresql-42.7.4.jar
```

```powershell
docker restart aerolinea-pasajeros-app
```

Una vez iniciado el servidor, crear el pool JDBC y verificar la conexión:

```powershell
docker exec aerolinea-pasajeros-app /opt/payara/appserver/bin/asadmin --user admin --passwordfile /opt/payara/passwordFile create-jdbc-connection-pool --datasourceclassname org.postgresql.ds.PGSimpleDataSource --restype javax.sql.DataSource --property 'serverName=aerolinea-pasajeros-db:portNumber=5432:databaseName=aerolinea:user=aerolinea:password=aerolinea_dev' AerolineaPool
```

```powershell
docker exec aerolinea-pasajeros-app /opt/payara/appserver/bin/asadmin --user admin --passwordfile /opt/payara/passwordFile ping-connection-pool AerolineaPool
```

Registrar el recurso JNDI que utiliza `persistence.xml`:

```powershell
docker exec aerolinea-pasajeros-app /opt/payara/appserver/bin/asadmin --user admin --passwordfile /opt/payara/passwordFile create-jdbc-resource --connectionpoolid AerolineaPool jdbc/AerolineaDS
```

### 5. Desplegar la aplicación

Copiar el WAR a una ruta temporal del contenedor:

```powershell
docker cp .\target\aerolinea-pasajeros.war aerolinea-pasajeros-app:/tmp/aerolinea-pasajeros.war
```

Desplegarlo con el contexto correspondiente:

```powershell
docker exec aerolinea-pasajeros-app /opt/payara/appserver/bin/asadmin --user admin --passwordfile /opt/payara/passwordFile deploy --force=true --name aerolinea-pasajeros --contextroot aerolinea-pasajeros /tmp/aerolinea-pasajeros.war
```

En una instalación nueva, ejecutar el despliegue cuando Payara haya terminado de iniciar y el recurso `jdbc/AerolineaDS` esté disponible.

## Acceso a la aplicación

Interfaz web:

`http://localhost:8080/aerolinea-pasajeros/`

API REST:

`http://localhost:8080/aerolinea-pasajeros/api/pasajeros`

## Servicios REST

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/pasajeros?q=&pagina=1&tamano=10` | Consultar pasajeros con filtros y paginación |
| GET | `/api/pasajeros/{id}` | Obtener un pasajero activo |
| GET | `/api/pasajeros/verificar?tipoDocumento=CC&numeroDocumento=1000001001` | Verificar un pasajero activo |
| POST | `/api/pasajeros` | Registrar un pasajero |
| PUT | `/api/pasajeros/{id}` | Actualizar información |
| DELETE | `/api/pasajeros/{id}` | Desactivar un pasajero |

La especificación de las operaciones se encuentra en `documentacion/openapi.yaml`.

## Verificación funcional

El proyecto se compiló correctamente y se ejecutó en Payara Server 5.2022.5 con PostgreSQL 16. En las comprobaciones locales se verificaron el registro, la edición, la búsqueda, la desactivación lógica, la consulta REST, la verificación de documentos existentes e inexistentes y la respuesta HTTP `404` para pasajeros no disponibles.

Las pruebas JUnit del proyecto registraron los siguientes resultados:

- `PasajeroTest`: 4 pruebas, 0 fallos, 0 errores.
- `DatosPasajeroTest`: 12 pruebas, 0 fallos, 0 errores.
- Total: 16 pruebas, 0 fallos, 0 errores y 0 omitidas.

Estas pruebas cubren el modelo y la validación de datos. Las operaciones integradas con PostgreSQL y Payara se comprobaron mediante pruebas funcionales locales.

## Documentación del proyecto

El repositorio incluye los documentos de diseño y especificación arquitectural en formatos Word y PDF, diagramas técnicos, plan de pruebas, contrato OpenAPI, scripts SQL y presentación académica.

## Equipo de desarrollo

Elena Vargas Grisales  
Sofía Vásquez Espinal  
Miguel Ángel Marín Otálvaro

## Instalación automatizada en Windows

Para una instalación local con Docker Desktop en ejecución, ejecutar desde la carpeta del proyecto:

```powershell
.\scripts\01_instalacion_docker.ps1
```

El script crea o reutiliza PostgreSQL, inicializa la base de datos si está vacía, ejecuta Maven y JUnit, configura el controlador y el recurso JDBC en Payara, y despliega el archivo WAR. Los datos existentes se conservan. Requiere los puertos locales 8080 y 5433 disponibles.

## Evidencias de ejecución

Las seis capturas de la aplicación, la API REST y los resultados JUnit están disponibles en `documentacion/evidencias/`. Se incluye el anexo `Evidencias_ELESOMI.pdf`.
