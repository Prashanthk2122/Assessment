@echo off
title Banking API Automation Assessment
echo ==============================================
echo Banking API Automation Assessment - ALL TESTS
echo ==============================================
call mvn clean test
echo.
echo Reports are under target\surefire-reports
pause
