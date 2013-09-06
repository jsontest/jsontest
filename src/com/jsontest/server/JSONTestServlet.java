package com.jsontest.server;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.*;

import org.json.*;

@SuppressWarnings("serial")
/**
 * This servlet manages all JSONTest.com public facing services.
 * 
 * @author Vinny
 *
 */
public class JSONTestServlet extends HttpServlet {
	
	/**
	 * All POST requests are piped to doGet; requests are treated identically 
	 * regardless of whether they are POST or GET requests.
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		doGet(req, resp);	
	}
	
	/**
	 * doGet is responsible for handling all requests.
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		/**
		 * First, we must figure out what service the client is 
		 * requesting. The service can be declared in two ways: 
		 * either as a request parameter ( ?service=something ) 
		 * or as a subdomain ( something.jsontest.com ).
		 * 
		 * We'll check the request parameter first, and if null, 
		 * we'll pull out the subdomain.
		 */
		String service = req.getParameter("service");
		if (service == null) {
			//The service was not specified via the request 
			//parameter. Pull out the service via the subdomain.
			service = req.getRequestURL().toString();
			service = service.substring(service.indexOf("/") + 2, service.indexOf("."));
			System.out.println("Service From Subdomain: " + service);
		}
		else {
			//The service was specified via the request parameter.
			System.out.println("Service From Parameter: " + service);
		}
		service = service.toLowerCase();
		
		
		/**
		 * Retrieve request parameters for services.
		 */
		//Callback for JSONP responses.
		String callback = req.getParameter("callback");
		
		//MIME type
		String mime = req.getParameter("mime");
		
		//Access-Control-Allow-Origin
		String allow_origin = req.getParameter("allow_origin");
		
		/**
		 * The String json_text holds the JSON text that will be 
		 * returned back to the client. JSONObject json represents 
		 * an org.json.JSONObject that can be converted into a 
		 * String and stored into json_text. The services below 
		 * will mostly use the JSONObject to store their 
		 * responses, but occasionally we'll need low-level 
		 * access to the returned json_text, and so services 
		 * will be allowed to store to json_text directly and 
		 * ignore the JSONObject.
		 */
		JSONObject json = new JSONObject();
		String json_text = null;
		
