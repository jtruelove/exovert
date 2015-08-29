
[![Build Status](https://travis-ci.org/jtruelove/exovert.svg?branch=master)](https://travis-ci.org/jtruelove/exovert)

# exovert
Is a tool that is designed to help free developers up to focus on developing services and not building the boiler plate glue code often involved with accessing a DB, creating POJOs, or building CRUD interfaces.

The tool is focused around using Cassandra for storage and Vert.x as the primary service framework. More DB options could be added based on demand at a later time.

It works by reading your schema from the DB then generating the entity classes (ie Table and UDT objects), then building a DAL for them, then the REST interface.

Current Generator Support

* entity classes
* DAL (coming soon)
* REST interface (coming soon)
* cache support (coming later)

## Getting Started

Get the command line tool.

```xml
<dependency>
    <groupId>com.cyngn.vertx</groupId>
    <artifactId>exovert</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Running it

```bash
$ java -jar build/libs/exovert-0.1.0-fat.jar
```
```
Option           Description
------           -----------
-c, --create     create the basic service infrastructure
-d, --db         the db host to connect to
--help           shows this message
-k, --keyspace   the keyspace to read from
-n, --namespace  the namespace to create java classes in
-o, --out        the output dir to place files in
-p, --preview    output all the java files to the
                   console, don't create files
-r, --rest       generate the REST API for the scheme
```

**Example Preview Run**
```bash
java -jar exovert-0.1.0-fat.jar --preview -k test_keyspace -db localhost -n com.test
```

**Example Run**
```bash
java -jar exovert-0.1.0-fat.jar --create -k test_keyspace -db localhost -n com.test -o src/java/generated
```

### Thanks
Especially to the [Java Poet Creators](https://github.com/square/javapoet) for making such a great and easy to use code generation library.
