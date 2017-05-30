---
layout: topic
---
## APIs
Limberest includes the ability to auto-generate API definitions based on JAX-RS and Swagger
annotations.  Here's what the Swagger annotations look like in the limberest-demo MoviesService:
```java
    @ApiOperation(value="Retrieve a movie or an array of movies",
        notes="If {id} is not present, returns all movies matching query criteria.",
        response=Movie.class, responseContainer="List")
    @ApiImplicitParams({
        @ApiImplicitParam(name="title", paramType="query", dataType="string", value="Movie title"),
        @ApiImplicitParam(name="year", paramType="query", dataType="int", value="Year movie was made"),
        @ApiImplicitParam(name="rating", paramType="query", dataType="float", value="Rating (out of 5)"),
        ...
    })
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
```
And here's the [YAML-formatted Swagger API](http://oakesville.io/limberest-demo/api/swagger.yaml) output this generates:
```yaml
paths:
  /movies/{id}:
    get:
      tags:
      - "limberest demo movies"
      summary: "Retrieve a movie or an array of movies"
      description: "If {id} is not present, returns all movies matching query criteria."
      operationId: "get"
      parameters:
      - name: "title"
        in: "query"
        description: "Movie title"
        required: false
        type: "string"
      - name: "year"
        in: "query"
        description: "Year movie was made"
        required: false
        type: "int"
      - name: "rating"
        in: "query"
        description: "Rating (out of 5)"
        required: false
        type: "float"
      ...
```

The Limberest engine serves up both YAML and JSON formatted API docs at a path like */api/movies.yaml*, or
to see all API docs: */api*.

Using API annotations to describe your services means that the docs are always up-to-date because they're maintained with your code.
This *bottom-up* or *prove-first* approach is the best way to ...