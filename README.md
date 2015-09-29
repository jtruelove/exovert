
[![Build Status](https://travis-ci.org/cyngn/exovert.svg?branch=master)](https://travis-ci.org/cyngn/exovert)

# exovert
Is a tool that is designed to help free developers up to focus on developing services and not building the boiler plate glue code often involved with accessing a DB, creating POJOs, building CRUD interfaces. It also helps in creating REST server and client classes, or creating Java classes with setters, getters, equals/hashCode/toString, builder and Json annotations.

The tool is focused around using Cassandra for storage and Vert.x as the primary service framework. More DB options could be added based on demand at a later time.

Currently tool has two generators - CrudGenerator and RestGenerator

CrudGenerator works by reading your schema from the DB then generating the entity classes (ie Table and UDT objects), then building a DAL for them, then the REST interface.

CrudGenerator Support

* entity classes
* DAL
* CRUD REST interface
* Simple Server
* metrics support (coming soon)
* cache support (coming later)

RestGenerator works by reading the specification file from the command line and generates either server or client or type classes depending on the command line option.

RestGenerator Support

* REST API classes for GET, POST and DELETE
* Validations for API inputs
* Request and Response classes for REST APIs
* Java classes with setters, getters, equals/hashCode/toString, builder and Json annotations
* Option to generate Immutable or Mutable classes
* Request and Response classes for REST clients
* REST client builder with service endpoint, timeouts, retries (coming later)

## Getting Started

Get the command line tool.

```xml
<dependency>
    <groupId>com.cyngn.vertx</groupId>
    <artifactId>exovert</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Running CrudCreator

```bash
$ build/bin/CrudCreator
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

### Example Cassandra Scheme

As an example [ChronoServer Cassandra Schema](https://github.com/cyngn/ChronoServer/blob/master/db/scheme.cql)

```cql
// schema script
CREATE TYPE IF NOT EXISTS chrono.url_package (
   method varchar,
   urls set<varchar>
);

CREATE TABLE IF NOT EXISTS chrono.test_batch (
    name varchar,
    url_packages list<frozen<url_package>>,
    created timestamp,
    PRIMARY KEY (name)
);

CREATE TYPE IF NOT EXISTS chrono.measurement (
   url varchar,
   time_in_milli bigint
);

CREATE TABLE IF NOT EXISTS chrono.payload (
    unit varchar,
    size bigint,
    data varchar,
    PRIMARY KEY (unit, size)
);

CREATE TABLE IF NOT EXISTS chrono.upload_data (
	  test_batch varchar,
    unit varchar,
    size bigint,
    data varchar,
    created timestamp,
    PRIMARY KEY (test_batch, unit, size, created)
);

CREATE TABLE IF NOT EXISTS chrono.report (
    batch_name varchar,
    mode varchar,
    device_id varchar,
    mobile_carrier varchar,
    mobile_rssi varchar,
    wifi_state varchar,
    wifi_rssi varchar,
    gps_coordinates varchar,
    tag varchar,
    mobile_network_class varchar,
    mobile_network_type varchar,
    client_ip varchar,
    created timestamp,
    measurements list<frozen<measurement>>,
    PRIMARY KEY (batch_name, device_id, created)
);
```

### CRUD Generated Code Example

see [CRUD_README.md](https://github.com/cyngn/exovert/blob/master/src/main/java/com/cyngn/exovert/generate/CRUD_README.md) for the output code sample.

## Running RestGenerator

```bash
$ build/bin/RestCreator
```
```
--client       create the client files on disk
-f, --spec     specification file (default: api.json)
--help         shows this message
-o, --out      the output dir in which to place files
                 (default: build/generated-src)
-p, --preview  output all the java files to the
                 console, don't create files
--server       create the server files on disk
--types        create the type files on disk  
```

**Example Preview Run to create server**
```bash
build/bin/RestGenerator --preview --server --spec api.json
```
For sample api json file, look at [samples](https://github.com/cyngn/exovert/blob/master/samples/)

**Example Run to create server**
```bash
build/bin/RestGenerator --server --spec api.json --out build/generated-src
```
### REST Generated Code Example
Examples are at [REST_README.md](https://github.com/cyngn/exovert/blob/master/src/main/java/com/cyngn/exovert/generate/server/rest/README.md)

### Thanks
Especially to the [Java Poet Creators](https://github.com/square/javapoet) for making such a great and easy to use code generation library.
