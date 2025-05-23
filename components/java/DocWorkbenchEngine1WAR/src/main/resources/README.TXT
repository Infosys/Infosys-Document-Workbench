# To run the application as a standalone application, follow below steps.

1. Navigate to the path where the application file is present.

2. Execute the following java command (Windows/Linux).

		java -Xms256m -Xmx1024m -jar <jar_or_war_file_name>

# -Xms: Defines the initial and minimum heap size for the app in the JVM
# -Xmx: Defines the maximum heap size for the app in the JVM

# To get default values configured for your system, run below command
		java -verbose:sizes -version

# Look for following property values. E.g. for 8GB RAM Windows 10 desktop is given below 
> ...
  -Xms8M          initial memory size
  ...
  -Xmx512M        memory maximum
  ...

===============================================================================

# To deploy the application to an external Tomcat Server, follow below steps.
 
1. Remove/comment the following properties in application.properties file (WAR file or source code).
 		
 		server.port=<tomcatPort>
		server.servlet.context-path=<contextPath>
		
2. Add the following properties in application.properties file (WAR file or source code).

		spring.boot.admin.client.instance.service-base-url=http://<tomcatHost>:<tomcatPort>

3. Deploy the updated WAR file to Tomcat Server.