## Contributing

### Source Code

### Documentation
1. Source
   - Documentation is in the /docs directory of the master branch on GitHub
2. Local GitHub Pages
   - To test doc access before pushing, and to make changes to default layouts and styles, you can build through [Jekyll](https://help.github.com/articles/about-github-pages-and-jekyll/) locally.
   - Install Ruby 2.1.0 or higher and add its bin directory to your PATH.
   - Install Bundler:
     ```
     gem install bundler
     ```
   - Download the CURL CA Certs from http://curl.haxx.se/ca/cacert.pem and save in your Ruby installation directory.
   - Set environment variable SSL_CERT_FILE to point to this this cacert.pem file location.
   - Build GitHub pages site locally (in the /docs directory):
     ```
   bundle exec jekyll serve --incremental --baseurl ''
   ```
   - Access local site in your browser:
     http://127.0.0.1:4000/