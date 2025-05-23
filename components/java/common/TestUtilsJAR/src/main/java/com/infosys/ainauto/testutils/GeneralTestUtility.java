/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.testutils;

import java.security.SecureRandom;

public class GeneralTestUtility {

	public static String generateRandomNumber() {
		return String.valueOf(Math.abs((new SecureRandom().nextInt())));
	}

	public static void waitForSeconds(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
		}
	}

}
