# DK Taglib

Image resizing taglib for the Magnolia cms

## Compiling

javac -classpath $home::apache-tomcat-6.0.32/lib/jsp-api.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/magnolia-core-4.4.6.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/jcr-1.0.jar:$home::apache-tomcat-6.0.32/webapps/magnoliaAuthor/WEB-INF/lib/servlet-api-2.4.jar: src/com/danielkueffer/mgnl/dmsimg/DmsImg.java

## Jar packaging

jar cvf dk-taglib-1.0.jar META-INF/ README.md com/

## Usage

Place the jar under WEB-INF/lib and restart Magnolia.

Include the taglib in the JSP file:

	xmlns:dk="http://www.danielkueffer.com/dk-taglib"

### Parameters

* uuid (The uuid of a image in the dms)
* width (The width)
* height (The height)
* css (CSS class)
* alt (Image alt text)

### Example Magnolia Paragraph

	<?xml version="1.0" encoding="UTF-8" ?>
	<jsp:root version="2.0" xmlns:jsp="http://java.sun.com/JSP/Page" 
							xmlns:cms="cms-taglib"
							xmlns:cmsu="cms-util-taglib"
							xmlns:cmsfn="http://www.magnolia.info/tlds/cmsfn-taglib.tld"
							xmlns:fn="http://java.sun.com/jsp/jstl/functions"
							xmlns:fmt="urn:jsptld:http://java.sun.com/jsp/jstl/fmt" 
							xmlns:c="http://java.sun.com/jsp/jstl/core"
							xmlns:dk="http://www.danielkueffer.com/dk-taglib">
	
			<div>
				<cms:editBar editLabel="Edit" moveLabel="Move" deleteLabel="Delete"/>
	
					<cms:out nodeDataName="image" var="imgUuid"/>
	
					<dk:dmsimg uuid="${imgUuid}" width="500" height="" css="my-style" alt="my image"/>
			</div>
	</jsp:root>
	
## License

[MIT License](http://www.opensource.org/licenses/mit-license.php). See LICENSE.