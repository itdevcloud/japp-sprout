call mvn install:install-file -DlocalRepositoryPath=${project.basedir}/../japp-sprout-spring-boot/japp-spring-boot-api-core/local-repo/ -Dfile=${project.basedir}/target/japp-se-common-0.1.0.jar  -DpomFile=${project.basedir}/pom.xml -DcreateChecksum=true
call mvn install:install-file -DlocalRepositoryPath=${project.basedir}/../japp-sprout-spring-boot/japp-spring-boot-api-app/local-repo/ -Dfile=${project.basedir}/target/japp-se-common-0.1.0.jar  -DpomFile=${project.basedir}/pom.xml -DcreateChecksum=true
