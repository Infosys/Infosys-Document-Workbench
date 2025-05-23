==========================================================================
		INSTRUCTIONS FOR INSTALLING/UNINSTALLING EXCHANGE ONPREM ADAPTER
==========================================================================
# To install the Exchange onprem Adapter JAR in local maven repository #
# Run the below command from same location where JAR file is present #
# Confirm that it returns BUILD SUCCESS # 

mvn install:install-file -Dfile=data-source-exchange-onprem-basic-0.0.1-SNAPSHOT.jar -DgroupId=com.infosys.ainauto -DartifactId=data-source-exchange-onprem-basic -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar

# To check if the  Exchange onprem Adapter JAR was successfully installed #
# Run the below command #
# Confirm that it returns BUILD SUCCESS # 

mvn dependency:get -Dartifact=com.infosys.ainauto:data-source-exchange-onprem-basic:0.0.1-SNAPSHOT -o -DrepoUrl=file://~/.m2/repository

# To uninstall the Exchange onprem JAR from local maven repository #
# Delete the following folder #

C:\Users\UserId\.m2\repository\com\infosys\ainauto\data-source-exchange-onprem-basic