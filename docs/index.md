<!-- use fully-qualified links so that proxying works from limberest.io -->
You're a Java developer and you aim to quickly spin up a REST API.   
For that you've come to the right place.  
<div style="position:relative;top:-5px;font-size:17px;">Here's how simple it is:</div>
  0. [Get Limberest](https://limberest.github.io/limberest/cookbook/install) for your favorite container:
     - Standalone War file for Tomcat or Jetty
     - Jar dependency to embed in your JavaEE webapp
     - Spring Boot dependency for freestanding apps
  0. Add `implements Jsonable` to your Java model class(es):
     - This gives you easily-customizable [autobinding](https://limberest.github.io/limberest/topics/binding)
  0. Extend [JsonRestService](https://limberest.github.io/limberest/javadoc/io/limberest/json/JsonRestService) to create your service:
     - Use JAX-RS @Path annotations to [declare your endpoints](https://limberest.github.io/limberest/topics/services)
     - Implement `get()`, `post()`, `put()`, and/or `delete()`
     - Optionally add Swagger annotations for auto-generated [API docs](https://limberest.github.io/limberest/topics/swagger)
  0. Have fun!

  
