/** =============================================================================================================== *
 * Copyright 2020 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleThreadFactory implements ThreadFactory {
	private final AtomicInteger cThreadCount = new AtomicInteger(1);
	private final String name;

	public SimpleThreadFactory(String name) {
		this.name = name;
	}

	public Thread newThread(Runnable r) {
		return new Thread(r, name + "-Thread-" + cThreadCount.getAndIncrement());
	}
}
