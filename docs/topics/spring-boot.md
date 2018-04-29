---
layout: topic
---
## Spring Boot
The [ply-demo](https://github.com/ply-ct/ply-demo) project is a full working example of deploying Limberest
services in a Spring Boot environment.

## Dependencies
Here's what typical `dependencies` look like in build.gradle:
```gradle
dependencies {
    compile "org.springframework.boot:spring-boot-starter-web"
    compile "io.limberest:limberest:${limberestVersion}"
    compile 'io.swagger:swagger-annotations:1.5.19'
    testCompile('org.springframework.boot:spring-boot-starter-test')
}
```

### Autowiring
In Spring Boot you can use autowiring to inject into your service implementation:
```java
@Path("/movies")
@Api("movies")
@Component
public class MoviesService extends JsonRestService {

    @Autowired
    private Persist<Movie> persist;
    public Persist<Movie> getPersist() {
        return persist;
    }
```

Next Topic: [Javadoc](/javadoc)