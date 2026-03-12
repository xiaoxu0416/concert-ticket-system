@echo off
setlocal

:: ============================================================
:: Concert Ticket System - Database Init Script
:: Database: concert_ticket
:: ============================================================

set "PROJECT_ROOT=%~dp0"
set "SQL_FILE=%PROJECT_ROOT%database\init.sql"
set "MYSQL_HOST=localhost"
set "MYSQL_PORT=3306"
set "MYSQL_USER=root"
set "MYSQL_PASS=123456"
set "DB_NAME=concert_ticket"

echo ============================================================
echo  Concert Ticket System - Database Initialization
echo ============================================================
echo.

:: -----------------------------------------------------------
:: Step 1: Check SQL file exists
:: -----------------------------------------------------------
echo [1/5] Checking SQL file ...
if not exist "%SQL_FILE%" goto :err_no_sql
echo       OK: %SQL_FILE%
echo.

:: -----------------------------------------------------------
:: Step 2: Check mysql command available
:: -----------------------------------------------------------
echo [2/5] Checking MySQL client ...
where mysql >nul 2>nul
if errorlevel 1 goto :err_no_mysql
echo       OK: mysql command found
echo.

:: -----------------------------------------------------------
:: Step 3: Test MySQL connection
:: -----------------------------------------------------------
echo [3/5] Testing MySQL connection (%MYSQL_USER%@%MYSQL_HOST%:%MYSQL_PORT%) ...
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -e "SELECT 1;" >nul 2>nul
if errorlevel 1 goto :err_conn_fail
echo       OK: MySQL connection successful
echo.

:: -----------------------------------------------------------
:: Step 4: Execute SQL init script
:: -----------------------------------------------------------
echo [4/5] Executing init SQL script ...
echo       This will DROP and recreate all tables in [%DB_NAME%].
echo.
set /p "CONFIRM=      Are you sure? (Y/N): "
if /i not "%CONFIRM%"=="Y" goto :err_cancelled

mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% < "%SQL_FILE%"
if errorlevel 1 goto :err_sql_exec
echo       OK: SQL script executed successfully
echo.

:: -----------------------------------------------------------
:: Step 5: Verify database and tables
:: -----------------------------------------------------------
echo [5/5] Verifying database and tables ...

:: Verify database exists
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% -e "USE %DB_NAME%;" >nul 2>nul
if errorlevel 1 goto :err_verify_db

:: Verify table: sys_user
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM sys_user LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_sys_user
echo       OK: sys_user

:: Verify table: concert_info
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM concert_info LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_concert_info
echo       OK: concert_info

:: Verify table: concert_session
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM concert_session LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_concert_session
echo       OK: concert_session

:: Verify table: concert_order
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM concert_order LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_concert_order
echo       OK: concert_order

:: Verify table: concert_stock_log
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM concert_stock_log LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_concert_stock_log
echo       OK: concert_stock_log

:: Verify table: seat_area
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM seat_area LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_other
echo       OK: seat_area

:: Verify table: announcement
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM announcement LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_other
echo       OK: announcement

:: Verify table: favorite
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM favorite LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_other
echo       OK: favorite

:: Verify table: review
mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -e "SELECT 1 FROM review LIMIT 1;" >nul 2>nul
if errorlevel 1 goto :err_verify_table_other
echo       OK: review

:: Count total tables
for /f "tokens=*" %%A in ('mysql -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PASS% %DB_NAME% -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='%DB_NAME%';" 2^>nul') do set "TABLE_COUNT=%%A"
echo.
echo       Total tables in [%DB_NAME%]: %TABLE_COUNT%
echo.

:: -----------------------------------------------------------
:: Success
:: -----------------------------------------------------------
echo ============================================================
echo  SUCCESS! Database [%DB_NAME%] initialized.
echo ============================================================
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
goto :end

:: ============================================================
:: Error Handlers
:: ============================================================

:err_no_sql
echo.
echo  [ERROR] SQL file not found: %SQL_FILE%
echo          Please make sure the database\init.sql file exists.
goto :end

:err_no_mysql
echo.
echo  [ERROR] mysql command not found.
echo          Please check:
echo          1. MySQL is installed
echo          2. MySQL bin directory is in your system PATH
echo             (e.g. C:\Program Files\MySQL\MySQL Server 8.0\bin)
echo          3. Try restarting your terminal after adding to PATH
goto :end

:err_conn_fail
echo.
echo  [ERROR] Cannot connect to MySQL server.
echo          Please check:
echo          1. MySQL service is running (net start mysql / services.msc)
echo          2. Host and port are correct: %MYSQL_HOST%:%MYSQL_PORT%
echo          3. Username and password are correct: %MYSQL_USER% / %MYSQL_PASS%
echo          4. Firewall is not blocking port %MYSQL_PORT%
echo          5. MySQL allows remote connections (if not localhost)
goto :end

:err_cancelled
echo.
echo  [INFO] Operation cancelled by user.
goto :end

:err_sql_exec
echo.
echo  [ERROR] Failed to execute SQL script.
echo          Please check the SQL file for syntax errors: %SQL_FILE%
goto :end

:err_verify_db
echo.
echo  [ERROR] Database [%DB_NAME%] does not exist after init.
echo          SQL script may have failed silently.
goto :end

:err_verify_table_sys_user
echo.
echo  [ERROR] Table [sys_user] verification failed.
goto :end

:err_verify_table_concert_info
echo.
echo  [ERROR] Table [concert_info] verification failed.
goto :end

:err_verify_table_concert_session
echo.
echo  [ERROR] Table [concert_session] verification failed.
goto :end

:err_verify_table_concert_order
echo.
echo  [ERROR] Table [concert_order] verification failed.
goto :end

:err_verify_table_concert_stock_log
echo.
echo  [ERROR] Table [concert_stock_log] verification failed.
goto :end

:err_verify_table_other
echo.
echo  [ERROR] Table verification failed. Please check the SQL script.
goto :end

:: ============================================================
:: End
:: ============================================================
:end
echo.
pause
endlocal
