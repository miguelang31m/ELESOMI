param([Parameter(Mandatory=$true)][string]$GlassFishHome)
$ErrorActionPreference = "Stop"
$asadmin = Join-Path $GlassFishHome "bin\asadmin.bat"
if (!(Test-Path $asadmin)) { throw "No se encuentra asadmin.bat" }
if (!(Test-Path "target\aerolinea-pasajeros.war")) { throw "Compile primero con mvn clean package" }
& $asadmin deploy --force=true --contextroot aerolinea-pasajeros "target\aerolinea-pasajeros.war"
if ($LASTEXITCODE -ne 0) { throw "Fallo el despliegue." }
Write-Host "Abra http://localhost:8080/aerolinea-pasajeros/"
