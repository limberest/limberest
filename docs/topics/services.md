---
layout: topic
---
## Services
You create a Limberest service by implementing the [Service](../javadoc/io/limberest/Service/Service) interface.
The easy way to do this is to extend a base implementation, probably [JsonRestService](../javadoc/io/limberest/json/JsonRestService).
The base implementation ordinarily takes care of request object binding.

You declare service endpoints using [JAX-RS @Path](http://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html) annotations.  


Here's how this looks for the MoviesService and its `get()` method in [limberest-demo](/demo/):
```java
@Path("/movies")
public class MoviesService extends JsonRestService {

    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        ...
```

This registers MoviesService to respond to requests for the */movies* resource.
When a request is received by Limberest, it's matched against the most specific
registered path (for example: a request to /movies/12345 will invoke the service
registered with path '/movies/{id}' in preference to that with just '/movies').

Optionally, HTTP query parameters can be included on the request endpoint URL.  These are made accessible in the
[Request](../javadoc/io/limberest/service/http/Request) argument via request.getQuery().  
Limberest [Query]() usage is described under the [queries](queries.md) topic.

The job of your service implementation is to interpret the request and build a corresponding response.
Here's the complete `get()` method from [MoviesService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MoviesService.java): 
```java
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        validate(request);
        List<Movie> movies = getPersist().retrieve(request.getQuery());
        JsonList<Movie> jsonList = new JsonList<>(movies, "movies");
        return new Response<>(jsonList.toJson());
    }
```

Let's take a look at each step in this example implementation:
0. We invoke the `validate()`, passing the request.  Validation may throw a ServiceException with an appropriate [Status Code](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
   if it does not like the request.  This mechanism is discussed in more detail in the [Validations](validations.md) topic.
0. Next we populate a List of movies by retrieving from a persistent store based on the request [query](queries.md). 

The default root path for limberest services is */api/*.  
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