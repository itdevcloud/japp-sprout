echo 'install to PiscesJappCore.............'
cd ..\PiscesJappCore
copy /y ..\PiscesJCommon\target\pisces-jcommon-*.jar .\lib\jcommon\*.jar
copy /y ..\PiscesJCommon\pom.xml .\lib\jcommon\pom.xml
call install-jcommon.bat
cd ..\PiscesJCommon
