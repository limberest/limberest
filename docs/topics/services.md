---
layout: topic
---
## Services
You create a Limberest service by implementing the [Service](../javadoc/io/limberest/service/Service) interface.
The easy way to do this is to extend a base implementation, probably 
[JsonRestService](../javadoc/io/limberest/json/JsonRestService).
The base implementation ordinarily takes care of request object binding.

You declare service endpoints using [JAX-RS @Path](http://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html) annotations.
Here's how this looks for the [MoviesService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MoviesService.java)
in [limberest-demo](../demo/):
```java
@Path("/movies")
public class MoviesService extends JsonRestService {
```

This registers MoviesService to respond to requests for */movies* resources.
When a request is received by Limberest, it's forwarded to the service with the most specific
matching registered path (for example: a request to /movies/a2d7721c will invoke the service
registered with path '/movies/{id}' in preference to that with just '/movies').

The job of your service is to interpret the request and build a corresponding response.
Here's the complete `get()` method from MoviesService: 
```java
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        validate(request);
        List<Movie> movies = getPersist().retrieve(request.getQuery());
        JsonList<Movie> jsonList = new JsonList<>(movies, "movies");
        return new Response<>(jsonList.toJson());
    }
```

Let's take a look at each step in this implementation:
0. We invoke `validate()`, passing the [Request](../javadoc/io/limberest/service/http/Request).
   Validation may throw a [ServiceException](../javadoc/io/limberest/service/ServiceException) 
   with an appropriate [Status Code](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
   if it does not like the request.
   This mechanism is discussed in more detail in the [Validations](validations) topic.
0. Next we populate a List of movies by retrieving from a persistent store.
   Filtering of results is governed by HTTP query parameters that
   may be appended to the resource URL (e.g. *?sort=title*).  These are accessible via `request.getQuery()`, 
   which returns a Limberest [Query](../javadoc/io/limberest/service/Query) object.
   Check out the [Queries](queries) topic to see how Limberest facilitates results filtering 
   through HTTP query parameters. 
0. The retrieved list is made [Jsonable](../javadoc/io/limberest/json/Jsonable) 
   by way of the [JsonList](../javadoc/io/limberest/json/JsonList) utility class.
   Jsonables are the easiest way to accomplish JSON data binding in Limberest.
   In limberest-demo, the [Movie](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/model/Movie.java)
   model object implements Jsonable to take advantage of Limberest's built-in [Binding](binding) facility.
0. Lastly, a [Response](../javadoc/io/limberest/service/http/Response) is contructed from the
   retrieved Movies list and returned to the caller. 

Source code for limberest-demo is available in its 
[GitHub repository](https://github.com/limberest/limberest-demo).
Also, the Movies service is deployed on the Limberest site, so you can access the example services
and try them out for yourself:
   - <http://limberest.io/demo/movies?sort=title>   
     (the [Queries](queries) topic has many more such examples).

**Note:** The default root path for limberest services is */api/*.
See the [Configuration](config) topic for details on how to override this default.

Next Topic: [Binding](binding)