		/**
		 * Start processing services.
		 * We have to enclose in a try block because JSONObject.put()
		 * is declared to be able to throw a JSONException.
		 */
		try {
			if (service.equals("ip")) {
				//Return the client's IP address.
				json.put("ip", req.getRemoteAddr());
			}
			else if (service.equals("date") || service.equals("time")) {
				//Return the current date and time.
				Date current_date = new Date();
				String date = new java.text.SimpleDateFormat("MM-dd-yyyy").format(current_date);
				String time = new java.text.SimpleDateFormat("hh:mm:ss aa").format(current_date);
				
				json.put("date", date);
				json.put("time", time);
				json.put("milliseconds_since_epoch", current_date.getTime());
				
			}
			else if (service.equals("header") || service.equals("headers")) {
				//Return the HTTP request headers we received.
				Enumeration<String> headers = req.getHeaderNames();
				
				//Loop through all headers, add those headers to the 
				//json object.
				while (headers.hasMoreElements()) {
					String header_name = headers.nextElement();
					String header_name_lowercase = header_name.toLowerCase();
					
					/**
					 * We host on Google App Engine, which adds several 
					 * request headers to every request. We don't want 
					 * to return those headers because they're not 
					 * from the client. We'll skip those headers by 
					 * using continue statements, which skip this 
					 * iteration of the loop.
					 */
					if (header_name_lowercase.startsWith("x-zoo")) {
						continue;
					}
					else if (header_name_lowercase.startsWith("x-google")) {
						continue;
					}
					else if (header_name_lowercase.startsWith("x-appengine")) {
						continue;
					}
					
					//Place the header into the json object.
					json.put(header_name, req.getHeader(header_name));
					//@ TODO Handle multiple header values with same name.
				}//end while loop going through headers.
			}//end else if service equals "header" or "headers"
			else if (service.equals("echo")) {
				//The user wants us to echo JSON that we 
				//build from the request URI.
				
				//Retrieve the part of the url after jsontest.com.
				//i.e. if the url was jsontest.com/key/value, this 
				//will pull out /key/value.
				String request_uri = req.getRequestURI().substring(1);
				String[] components = request_uri.split("/");
				
				//Loop through each key/value pair.
				for (int i = 0; i < components.length; i++) {
					String key = components[i];
					String value = "";
					
					//Try to retrieve the value component.
					try {
						value = components[i + 1];
					}
					catch (ArrayIndexOutOfBoundsException e) {
						//If this exception is thrown, that means there are an odd number of tokens
						//in the request url (in other terms, there is a key value specified, but no 
						//value). It's OK, because we'll just put a blank String into the value component.
					}
					
					//Put the key:value component into the JSON object.
					json.put(key, value);
					
					//Increase the i variable, because we're looping through 
					//two at a time.
					i++;
				}//end loop through key/value components in the request URI.
			}//end else if service equals echo.
			else if (service.equals("validate")) {
				//The user wants to validate JSON.
				String incoming_json = req.getParameter("json");
				
				if (incoming_json == null) {
					json.put("error", "You must pass JSON via the ?json= parameter to validate.");
				}
				else {
					//Validate the JSON
					JSONValidator validator = new JSONValidator(incoming_json);
					
					/**
					 * If validation succeeds, note that. If it failed, note 
					 * the error message.
					 */
					json.put("validate", validator.validate);
					
					/**
					 * Whether the incoming JSON was parsed as an object 
					 * or an array.
					 */
					json.put("object_or_array", validator.object_or_array);
					
					if (validator.validate) {
						//The validation was successful.
						json.put("size", validator.size);
						json.put("empty", validator.empty);
						json.put("parse_time_nanoseconds", validator.parse_time_nanoseconds);
					}
					else {
						//Validation failed.
						json.put("error", validator.error);
						json.put("error_info", validator.error_info);
					}
				}//end else (incoming json does not equal null)
			}//end else if service equals "validate"
			else if (service.equals("cookie")) {
				//The user wants a cookie set.
				
				//This sets the cookie.
				Long ms_since_epoch = (new Date()).getTime();
				javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("jsontestdotcom", "ms:" + ms_since_epoch.toString());
				cookie.setMaxAge(60 * 60 * 24 * 7);
				resp.addCookie(cookie);
				
				//Tell the user.
				json.put("cookie_status", "Cookie set with name jsontestdotcom");
			}//end else if service equals "cookie"
			else if (service.equals("md5")) {
				//The user wants us to MD5 a String.
				
				//The String that we calculate a md5 hash from is passed 
				//through the text parameter.
				String to_be_md5 = req.getParameter("text");
				
				//Put the original String that was passed to us in the JSON.
				json.put("original", to_be_md5);
				
				//Try to MD5 the passed String.
				try {
					String md5 = JSONUtilities.generateMD5(to_be_md5);
					json.put("md5", md5);
				}
				catch (RuntimeException e) {
					//A RuntimeException was encountered. The provided String 
					//had an error.
					json.put("error", "An error was encountered during MD5 hashing. Message: " + e.getMessage());
					json.put("info", "You must pass a String through the ?text= parameter for a hash to be calculated.");
					
					//Make a note in logging.
					System.out.println("Unable to MD5: " + to_be_md5 + " Message: " + e.getMessage());
				}//end catch clause if RuntimeException occurs.
			}//end else if service equals "md5"
			else if (service.equals("sha1")) {
				//The String that we calculate the SHA1 hash from 
				//is passed through the text parameter.
				String to_be_sha1 = req.getParameter("text");
				
				//Put the original String that was passed to us in the JSON.
				json.put("original", to_be_sha1);
				
				//Try to SHA1 the passed String.
				try {
					String sha1 = JSONUtilities.generateSHA1(to_be_sha1);
					json.put("sha1", sha1);
				}
				catch (RuntimeException e) {
					//A RuntimeException was encountered. The provided String 
					//had an error.
					json.put("error", "An error was encountered during SHA1 hashing. Message: " + e.getMessage());
					json.put("info", "You must pass a String through the ?text= parameter for a hash to be calculated.");
					
					//Make a note in logging.
					System.out.println("Unable to SHA1: " + to_be_sha1 + " Message: " + e.getMessage());
				}//end catch clause if RuntimeException occurs.
			}//end else if service equals "sha1"
			else if (service.equals("404")) {
				//Return a 404 error code.
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				
				//Return immediately; we don't need any further processing.
				return;
			}
			else if (service.equals("malformed")) {
				//The user wants malformed JSON text
				
				//Generate the JSON object.
				JSONObject malformed_json = new JSONObject();
				malformed_json.put("ip", req.getRemoteAddr());
				
				//Pull out the text, and delete the last } character, 
				//making it invalid JSON.
				String malformed_json_text = malformed_json.toString(3);
				malformed_json_text = malformed_json_text.trim();
				malformed_json_text = malformed_json_text.substring(0, malformed_json_text.length() - 1);
				
				//Set the response directly into the response text.
				json_text = malformed_json_text;
			}//end else if service equals "malformed"
			else if (service.equals("code")) {
				//The user wants JS code
				String code = "alert(\"Your IP address is: " + req.getRemoteAddr() + "\");";
				
				//Send to user.
				json_text = code;
				
				//Cancel any callback set, because it makes no sense 
				//to wrap JS code in a callback.
				callback = null;
			}//end else if service equals "code"
			else {
				//No JSONTest service matched the client's requested 
				//service. Send back an error.
				json.put("error", "A valid JSONTest.com service was not specified.");
				json.put("info", "Visit JSONTest.com for available services.");
				json.put("url", "jsontest.com");
				json.put("version", "Production");
			}
			//End processing services.
			
			/**
			 * Generate JSON from the JSONObject, but only if the service 
			 * did not specify the return text directly.
			 */
			if (json_text == null) {
				json_text = json.toString(3);
			}//end if json_text == null
			
			/**
			 * Wrap with callback, if a callback is specified.
			 */
			if (callback != null) {
				json_text = callback + "(" + json_text + ");";
			}//end if callback !- null
		}//end try clause around services
		catch (JSONException e) {
			/**
			 * We were unable to generate a JSONObject. Issue 
			 * a notice to the user.
			 * 
			 * This clause should never pop, because the only reasons 
			 * a JSONException can occur is if the name part of the pair 
			 * is null, or if we put an invalid number into JSON. We only 
			 * put non-null names into JSON, and all numbers are valid.
			 */
			json_text = "{\"error\":\"Unable to generate JSON. Please notify ";
			json_text += "the webmaster.\"}";
			
			//Note the error in logging.
			System.err.println("Exception while processing services: " + e.getMessage());
		}
		
		
		/**
		 * Set up the Access-Control-Allow-Origin header. 
		 * 
		 * The user may shut this header off, but it is required for 
		 * most web apps to be able to use external services.
		 */
		if ("false".equals(allow_origin)) {
			//Do nothing, the user does not want this header.
			//Make a note in logging, since this is a very unusual request.
			System.out.println("Access-Control-Allow-Origin header turned off. User's web application may not work.");
		}
		else {
			//By default, we set access-control-allow-origin to *, which 
			//means that all web apps regardless of domain may access 
			//and use our services. 
			resp.setHeader("Access-Control-Allow-Origin", "*");
		}
		
