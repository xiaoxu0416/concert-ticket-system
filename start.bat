@echo off
setlocal

:: ============================================================
:: Concert Ticket System - One-Click Build and Start
:: Backend:  Spring Boot (port 8080, context /api)
:: Frontend: Vue 2 + Element UI (port 8081)
:: ============================================================

set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%concert-ticket-server"
set "FRONTEND_DIR=%PROJECT_ROOT%concert-ticket-front\concert-ticket-vue"
set "JAR_NAME=concert-ticket-server-1.0-SNAPSHOT.jar"
set "JAR_PATH=%BACKEND_DIR%\target\%JAR_NAME%"
set "BACKEND_PORT=8080"
set "FRONTEND_PORT=8081"

echo ============================================================
echo  Concert Ticket System - One-Click Build and Start
echo ============================================================
echo.
echo  Project Root : %PROJECT_ROOT%
echo  Backend Dir  : %BACKEND_DIR%
echo  Frontend Dir : %FRONTEND_DIR%
echo.

:: ============================================================
:: PHASE 1: Environment Check
:: ============================================================
echo ------------------------------------------------------------
echo  PHASE 1: Environment Check
echo ------------------------------------------------------------
echo.

:: Check Java
echo [1/4] Checking Java ...
where java >nul 2>nul
if errorlevel 1 goto :err_no_java
echo       OK: Java found
echo.

:: Check Maven
echo [2/4] Checking Maven ...
where mvn >nul 2>nul
if errorlevel 1 goto :err_no_mvn
echo       OK: Maven found
echo.

:: Check Node.js
echo [3/4] Checking Node.js ...
where node >nul 2>nul
if errorlevel 1 goto :err_no_node
echo       OK: Node.js found
echo.

:: Check npm
echo [4/4] Checking npm ...
where npm >nul 2>nul
if errorlevel 1 goto :err_no_npm
echo       OK: npm found
echo.

:: ============================================================
:: PHASE 2: Backend Build
:: ============================================================
echo ------------------------------------------------------------
echo  PHASE 2: Backend Build
echo ------------------------------------------------------------
echo.

:: Check if JAR already exists
if exist "%JAR_PATH%" goto :skip_backend_build

echo [BUILD] Compiling backend with Maven (this may take a few minutes) ...
echo.
cd /d "%BACKEND_DIR%"
mvn clean package -DskipTests
if errorlevel 1 goto :err_mvn_build

echo.
echo       OK: Backend build successful
echo.
goto :after_backend_build

:skip_backend_build
echo [BUILD] JAR already exists, skipping build.
echo         %JAR_PATH%
echo         (Delete the target folder to force rebuild)
echo.

:after_backend_build

:: Verify JAR exists after build
if not exist "%JAR_PATH%" goto :err_jar_not_found

:: ============================================================
:: PHASE 3: Frontend Dependencies
:: ============================================================
echo ------------------------------------------------------------
echo  PHASE 3: Frontend Dependencies
echo ------------------------------------------------------------
echo.

:: Check if node_modules already exists
if exist "%FRONTEND_DIR%\node_modules" goto :skip_npm_install

echo [NPM] Installing frontend dependencies (taobao mirror) ...
echo.
cd /d "%FRONTEND_DIR%"
npm install --registry=https://registry.npmmirror.com
if errorlevel 1 goto :err_npm_install

echo.
echo       OK: npm install successful
echo.
goto :after_npm_install

:skip_npm_install
echo [NPM] node_modules already exists, skipping npm install.
echo       (Delete node_modules folder to force reinstall)
echo.

:after_npm_install

:: ============================================================
:: PHASE 4: Start Services
:: ============================================================
echo ------------------------------------------------------------
echo  PHASE 4: Starting Services
echo ------------------------------------------------------------
echo.

:: Start Backend
echo [START] Launching backend (Spring Boot) ...
start "Backend-SpringBoot" cmd /k "cd /d %BACKEND_DIR% & java -jar target\%JAR_NAME%"
echo       Backend starting in new window ...
echo.

:: Wait a moment for backend to begin startup
echo [WAIT] Waiting 5 seconds for backend to initialize ...
timeout /t 5 /nobreak >nul
echo.

:: Start Frontend
echo [START] Launching frontend (Vue Dev Server) ...
start "Frontend-Vue" cmd /k "cd /d %FRONTEND_DIR% & npm run serve"
echo       Frontend starting in new window ...
echo.

:: ============================================================
:: Success Summary
:: ============================================================
echo ============================================================
echo  ALL SERVICES STARTED!
echo ============================================================
echo.
echo  Backend  : http://localhost:%BACKEND_PORT%/api
echo  Frontend : http://localhost:%FRONTEND_PORT%
echo.
echo  Test Accounts:
echo  +-----------+-----------+-----------+
echo  ^| Role      ^| Username  ^| Password  ^|
echo  +-----------+-----------+-----------+
echo  ^| Admin     ^| admin     ^| admin123  ^|
echo  ^| User      ^| zhangsan  ^| 123456    ^|
echo  ^| User      ^| lisi      ^| 123456    ^|
echo  ^| User      ^| wangwu    ^| 123456    ^|
echo  +-----------+-----------+-----------+
echo.
echo  NOTE: Make sure MySQL and Redis and RabbitMQ are running!
echo  NOTE: Run init-database.bat first if database is not initialized.
echo.
goto :end

:: ============================================================
:: Error Handlers
:: ============================================================

:err_no_java
echo.
echo  [ERROR] Java not found.
echo          Please install JDK 11+ and add it to your system PATH.
echo          Download: https://adoptium.net/
goto :end

:err_no_mvn
echo.
echo  [ERROR] Maven not found.
echo          Please install Apache Maven and add it to your system PATH.
echo          Download: https://maven.apache.org/download.cgi
echo          After install, make sure 'mvn' command works in a new terminal.
goto :end

:err_no_node
echo.
echo  [ERROR] Node.js not found.
echo          Please install Node.js (v16+) and add it to your system PATH.
echo          Download: https://nodejs.org/
goto :end

:err_no_npm
echo.
echo  [ERROR] npm not found.
echo          npm should come with Node.js. Please reinstall Node.js.
echo          Download: https://nodejs.org/
goto :end

:err_mvn_build
echo.
echo  [ERROR] Maven build failed.
echo          Please check:
echo          1. The error messages above for compilation errors
echo          2. Your pom.xml dependencies are correct
echo          3. Your Maven settings.xml has proper mirror config
echo          4. Try running manually: cd concert-ticket-server ^&^& mvn clean package -DskipTests
goto :end

:err_jar_not_found
echo.
echo  [ERROR] JAR file not found after build: %JAR_PATH%
echo          The build may have produced a differently named JAR.
echo          Check the target directory: %BACKEND_DIR%\target\
goto :end

:err_npm_install
echo.
echo  [ERROR] npm install failed.
echo          Please check:
echo          1. The error messages above
echo          2. Try running manually: cd concert-ticket-front\concert-ticket-vue ^&^& npm install
echo          3. Try clearing npm cache: npm cache clean --force
goto :end

:: ============================================================
:: End
:: ============================================================
:end
echo.
pause
endlocal
