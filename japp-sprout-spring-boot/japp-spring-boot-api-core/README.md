# STARTKIT 
## You`ll need
- JDK 1.8
- Maven 3.5+ (https://maven.apache.org/)

## Setup
- Install JDK 1.8+
- Install Maven 3.5+ (https://maven.apache.org/)
- Install Application Server of your choice(e.g. Weblogic, Jboss, Tomcat 9.0.12+)
- Setup for build and deploy (optional, application server dependent. Use Tomcat 9.0.12 as an example)
	1. Download and install latest Tomcat (currently 9.0.12)
	2. Add following to catalina.bat file under $TOMCAT_HOME\bin folder 
		consider add it after set "JAVA_OPTS=%JAVA_OPTS% ...." command around 209~213
	
	rem ***Startkit***
	set "JAVA_OPTS=%JAVA_OPTS% -Dspring.profiles.active=default -Dstartkit.deployment.root.dir=C:\Tomcat\apache-tomcat-9.0.12\webapps\ROOT -Dstartkit.log.dir=C:\Tomcat\apache-tomcat-9.0.12\logs\startkit -Dhttps.proxySet=true -Dhttps.proxyHost=204.40.130.129 -Dhttps.proxyPort=3128  -Dhttp.proxyHost=204.40.130.129 -Dhttp.proxyPort=3128"

	3. Add following to tomcat-users.xml file under $TOMCAT_HOME\conf folder 
	    <role rolename="manager-gui"/>
		<role rolename="manager-script"/>
		<user username="admin" password="password" roles="manager-gui,manager-script" />
		
	4. Add/Update following section to server.xml under $TOMCAT_HOME\conf folder
		  <Connector port="8080" protocol="HTTP/1.1"
               connectionTimeout="20000" relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"
               redirectPort="8443" />
          <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true" 
	           relaxedQueryChars="[]|{}^&#x5c;&#x60;&quot;&lt;&gt;"
	           maxThreads="150" scheme="https" secure="true"
	           keystoreFile="conf/keystore.jks" keystorePass="123456"
              clientAuth="false" sslProtocol="TLS" />
              
	5. Add following section to settings.xml under $MAVEN_HOME\conf folder
		<server>
			<id>TomcatServer</id>
			<username>admin</username>
			<password>password</password>
		</server>
	
- Download Project Source Code


## Build & Run
 - start tomcat server
 - go to project root folder
 - run api-deploy.bat
   
## CI-CD
LTC.RUSSB.PaaSNativeAppStartKit-Core-Maven-CI
