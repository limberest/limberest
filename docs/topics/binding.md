---
layout: topic
---
## Binding
Most frameworks emphasize general-purpose JSON binding.
Default is easy, customization is hard.
Limberest takes the opposite approach with its [Jsonable]({{site.baseurl}}/javadoc/io/limberest/json/Jsonable) interface.
You get the basics for free (through Java 8's `default` implementation), but edge-cases require
you to implement something.  The good news is that the something you implement provides an easy way for 
you to exercise full control.  You customize parsing and serialization by implementing a constructor and the toJson() method. 

Here's how it works:
Only dependency is org.json.JSONObject.
Cascading to contained objects and supers.
By convention, constructor takes JSONObject.

The Jsonable interface takes advantage of Java 8's `default` methods to provide 

JSON property order not significant or even necessarily predictable,
but repeatable.  This makes it possible to write automated tests that
evaluate outcomes by straight text comparison of formatted JSON.
It also makes it easy for humans to perform eyeball comparisons and 
locate mismatches.

