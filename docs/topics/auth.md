---
layout: topic
---
## Access Control
### Authentication
Authentication in your Limberest service is enforced by overriding 
[isAuthenticationRequired()](../javadoc/io/limberest/service/http/RestService.html#isAuthenticationRequired-io.limberest.service.http.Request-).
Here's how the [limberest-demo](https://limberest.io/ui/) 
[MoviesService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MoviesService.java)
requires authentication for all HTTP methods except GET:
```java
    @Override
    public boolean isAuthenticationRequired(Request<JSONObject> request) {
        return request.getMethod() != HttpMethod.GET;
    }
```

### Authorization
Authorization is governed by 
[getRolesAllowedAccess()](../javadoc/io/limberest/service/http/RestService.html#getRolesAllowedAccess-io.limberest.service.http.Request-).
[MovieService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MovieService.java)
requires the role "Deleters" in order to perform a DELETE operation.
```java
    public List<String> getRolesAllowedAccess(Request<JSONObject> request) {
        if (request.getMethod() == HttpMethod.DELETE) {
            List<String> roles = new ArrayList<>();
            roles.add("Deleters");
            return roles;
        }
        return null; // access is not restricted for other operations
    }
```

This combination means that in limberest-demo, user credentials are not required to retrieve movies, 
but they are required to create, update or delete.  Furthermore, even authenticated users require
membership in the "Deleters" role to be able to delete a movie. 

### Tomcat Setup
The user named *regular* in the following tomcat-users.xml sample is able to create and update movies
in limberest-demo, but is prohibited from deleting movies.
```xml
<tomcat-users version="1.0" xmlns="http://tomcat.apache.org/xml"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd">
  <role rolename="Deleters"/>
  <user username="deleter" password="iamdeleter" roles="Deleters"/>
  <user username="regular" password="norolesforme" />
</tomcat-users>
```
With this setup, only user *deleter* is allowed to delete.

Next Topic: [Configuration](config)