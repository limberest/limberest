---
layout: topic
---
## Queries

HTTP query parameters are captured by Limberest into a [Query]({{site.baseurl}}/javadoc/io/limberest/service/Query.html) object.
The method `Query.getFilters()` returns a map that your service can use
to filter results:
```java
    public Response<JSONObject> get(Request<JSONObject> request) throws ServiceException {
        List<Movie> movies = new MoviesAccess().getMovies(query);
        String title = request.getQuery().getFilter("title");
        if (title != null)
            movies = movies.stream().filter(m -> m.getTitle().contains(title)).collect(Collectors.toList());
        JsonList<Movie> jsonList = new JsonList<>(movies, "movies");
        return new Response<>(jsonList.getJson());
    }
```
A few **meta** parameters have special meaning to Query objects:
  - "count" (`boolean`): Return item count only (no retrieval).
  - "search" (`String`): Find matching results across fields.
  - "start" (`int`): Start the item list at the specified index.
  - "max" (`int`): Limit results to no more than this (paginate).
  - "sort" (`String`): Sort results on this item property.
  - "descending" (`boolean`): Sort results in descending order.

These special parameters are not included in `getFilters()`, but can be accessed directly (`getSort()`).
You can use these special fields to further process the response returned by your service.

The MoviesAccess class in limberest-demo uses the prebuilt `JsonMatcher` for filtering:
```java
    if (query.hasFilters() || query.getSearch() != null) {
        stream = stream.filter(movie -> query.match(new JsonMatcher(movie.getJson())));
    }
```
And it uses a JsonableComparator for sorting:
```java
    if (query.getSort() != null || query.isDescending()) {
        stream = stream.sorted(new JsonableComparator(query, (j1, j2) -> {
            return getSortTitle(j1).compareToIgnoreCase(getSortTitle(j2));
        }));
    }
```
And then it optionally paginates based on the "start" and "max" Query parameters:
```
    if (query.getStart() > 0)
        stream = stream.skip(query.getStart());
    if (query.getMax() != Query.MAX_ALL)
        stream = stream.limit(query.getMax());
```

<a name="sample-requests"></a>
### Sample requests 
Try these against the limberest-demo _movies_ service to see how query parameters affect results:

retrieve all movies:
 - [service/movies](http://oakesville.io/limberest-demo/service/movies)

retrieve movies made in 1931:
 - [service/movies?year=1931](http://oakesville.io/limberest-demo/service/movies?year=1931)

retrieve movies made after 1935:
 - [service/movies?year=>1935](http://oakesville.io/limberest-demo/service/movies?year=>1935)
   (note > is part of query **value**)

retrieve all movies, sorted by rating in descending order:
 - [service/movies?sort=rating&descending=true](http://oakesville.io/limberest-demo/service/movies?sort=rating&descending=true)

retrieve movies with a rating of 3.5 or above, sorted by year:
 - [service/movies?rating=>=3.5&sort=year](http://oakesville.io/limberest-demo/service/movies?rating=>=3.5&sort=year)
   (note >= is prepended to query **value**)

find all movies with Bela Lugosi:
 - [service/movies?search=Bela Lugosi](http://oakesville.io/limberest-demo/service/movies?search=Bela Lugosi)

retrieve the first page of movies, with page size = 25
 - [service/movies?max=25&start=0](http://oakesville.io/limberest-demo/service/movies?max=25&start=0)

retrieve the second page of movies, with page size = 25
 - [service/movies?max=25&start=25](http://oakesville.io/limberest-demo/service/movies?max=25&start=25)

retrieve the first page of movies from a list sorted by year
 - [service/movies?max=25&sort=year](http://oakesville.io/limberest-demo/service/movies?max=25&sort=year)

retrieve the first page of movies featuring Boris Karloff, sorted by year
 - [service/movies?max=25&sort=year&search=Boris Karloff](http://oakesville.io/limberest-demo/service/movies?max=25&sort=year&search=Boris Karloff)