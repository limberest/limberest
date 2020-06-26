---
layout: topic
---
## Validation
Now that you've crafted an airtight API and documented it using [Swagger](swagger),
you'll want a way of enforcing constraints so that your service is not in the 
habit of accepting bad requests.  This is where the Limberest validation API
comes in.

Let's harken back to [limberest-demo](https://limberest.io/ui/) and take a look at the
[MoviesService](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/service/MoviesService.java)
`post` method to see how validation is invoked:
```java
    public Response<JSONObject> post(Request<JSONObject> request) throws ServiceException {
        
        validate(request);
        
        Movie movie = getPersist().create(new Movie(request.getBody()));
        return new Response<>(Status.CREATED, movie.toJson());
    }
  ...
  
    protected void validate(Request<JSONObject> request) throws ValidationException {
        Result result = getSwaggerValidator().validate(request, true);
        if (result.isError())
            throw new ValidationException(result);
    }
```

The [ValidationException](../javadoc/io/limberest/validate/ValidationException) thrown by `MoviesService.validate()`
is handled specially by Limberest to trigger a service response that reflects the validation outcome.

Without worrying about how the [SwaggerValidator](../javadoc/io/limberest/api/validate/SwaggerValidator)
is obtained for the moment, let's focus on the [Validator](../javadoc/io/limberest/validate/Validator) 
functional interface it implements:
```java
@FunctionalInterface
public interface Validator<T> {
    
    public Result validate(Request<T> request) throws ValidationException;
}
```

This stipulates a `validate()` method that takes a Request and returns a Limberest validation [Result](../javadoc/io/limberest/validate/Result).
Results are cumulative, and can be appended to each other through [Result.also()](../javadoc/io/limberest/validate/Result.html#also-io.limberest.validate.Result-).
Maximum accumulation can be controlled through [Result.setMaxErrors()](../javadoc/io/limberest/validate/Result.html#setMaxErrors-int-).

The parameterized type for SwaggerValidator's `Validator` implementation is [JSONObject](https://stleary.github.io/JSON-java/org/json/JSONObject.html).
It evaluates the request body against the requirements spelled out in the model object annotations.

For example, look again at the
[@ApiModelProperty](https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X#apimodelproperty) annotation
on [Movie](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/model/Movie.java)'s `year` member:
```java
    @ApiModelProperty(required=true, allowableValues="range[1900, infinity]")    
    private int year;
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
```

If you submitted a POST request to limberest-demo/movies with a value for `year` of 100, you'd receive a JSON
response like this:
```json
{"status": {
  "code": 400,
  "message": "year: value '100' is less than minimum (1900)"
}}
```
Since 100 is less than 1900, the *allowableValues* attribute is violated, and an error Result is returned.
The response payload above comes from the built-in [Jsonable](../javadoc/io/limberest/json/Jsonable) class
[StatusResponse](../javadoc/io/limberest/json/StatusResponse).  It's returned by default whenever any type of 
[ServiceException](../javadoc/io/limberest/service/ServiceException) is encountered.  A payload like this implies that the indicated
code and message are reflected in the HTTP protocol [status code](https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html)
and message on the response.

**Note:** POST, PUT and DELETE requests in limberest-demo require authentication, so you'll not be able to perform these 
operations at [limberest.io](http://limberest.io).  To try out these examples, you can clone 
[limberest-demo from GitHub](https://github.com/limberest/limberest-demo) and deploy on your favorite servlet container
or on Spring Boot.  Detailed instructions are here: <https://limberest.github.io/limberest/demo/>.

You can replace or supplement SwaggerValidator with custom validation logic by implementing Validator yourself.
The **rating** member on limberest-demo's Movie class is a good illustration:
```java
    @Size(max=5)
    @ApiModelProperty("Must be a multiple of 0.5")
    private float rating;
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
```  

One thing to note about this is [@Size](http://docs.oracle.com/javaee/7/api/javax/validation/constraints/Size.html)
which is not actually a Swagger annotation but rather comes from the [JSR-303](http://beanvalidation.org/1.0/spec/) 
Bean Validation API.  The other thing that stands out is that we mention in our @ApiModelProperty annotation that 
`rating` must be a multiple of 0.5.  But Swagger annotations do not provide a good way to enforce this requirement, 
so we'll have to take care of that ourselves.  Here's how `MoviesService.getSwaggerValidator()` adds this custom logic:
```java
    protected SwaggerValidator getSwaggerValidator() {
        SwaggerValidator val = new SwaggerValidator();
        val.addValidator(DecimalProperty.class, (json, property, path, strict) -> {
            if (property.getName().equals("rating")) {
                BigDecimal value = json.getBigDecimal(property.getName());
                if (value.floatValue() % 0.5 != 0)
                    return new Result(Status.BAD_REQUEST,  path + ": value '" + value + "' must be a multiple of 0.5");
            }
            return new Result(Status.OK);
        });
        return val;
    }
``` 

A POST request with a rating of 6.7 fails on two counts:
```json
{"status": {
  "code": 400,
  "message": "rating: value '6.7' must be a multiple of 0.5\nrating: value '6.7' exceeds maximum (5)"
}}
```

By default, multiple outcomes are combined into a status with the highest individual code and a newline-separated
list of messages.  This behavior can be overridden by passing a custom 
[Consolidator](../javadoc/io/limberest/validate/Result.Consolidator) to `Result.getStatus()`.

Next Topic: [Access Control](auth)
