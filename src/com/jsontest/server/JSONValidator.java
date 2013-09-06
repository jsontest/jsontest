package com.jsontest.server;

import org.json.*;

/**
 * Validates JSON, from the org.json reference parser.
 * 
 * @author Vinny
 *
 */
public class JSONValidator {
	
	/**
	 * Variables used when validation is a success.
	 */
	
	/**
	 * Whether the validation was successful or not.
	 */
	boolean validate;
	
	/**
	 * If the validation was successful, the number of 
	 * first level keys within the JSON object.
	 */
	int size;
	
	/**
	 * If the validation was successful, whether or not 
	 * the JSON object is empty or not (it is not empty 
	 * if it has at least 1 key:value pair).
	 */
	boolean empty;
	
	/**
	 * Whether the JSON text was interpreted as a JSONObject 
	 * or as a JSONArray. Valid values are either "object" 
	 * or "array".
	 */
	String object_or_array;
	
	/**
	 * Error message from parser. If parsing and validation 
	 * failed, a reason why it failed. Taken from the parser.
	 */
	String error;
	
	/**
	 * If validation failed, a courtesy message letting the 
	 * user know where the parsing failure message came from.
	 */
	String error_info;
	
	/**
	 * How long the parsing took. Counted in nanoseconds.
	 */
	long parse_time_nanoseconds;
	
	
	public JSONValidator(String incoming_json) {
		error_info = "This error came from the org.json reference parser.";
		
		incoming_json = incoming_json.trim();
		
		//Have to watch for JSONExceptions, which will occur if invalid 
		//JSON is passed in.
		try {
			/**
			 * We have to figure out whether the user passed in a 
			 * JSONObject or JSONArray.
			 */
			if (incoming_json.startsWith("[")) {
				//If the JSON text starts with a [ character, then 
				//it's an array.
				object_or_array = "array";
				
				//Parse and count the time required to parse.
				long parse_time_ns_start = System.nanoTime();
				JSONArray incoming_json_array = new JSONArray(incoming_json);
				long parse_time_ns_end = System.nanoTime();
				
				//Count how long it took to parse.
				parse_time_nanoseconds = parse_time_ns_end - parse_time_ns_start;
				
				//Count the number of values in the array.
				size = incoming_json_array.length();
			}//end if json text looks to be an array
			else {
				//If it's not an array, then it's an object.
				object_or_array = "object";
				
				long parse_time_ns_start = System.nanoTime();
				JSONObject incoming_json_object = new JSONObject(incoming_json);
				long parse_time_ns_end = System.nanoTime();
				
				//Count how long it took to parse.
				parse_time_nanoseconds = parse_time_ns_end - parse_time_ns_start;
				
				/**
				 * Now we figure out the size of the JSON object.
				 */
				size = incoming_json_object.length();
			}//end else, incoming json is an object
			
			/**
			 * If we've managed to get to this point, the parser was able 
			 * to parse the JSON string into a JSON object. Therefore, validate 
			 * should be true.
			 */
			validate = true;
			
			/**
			 * If the JSON object contains key:value pairs, note that it is not 
			 * empty. If the object does not contain any pairs, note that it is empty.
			 */
			if (size > 0) {
				empty = false;
			}
			else {
				empty = true;
			}
		}//end try covering possible JSONExceptions
		catch (JSONException e) {
			/**
			 * A JSONException occurred, which means that the JSON string 
			 * had something wrong with it. Note the error, and note that validation 
			 * failed.
			 */
			validate = false;
			error = e.getMessage();
		}
		
	}//end constructor JSONValidator(String incoming_json)
	
}//end file
