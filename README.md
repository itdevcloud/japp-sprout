## Java Application Sprout Project
Description: TBA.

## You`ll need
- JDK 11
- Maven 3.5+ (https://maven.apache.org/)

## Setup
- Install JDK 11+
- Install Maven 3.5+ (https://maven.apache.org/)
- install eclipse (or other IDE)
- install tomcat 9+ (or other server)
	1. Download and install Tomcat (currently 9.0.x)
	2. copy keystore.jks into $TOMCAT_HOME\conf folder
	3. Add following to catalina.bat file under $TOMCAT_HOME\bin folder 
		consider add it after set "JAVA_OPTS=%JAVA_OPTS% ...." command around 213
	
		rem ***JAPP***
		set "JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=default -Djapp.deployment.root.dir=$TOMCAT_HOME\webapps\ROOT -Djapp.log.dir=$TOMCAT_HOME\logs\japp -Dhttps.proxySet=true -Dhttps.proxyHost=x.x.x.x -Dhttps.proxyPort=xxxx  -Dhttp.proxyHost=x.x.x.x -Dhttp.proxyPort=xxxx"

	4. Add following to tomcat-users.xml file under $TOMCAT_HOME\conf folder 
	    <role rolename="manager-gui"/>
		<role rolename="manager-script"/>
		<user username="admin" password="password" roles="manager-gui,manager-script" />
		
	5. Add/Update following section to server.xml under $TOMCAT_HOME\conf folder
		  <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"
               redirectPort="8443" />
          <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" 
	           relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"
	           maxThreads="150" scheme="https" secure="true"
	           keystoreFile="conf/keystore.jks" keystorePass="123456"
              clientAuth="false" sslProtocol="TLS" />
    MAVEN          
	6. Add following section to settings.xml under $MAVEN_HOME\conf folder
		<server>
			<id>TomcatServer</id>
			<username>admin</username>
			<password>password</password>
		</server>
 		<server>
			<id>ossrh</id>
			<username>your user name</username>
			<password>your password</password>
		</server>
  
- import GIT japp-sprout prject into eclipse (or other IDE)

- install GnuPG
  migrate gpg keys - copy exisitng gnupg folder into new computer folder (e.g. windows: C:\Users\youruser\AppData\Roaming\gnupg) 

##  Deploy to the Central Repository

- Build and deploy to ossrh local stage:
      e.g. for jaspp-se-common.jar
      run ossrh-deploy.bat (in japp-se-common folder)
      login to https://oss.sonatype.org/#stagingRepositories to validate it.
      
- release to repository: (If you are using autoReleaseAfterClose set to false you or you are using the default Maven deploy plugin) 
  * mvn nexus-staging:release
  
## build and deploy 
- refer to readme.me in each module / sub project

## Install jar to local-repo example:
1. copy jar and pom/xml into ${project.basedir}/lib folder
2.mvn install:install-file -DlocalRepositoryPath=${project.basedir}/local-repo/ -Dfile=${project.basedir}/lib/<jar-name>  -DpomFile=${project.basedir}/lib/pom.xml -DcreateChecksum=true
