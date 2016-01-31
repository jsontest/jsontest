# Perl re-implementation of the JSON Test project

The goal of this project is to provide a JSON Test compatible web service that has fewer runtime requirements.  This is accomplished by using the Mojolicious framework which is easy to install on perl.  Installing Mojolicious from CPAN requires no other modules outside of perl core modules.

## Running the JsonTests web server
The server can be configured as any Mojolicious program, including via PSGI or as CGI script.  These are left as exercises for the reader.

To run JsonTests.pl from the command line in a development configuration run:
````
perl JsonTests.pl prefork
````

## Services implemented
**IP Address**<br>
Calling http://localhost:3000/ip will return data matching the Java JSON Test response.

Example response:
````
{"ip": "127.0.0.1"}
````

**Headers**<br>
Calling http://localhost:3000/headers will return data matching the Java JSON Test response.

Example response:
````
{
"host": "127.0.0.1:3000",
"user-agent": "Apache-HttpClient/4.2.6 (java 1.5)",
"jmeter-test-script": "DO_NOT_DELETE_THIS HEADER",
"connection": "keep-alive"
}
````

**Date & Time**<br>
Calling http://localhost:3000/date will return data matching the Java JSON Test response.

Example response:
````
{
"time": "20:50:43 PM",
"milliseconds_since_epoch": "1454205043000",
"date": "00-30-2016"
}
````

**Echo**<br>
Not implemented in the perl version.

**Validation**<br>
Not implemented in the perl version.

**Cookie**<br>
Calling http://localhost:3000/cookie will return data similar to the Java JSON Test response.  Cookie is named time and contains the time value in milliseconds.  In addition to setting a cookie via HTTP headers, the response body contains a JSON response containing the same time value.

Example response header:
````
Set-Cookie: time=1454205043000
````

Example response:
````
{"time": "1454205043000"}
````

**MD5**<br>
Calling http://localhost:3000/md5/TEXT will return the MD5 sum of the TEXT parameter in the same format as the Java JSON Test response.

Example response:
````
{
"md5": "033bd94b1168d7e4f0d644c3c95e35bf",
"original": "TEST"
}
````

## Misc
Access-Control-Allow-Origin is configured for '*'.<br>
MIME type is configured as 'application/json'.<br>
Callbacks are not implemented.

## License
Licensed under the MIT license.
