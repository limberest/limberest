---
layout: topic
---
## Options

This javascript object shows the default values for each limberest-js option.
These can be overridden programmatically in your tests by passing to the `run()` function. 
 
```javascript
const options = {
  location: path.dirname(process.argv[1]),
  // extensions: (eg: ['.postman']
  // expectedResultLocation: (same as 'location')
  resultLocation: 'results',
  // logLocation: (same as 'resultLocation'),
  // localLocation: (indicates local override possible)
  debug: false,
  retainLog: false,  // accumulate for multiple runs
  captureResult: true,
  retainResult: false,
  prettifyResult: true,
  prettyIndent: 2,
  qualifyLocations: true, // result and log paths prefixed by group (or can be string for custom)
  overwriteExpected: false,
  // responseHeaders: (array of validated response headers, in the order they'll appear in result yaml)
};
```

TODO: Describe each option

