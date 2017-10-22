---
layout: topic
---
## Configuration

Zero configuration is required to deploy Limberest.
However, there are a few defaults you might want to override; the
most-likely being the service API and/or Docs root paths.

### Service Path
The default root path for limberest services is **_/api/_**.
To override the service path in your webapp, declare a different servlet mapping in your web.xml:
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
**Note:** The servlet-name element must be exactly as above to override Limberest's base.  
A different servlet-name will add separate mapping(s) instead of overriding.

### API Docs Path
The default path for limberest Swagger output is **_/api-docs/_**.
To override the swagger path in your webapp, declare a different servlet mapping in your web.xml:
```xml
  <servlet>
    <servlet-name>io.limberest.service.http.SwaggerServlet</servlet-name>
    <servlet-class>io.limberest.service.http.SwaggerServlet</servlet-class>
  </servlet>  

  <servlet-mapping>
    <servlet-name>io.limberest.service.http.SwaggerServlet</servlet-name>
    <url-pattern>/swagger/*</url-pattern>
  </servlet-mapping>
```

TODO: override root paths in Spring Boot.

### Optional services.yaml
TODO: document services.yaml content and how located

### Limberest JS
TODO: config options for limberest-js

### Limberest UI
TODO: config options for limberest-ui

