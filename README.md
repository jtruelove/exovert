
[![Build Status](https://travis-ci.org/jtruelove/exovert.svg?branch=master)](https://travis-ci.org/jtruelove/exovert)

# exovert
Is a tool that is designed to help free developers up to focus on developing services and not building the boiler plate glue code often involved with accessing a DB, creating POJOs, or building CRUD interfaces.

The tool is focused around using Cassandra for storage and Vert.x as the primary service framework. More DB options could be added based on demand at a later time.

It works by reading your schema from the DB then generating the entity classes (ie Table and UDT objects), then building a DAL for them, then the REST interface.

Current Generator Support

* entity classes
* DAL
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

## Sample

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

### The Generated Code

```java
package com.cyngn.chrono.storage.udt;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;
import java.util.Set;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * UDT for Cassandra - url_package
 */
@UDT(
    keyspace = "chrono",
    name = "url_package"
)
public class UrlPackage {
  @Field
  @JsonProperty
  public String method;

  @Field
  @JsonProperty
  public Set<String> urls;

  public void setMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }

  public void setUrls(Set<String> urls) {
    this.urls = urls;
  }

  public Set<String> getUrls() {
    return urls;
  }

  @Override
  public String toString() {
    return "UrlPackage{" +
    "method=" + method +
    ", urls=" + urls +
    "}";
  }
}
package com.cyngn.chrono.storage.udt;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * UDT for Cassandra - measurement
 */
@UDT(
    keyspace = "chrono",
    name = "measurement"
)
public class Measurement {
  @Field
  @JsonProperty
  public String url;

  @Field("time_in_milli")
  @JsonProperty("time_in_milli")
  public Long timeInMilli;

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setTimeInMilli(Long timeInMilli) {
    this.timeInMilli = timeInMilli;
  }

  public Long getTimeInMilli() {
    return timeInMilli;
  }

  @Override
  public String toString() {
    return "Measurement{" +
    "url=" + url +
    ", timeInMilli=" + timeInMilli +
    "}";
  }
}
package com.cyngn.chrono.storage.table;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * Table for Cassandra - payload
 */
@Table(
    keyspace = "chrono",
    name = "payload"
)
public class Payload {
  @Column
  @JsonProperty
  @PartitionKey(0)
  public String unit;

  @Column
  @JsonProperty
  @ClusteringColumn(0)
  public Long size;

  @Column
  @JsonProperty
  public String data;

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getUnit() {
    return unit;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Long getSize() {
    return size;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  @Override
  public String toString() {
    return "Payload{" +
    "unit=" + unit +
    ", size=" + size +
    ", data=" + data +
    "}";
  }
}
package com.cyngn.chrono.storage.table;

import com.cyngn.chrono.storage.udt.Measurement;
import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;
import java.util.Date;
import java.util.List;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * Table for Cassandra - report
 */
@Table(
    keyspace = "chrono",
    name = "report"
)
public class Report {
  @Column("batch_name")
  @JsonProperty("batch_name")
  @PartitionKey(0)
  public String batchName;

  @Column("device_id")
  @JsonProperty("device_id")
  @ClusteringColumn(0)
  public String deviceId;

  @Column
  @JsonProperty
  @ClusteringColumn(1)
  public Date created;

  @Column("client_ip")
  @JsonProperty("client_ip")
  public String clientIp;

  @Column("gps_coordinates")
  @JsonProperty("gps_coordinates")
  public String gpsCoordinates;

  @FrozenValue
  @Column
  @JsonProperty
  public List<Measurement> measurements;

  @Column("mobile_carrier")
  @JsonProperty("mobile_carrier")
  public String mobileCarrier;

  @Column("mobile_network_class")
  @JsonProperty("mobile_network_class")
  public String mobileNetworkClass;

  @Column("mobile_network_type")
  @JsonProperty("mobile_network_type")
  public String mobileNetworkType;

  @Column("mobile_rssi")
  @JsonProperty("mobile_rssi")
  public String mobileRssi;

  @Column
  @JsonProperty
  public String mode;

  @Column
  @JsonProperty
  public String tag;

  @Column("wifi_rssi")
  @JsonProperty("wifi_rssi")
  public String wifiRssi;

  @Column("wifi_state")
  @JsonProperty("wifi_state")
  public String wifiState;

  public void setBatchName(String batchName) {
    this.batchName = batchName;
  }

  public String getBatchName() {
    return batchName;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreated() {
    return created;
  }

  public void setClientIp(String clientIp) {
    this.clientIp = clientIp;
  }

  public String getClientIp() {
    return clientIp;
  }

  public void setGpsCoordinates(String gpsCoordinates) {
    this.gpsCoordinates = gpsCoordinates;
  }

  public String getGpsCoordinates() {
    return gpsCoordinates;
  }

  public void setMeasurements(List<Measurement> measurements) {
    this.measurements = measurements;
  }

  public List<Measurement> getMeasurements() {
    return measurements;
  }

  public void setMobileCarrier(String mobileCarrier) {
    this.mobileCarrier = mobileCarrier;
  }

  public String getMobileCarrier() {
    return mobileCarrier;
  }

  public void setMobileNetworkClass(String mobileNetworkClass) {
    this.mobileNetworkClass = mobileNetworkClass;
  }

  public String getMobileNetworkClass() {
    return mobileNetworkClass;
  }

  public void setMobileNetworkType(String mobileNetworkType) {
    this.mobileNetworkType = mobileNetworkType;
  }

  public String getMobileNetworkType() {
    return mobileNetworkType;
  }

  public void setMobileRssi(String mobileRssi) {
    this.mobileRssi = mobileRssi;
  }

  public String getMobileRssi() {
    return mobileRssi;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getMode() {
    return mode;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }

  public void setWifiRssi(String wifiRssi) {
    this.wifiRssi = wifiRssi;
  }

  public String getWifiRssi() {
    return wifiRssi;
  }

  public void setWifiState(String wifiState) {
    this.wifiState = wifiState;
  }

  public String getWifiState() {
    return wifiState;
  }

  @Override
  public String toString() {
    return "Report{" +
    "batchName=" + batchName +
    ", deviceId=" + deviceId +
    ", created=" + created +
    ", clientIp=" + clientIp +
    ", gpsCoordinates=" + gpsCoordinates +
    ", measurements=" + measurements +
    ", mobileCarrier=" + mobileCarrier +
    ", mobileNetworkClass=" + mobileNetworkClass +
    ", mobileNetworkType=" + mobileNetworkType +
    ", mobileRssi=" + mobileRssi +
    ", mode=" + mode +
    ", tag=" + tag +
    ", wifiRssi=" + wifiRssi +
    ", wifiState=" + wifiState +
    "}";
  }
}
package com.cyngn.chrono.storage.table;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.util.Date;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * Table for Cassandra - upload_data
 */
@Table(
    keyspace = "chrono",
    name = "upload_data"
)
public class UploadData {
  @Column("test_batch")
  @JsonProperty("test_batch")
  @PartitionKey(0)
  public String testBatch;

  @Column
  @JsonProperty
  @ClusteringColumn(0)
  public String unit;

  @Column
  @JsonProperty
  @ClusteringColumn(1)
  public Long size;

  @Column
  @JsonProperty
  @ClusteringColumn(2)
  public Date created;

  @Column
  @JsonProperty
  public String data;

  public void setTestBatch(String testBatch) {
    this.testBatch = testBatch;
  }

  public String getTestBatch() {
    return testBatch;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public String getUnit() {
    return unit;
  }

  public void setSize(Long size) {
    this.size = size;
  }

  public Long getSize() {
    return size;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreated() {
    return created;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getData() {
    return data;
  }

  @Override
  public String toString() {
    return "UploadData{" +
    "testBatch=" + testBatch +
    ", unit=" + unit +
    ", size=" + size +
    ", created=" + created +
    ", data=" + data +
    "}";
  }
}
package com.cyngn.chrono.storage.table;

import com.cyngn.chrono.storage.udt.UrlPackage;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;
import java.util.Date;
import java.util.List;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * Table for Cassandra - test_batch
 */
@Table(
    keyspace = "chrono",
    name = "test_batch"
)
public class TestBatch {
  @Column
  @JsonProperty
  @PartitionKey(0)
  public String name;

  @Column
  @JsonProperty
  public Date created;

  @FrozenValue
  @Column("url_packages")
  @JsonProperty("url_packages")
  public List<UrlPackage> urlPackages;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public Date getCreated() {
    return created;
  }

  public void setUrlPackages(List<UrlPackage> urlPackages) {
    this.urlPackages = urlPackages;
  }

  public List<UrlPackage> getUrlPackages() {
    return urlPackages;
  }

  @Override
  public String toString() {
    return "TestBatch{" +
    "name=" + name +
    ", created=" + created +
    ", urlPackages=" + urlPackages +
    "}";
  }
}
package com.cyngn.chrono.storage.dal;

import java.lang.Boolean;
import java.lang.Object;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * Common interface for all DAL classes
 */
public interface CommonDal<T> {
  void save(T entity, Consumer<Boolean> onComplete);

  void get(BiConsumer<Boolean, T> onComplete, Object... primaryKeys);

  void delete(Consumer<Boolean> onComplete, Object... primaryKeys);

  void delete(T entity, Consumer<Boolean> onComplete);
}
package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.Payload;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * DAL for Cassandra entity - Payload
 */
public class PayloadDal implements CommonDal<Payload> {
  static final Logger logger = LoggerFactory.getLogger(PayloadDal.class);

  final CassandraSession session;

  final VertxMapper<Payload> mapper;

  public PayloadDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(Payload.class);
  }

  /**
   * Save a Payload object.
   */
  public void save(Payload payloadObj, Consumer<Boolean> onComplete) {
    logger.info("save - {}", payloadObj);

    mapper.saveAsync(payloadObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", payloadObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a Payload object.
   */
  public void delete(Payload payloadObj, Consumer<Boolean> onComplete) {
    logger.info("delete - {}", payloadObj);

    mapper.deleteAsync(payloadObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", payloadObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a Payload object by key.
   */
  public void delete(Consumer<Boolean> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(false);
      }
    }, primaryKey);
  }

  /**
   * Get a Payload object by primary key.
   */
  public void get(BiConsumer<Boolean, Payload> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<Payload>() {
      @Override
      public void onSuccess(Payload result) {
        onComplete.accept(true, result);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(false, null);
      }
    }, primaryKey);
  }
}
package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.Report;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * DAL for Cassandra entity - Report
 */
public class ReportDal implements CommonDal<Report> {
  static final Logger logger = LoggerFactory.getLogger(ReportDal.class);

  final CassandraSession session;

  final VertxMapper<Report> mapper;

  public ReportDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(Report.class);
  }

  /**
   * Save a Report object.
   */
  public void save(Report reportObj, Consumer<Boolean> onComplete) {
    logger.info("save - {}", reportObj);

    mapper.saveAsync(reportObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", reportObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a Report object.
   */
  public void delete(Report reportObj, Consumer<Boolean> onComplete) {
    logger.info("delete - {}", reportObj);

    mapper.deleteAsync(reportObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", reportObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a Report object by key.
   */
  public void delete(Consumer<Boolean> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(false);
      }
    }, primaryKey);
  }

  /**
   * Get a Report object by primary key.
   */
  public void get(BiConsumer<Boolean, Report> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<Report>() {
      @Override
      public void onSuccess(Report result) {
        onComplete.accept(true, result);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(false, null);
      }
    }, primaryKey);
  }
}
package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.UploadData;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * DAL for Cassandra entity - UploadData
 */
public class UploadDataDal implements CommonDal<UploadData> {
  static final Logger logger = LoggerFactory.getLogger(UploadDataDal.class);

  final CassandraSession session;

  final VertxMapper<UploadData> mapper;

  public UploadDataDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(UploadData.class);
  }

  /**
   * Save a UploadData object.
   */
  public void save(UploadData uploadDataObj, Consumer<Boolean> onComplete) {
    logger.info("save - {}", uploadDataObj);

    mapper.saveAsync(uploadDataObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", uploadDataObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a UploadData object.
   */
  public void delete(UploadData uploadDataObj, Consumer<Boolean> onComplete) {
    logger.info("delete - {}", uploadDataObj);

    mapper.deleteAsync(uploadDataObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", uploadDataObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a UploadData object by key.
   */
  public void delete(Consumer<Boolean> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(false);
      }
    }, primaryKey);
  }

  /**
   * Get a UploadData object by primary key.
   */
  public void get(BiConsumer<Boolean, UploadData> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<UploadData>() {
      @Override
      public void onSuccess(UploadData result) {
        onComplete.accept(true, result);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(false, null);
      }
    }, primaryKey);
  }
}
package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.TestBatch;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Boolean;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-01T06:04:48.824Z
 *
 * DAL for Cassandra entity - TestBatch
 */
public class TestBatchDal implements CommonDal<TestBatch> {
  static final Logger logger = LoggerFactory.getLogger(TestBatchDal.class);

  final CassandraSession session;

  final VertxMapper<TestBatch> mapper;

  public TestBatchDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(TestBatch.class);
  }

  /**
   * Save a TestBatch object.
   */
  public void save(TestBatch testBatchObj, Consumer<Boolean> onComplete) {
    logger.info("save - {}", testBatchObj);

    mapper.saveAsync(testBatchObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", testBatchObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a TestBatch object.
   */
  public void delete(TestBatch testBatchObj, Consumer<Boolean> onComplete) {
    logger.info("delete - {}", testBatchObj);

    mapper.deleteAsync(testBatchObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", testBatchObj, error);
        onComplete.accept(false);
      }
    });
  }

  /**
   * Delete a TestBatch object by key.
   */
  public void delete(Consumer<Boolean> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(true);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(false);
      }
    }, primaryKey);
  }

  /**
   * Get a TestBatch object by primary key.
   */
  public void get(BiConsumer<Boolean, TestBatch> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<TestBatch>() {
      @Override
      public void onSuccess(TestBatch result) {
        onComplete.accept(true, result);
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(false, null);
      }
    }, primaryKey);
  }
}
```

### Thanks
Especially to the [Java Poet Creators](https://github.com/square/javapoet) for making such a great and easy to use code generation library.
