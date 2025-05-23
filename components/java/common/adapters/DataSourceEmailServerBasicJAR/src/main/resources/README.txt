==========================================================================
		INSTRUCTIONS FOR INSTALLING/UNINSTALLING EMAIL ADAPTER
==========================================================================
# To install the Email Adapter JAR in local maven repository #
# Run the below command from same location where JAR file is present #
# Confirm that it returns BUILD SUCCESS # 

mvn install:install-file -Dfile=email-adapter-0.0.2-SNAPSHOT.jar -DgroupId=com.infosys.ainauto -DartifactId=email-adapter -Dversion=0.0.2-SNAPSHOT -Dpackaging=jar

# To check if the Email Adapter JAR was successfully installed #
# Run the below command #
# Confirm that it returns BUILD SUCCESS # 

mvn dependency:get -Dartifact=com.infosys.ainauto:email-adapter:0.0.2-SNAPSHOT -o -DrepoUrl=file://~/.m2/repository

# To uninstall the Email Adapter JAR from local maven repository #
# Delete the following folder #

C:\Users\user\.m2\repository\com\infosys\ainauto\email-adapter 