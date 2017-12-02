---
layout: topic
---
## Requests
Automated API testing is the most versatile feature of Limberest. When executing a test, 
you'll invoke actual REST calls to exercise *any* API (not just those built on Limberest).
Every aspect of a test can be parameterized using [Values](values).

Requests are the reusable building blocks you'll arrange to create test sequences.
Each request represents an HTTP request with a parameterized endpoint, header and payload.
Limberest has its own native JSON format, but the easiest way to get started is to
[create and export a collection](https://www.getpostman.com/docs/postman/collections/creating_collections) 
in [Postman](https://www.getpostman.com/).  As an example, the [limberest-demo](https://github.com/limberest/limberest-demo)
project has an exported [movies-api collection](https://github.com/limberest/limberest-demo/blob/master/test/movies-api.postman).

Every request in the collection can be run individually, or linked together in Limberest [Cases](cases).
An intuitive way to visualize a request is through [Limberest UI](https://limberest.io/ui/requests).
Here's what the [`GET movies/{id}`](https://limberest.io/ui/requests/movies-api/GET/movies/{id}) request looks like:

!['GET movies/id' request](../img/get-movies-id-request.png)

(Take a look at [`POST movies`](https://limberest.io/ui/requests/movies-api/POST/movies) for an example that includes a JSON body.)

Next Topic: [Results](results)
