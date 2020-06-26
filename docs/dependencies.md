### Gradle Dependency Tree
```
:dependencies

------------------------------------------------------------
Root project
------------------------------------------------------------

runtimeClasspath - Runtime classpath of source set 'main'.
+--- javax.servlet:javax.servlet-api:3.1.0
+--- javax.ws.rs:javax.ws.rs-api:2.1
+--- javax.validation:validation-api:2.0.1.Final
+--- org.json:json:20180130
+--- org.slf4j:slf4j-api:1.7.25
+--- org.reflections:reflections:0.9.11
|    +--- com.google.guava:guava:20.0
|    \--- org.javassist:javassist:3.21.0-GA
+--- com.google.guava:guava:20.0
+--- io.swagger:swagger-annotations:1.5.19
+--- io.swagger:swagger-core:1.5.19
|    +--- org.apache.commons:commons-lang3:3.2.1
|    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.25
|    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5
|    +--- com.fasterxml.jackson.core:jackson-databind:2.9.5
|    |    \--- com.fasterxml.jackson.core:jackson-core:2.9.5
|    +--- com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.5
|    |    +--- org.yaml:snakeyaml:1.18
|    |    \--- com.fasterxml.jackson.core:jackson-core:2.9.5
|    +--- io.swagger:swagger-models:1.5.19
|    |    +--- com.fasterxml.jackson.core:jackson-annotations:2.9.5
|    |    +--- org.slf4j:slf4j-api:1.7.22 -> 1.7.25
|    |    \--- io.swagger:swagger-annotations:1.5.19
|    +--- com.google.guava:guava:20.0
|    \--- javax.validation:validation-api:1.1.0.Final -> 2.0.1.Final
+--- io.swagger:swagger-models:1.5.19 (*)
+--- io.swagger:swagger-servlet:1.5.19
|    +--- io.swagger:swagger-core:1.5.19 (*)
|    +--- org.reflections:reflections:0.9.11 (*)
|    \--- com.google.guava:guava:20.0
\--- javax.xml.bind:jaxb-api:2.2.12
```