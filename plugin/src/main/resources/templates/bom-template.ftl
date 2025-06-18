<?xml version='1.0' encoding='UTF-8'?>
<project xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns='http://maven.apache.org/POM/4.0.0' xsi:schemaLocation='http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd'>
	<modelVersion>4.0.0</modelVersion>
	<groupId>${library.group}</groupId>
	<artifactId>${library.module}</artifactId>
	<version>${library.version}</version>
	<packaging>pom</packaging>
	<dependencyManagement>
		<dependencies>
            <#list dependencies as dependency>
			<dependency>
				<groupId>${dependency.group}</groupId>
				<artifactId>${dependency.module}</artifactId>
				<version>${dependency.version}</version>
			</dependency>
            </#list>
		</dependencies>
	</dependencyManagement>
</project>
