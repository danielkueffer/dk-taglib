# DK Taglib

Image resizing taglib for the Magnolia cms

## Compiling

javac -classpath $home::apache-tomcat-6.0.32/lib/jsp-api.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/magnolia-core-4.4.6.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/jcr-1.0.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/servlet-api-2.4.jar: src/com/danielkueffer/mgnl/dmsimg/DmsImg.java

## Jar packaging

jar cvf dk-taglib-1.0.jar META-INF/ README.md com/

## Usage

Place the jar under WEB-INF/lib and restart Magnolia.

Include the taglib in the JSP file:

	xmlns:dk="http://www.danielkueffer.com/dk-taglib

### Parameters

* uuid (The uuid of a image in the dms)
* width (The width)
* height (The height)
* css (CSS class)
* alt (Image alt text)