@echo off
title Customer Lifecycle Test
echo ==============================================
echo Banking API - CUSTOMER LIFECYCLE TEST
echo ==============================================
call mvn clean test -Dtest=CustomerLifecycleTest
echo.
echo Reports are under target\surefire-reports
pause
