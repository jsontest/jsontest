# Perl re-implementation of the JSON Test project

The goal of this project is to provide a JSON Test compatible web service that has fewer runtime requirements.  This is accomplished by using the Mojolicious framework which is easy to install on perl.  Installing mojolicious from CPAN requires no other modules outside of perl core modules.

## Services implemented
**IP Address**
Calling http://localhost:3000/ip will return data matching the Java JSON Test response.

**Headers**
Calling http://localhost:3000/headers will return data matching the Java JSON Test response.

**Date & Time**
Calling http://localhost:3000/date will return data matching the Java JSON Test response.

**Echo**
Not implemented in the perl version.

**Validation**
Not implemented in the perl version.

**JS Code**
Not implemented in the perl version.

**Cookie**
Calling http://localhost:3000/cookie will return data similar to the Java JSON Test response.  Cookie is named time and contains the time value in milliseconds.  Example JSON:
{"time": "1443931182000"}

**MD5**
Calling http://localhost:3000/md5?text=TEXT will return the MD5 sum of the TEXT parameter in the same format as the Java JSON Test response.

## Misc
Access-Control-Allow-Origin is configured for '*'.
MIME type is configured as 'application/json'.
Callbacks are not implemented.
