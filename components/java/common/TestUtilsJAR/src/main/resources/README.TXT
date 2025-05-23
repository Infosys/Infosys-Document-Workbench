

For using HttpClientBase.java in your Java project, please add following dependencies in your project's pom.xml
The reason is that they are NOT included in common-utils-x.x.x.jar

	<dependency>
		<groupId>org.apache.httpcomponents</groupId>
		<artifactId>httpmime</artifactId>
		<version>4.5.2</version>
		<!-- <scope>provided</scope> --> <!-- Not needed to be added to fat JAR -->
		<exclusions>
			<exclusion>
				<artifactId>commons-logging</artifactId>
				<groupId>commons-logging</groupId>
			</exclusion>
		</exclusions>
	</dependency>