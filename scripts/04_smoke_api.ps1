param([string]$BaseUrl = "http://localhost:8080/aerolinea-pasajeros/api")
$ErrorActionPreference = "Stop"
$numero = "QA" + (Get-Date -Format "yyyyMMddHHmmss")
$payload = @{ nombres="Prueba"; apellidos="Integracion"; tipoDocumento="PAS"; numeroDocumento=$numero; fechaNacimiento="1995-01-10"; correo="qa@example.com"; telefono="3001234567"; nacionalidad="Colombiana" } | ConvertTo-Json
Write-Host "1. Crear"
$p = Invoke-RestMethod -Method Post -Uri "$BaseUrl/pasajeros" -ContentType "application/json" -Body $payload
if (!$p.id) { throw "No se creo el pasajero" }
Write-Host "2. Consultar"
$obtenido = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pasajeros/$($p.id)"
if ($obtenido.numeroDocumento -ne $numero) { throw "Consulta incorrecta" }
Write-Host "3. Verificar documento"
$v = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pasajeros/verificar?tipoDocumento=PAS&numeroDocumento=$numero"
if (!$v.existe) { throw "Verificacion incorrecta" }
Write-Host "4. Actualizar"
$datos = $payload | ConvertFrom-Json
$datos.nombres = "PruebaActualizada"
$actualizado = Invoke-RestMethod -Method Put -Uri "$BaseUrl/pasajeros/$($p.id)" -ContentType "application/json" -Body ($datos | ConvertTo-Json)
if ($actualizado.nombres -ne "PruebaActualizada") { throw "Actualizacion incorrecta" }
Write-Host "5. Listar"
$lista = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pasajeros?pagina=1&tamano=10&q=$numero"
if ($lista.total -lt 1) { throw "Listado incorrecto" }
Write-Host "6. Desactivar"
Invoke-RestMethod -Method Delete -Uri "$BaseUrl/pasajeros/$($p.id)"
$v2 = Invoke-RestMethod -Method Get -Uri "$BaseUrl/pasajeros/verificar?tipoDocumento=PAS&numeroDocumento=$numero"
if ($v2.existe) { throw "No se desactivo" }
Write-Host "OK: 6 escenarios completados."
