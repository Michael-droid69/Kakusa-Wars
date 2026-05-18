@echo off
REM ═══════════════════════════════════════════════════════════════
REM  RPG Battle System - Optimized Launch Script
REM  Professional-grade JVM flags for maximum performance
REM ═══════════════════════════════════════════════════════════════

echo.
echo ⚔️  RPG Battle System - Optimized Launch
echo ═══════════════════════════════════════════════════════════════
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 11 or higher
    pause
    exit /b 1
)

echo ✅ Java detected
echo.
echo 🚀 Launching with performance optimizations...
echo.
echo Optimizations enabled:
echo   • Memory: 512 MB initial, 1 GB max
echo   • GC: G1 collector with 20ms max pause
echo   • Hardware acceleration: OpenGL + Direct3D
echo   • Rendering: Fast mode enabled
echo.

REM Launch with optimized JVM flags
java -Xms512m ^
     -Xmx1024m ^
     -XX:+UseG1GC ^
     -XX:MaxGCPauseMillis=20 ^
     -Dsun.java2d.opengl=true ^
     -Dsun.java2d.d3d=true ^
     -Dswing.aatext=true ^
     -Dawt.useSystemAAFontSettings=on ^
     -cp bin ui.MainFrame

echo.
echo Game closed.
pause
