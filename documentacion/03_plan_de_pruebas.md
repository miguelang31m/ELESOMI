# Plan de pruebas - Gestión de Pasajeros

**Versión:** 1.0.0
**Entorno:** GlassFish 4 + PostgreSQL 16 + Java 8
**Estado inicial:** PENDIENTE DE EJECUCIÓN EN EL EQUIPO DEL ESTUDIANTE.

No se deben marcar como aprobadas hasta ejecutarlas contra la aplicación desplegada.

| ID | Escenario | Acción | Resultado esperado | Resultado real |
|---|---|---|---|---|
| P01 | Alta válida | POST con documento nuevo | HTTP 201, id asignado | Pendiente |
| P02 | Documento duplicado | POST con mismo tipo y número | HTTP 409 DOCUMENTO_DUPLICADO | Pendiente |
| P03 | Fecha futura | POST con fecha posterior a hoy | HTTP 400 FECHA_INVALIDA | Pendiente |
| P04 | Listar/buscar | GET ?q=laura | HTTP 200, items/total | Pendiente |
| P05 | Actualizar | PUT con id existente | HTTP 200, campos actualizados | Pendiente |
| P06 | Verificar existente | GET /verificar con documento válido | HTTP 200 existe=true | Pendiente |
| P07 | Desactivar | DELETE id | HTTP 204 | Pendiente |
| P08 | Consultar inactivo | GET id desactivado | HTTP 404 | Pendiente |
| P09 | Paginación inválida | GET ?pagina=0 | HTTP 400 PAGINACION_INVALIDA | Pendiente |
| P10 | Documento inexistente | GET /verificar con documento nuevo | HTTP 200 existe=false | Pendiente |
| P11 | Concurrencia de documento | Dos POST simultáneos mismo documento | Como máximo uno creado por UNIQUE | Pendiente |
| P12 | Navegador responsive | Probar ancho móvil y escritorio | Sin controles ocultos/inaccesibles | Pendiente |

## Smoke test automatizado

```powershell
.\scripts\04_smoke_api.ps1
```

Seis operaciones de integración; registra el resultado final y captura de pantalla.

## Evidencias a guardar

- Captura del directorio con registros.
- Captura del formulario y operación de alta.
- Captura de respuesta HTTP 201 y 409.
- Resultado de `mvn clean package` y despliegue GlassFish.
- Resultado de `scripts/04_smoke_api.ps1`.
- Registro de errores de servidor si se presenta alguna falla.

## Control de cambios

| Fecha | Versión | Cambio |
|---|---|---|
| 2026-11-24 | 1.0.0 | Versión académica inicial; pruebas pendientes de ejecución real. |
