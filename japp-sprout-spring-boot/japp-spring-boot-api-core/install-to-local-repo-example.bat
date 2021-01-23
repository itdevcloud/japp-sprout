echo 'install to abc.jar to local repo.............'
cd ..\projectA\
copy /y ..\Abc\target\abc*.jar .\lib\abc\abc.jar
copy /y ..\abc\pom.xml .\lib\abc\pom.xml
mvn install:install-file -DlocalRepositoryPath=${project.basedir}/local-repo/ -Dfile=${project.basedir}/lib/jcommon/pisces-jcommon-1.0.0.jar  -DpomFile=${project.basedir}/lib/jcommon/pom.xml -DcreateChecksum=true
