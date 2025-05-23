# To deploy the application to an external Tomcat Server, follow below steps.
 
1. If config file changed or first time deployment then using beyond compare tool move config files and place it outside tomcat folder. 
   And add the config folder path to common.loader property in apache-tomcat-9.0.24\conf\catalina.properties.
   e.g.,
	Before -
		common.loader="${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar"
	After - 
		common.loader="${catalina.base}/lib","${catalina.base}/lib/*.jar","${catalina.home}/lib","${catalina.home}/lib/*.jar","C:/WorkArea/EmailWorkbench/DocwbService/config"


2. Deploy the updated WAR file to Tomcat Server.
