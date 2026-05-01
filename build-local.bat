@echo off
REM NaviMusic 本地构建脚本
REM 需要管理员权限运行（安装依赖）

set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
set "CMDLINE_TOOLS=%ANDROID_HOME%\cmdline-tools\latest"

echo ================================================
echo   NaviMusic 本地构建脚本
echo ================================================
echo.

REM 检查 Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo [1/5] 未检测到 Java，正在下载 OpenJDK 17...
    curl -L -o "%TEMP%\jdk17.zip" "https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_windows-x64_bin.zip"
    echo 请手动安装 %TEMP%\jdk17.zip 中的 JDK，然后重新运行此脚本。
    pause
    exit /b 1
)
echo [1/5] Java OK

REM 检查 Android SDK
if not exist "%CMDLINE_TOOLS%\bin\sdkmanager.bat" (
    echo [2/5] 未检测到 Android SDK，正在下载...
    if not exist "%ANDROID_HOME%" mkdir "%ANDROID_HOME%"
    if not exist "%ANDROID_HOME%\cmdline-tools" mkdir "%ANDROID_HOME%\cmdline-tools"
    curl -L -o "%TEMP%\cmdline.zip" "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    tar -xf "%TEMP%\cmdline.zip" -C "%ANDROID_HOME%\cmdline-tools"
    ren "%ANDROID_HOME%\cmdline-tools\cmdline-tools" "latest"
    echo Android SDK 命令行工具已下载
)

set "PATH=%CMDLINE_TOOLS%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

echo [3/5] 接受 Android SDK 许可...
echo y | sdkmanager --licenses >nul 2>&1

echo [3/5] 安装 Android SDK 组件...
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo [4/5] 授予 Gradle 执行权限...
icacls "gradlew" /grant Everyone:RX >nul 2>&1

echo [5/5] 开始构建 APK...
call gradlew.bat assembleDebug --no-daemon --stacktrace

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo.
    echo ================================================
    echo   构建成功！
    echo   APK 位置：
    echo   %CD%\app\build\outputs\apk\debug\app-debug.apk
    echo ================================================
) else (
    echo.
    echo ================================================
    echo   构建失败，请检查上面的错误信息
    echo ================================================
)

pause
