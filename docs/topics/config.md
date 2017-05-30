---
layout: topic
---
## Configuration

zero config required


The default root path for limberest services is */api/*.
See the [Configuration](configuration) topic for details on how to override this default.  
To override the default root path in your webapp, declare a different servlet mapping in your web.xml:
```xml
  <servlet>
    <servlet-name>io.limberest.service.http.RestServlet</servlet-name>
    <servlet-class>io.limberest.service.http.RestServlet</servlet-class>
  </servlet>  

  <servlet-mapping>
    <servlet-name>io.limberest.service.http.RestServlet</servlet-name>
    <url-pattern>/services/*</url-pattern>
  </servlet-mapping>
```
**Note:** The servlet-name must be exactly as above to override Limberest's base.  A different servlet-name will add separate
mapping(s) instead of overriding.



todo: document yaml content and how located