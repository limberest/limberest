---
layout: topic
---
## Services
You create a Limberest service by implementing the Service interface.
The easy way to do this is to extend a base REST implementation, like [JsonRestService]({{site.baseurl}}/javadoc/io/limberest/json/JsonRestService).
The base implementation is responsible for request object binding.

You declare service endpoints using [JAX-RS @Path](http://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html) annotations.  


Here's how this looks for the MoviesService and its `get()` method in limberest-demo:
```java
@Path("/movies")
public class MoviesService extends JsonObjectRestService {

    @Override
    @Path("/{id}")
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
```

These annotations register the movies service to respond to requests for resources like
/movies/{id}, where {id} is optional.

An @Path annotation on a method is appended to any @Path annotation from its declaring class.
When a REST request is received by Limberest, it's matched against the most specific
registered endpoint path (for example: a request to /movies/12345 will invoke the service
with path '/movies/{id}' in preference to that with just '/movies').

When {id} is present, MoviesService.get() responds with the movie whose 'id' property matches {id} in the path
(or an HTTP 404 response if no such movie is found).  When {id} is absent, the service responds with a list of movies.

Optionally, HTTP query parameters can be included on the request endpoint URL.  These are made accessible in the
[Request]() argument via request.getQuery().  Usage is described in the [queries](queries.md) document.

TODO: how to customize error response for a given ServiceException
