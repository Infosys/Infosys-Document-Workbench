/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import org.everit.json.schema.ValidationException;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

public class JsonSchemaUtil {

	public static String generateSchema(Class<?> className) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
		JsonSchema jsonSchema = generator.generateSchema(className);
		return mapper.writeValueAsString(jsonSchema);
	}

	public static ValidationException validateSchema(String schema, String jsonStr) {
		JSONObject schemaStr = new JSONObject(new JSONTokener(schema));
		JSONObject jsonSubject = new JSONObject(jsonStr);

		Schema schemaLoaded = SchemaLoader.load(schemaStr);
		try {
			schemaLoaded.validate(jsonSubject);
			return null;
		} catch (ValidationException ex) {
			return ex;
		}
	}
}
