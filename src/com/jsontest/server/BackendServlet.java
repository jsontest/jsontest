package com.jsontest.server;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

@SuppressWarnings("serial")
public class BackendServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
		
		Vector<Entity> entities = new Vector<Entity>();
		
		//Unemployment Percent
		String unemployment_percent_info = "Unemployment rate in percent.";
		Entity unemployment_percent = getFREDStat("http://research.stlouisfed.org/fred2/data/UNRATE.txt", "unemployment_percent", unemployment_percent_info);
		entities.add(unemployment_percent);
		
		
		//Add all entities to datastore & Memcache.
		AsyncDatastoreService datastore = DatastoreServiceFactory.getAsyncDatastoreService();
		AsyncMemcacheService memcache = MemcacheServiceFactory.getAsyncMemcacheService();
		for (int i = 0; i < entities.size(); i++) {
			/**
			 * We have to make sure that each entity is not null.
			 * The Vector could contain null entries, which 
			 * occurs when there are problems generating 
			 * their associated information (for instance, an 
			 * inability to access the API.)
			 */
			Entity entity = entities.get(i);
			if (entity != null) {
				/**
				 * Copy the data into Memcache as well, for speed.
				 */
				
				
				System.out.println(entity);
				datastore.put(entity);
			}//end if entity does not equal null.
		}//Loop through entities list.
		
	}//end doGet
	
	/**
	 * Collects the latest statistic from the FRED economic statistics database.
	 * 
	 * @param data_url URL to the FRED statistic.
	 * @param name Name the FRED statistic. Used to name the Entity.
	 * @param info Describes the statistic.
	 * @return A Entity encapsulating this statistic's information. Will be null if an Exception was encountered.
	 */
	public Entity getFREDStat(String data_url, String name, String info) {
		Entity entity;
		
		try {
			// Create the URL and call it.
			URL url = new URL(data_url);
			HTTPResponse response = URLFetchServiceFactory.getURLFetchService().fetch(url);
			
			if (response.getResponseCode() == 200) {
				// If the response was successful, pull out the content.
				String content = new String(response.getContent());
				
				//Trim out the ending whitespace.
				content = content.trim();
				
				//Find the line break that separates the last line 
				//and the second to the last line.
				int last_line_start_point = content.lastIndexOf("\r");
				
				//Isolate the last line and trim it.
				String last_line = content.substring(last_line_start_point).trim();
				
				//Isolate and parse the date that this statistic 
				//was recorded.
				String stat_date_text = last_line.substring(0, last_line.indexOf(" ")).trim();
				//Reform the date into month-day-year configuration
				String[] stat_date_components = stat_date_text.split("-");
				String year = stat_date_components[0];
				String month = stat_date_components[1];
				String day = stat_date_components[2];
				String stat_date = month + "-" + day + "-" + year;
				
				//Isolate and parse the statistic number.
				String stat_point_text = last_line.substring(last_line.indexOf(" "));
				Double stat_point = new Double(stat_point_text);
				
				//Create entity, record properties.
				entity = new Entity("stat", name);
				entity.setUnindexedProperty("stat_date", stat_date);
				entity.setUnindexedProperty("stat_point", stat_point);
				entity.setUnindexedProperty("info", info);
				entity.setUnindexedProperty("add_date", new java.util.Date());
			}//end if response code is 200.
			else {
				/**
				 * If the response code is not 200, then response.getContent()
				 * will be null. To make the exception notice clearer, 
				 * we'll explicitly throw our own Exception.
				 */
				throw new NullPointerException("Response has no content. Status code was not 200.");
			}//end else to if response code is 200
		}//end try
		catch (Exception e) {
			//An error occurred. Return a null Entity.
			System.err.println("Exception while parsing FRED statistic (" + data_url + "). Exception: " + e.getMessage());
			return null;
		}//end catch
		
		return entity;
	}//end getFREDStat()
}//end file