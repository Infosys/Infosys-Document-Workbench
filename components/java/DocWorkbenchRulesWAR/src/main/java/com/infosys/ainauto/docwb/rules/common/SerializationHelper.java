/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */
 
package com.infosys.ainauto.docwb.rules.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.drools.core.marshalling.impl.ProtobufMarshaller;
import org.drools.core.util.DroolsStreamUtils;
import org.kie.api.KieBase;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.time.SessionClock;
import org.kie.internal.marshalling.MarshallerFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * Marshalling helper class to perform serialize/de-serialize a given object
 */
public class SerializationHelper {
	public static <T> T serializeObject(T obj) throws IOException, ClassNotFoundException {
		return serializeObject(obj, null);
	}

	@SuppressWarnings("unchecked")
	public static <T> T serializeObject(T obj, ClassLoader classLoader) throws IOException, ClassNotFoundException {
		return (T) DroolsStreamUtils.streamIn(DroolsStreamUtils.streamOut(obj), classLoader);
	}

	public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(KieSession ksession, boolean dispose)
			throws Exception {
		return getSerialisedStatefulKnowledgeSession(ksession, dispose, true);

	}

	public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(KieSession ksession, boolean dispose,
			boolean testRoundTrip) throws Exception {
		return getSerialisedStatefulKnowledgeSession(ksession, ksession.getKieBase(), dispose, testRoundTrip);
	}

	public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(KieSession ksession, KieBase kbase,
			boolean dispose) throws Exception {
		return getSerialisedStatefulKnowledgeSession(ksession, kbase, dispose, true);
	}

	public static StatefulKnowledgeSession getSerialisedStatefulKnowledgeSession(KieSession ksession, KieBase kbase,
			boolean dispose, boolean testRoundTrip) throws Exception {
		ProtobufMarshaller marshaller = (ProtobufMarshaller) MarshallerFactory.newMarshaller(kbase,
				(ObjectMarshallingStrategy[]) ksession.getEnvironment()
						.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
		long time = ksession.<SessionClock>getSessionClock().getCurrentTime();
		// make sure globas are in the environment of the session
		ksession.getEnvironment().set(EnvironmentName.GLOBALS, ksession.getGlobals());

		// Serialize object
		final byte[] b1;
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			marshaller.marshall(bos, ksession, time);
			b1 = bos.toByteArray();
			bos.close();
		}

		// Deserialize object
		StatefulKnowledgeSession ksession2;
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(b1);
			ksession2 = marshaller.unmarshall(bais, ksession.getSessionConfiguration(), ksession.getEnvironment());
			bais.close();
		}

		if (testRoundTrip) {
			// for now, we can ensure the IDs will match because queries are creating
			// untraceable fact handles at the moment
			// int previous_id =
			// ((StatefulKnowledgeSessionImpl)ksession).session.getFactHandleFactory().getId();
			// long previous_recency =
			// ((StatefulKnowledgeSessionImpl)ksession).session.getFactHandleFactory().getRecency();
			// int current_id =
			// ((StatefulKnowledgeSessionImpl)ksession2).session.getFactHandleFactory().getId();
			// long current_recency =
			// ((StatefulKnowledgeSessionImpl)ksession2).session.getFactHandleFactory().getRecency();
			// ((StatefulKnowledgeSessionImpl)ksession2).session.getFactHandleFactory().clear(
			// previous_id, previous_recency );

			// Reserialize and check that byte arrays are the same
			final byte[] b2;
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				marshaller.marshall(bos, ksession2, time);
				b2 = bos.toByteArray();
				bos.close();
			}

			// bytes should be the same.
			if (!areByteArraysEqual(b1, b2)) {
				// throw new IllegalArgumentException( "byte streams for serialisation test are
				// not equal" );
			}

			// ((StatefulKnowledgeSessionImpl)
			// ksession2).session.getFactHandleFactory().clear( current_id, current_recency
			// );
			// ((StatefulKnowledgeSessionImpl) ksession2).session.setGlobalResolver(
			// ((StatefulKnowledgeSessionImpl) ksession).session.getGlobalResolver() );

		}

		if (dispose) {
			ksession.dispose();
		}

		return ksession2;
	}

	private static boolean areByteArraysEqual(byte[] b1, byte[] b2) {

		if (b1.length != b2.length) {
//			System.out.println("Different length: b1=" + b1.length + " b2=" + b2.length);
			return false;
		}

		// System.out.println( "b1" );
		// for ( int i = 0, length = b1.length; i < length; i++ ) {
		// if ( i == 81 ) {
		// System.out.print( "!" );
		// }
		// System.out.print( b1[i] );
		// if ( i == 83 ) {
		// System.out.print( "!" );
		// }
		// }
		//
		// System.out.println( "\nb2" );
		// for ( int i = 0, length = b2.length; i < length; i++ ) {
		// if ( i == 81 ) {
		// System.out.print( "!" );
		// }
		// System.out.print( b2[i] );
		// if ( i == 83 ) {
		// System.out.print( "!" );
		// }
		// }

		boolean result = true;
		for (int i = 0, length = b1.length; i < length; i++) {
			if (b1[i] != b2[i]) {
//				System.out.println("Difference at " + i + ": [" + b1[i] + "] != [" + b2[i] + "]");
				result = false;
			}
		}

		return result;
	}

}
