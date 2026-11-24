# ELESOMI - Instalacion local con Docker (Windows PowerShell)
# Requiere Docker Desktop en ejecucion, puerto 8080 y 5433 disponibles.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
function Invoke-Checked { param([string[]]$command)
    & $command[0] @($command | Select-Object -Skip 1)
    if ($LASTEXITCODE -ne 0) { throw "Fallo comando: $($command -join ' ')" }
}
$ContainerDb = 'aerolinea-pasajeros-db'
$ContainerApp = 'aerolinea-pasajeros-app'
$Image = 'payara/server-full:5.2022.5'
$Asadmin = '/opt/payara/appserver/bin/asadmin'
$AdminArgs = @('--user','admin','--passwordfile','/opt/payara/passwordFile')
Write-Host 'ELESOMI - Preparando PostgreSQL'
Invoke-Checked -command @('docker','compose','up','-d')
$ready = $false
for ($i=0; $i -lt 30; $i++) {
    & docker exec $ContainerDb pg_isready -U aerolinea -d aerolinea *> $null
    if ($LASTEXITCODE -eq 0) { $ready=$true;break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'PostgreSQL no quedo listo.' }
$tables = & docker exec $ContainerDb psql -U aerolinea -d aerolinea -tAc "SELECT count(*) FROM information_schema.tables WHERE table_schema='public' AND table_name='pasajero'"
if ($LASTEXITCODE -ne 0) { throw 'No fue posible inspeccionar el esquema.' }
if ($tables.Trim() -eq '0') {
    Write-Host 'Instalando esquema y datos de demostracion'
    Get-Content (Join-Path $PSScriptRoot '../sql/01_esquema_postgresql.sql') -Raw | docker exec -i $ContainerDb psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
    if ($LASTEXITCODE -ne 0) {throw 'Fallo el esquema SQL.'}
    Get-Content (Join-Path $PSScriptRoot '../sql/02_datos_demo.sql') -Raw | docker exec -i $ContainerDb psql -v ON_ERROR_STOP=1 -U aerolinea -d aerolinea
    if ($LASTEXITCODE -ne 0) {throw 'Fallo la semilla SQL.'}
} else { Write-Host 'Esquema existente: se conservan los pasajeros.' }
Write-Host 'Compilando WAR y ejecutando JUnit'
Invoke-Checked -command @('docker','run','--rm','-v',"${PWD}:/workspace",'-w','/workspace','maven:3.9.11-eclipse-temurin-17','mvn','-B','clean','package')
if (-not (Test-Path './target/aerolinea-pasajeros.war')) {throw 'No se genero el WAR.'}
Write-Host 'Configurando Payara'
$exists = & docker ps -aq --filter "name=^/${ContainerApp}$"
if (-not $exists) {
    Invoke-Checked -command @('docker','run','-d','--name',$ContainerApp,'--network','aerolinea-pasajeros_default','-p','8080:8080',$Image)
} else {
    $running = & docker inspect -f '{{.State.Running}}' $ContainerApp
    if ($running -ne 'true') { Invoke-Checked -command @('docker','start',$ContainerApp) }
}
$driver=Join-Path (Get-Location) 'postgresql-42.7.4.jar'
if (-not (Test-Path $driver)) {
    Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar' -OutFile $driver
}
$existingDriver = & docker exec $ContainerApp test -f /opt/payara/appserver/glassfish/domains/domain1/lib/postgresql-42.7.4.jar
if ($LASTEXITCODE -ne 0) {
    Invoke-Checked -command @('docker','cp',$driver,"${ContainerApp}:/opt/payara/appserver/glassfish/domains/domain1/lib/postgresql-42.7.4.jar")
    Invoke-Checked -command @('docker','restart',$ContainerApp)
}
$adminReady=$false
for ($i=0; $i -lt 60; $i++) {
    & docker exec $ContainerApp $Asadmin @AdminArgs list-jdbc-connection-pools *> $null
    if ($LASTEXITCODE -eq 0) {$adminReady=$true;break}
    Start-Sleep -Seconds 3
}
if (-not $adminReady) {throw 'Payara no respondio por el puerto administrativo.'}
$pools = & docker exec $ContainerApp $Asadmin @AdminArgs list-jdbc-connection-pools
if ($pools -notcontains 'AerolineaPool') {
    Invoke-Checked -command (@('docker','exec',$ContainerApp,$Asadmin) + $AdminArgs + @('create-jdbc-connection-pool','--datasourceclassname','org.postgresql.ds.PGSimpleDataSource','--restype','javax.sql.DataSource','--property','serverName=aerolinea-pasajeros-db:portNumber=5432:databaseName=aerolinea:user=aerolinea:password=aerolinea_dev','AerolineaPool'))
}
Invoke-Checked -command (@('docker','exec',$ContainerApp,$Asadmin) + $AdminArgs + @('ping-connection-pool','AerolineaPool'))
$resources = & docker exec $ContainerApp $Asadmin @AdminArgs list-jdbc-resources
if ($resources -notcontains 'jdbc/AerolineaDS') {
    Invoke-Checked -command (@('docker','exec',$ContainerApp,$Asadmin) + $AdminArgs + @('create-jdbc-resource','--connectionpoolid','AerolineaPool','jdbc/AerolineaDS'))
}
Invoke-Checked -command @('docker','cp','./target/aerolinea-pasajeros.war',"${ContainerApp}:/tmp/elesomi.war")
Invoke-Checked -command (@('docker','exec',$ContainerApp,$Asadmin) + $AdminArgs + @('deploy','--force=true','--name','aerolinea-pasajeros','--contextroot','aerolinea-pasajeros','/tmp/elesomi.war'))
Write-Host 'ELESOMI disponible en http://localhost:8080/aerolinea-pasajeros/'
