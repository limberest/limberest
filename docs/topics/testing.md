---
layout: topic
---
## Testing
Automated API testing is the most popular feature of Limberest. When execution a test, 
you'll invoke actual REST calls to exercise *any* API (not just those built on Limberest).
Every aspect of a test can be parameterized using [Values](#values).

### [Tests](https://limberest.io/ui/testing)
Individual tests are grouped in JSON artifacts that include parameterized request information.
Limberest has its own native JSON format, but the easiest way to get started is to
[create and export a collection](https://www.getpostman.com/docs/postman/collections/creating_collections) 
in [Postman](https://www.getpostman.com/).  For example, the [limberest-demo](https://github.com/limberest/limberest-demo)
project has an exported [movies-api collection](https://github.com/limberest/limberest-demo/blob/master/test/movies-api.postman).

Every request in the collection can be run individually, or linked together in Limberest [Cases](#cases).
An intuitive way to visualize a request is through in the Limberest UI.  Here's what the 
['GET movies/{id}'](https://limberest.io/ui/testing/movies-api/GET/movies/{id}) request looks like:

!['GET movies/id' request](../img/get-movies-id-request.png)

(See ['POST movies'](https://limberest.io/ui/testing/movies-api/POST/movies) for an example of a request that includes a body.) 

When you run a test in Limberest, it generates results in [YAML](http://yaml.org/) format.  After execution, here's 
what the Result tab looks like for 'GET movies/{id}':

!['GET movies/id' result](../img/get-movies-id-result.png)

The green highlights indicate successful matching of runtime [Values](#values).  Mismatches would cause the test to fail
and be highlighted in red.

YAML gives a nicely readable rendition of the expected vs. actual result.  Notice that the result includes not only 
the response body, but also its status and headers.  If the request had a body, that would be represented as well.

The [expected result YAML](https://github.com/limberest/limberest-demo/blob/master/test/results/expected/movies-api/GET_movies_{id}.yaml) 
is retrieved from GitHub, where the results for all tests and cases are reposited.

### [Values](https://limberest.io/ui/values)
Values provide a way to repeatedly run tests with varying input.  Values files are JSON.  They can be in
[Postman environment](https://www.getpostman.com/docs/postman/environments_and_globals/manage_environments) format,
or as (simpler and preferred) straight-JSON objects.  Values can hold environment-specific parameters like these:
  - [limberest.io.values](https://github.com/limberest/limberest-demo/blob/master/test/limberest.io.values)
  - [localhost.values](https://github.com/limberest/limberest-demo/blob/master/test/localhost.values)

But values can also hold arbitrary testing data like this:
  - [global.values](https://github.com/limberest/limberest-demo/blob/master/test/global.values)
  
As illustrated above, values are referenced in result YAML using Javascript 
[template literal expressions](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Template_literals) syntax:
```
'${base-url}/movies/${id}'
``` 
In Limberest UI, values are applied by selecting their JSON source file like so:

![Values selections](../img/values-selections.png)

(applied from left to right like in Javascript's 
[Object.assign()](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/assign) function).
Values JSON does not have to be flat; it can contain nested objects referenced through expressions like `${order.customer.name.first}`. 

Feel free to apply values and try GET requests yourself in Limberest's [Demo UI](https://limberest.io/ui/testing).
Tests execute in your browser, and results are retained in [local storage](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage),
so nothing is saved back to the server.  Unless you install and run [limberest-demo](https://github.com/limberest/limberest-demo/blob/master/README.md)
locally, you won't be able to submit POST, PUT or DELETE requests since those require authorization.

You can even edit the request content or expected result YAML by clicking the pencil icon ![Values selections](../img/values-selections.png).
All your changes are saved in browser local storage, so experiment freely with successful and unsuccessful results.

### [Cases](https://limberest.io/ui/cases)




Next Topic: [Access Control](auth)
