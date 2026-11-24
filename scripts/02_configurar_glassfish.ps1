param(
    [Parameter(Mandatory=$true)][string]$GlassFishHome,
    [string]$JdbcJar = ""
)
$ErrorActionPreference = "Stop"
$asadmin = Join-Path $GlassFishHome "bin\asadmin.bat"
if (!(Test-Path $asadmin)) { throw "No se encuentra asadmin.bat en $asadmin" }
if ($JdbcJar -ne "") {
    if (!(Test-Path $JdbcJar)) { throw "No existe el driver JDBC: $JdbcJar" }
    $destino = Join-Path $GlassFishHome "domains\domain1\lib\postgresql.jar"
    Copy-Item $JdbcJar $destino -Force
    Write-Host "Driver copiado. Reiniciando dominio para cargarlo..."
    & $asadmin restart-domain
    if ($LASTEXITCODE -ne 0) { throw "No se pudo reiniciar el dominio; reinicielo manualmente." }
}
Write-Host "Asegurese de haber iniciado el dominio y de tener el driver PostgreSQL instalado."
& $asadmin create-jdbc-connection-pool --restype javax.sql.DataSource --datasourceclassname org.postgresql.ds.PGSimpleDataSource --property "user=aerolinea:password=aerolinea_dev:DatabaseName=aerolinea:ServerName=localhost:PortNumber=5433" AerolineaPool
if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el pool; si ya existe, reviselo en la consola de GlassFish." }
& $asadmin create-jdbc-resource --connectionpoolid AerolineaPool jdbc/AerolineaDS
if ($LASTEXITCODE -ne 0) { throw "No se pudo crear el recurso JDBC." }
& $asadmin ping-connection-pool AerolineaPool
if ($LASTEXITCODE -ne 0) { throw "Fallo la conexion a PostgreSQL." }
Write-Host "Pool y recurso JDBC creados."
