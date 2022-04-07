## Java Application Sprout Project
Description: TBA.

## Setup
- refer to japp-sprout/readme.md

## build 
- refer to readme.md in each module / sub project

## update spring boot version 
- change version in pom
	<artifactId>spring-boot-dependencies</artifactId>
	<version>2.6.6</version>
- change plugin-version if necessary, e.g.
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
				<version>2.6.6</version>
			</plugin>
- run install.bat (send to .m2 repository)