		//Set the content type.
		String content_type = getContentType(mime, callback);
		resp.setContentType(content_type);
		System.out.println("Content Type: " + content_type);
		
		//Print out the JSON.
		resp.getWriter().println(json_text);
		System.out.println(json_text);
	}//end doGet
	
	/**
	 * Set up MIME type.
	 * 
	 * The MIME type is set via the parameter mime, where it 
	 * can be set to a integer corresponding to a particular 
	 * mime type. If this parameter is not set, then we set the 
	 * MIME type according to the response: application/json 
	 * for JSON responses, application/javascript for 
	 * JSONP responses. These MIME types conform to official 
	 * IETF recommendations.
	 * 
	 * @param mime The mime request parameter.
	 * @param callback The callback request parameter.
	 * @return A String with an appropriate MIME type for the response.
	 */
	private String getContentType(String mime, String callback) {
		
		String content_type = "text/plain";
		
		if ("1".equals(mime)) {
			content_type = "application/json";
		}
		else if ("2".equals(mime)) {
			content_type = "application/javascript";
		}
		else if ("3".equals(mime)) {
			content_type = "text/javascript";
		}
		else if ("4".equals(mime)) {
			content_type = "text/html";
		}
		else if ("5".equals(mime)) {
			content_type = "text/plain";
		}
		else {
			//The user didn't set up a requested MIME type. We'll set it for them.
			if (callback == null) {
				//If no callback is set
				content_type = "application/json";
			}
			else {
				//If a callback is set
				content_type = "application/javascript";
			}
		}//end else if content_type is null or does not match an accepted value.
		
		return content_type;
	}//end getContentType
	
}//end file
