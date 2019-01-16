cls
@echo off
CLS
color 0a
cd/D %~dp0
 
Title 工程发布-WALK框架
REM =========工程定义开始=========
set p1=walk-tools
set p2=walk-data
set p3=walk-redis
set p4=walk-cache
set p5=walk-batis
set p6=walk-mq
set p7=walk-shiro
set p8=walk-fusioncharts
set p9=walk-base
set p10=walk-restful
set p11=walk-console
set p12=walk-boot
set p13=walk-boot-plugin
REM =========工程定义结束=========

:cl
echo 工程发布
echo =============================
echo   0、发布所有工程
echo   1、发布%p1%
echo   2、发布%p2%
echo   3、发布%p3%
echo   4、发布%p4%
echo   5、发布%p5%
echo   6、发布%p6%
echo   7、发布%p7%
echo   8、发布%p8%
echo   9、发布%p9%
echo   10、发布%p10%
echo   11、发布%p11%
echo   12、发布%p12%
echo   13、发布%p13%
echo   e、退   出
echo =============================
set /p choice= 请选择数字发布相应的工程，然后按回车: 
echo.
if /i "%choice%"=="" echo.
if /i "%choice%"=="0" goto s0
if /i "%choice%"=="1" goto s1
if /i "%choice%"=="2" goto s2
if /i "%choice%"=="3" goto s3
if /i "%choice%"=="4" goto s4
if /i "%choice%"=="5" goto s5
if /i "%choice%"=="6" goto s6
if /i "%choice%"=="7" goto s7
if /i "%choice%"=="8" goto s8
if /i "%choice%"=="9" goto s9
if /i "%choice%"=="10" goto s10
if /i "%choice%"=="11" goto s11
if /i "%choice%"=="12" goto s12
if /i "%choice%"=="13" goto s13
if /i "%choice%"=="e" goto EX

echo.
echo              选择无效，请重新输入
echo.
echo.
goto cl

:s0
set project=
goto build

:s1
set project=p0%choice%-%p1%:
goto build

:s2
set project=p0%choice%-%p2%:
goto build

:s3
set project=p0%choice%-%p3%:
goto build

:s4
set project=p0%choice%-%p4%:
goto build

:s5
set project=p0%choice%-%p5%:
goto build

:s6
set project=p0%choice%-%p6%:
goto build

:s7
set project=p0%choice%-%p7%:
goto build

:s8
set project=p0%choice%-%p8%:
goto build

:s9
set project=p0%choice%-%p9%:
goto build

:s10
set project=p%choice%-%p10%:
goto build

:s11
set project=p%choice%-%p11%:
goto build

:s12
set project=p%choice%-%p12%:
goto build

:s13
set project=p%choice%-%p13%:
goto build

:EX
exit

:build
if "%project%"=="" echo 开始发布所有工程...
if not "%project%"=="" echo 开始发布%project%...
call gradle/gradlew.bat %project%clean %project%uploadArchives -x test
goto cl