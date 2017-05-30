---
layout: topic
---
## Binding
Most frameworks emphasize general-purpose JSON binding.
Default is easy, customization is hard.
Limberest takes a contrary approach with its 
[Jsonable](../javadoc/io/limberest/json/Jsonable) interface.
You get the basics for free (through Java 8's `default` interface methods), but edge-cases 
may require you to implement something.  That something gives you a very straightforward 
way to exercise full control.  You can override and customize parsing and serialization 
by simply implementing a constructor and a `toJson()` method. 

Jsonables deal in [org.json.JSONObject](http://stleary.github.io/JSON-java/org/json/JSONObject.html)s.
This exposes a lightweight, ubiquitous binding protocol. By convention, every Jsonable declares a 
constructor that takes a JSONObject. Take a look at the constructor for the
[Credit](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/model/Credit.java)
class in [limberest-demo](../demo/):
```java
    public Credit(JSONObject json) {
        bind(json);
    }
```

The call to `bind()` is where the magic happens by way of Limberests autobinding mechanism.
Always call `bind()` unless you want to completely bypass autobinding, or your model class extends another
Jsonable in its inheritance chain whose constructor already calls `bind()`.  This is the case with 
[Movie](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/model/Movie.java),
whose constructor invokes `super(JSONObject)` so that its base class 
[Item](https://github.com/limberest/limberest-demo/blob/master/src/io/limberest/demo/model/Item.java)'s 
inherited properties will be autobound to any constructed Movie object.

Here's what Item's constructor looks like:
```java
    public Item(JSONObject json) {
        bind(json);
        // explicitly bind id since it has no public setter
        if (json.has("id"))
            id = json.getString("id");
    }
```

Notice how the *id* property is bound explicitly.  Limberest autobinding won't work in this case
since it relies on reflection.  Our handling of *id* shows how we can take charge of binding for
special cases where autobinding doesn't produce what we want.

The constructor convention handles JSON content on the way in.  For serializing Java objects to JSON
on the way out, Jsonables expose the `toJson()` method.  For this, again, the default behavior requires
no coding.  The Credit class does not even implement `toJson()`.  However, in Movie we override `toJson()`
to get some special behavior:
```java
    /**
     * Overridden since rating (float) is a nonstandard JSONObject type.
     */
    @Override
    public JSONObject toJson() {
      JSONObject json = super.toJson();
      if (rating > 0)
          json.put("rating", BigDecimal.valueOf(rating).setScale(1));
      else if (json.has("rating"))
          json.remove("rating"); // zero means unrated
      return json;
    }
```

Almost all of Movie's properties are autopopulated by calling `super.toJson()`.  However, we
want special handling for the *rating* property, so we override `toJson()` to take care of that field only.

Limberest autobinding cascades through contained and inherited Jsonables to build a complete JSON 
representation of your Java object model.  Movie extends Item for its properties, and contains a list
of Credit Jsonables.  The limberest-demo model object tree results in complete two-way binding between
JSON and Java.  Since Jsonable is an interface, any existing model hierarchy can be hooked up to Limberest
and exposed as a JSON REST service API.  No need to extend any concrete base class.

## JSON Property Ordering
You're probably aware that JSON object property order is not considered significant.
This is a central tenet of the JSON format specification.  Limberest makes use of a custom
[JSONObject extension](../javadoc/io/limberest/json/JsonObject) that provides repeatable, 
predictable ordering of JSON properties.  This makes it possible to write automated tests that 
evaluate outcomes by straight text comparison of formatted JSON (see the [Testing](testing) topic).
It also makes it easy for humans to perform eyeball comparisons and locate mismatches.
This is not to be construed as an indication that JSON property ordering is significant in Limberest.

