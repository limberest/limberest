---
layout: topic
---
## Configuration

Zero configuration is required to deploy Limberest.
However, there are a few defaults you might want to override; the
most-likely being the service API and/or Docs root paths.

### Service Path
The default root path for limberest services is **_/api/_**.

#### **[Webapps](http://www.oracle.com/technetwork/articles/java/webapps-1-138794.html)**
To override the service path in your webapp, declare a different servlet mapping in your web.xml:
```xml
  <servlet>
    <servlet-name>io.limberest.service.http.RestServlet</servlet-name>
    <servlet-class>io.limberest.service.http.RestServlet</servlet-class>
  </servlet>  

  <servlet-mapping>
    <servlet-name>io.limberest.service.http.RestServlet</servlet-name>
    <url-pattern>/my-api/*</url-pattern>
  </servlet-mapping>
```
**Note:** The servlet-name element must be exactly as above to override Limberest's base.  
A different servlet-name will add separate mapping(s) instead of overriding.

#### **[Spring Boot](https://projects.spring.io/spring-boot/)**
To override the default root path in Spring Boot,
specify something like the following in your application.yml:
```yaml
server:
  servlet:
    context-path: /my-api
```


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

### limberest.yaml
Limberest-specific config settings are controlled by limberest.yaml.  The location of limberest.yaml
can be designated through a Java system property:
```
 -Dio.limberest.config.file=/home/ubuntu/deploy/limberest/config/limberest.yaml
```
If the location is not so specified, Limberest will look on the runtime classpath for limberest.yaml.
Here's an example showing some of the most common options:
```yaml

# packages to scan for service impls (avoids full scan)
scan
  packages:
    - com.example.awesome.api
    - com.example.other.api

api:
  definitionClasses: # controls swagger API definition info
    - io.limberest.demo.api.Definition
  prettyIndent: 2

request:
  fallbackContentType:
    get: application/json
  
response:
  prettyIndent: 2

```

Next Topic: [Spring Boot](spring-boot)