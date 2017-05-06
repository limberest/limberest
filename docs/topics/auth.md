---
layout: topic
---
## Authentication and Authorization

Authentication in your service is controlled by overriding `isAuthenticationRequired()`.  Here's how the
limberest-demo MoviesService requires authentication for all HTTP methods exception GET:
```java
    @Override
    public boolean isAuthenticationRequired(Request<JSONObject> request) {
        return request.getMethod() != HttpMethod.GET;
    }
```

Authorization is governed by `getRolesAllowedAccess()`.  MoviesService requires the role "Deleters" in order
to perform a DELETE operation. 

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

This means in limberest-demo user credentials are not required to retrieve movies, but they are required to create, 
update or delete.  Furthermore, even authenticated users require membership in the "Deleters" role to be able
to delete a movie. 

So the user named *regular* in the following tomcat-users.xml sample is able to create and update movies,
but is prohibited from deleting movies.  With this setup only user *deleter* is allowed to delete.
tomcat-users.xml:
```xml
<tomcat-users version="1.0" xmlns="http://tomcat.apache.org/xml"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://tomcat.apache.org/xml tomcat-users.xsd">
  <role rolename="Deleters"/>
  <user username="deleter" password="iamdeleter" roles="Deleters"/>
  <user username="regular" password="norolesforme" />
</tomcat-users>
```