### Gradle Dependency Tree
```
:dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

compile - Dependencies for source set 'main'.
+--- javax.servlet:javax.servlet-api:3.0.1
+--- javax.ws.rs:javax.ws.rs-api:2.0
+--- org.json:json:20160810
+--- org.slf4j:slf4j-api:1.7.22
+--- com.google.guava:guava:21.0
+--- commons-codec:commons-codec:1.10
+--- org.reflections:reflections:0.9.10
|    +--- com.google.guava:guava:18.0 -> 21.0
|    +--- org.javassist:javassist:3.18.2-GA
|    \--- com.google.code.findbugs:annotations:2.0.1
+--- io.swagger:swagger-annotations:1.5.13
+--- io.swagger:swagger-core:1.5.13
|    +--- org.apache.commons:commons-lang3:3.2.1
|    +--- org.slf4j:slf4j-api:1.7.22
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.8.5
|    +--- com.fasterxml.jackson.core:jackson-databind:2.8.5 -> 2.8.6
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.8.0 -> 2.8.5
|    |    \--- com.fasterxml.jackson.core:jackson-core:2.8.6
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.8.5
|    |    +--- com.fasterxml.jackson.core:jackson-core:2.8.5 -> 2.8.6
|    |    \--- org.yaml:snakeyaml:1.17
|    +--- io.swagger:swagger-models:1.5.13
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.8.5
|    |    +--- org.slf4j:slf4j-api:1.7.22
|    |    \--- io.swagger:swagger-annotations:1.5.13
|    +--- com.google.guava:guava:20.0 -> 21.0
|    \--- javax.validation:validation-api:1.1.0.Final
+--- io.swagger:swagger-models:1.5.13 (*)
+--- io.swagger:swagger-servlet:1.5.13
|    +--- io.swagger:swagger-core:1.5.13 (*)
|    +--- org.reflections:reflections:0.9.10 (*)
|    \--- com.google.guava:guava:20.0 -> 21.0
+--- com.fasterxml.jackson.core:jackson-core:2.8.6
\--- com.fasterxml.jackson.core:jackson-databind:2.8.6 (*)
```