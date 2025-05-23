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
# Execute following for JDK9 onwards use below 
	java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED -jar docwb-rules.war 
===============================================================================

# To deploy the application to an external Tomcat Server, follow below steps.
 
1. If config file changed or first time deployment then using beyond compare tool move config files and place it outside tomcat folder. 
   And add the config folder path to common.loader property in apache-tomcat-9.0.24\conf\catalina.properties. 

2. Remove/comment the following properties in application.properties file (WAR file or source code).
 		
 		server.port=<tomcatPort>
		server.servlet.context-path=<contextPath>
		
3. Add the following properties in application.properties file (WAR file or source code).

		spring.boot.admin.client.instance.service-base-url=http://<tomcatHost>:<tomcatPort>

4. Deploy the updated WAR file to Tomcat Server.
