---
layout: topic
---
## Swagger
Limberest comes with the ability to auto-generate API documentation based on 
[JAX-RS](http://docs.oracle.com/javaee/7/api/javax/ws/rs/Path.html) and 
[Swagger](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X)
annotations.  Here's an example from the [limberest-demo](demo.md)
[MovieService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MovieService.java)
`put()` method that shows what Swagger annotations look like:
```java
    @ApiOperation(value="Update a movie.", response=StatusResponse.class)
    @ApiImplicitParams({
        @ApiImplicitParam(name="{id}", paramType="path", dataType="string", required=true),
        @ApiImplicitParam(name="Movie", paramType="body", dataType="io.limberest.demo.model.Movie", required=true)})
    public Response<JSONObject> put(Request<JSONObject> request) throws ServiceException {
```
And here are a few snippets from the [Swagger JSON](http://limberest.io/demo/api-docs/movies)
output this yields:
```json
{
  "swagger" : "2.0",
  "info" : {
    "version" : "1.0",
    "title" : "Movies API",
    "license" : {
      "name" : "Apache 2.0"
    }
  },
  "host" : "limberest.io",
  ...
  "paths" : {
  ...
    "/movies/{id}" : {
  ...
      "put" : {
        "tags" : [
          "limberest demo movie"
        ],
        "summary" : "Update a movie.",
        "parameters" : [
          {
            "name" : "{id}",
            "in" : "path",
            "required" : true,
            "type" : "string"
          },
          {
            "in" : "body",
            "name" : "Movie",
            "required" : true,
            "schema" : {
              "$ref" : "#/definitions/Movie"
            }
          }
        ],
        "responses" : {
          "200" : {
            "description" : "OK",
            "schema" : {
              "$ref" : "#/definitions/StatusResponse"
            }
          }
        }
      }
    }
  }
  ...
}
```

Note how the [@ApiImplicitParam](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#apiimplicitparam-apiimplicitparams)
annotations are reflected in the generated API docs.  The `#/definitions/Movie` reference syntax points to a 
[JSON Schema](http://json-schema.org/) representation of our model object:
```json
{
  ...
  "definitions" : {
  ...
    "Movie": {
      "type": "object",
      "required": [
        "title",
        "year"
      ],
      "properties": {
        "id": {
          "type": "string"
        },
        "title": {
          "type": "string"
        },
        "category": {
          "type": "string"
        },
        "year": {
          "type": "integer",
          "format": "int32",
          "minimum": 1900
        },
        "credits": {
          "type": "array",
          "items": {
            "$ref": "#/definitions/Credit"
          }
        },
        "poster": {
          "type": "string"
        },
        "rating": {
          "type": "number",
          "format": "float",
          "description": "Must be a multiple of 0.5",
          "minimum": 0,
          "maximum": 5
        },
        "description": {
          "type": "string",
          "minLength": 0,
          "maxLength": 2048
        },
        "webRef": {
          "$ref": "#/definitions/WebRef"
        },
        "owned": {
          "type": "boolean"
        }
      }
    }
  }
}
```

Swagger reflects on model objects like Movie to identify their properties.
You can control how this translates using Swagger annotations like
[@ApiModel](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#apimodel),
but if the default output is okay, then model annotations are not required.
For the `year` property, we use the 
[@ApiModelProperty](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#apimodelproperty) 
annotation to identify this field as required and to constrain its value:
```java
    @ApiModelProperty(required=true, allowableValues="range[1900, infinity]")    
    private int year;
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
```  

...and this is reflected in the generated api-docs output:
```json
{
  ...
      "required": [
        "title",
        "year"
      ],
      "properties": {
        ...
        "year": {
          "type": "integer",
          "format": "int32",
          "minimum": 1900
        }
        ...
      }
  ...
}
```

The value of API constraints like this is further illustrated in the [Validation](validation) topic.

The Limberest engine serves up either JSON or YAML formatted API docs at a path like:
 - [/api-docs/movies](http://limberest.io/demo/api-docs/movies)
 - [/api-docs/movies.yaml](http://limberest.io/demo/api-docs/movies.yaml)
to see all API docs: 
 - [/api-docs](http://limberest.io/demo/api-docs)
 - [/api-docs/yaml](http://limberest.io/demo/api-docs/swagger.yaml)

Using API annotations to describe your services means that the docs are always up-to-date
because they're maintained with your code.  Not only that, the same annotations that produce
the API docs can also be used to automatically [validate](validation) requests.
This *bottom-up* or *prove-first* approach is the best way to ensure that your docs stay
in sync with your code.

Next Topic: [Validation](validation)