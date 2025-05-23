/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.docwb.engine.common;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class TestHelper {

	public static Properties readPropertiesFileFromClassPath(String propertiesFile) throws Exception {
	      Properties properties = new Properties();
	      InputStream input = null;
	      try{
	          ClassLoader classLoader = Thread.currentThread()
	                  .getContextClassLoader();
	          input = classLoader.getResourceAsStream(propertiesFile);

	          properties.load(input);
	      }catch (Throwable th) {
	          throw new Exception("Error while loading properties file", th);
	      }
	      return properties;
	  }
	
	public static String getAbsolutePathOfResourceFile(String resourceFileName) {
		ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
		URL resource = classLoader.getResource(resourceFileName);
		File file = new File(resource.getPath());
		return file.getAbsolutePath();
	}
	
}
