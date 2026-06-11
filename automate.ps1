$ErrorActionPreference = "Continue"

Clear-Host
Write-Host "=========================================================================" -ForegroundColor Yellow
Write-Host "   ETRS - AUTOMATIC BUILD, TEST AND DOCUMENTATION SYSTEM                 " -ForegroundColor Yellow
Write-Host "=========================================================================" -ForegroundColor Yellow

# 1. Authorization Service (.NET)
Write-Host "`n[1/3] Processing .NET project..." -ForegroundColor Cyan
try {
    Push-Location "./event-ticket-auth-service"
    Write-Host "-> Running .NET unit tests..." -ForegroundColor Gray
    dotnet test --logger:"console;verbosity=normal"
    if ($LastExitCode -ne 0) { throw "NET tests failed." }
    
    Write-Host "-> Building .NET application..." -ForegroundColor Gray
    dotnet build -c Release
    if ($LastExitCode -ne 0) { throw "NET build failed." }
    
    Pop-Location
    Write-Host "[OK] Authorization Service secured and built." -ForegroundColor Green
} catch {
    Write-Host "`n[ERROR] ERROR IN NET SERVICE!" -ForegroundColor Red
    Write-Host "Details: $_" -ForegroundColor DarkRed
    if ((Get-Location).Path -match "event-ticket-auth-service") { Pop-Location }
    exit 1
}

# 2. Java Microservices
Write-Host "`n[2/3] Processing Java projects..." -ForegroundColor Cyan
try {
    Push-Location "./event-ticket-main-services"
    Write-Host "-> Running Java unit tests..." -ForegroundColor Gray
    
    cmd.exe /c "mvnw.cmd clean test"
    if ($LastExitCode -ne 0) { throw "Maven tests failed. Scroll up to see details." }
    
    Write-Host "-> Building JAR packages..." -ForegroundColor Gray
    cmd.exe /c "mvnw.cmd clean package -DskipTests"
    if ($LastExitCode -ne 0) { throw "Maven build failed." }
    
    # 3. Documentation Generation
    Write-Host "`n[3/3] Generating code documentation (Javadoc)..." -ForegroundColor Cyan
    cmd.exe /c "mvnw.cmd javadoc:javadoc -DskipTests -q"
    if ($LastExitCode -ne 0) { throw "Javadoc generation failed." }
    
    Pop-Location
    Write-Host "[OK] Java projects tested, built, and documentation generated." -ForegroundColor Green
} catch {
    Write-Host "`n[ERROR] ERROR IN JAVA PROJECTS!" -ForegroundColor Red
    Write-Host "Details: $_" -ForegroundColor DarkRed
    if ((Get-Location).Path -match "event-ticket-main-services") { Pop-Location }
    exit 1
}

Write-Host "`n=========================================================================" -ForegroundColor Yellow
Write-Host " SUCCESS: All components have been successfully verified! " -ForegroundColor Green
Write-Host " Javadoc documentation is located at:" -ForegroundColor Yellow

# Dynamiczne wyszukiwanie wygenerowanej dokumentacji
$javadocs = Get-ChildItem -Path "./event-ticket-main-services" -Recurse -Filter "index.html" | Where-Object { $_.DirectoryName -match "apidocs" }
foreach ($doc in $javadocs) {
    Write-Host " -> $($doc.FullName)" -ForegroundColor Cyan
}

Write-Host "`n You can now start the system using: docker compose up --build -d" -ForegroundColor Yellow
Write-Host "=========================================================================" -ForegroundColor Yellow