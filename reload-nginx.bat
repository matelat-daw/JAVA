@echo off
REM Script para recargar nginx
setlocal enabledelayedexpansion

REM Buscar nginx
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":80 "') do (
    set PID=%%a
    echo Nginx PID encontrado: !PID!
)

REM Enviar señal de reload (requiere permisos de administrador)
if defined PID (
    echo Recargando nginx...
    taskkill /PID %PID% /F
    timeout /t 2
    echo Reiniciando nginx...
    REM Buscar nginx.exe en ubicaciones comunes
    if exist "C:\nginx\nginx.exe" (
        start "" "C:\nginx\nginx.exe"
    ) else if exist "C:\nginx-1.28.2\nginx.exe" (
        start "" "C:\nginx-1.28.2\nginx.exe"
    ) else if exist "D:\nginx\nginx.exe" (
        start "" "D:\nginx\nginx.exe"
    ) else (
        echo Error: No se encontro nginx.exe
    )
    echo Done!
) else (
    echo Error: No se encontro nginx
)
pause
