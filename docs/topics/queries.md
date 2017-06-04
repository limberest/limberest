---
layout: topic
---
## Queries
HTTP query parameters are captured by Limberest into a [Query](../javadoc/io/limberest/service/Query.html)
object.  The method `Query.getFilters()` returns a map that your service can use to filter results.  
Query also provides the built-in `match()` method which can be used with with Java
[Streams](http://www.oracle.com/technetwork/articles/java/ma14-java-se-8-streams-2177646.html) for 
convenient autofiltering.  Here's an example from [limberest-demo](../demo/)'s
[MoviesPersistFile](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/persist/MoviesPersistFile.java): 
```java
    @Override
    public List<Movie> retrieve(Query query) throws PersistException {
        List<Movie> movies = getMovies();
        Stream<Movie> stream = movies.stream();
        
        // filter
        if (query.hasFilters() || query.getSearch() != null) {
            stream = stream.filter(movie -> query.match(new JsonMatcher(movie.toJson())));
        }
        
        // sort
        if ((query.getSort() != null && !"title".equals(query.getSort())) || query.isDescending()) {
            stream = stream.sorted(new JsonableComparator(query, (j1, j2) -> {
                return getSortTitle(j1).compareToIgnoreCase(getSortTitle(j2));
            }));
        }
        
        // paginate
        if (query.getStart() > 0)
            stream = stream.skip(query.getStart());
        if (query.getMax() != Query.MAX_ALL)
            stream = stream.limit(query.getMax());
        
        return stream.collect(Collectors.toList());
    }
```

In the limberest-demo app a streamable list of Movies is held in memory.  The `retrieve()` method above:
 - filters using the handy [JsonMatcher](../javadoc/io/limberest/json/JsonMatcher.html) predicate:
   ```java
        if (query.hasFilters() || query.getSearch() != null) {
            stream = stream.filter(movie -> query.match(new JsonMatcher(movie.toJson())));
        }
   ```   
 - sorts according to special [meta](#meta) parameters, using 
   [JsonableComparator](../javadoc/io/limberest/json/JsonableComparator.html)
   ```java
        if ((query.getSort() != null && !"title".equals(query.getSort())) || query.isDescending()) {
            stream = stream.sorted(new JsonableComparator(query, (j1, j2) -> {
                return getSortTitle(j1).compareToIgnoreCase(getSortTitle(j2));
            }));
        }
   ```   
 - and then optionally paginates based on the Query's paging meta parameters:
   ```java
        if (query.getStart() > 0)
            stream = stream.skip(query.getStart());
        if (query.getMax() != Query.MAX_ALL)
            stream = stream.limit(query.getMax());
   ```

<a name="meta"></a>
### Meta Parameters
A few **meta** parameters have special meaning to Query objects:
  - **count** (`boolean`): Return item count only (no retrieval).
  - **search** (`String`): Find matching results across fields.
  - **start** (`int`): Start the item list at the specified index.
  - **max** (`int`): Limit results to no more than this (paginate).
  - **sort** (`String`): Sort results on this item property.
  - **descending** (`boolean`): Sort results in descending order.

These special parameters are not included in `getFilters()`, but can be accessed directly (`getSort()`).
You can use these special fields to further process the results returned by your service as in the 
filtering, sorting and paginating examples above.

<a name="sample-requests"></a>
### Sample requests 
Try these against the limberest-demo _movies_ service to see how query parameters affect results:

retrieve all movies:
 - [/movies](http://limberest.io/demo/movies)

retrieve movies made in 1931:
 - [/movies?year=1931](http://limberest.io/demo/movies?year=1931)

retrieve movies made after 1935:
 - [/movies?year=>1935](http://limberest.io/demo/movies?year=>1935)
   (note > is part of query **value**)

retrieve all movies, sorted by rating in descending order:
 - [/movies?sort=rating&descending=true](http://limberest.io/demo/movies?sort=rating&descending=true)

retrieve movies with a rating of 3.5 or above, sorted by year:
 - [/movies?rating=>=3.5&sort=year](http://limberest.io/demo/movies?rating=>=3.5&sort=year)
   (note >= is prepended to query **value**)

find all movies with Bela Lugosi:
 - [/movies?search=Bela Lugosi](http://limberest.io/demo/movies?search=Bela Lugosi)

retrieve the first page of movies, with page size = 25
 - [/movies?max=25&start=0](http://limberest.io/demo/movies?max=25&start=0)

retrieve the second page of movies, with page size = 25
 - [/movies?max=25&start=25](http://limberest.io/demo/movies?max=25&start=25)

retrieve the first page of movies from a list sorted by year
 - [/movies?max=25&sort=year](http://limberest.io/demo/movies?max=25&sort=year)

retrieve the first page of movies featuring Boris Karloff, sorted by year
 - [/movies?max=25&sort=year&search=Boris Karloff](http://limberest.io/demo/movies?max=25&sort=year&search=Boris Karloff)
 
Next Topic: [Swagger](swagger)