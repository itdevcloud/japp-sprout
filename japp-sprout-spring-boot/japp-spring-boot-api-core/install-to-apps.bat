echo 'install to StartkitSampleApp.............'
cd ..\StartkitSampleApp
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore

echo 'install to SmimApp.............'
cd ..\SmimApp
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore

echo 'install to SimpleEmailService.............'
cd ..\SimpleEmailService
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore

echo 'install to cnass.............'
cd ..\cnass
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore

echo 'install to TRACS.............'
cd ..\TRACS
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore

echo 'install to TRACS_V2.............'
cd ..\TRACS_V2
copy /y ..\StartkitCore\target\startkit*.jar .\lib\startkit\*.jar
copy /y ..\StartkitCore\pom.xml .\lib\startkit\pom.xml
call install-startkit.bat
cd ..\StartkitCore