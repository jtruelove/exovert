
## Entity classes

```java
package com.cyngn.chrono.storage.udt;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.Override;
import java.lang.String;
import java.util.Set;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * UDT class for Cassandra - url_package
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
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * UDT class for Cassandra - measurement
 */
@UDT(
    keyspace = "chrono",
    name = "measurement"
)
public class Measurement {
  @Field
  @JsonProperty
  public String url;

  @Field(name = "time_in_milli")
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
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Table class for Cassandra - payload
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
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Table class for Cassandra - report
 */
@Table(
    keyspace = "chrono",
    name = "report"
)
public class Report {
  @Column(name = "batch_name")
  @JsonProperty("batch_name")
  @PartitionKey(0)
  public String batchName;

  @Column(name = "device_id")
  @JsonProperty("device_id")
  @ClusteringColumn(0)
  public String deviceId;

  @Column
  @JsonProperty
  @ClusteringColumn(1)
  public Date created;

  @Column(name = "client_ip")
  @JsonProperty("client_ip")
  public String clientIp;

  @Column(name = "gps_coordinates")
  @JsonProperty("gps_coordinates")
  public String gpsCoordinates;

  @FrozenValue
  @Column
  @JsonProperty
  public List<Measurement> measurements;

  @Column(name = "mobile_carrier")
  @JsonProperty("mobile_carrier")
  public String mobileCarrier;

  @Column(name = "mobile_network_class")
  @JsonProperty("mobile_network_class")
  public String mobileNetworkClass;

  @Column(name = "mobile_network_type")
  @JsonProperty("mobile_network_type")
  public String mobileNetworkType;

  @Column(name = "mobile_rssi")
  @JsonProperty("mobile_rssi")
  public String mobileRssi;

  @Column
  @JsonProperty
  public String mode;

  @Column
  @JsonProperty
  public String tag;

  @Column(name = "wifi_rssi")
  @JsonProperty("wifi_rssi")
  public String wifiRssi;

  @Column(name = "wifi_state")
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
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Table class for Cassandra - upload_data
 */
@Table(
    keyspace = "chrono",
    name = "upload_data"
)
public class UploadData {
  @Column(name = "test_batch")
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
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Table class for Cassandra - test_batch
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
  @Column(name = "url_packages")
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
```

## DAL classes

```java
package com.cyngn.chrono.storage.dal;

import com.cyngn.vertx.async.ResultContext;
import java.lang.Object;
import java.util.function.Consumer;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Common interface for all DAL classes
 */
public interface CommonDal<T> {
  void save(T entity, Consumer<ResultContext> onComplete);

  void get(Consumer<ResultContext<T>> onComplete, Object... primaryKeys);

  void delete(Consumer<ResultContext> onComplete, Object... primaryKeys);

  void delete(T entity, Consumer<ResultContext> onComplete);
}

package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.Payload;
import com.cyngn.vertx.async.ResultContext;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * DAL for Cassandra entity - {@link com.cyngn.chrono.storage.table.Payload}
 */
public class PayloadDal implements CommonDal<Payload> {
  private static final Logger logger = LoggerFactory.getLogger(PayloadDal.class);

  final CassandraSession session;

  final VertxMapper<Payload> mapper;

  public PayloadDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(Payload.class);
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Payload} object.
   */
  public void save(Payload payloadObj, Consumer<ResultContext> onComplete) {
    logger.info("save - {}", payloadObj);

    mapper.saveAsync(payloadObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", payloadObj, error);
        onComplete.accept(new ResultContext(error, "Failed to save Payload: " +  payloadObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Payload} object.
   */
  public void delete(Payload payloadObj, Consumer<ResultContext> onComplete) {
    logger.info("delete - {}", payloadObj);

    mapper.deleteAsync(payloadObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", payloadObj, error);
        onComplete.accept(new ResultContext(error, "Failed to delete Payload: " +  payloadObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Payload} object by key.
   */
  public void delete(Consumer<ResultContext> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to delete Payload by key: " +  primaryKey));
      }
    }, primaryKey);
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.Payload} object by primary key.
   */
  public void get(Consumer<ResultContext<Payload>> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<Payload>() {
      @Override
      public void onSuccess(Payload result) {
        onComplete.accept(new ResultContext(true, result));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to get Payload by key: " +  primaryKey));
      }
    }, primaryKey);
  }
}

package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.Report;
import com.cyngn.vertx.async.ResultContext;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * DAL for Cassandra entity - {@link com.cyngn.chrono.storage.table.Report}
 */
public class ReportDal implements CommonDal<Report> {
  private static final Logger logger = LoggerFactory.getLogger(ReportDal.class);

  final CassandraSession session;

  final VertxMapper<Report> mapper;

  public ReportDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(Report.class);
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Report} object.
   */
  public void save(Report reportObj, Consumer<ResultContext> onComplete) {
    logger.info("save - {}", reportObj);

    mapper.saveAsync(reportObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", reportObj, error);
        onComplete.accept(new ResultContext(error, "Failed to save Report: " +  reportObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Report} object.
   */
  public void delete(Report reportObj, Consumer<ResultContext> onComplete) {
    logger.info("delete - {}", reportObj);

    mapper.deleteAsync(reportObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", reportObj, error);
        onComplete.accept(new ResultContext(error, "Failed to delete Report: " +  reportObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Report} object by key.
   */
  public void delete(Consumer<ResultContext> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to delete Report by key: " +  primaryKey));
      }
    }, primaryKey);
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.Report} object by primary key.
   */
  public void get(Consumer<ResultContext<Report>> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<Report>() {
      @Override
      public void onSuccess(Report result) {
        onComplete.accept(new ResultContext(true, result));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to get Report by key: " +  primaryKey));
      }
    }, primaryKey);
  }
}

package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.UploadData;
import com.cyngn.vertx.async.ResultContext;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * DAL for Cassandra entity - {@link com.cyngn.chrono.storage.table.UploadData}
 */
public class UploadDataDal implements CommonDal<UploadData> {
  private static final Logger logger = LoggerFactory.getLogger(UploadDataDal.class);

  final CassandraSession session;

  final VertxMapper<UploadData> mapper;

  public UploadDataDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(UploadData.class);
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.UploadData} object.
   */
  public void save(UploadData uploadDataObj, Consumer<ResultContext> onComplete) {
    logger.info("save - {}", uploadDataObj);

    mapper.saveAsync(uploadDataObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", uploadDataObj, error);
        onComplete.accept(new ResultContext(error, "Failed to save UploadData: " +  uploadDataObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.UploadData} object.
   */
  public void delete(UploadData uploadDataObj, Consumer<ResultContext> onComplete) {
    logger.info("delete - {}", uploadDataObj);

    mapper.deleteAsync(uploadDataObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", uploadDataObj, error);
        onComplete.accept(new ResultContext(error, "Failed to delete UploadData: " +  uploadDataObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.UploadData} object by key.
   */
  public void delete(Consumer<ResultContext> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to delete UploadData by key: " +  primaryKey));
      }
    }, primaryKey);
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.UploadData} object by primary key.
   */
  public void get(Consumer<ResultContext<UploadData>> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<UploadData>() {
      @Override
      public void onSuccess(UploadData result) {
        onComplete.accept(new ResultContext(true, result));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to get UploadData by key: " +  primaryKey));
      }
    }, primaryKey);
  }
}

package com.cyngn.chrono.storage.dal;

import com.cyngn.chrono.storage.table.TestBatch;
import com.cyngn.vertx.async.ResultContext;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import java.lang.Object;
import java.lang.Override;
import java.lang.Throwable;
import java.lang.Void;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * DAL for Cassandra entity - {@link com.cyngn.chrono.storage.table.TestBatch}
 */
public class TestBatchDal implements CommonDal<TestBatch> {
  private static final Logger logger = LoggerFactory.getLogger(TestBatchDal.class);

  final CassandraSession session;

  final VertxMapper<TestBatch> mapper;

  public TestBatchDal(CassandraSession session) {
    this.session = session;
    VertxMappingManager manager = new DefaultVertxMappingManager(session);
    mapper = manager.mapper(TestBatch.class);
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   */
  public void save(TestBatch testBatchObj, Consumer<ResultContext> onComplete) {
    logger.info("save - {}", testBatchObj);

    mapper.saveAsync(testBatchObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("save - {}, ex: ", testBatchObj, error);
        onComplete.accept(new ResultContext(error, "Failed to save TestBatch: " +  testBatchObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   */
  public void delete(TestBatch testBatchObj, Consumer<ResultContext> onComplete) {
    logger.info("delete - {}", testBatchObj);

    mapper.deleteAsync(testBatchObj, new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", testBatchObj, error);
        onComplete.accept(new ResultContext(error, "Failed to delete TestBatch: " +  testBatchObj));
      }
    });
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.TestBatch} object by key.
   */
  public void delete(Consumer<ResultContext> onComplete, Object... primaryKey) {
    logger.info("delete - {}", primaryKey);

    mapper.deleteAsync(new FutureCallback<Void>() {
      @Override
      public void onSuccess(Void result) {
        onComplete.accept(new ResultContext(true));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("delete - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to delete TestBatch by key: " +  primaryKey));
      }
    }, primaryKey);
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.TestBatch} object by primary key.
   */
  public void get(Consumer<ResultContext<TestBatch>> onComplete, Object... primaryKey) {
    logger.info("get - {}", primaryKey);

    mapper.getAsync(new FutureCallback<TestBatch>() {
      @Override
      public void onSuccess(TestBatch result) {
        onComplete.accept(new ResultContext(true, result));
      }

      @Override
      public void onFailure(Throwable error) {
        logger.error("get - {}, ex: ", primaryKey, error);
        onComplete.accept(new ResultContext(error, "Failed to get TestBatch by key: " +  primaryKey));
      }
    }, primaryKey);
  }
}

package com.cyngn.chrono.rest;

import com.cyngn.vertx.web.HttpHelper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import java.lang.Object;
import java.lang.String;
import org.apache.commons.lang.StringUtils;

/**
 * Central place to put shared functions for REST call processing.
 */
public class RestUtil {
  /**
   * Does the query string of this request contain the full primary key?
   */
  public static Object[] isValid(HttpServerRequest request, String[] primaryKey) {
    Object[] queryKey = new Object[primaryKey.length];
    String error = null;
    for(int i = 0; i < primaryKey.length; i++) {
      String key = primaryKey[i];
      String value = request.getParam(key);
      if(StringUtils.isEmpty(value)) {
        error = "You must supply parameter: " + key;
        HttpHelper.processErrorResponse(error, request.response(), HttpResponseStatus.BAD_REQUEST.code());
        break;
      } else {
        queryKey[i] = value;
      }
    }
    return StringUtils.isEmpty(error) ? queryKey : null;
  }
}
```

## REST API

```java
package com.cyngn.chrono.rest;

import com.cyngn.chrono.storage.dal.PayloadDal;
import com.cyngn.chrono.storage.table.Payload;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * REST Api for Cassandra entity - {@link com.cyngn.chrono.storage.table.Payload}
 */
public class PayloadApi implements RestApi {
  private static final Logger logger = LoggerFactory.getLogger(PayloadApi.class);

  public static final String PAYLOAD_API = "/api/v1/payload";

  private final PayloadDal storage;

  private final RestApi.RestApiDescriptor[] supportedApi =  {
    new RestApi.RestApiDescriptor(HttpMethod.POST, PAYLOAD_API, this::save),
    new RestApi.RestApiDescriptor(HttpMethod.GET, PAYLOAD_API, this::get),
    new RestApi.RestApiDescriptor(HttpMethod.DELETE, PAYLOAD_API, this::delete)
  };

  final String[] primaryKey;

  public PayloadApi(PayloadDal storage) {
    this.storage = storage;
    primaryKey = new String[] {
      "unit",
      "size"
    } ;
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Payload} object.
   */
  public void save(RoutingContext context) {
    HttpServerRequest request = context.request();
    if(request.isEnded()) {
      save(context, context.getBody());
    } else {
      request.bodyHandler(buffer -> save(context, buffer));
    }
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Payload} object.
   *
   * NOTE: this method is left intentionally package protected to allow you to call it in a different way
   */
  void save(RoutingContext context, Buffer body) {
    Payload entity = JsonUtil.parseJsonToObject(body.toString(), Payload.class);
    if(entity == null) {
      HttpHelper.processErrorResponse("Failed to parse body: " + body, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      return;
    }

    storage.save(entity, result ->  {
      if(result.succeeded) {
        HttpHelper.processResponse(context.response());
      } else if(result.error != null) {
        String error = "Could not persist " + entity.toString() + ", error: " + result.error.getMessage();
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      } else {
        String error = "Could not persist " + entity.toString() + ", error: " + result.errorMessage;
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      }
    } );
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Payload} object.
   */
  public void delete(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.delete(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(context.response());
        } else if (result.error != null) {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("delete - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("delete - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.Payload} object.
   */
  public void get(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.get(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(result.value, context.response());
        } else if(result.error != null) {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("get - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("get - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  @Override
  public RestApi.RestApiDescriptor[] supportedApi() {
    return supportedApi;
  }
}

package com.cyngn.chrono.rest;

import com.cyngn.chrono.storage.dal.ReportDal;
import com.cyngn.chrono.storage.table.Report;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * REST Api for Cassandra entity - {@link com.cyngn.chrono.storage.table.Report}
 */
public class ReportApi implements RestApi {
  private static final Logger logger = LoggerFactory.getLogger(ReportApi.class);

  public static final String REPORT_API = "/api/v1/report";

  private final ReportDal storage;

  private final RestApi.RestApiDescriptor[] supportedApi =  {
    new RestApi.RestApiDescriptor(HttpMethod.POST, REPORT_API, this::save),
    new RestApi.RestApiDescriptor(HttpMethod.GET, REPORT_API, this::get),
    new RestApi.RestApiDescriptor(HttpMethod.DELETE, REPORT_API, this::delete)
  };

  final String[] primaryKey;

  public ReportApi(ReportDal storage) {
    this.storage = storage;
    primaryKey = new String[] {
      "batch_name",
      "device_id",
      "created"
    } ;
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Report} object.
   */
  public void save(RoutingContext context) {
    HttpServerRequest request = context.request();
    if(request.isEnded()) {
      save(context, context.getBody());
    } else {
      request.bodyHandler(buffer -> save(context, buffer));
    }
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.Report} object.
   *
   * NOTE: this method is left intentionally package protected to allow you to call it in a different way
   */
  void save(RoutingContext context, Buffer body) {
    Report entity = JsonUtil.parseJsonToObject(body.toString(), Report.class);
    if(entity == null) {
      HttpHelper.processErrorResponse("Failed to parse body: " + body, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      return;
    }

    storage.save(entity, result ->  {
      if(result.succeeded) {
        HttpHelper.processResponse(context.response());
      } else if(result.error != null) {
        String error = "Could not persist " + entity.toString() + ", error: " + result.error.getMessage();
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      } else {
        String error = "Could not persist " + entity.toString() + ", error: " + result.errorMessage;
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      }
    } );
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.Report} object.
   */
  public void delete(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.delete(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(context.response());
        } else if (result.error != null) {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("delete - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("delete - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.Report} object.
   */
  public void get(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.get(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(result.value, context.response());
        } else if(result.error != null) {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("get - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("get - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  @Override
  public RestApi.RestApiDescriptor[] supportedApi() {
    return supportedApi;
  }
}

package com.cyngn.chrono.rest;

import com.cyngn.chrono.storage.dal.UploadDataDal;
import com.cyngn.chrono.storage.table.UploadData;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * REST Api for Cassandra entity - {@link com.cyngn.chrono.storage.table.UploadData}
 */
public class UploadDataApi implements RestApi {
  private static final Logger logger = LoggerFactory.getLogger(UploadDataApi.class);

  public static final String UPLOAD_DATA_API = "/api/v1/upload_data";

  private final UploadDataDal storage;

  private final RestApi.RestApiDescriptor[] supportedApi =  {
    new RestApi.RestApiDescriptor(HttpMethod.POST, UPLOAD_DATA_API, this::save),
    new RestApi.RestApiDescriptor(HttpMethod.GET, UPLOAD_DATA_API, this::get),
    new RestApi.RestApiDescriptor(HttpMethod.DELETE, UPLOAD_DATA_API, this::delete)
  };

  final String[] primaryKey;

  public UploadDataApi(UploadDataDal storage) {
    this.storage = storage;
    primaryKey = new String[] {
      "test_batch",
      "unit",
      "size",
      "created"
    } ;
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.UploadData} object.
   */
  public void save(RoutingContext context) {
    HttpServerRequest request = context.request();
    if(request.isEnded()) {
      save(context, context.getBody());
    } else {
      request.bodyHandler(buffer -> save(context, buffer));
    }
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.UploadData} object.
   *
   * NOTE: this method is left intentionally package protected to allow you to call it in a different way
   */
  void save(RoutingContext context, Buffer body) {
    UploadData entity = JsonUtil.parseJsonToObject(body.toString(), UploadData.class);
    if(entity == null) {
      HttpHelper.processErrorResponse("Failed to parse body: " + body, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      return;
    }

    storage.save(entity, result ->  {
      if(result.succeeded) {
        HttpHelper.processResponse(context.response());
      } else if(result.error != null) {
        String error = "Could not persist " + entity.toString() + ", error: " + result.error.getMessage();
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      } else {
        String error = "Could not persist " + entity.toString() + ", error: " + result.errorMessage;
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      }
    } );
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.UploadData} object.
   */
  public void delete(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.delete(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(context.response());
        } else if (result.error != null) {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("delete - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("delete - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.UploadData} object.
   */
  public void get(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.get(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(result.value, context.response());
        } else if(result.error != null) {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("get - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("get - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  @Override
  public RestApi.RestApiDescriptor[] supportedApi() {
    return supportedApi;
  }
}

package com.cyngn.chrono.rest;

import com.cyngn.chrono.storage.dal.TestBatchDal;
import com.cyngn.chrono.storage.table.TestBatch;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.JsonUtil;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * REST Api for Cassandra entity - {@link com.cyngn.chrono.storage.table.TestBatch}
 */
public class TestBatchApi implements RestApi {
  private static final Logger logger = LoggerFactory.getLogger(TestBatchApi.class);

  public static final String TEST_BATCH_API = "/api/v1/test_batch";

  private final TestBatchDal storage;

  private final RestApi.RestApiDescriptor[] supportedApi =  {
    new RestApi.RestApiDescriptor(HttpMethod.POST, TEST_BATCH_API, this::save),
    new RestApi.RestApiDescriptor(HttpMethod.GET, TEST_BATCH_API, this::get),
    new RestApi.RestApiDescriptor(HttpMethod.DELETE, TEST_BATCH_API, this::delete)
  };

  final String[] primaryKey;

  public TestBatchApi(TestBatchDal storage) {
    this.storage = storage;
    primaryKey = new String[] {
      "name"
    } ;
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   */
  public void save(RoutingContext context) {
    HttpServerRequest request = context.request();
    if(request.isEnded()) {
      save(context, context.getBody());
    } else {
      request.bodyHandler(buffer -> save(context, buffer));
    }
  }

  /**
   * Save a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   *
   * NOTE: this method is left intentionally package protected to allow you to call it in a different way
   */
  void save(RoutingContext context, Buffer body) {
    TestBatch entity = JsonUtil.parseJsonToObject(body.toString(), TestBatch.class);
    if(entity == null) {
      HttpHelper.processErrorResponse("Failed to parse body: " + body, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      return;
    }

    storage.save(entity, result ->  {
      if(result.succeeded) {
        HttpHelper.processResponse(context.response());
      } else if(result.error != null) {
        String error = "Could not persist " + entity.toString() + ", error: " + result.error.getMessage();
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
      } else {
        String error = "Could not persist " + entity.toString() + ", error: " + result.errorMessage;
        logger.error("save - {}",  error);
        HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
      }
    } );
  }

  /**
   * Delete a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   */
  public void delete(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.delete(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(context.response());
        } else if (result.error != null) {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("delete - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not delete key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("delete - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  /**
   * Get a {@link com.cyngn.chrono.storage.table.TestBatch} object.
   */
  public void get(RoutingContext context) {
    // if query params aren't valid a HttpResponseStatus.BAD_REQUEST will be sent with the missing field
    Object[] queryKey = RestUtil.isValid(context.request(), primaryKey);

    if(queryKey != null) {
      storage.get(result ->  {
        if(result.succeeded) {
          HttpHelper.processResponse(result.value, context.response());
        } else if(result.error != null) {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.error.getMessage();
          logger.error("get - {}",  error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        } else {
          String error = "Could not get key: {" + StringUtils.join(queryKey, ",") + "}, error: " + result.errorMessage;
          logger.error("get - {}", error);
          HttpHelper.processErrorResponse(error, context.response(), HttpResponseStatus.BAD_REQUEST.code());
        }
      } , queryKey);
    }
  }

  @Override
  public RestApi.RestApiDescriptor[] supportedApi() {
    return supportedApi;
  }
}

package com.cyngn.chrono;

import com.cyngn.chrono.rest.PayloadApi;
import com.cyngn.chrono.rest.ReportApi;
import com.cyngn.chrono.rest.TestBatchApi;
import com.cyngn.chrono.rest.UploadDataApi;
import com.cyngn.chrono.storage.dal.PayloadDal;
import com.cyngn.chrono.storage.dal.ReportDal;
import com.cyngn.chrono.storage.dal.TestBatchDal;
import com.cyngn.chrono.storage.dal.UploadDataDal;
import com.cyngn.vertx.web.RestApi;
import com.cyngn.vertx.web.RouterTools;
import com.datastax.driver.core.Cluster;
import com.englishtown.vertx.cassandra.impl.DefaultCassandraSession;
import com.englishtown.vertx.cassandra.impl.JsonCassandraConfigurator;
import com.google.common.collect.Lists;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.Thread;
import java.lang.Void;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GENERATED CODE DO NOT MODIFY - last updated: 2015-09-10T15:47:30.673Z
 *
 * Simple server that registers all {@link com.cyngn.vertx.web.RestApi} for CRUD operations.
 *
 * to build: ./gradlew clean shadowJar
 * to run: java -jar build/libs/[project-name]-fat.jar -conf [your_conf.json]
 */
public class Server extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private static final String SHARED_DATA_KEY = "shared_data";

  private static final String INITIALIZER_THREAD_KEY = "initializer_thread";

  private LocalMap<String, Long> sharedData;

  private HttpServer server;

  private DefaultCassandraSession session;

  private int port;

  @Override
  public void start(final Future<Void> startedResult) {
    JsonObject config = config();

    if(!config.containsKey("cassandra")) { stop(); }

    sharedData = vertx.sharedData().getLocalMap(SHARED_DATA_KEY);
    sharedData.putIfAbsent(INITIALIZER_THREAD_KEY, Thread.currentThread().getId());
    session = new DefaultCassandraSession(Cluster.builder(), new JsonCassandraConfigurator(vertx), vertx);
    port = config.getInteger("port", 80);

    if(isInitializerThread()) {
      try {
        logger.info("Starting up server... on ip: {} port: {}", InetAddress.getLocalHost().getHostAddress(), port);
      } catch(UnknownHostException ex) {
        logger.error("Failed to get host ip address, ex: ", ex);
        stop();
      }
    }

    startServer();
    startedResult.complete();
  }

  public boolean isInitializerThread() {
    return sharedData.get(INITIALIZER_THREAD_KEY) == Thread.currentThread().getId();
  }

  private void buildApi(Router router) {
    RouterTools.registerRootHandlers(router, LoggerHandler.create());

    List<RestApi> apis = Lists.newArrayList(
      new PayloadApi(new PayloadDal(session)),
      new ReportApi(new ReportDal(session)),
      new UploadDataApi(new UploadDataDal(session)),
      new TestBatchApi(new TestBatchDal(session))
    );

    for(RestApi api: apis) {
      api.init(router);
      if(isInitializerThread()) {api.outputApi(logger);}
    }
  }

  private void startServer() {
    server = vertx.createHttpServer();
    Router router = Router.router(vertx);
    buildApi(router);
    server.requestHandler(router::accept);

    server.listen(port, "0.0.0.0", event ->  {
      if(event.failed()) {
        logger.error("Failed to start server, error: ", event.cause());
        stop();
      } else {
        logger.info("Thread: {} starting to handle request", Thread.currentThread().getId());
      }
    } );
  }

  @Override
  public void stop() {
    logger.info("Stopping the server.");
    try {
      if(server != null) { server.close(); }
    } finally {
      //make sure only one thread tries to shutdown.
      Long shutdownThreadId = sharedData.putIfAbsent("shutdown", Thread.currentThread().getId());
      if(shutdownThreadId == null) {
        vertx.close(event -> {
          logger.info("Vertx shutdown");
          System.exit(-1);
        } );
      }
    }
  }
}
```

## Default logback.xml

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

## Default conf.json

```json
{
    "port" : 8080,
    "cassandra": {
        "seeds": ["localhost"],
        "reconnect": {
            "name": "exponential",
            "base_delay": 1000,
            "max_delay": 10000
        }
    }
}
```

## Default build.gradle

```gradle
buildscript {
    repositories { jcenter() }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:1.1.1'
    }
}

apply plugin: 'java'
apply plugin: 'com.github.johnrengelman.shadow'

version = '0.1.0'
group = 'com.cyngn.chrono'
archivesBaseName = 'chrono'

if (!JavaVersion.current().java8Compatible) {
    throw new IllegalStateException('''A Haiku:
                                      |  This needs Java 8,
                                      |  You are using something else,
                                      |  Refresh. Try again.'''.stripMargin())
}

repositories {
    mavenCentral()
    maven { url = 'http://oss.sonatype.org/content/repositories/snapshots/' }
    maven { url = 'http://oss.sonatype.org/content/repositories/releases/' }
}

dependencies {
    compile 'io.vertx:vertx-core:3.0.0'
    compile "joda-time:joda-time:2.4"
    compile "com.google.guava:guava:18.0"
    compile "commons-lang:commons-lang:2.6"
    compile "net.sf.jopt-simple:jopt-simple:4.9"
    compile "com.cyngn.vertx:vertx-util:0.5.4"
    compile "com.englishtown.vertx:vertx-cassandra:3.0.0"
    compile "com.englishtown.vertx:vertx-cassandra-mapping:3.0.0"
    compile "ch.qos.logback:logback-classic:1.0.13"
    compile "ch.qos.logback:logback-core:1.0.13"
    compile "io.vertx:vertx-codegen:3.0.0"
    testCompile "junit:junit:4.11"
    testCompile "io.vertx:vertx-unit:3.0.0"
}

task wrapper(type: Wrapper) {
    gradleVersion = '2.0'
}

task release() << {}

gradle.taskGraph.whenReady {taskGraph ->
    if (!taskGraph.hasTask(release)) {
        version += '-SNAPSHOT'
    }
}

task javadocJar(type: Jar) {
    classifier = 'javadoc'
    from javadoc
}

task sourcesJar(type: Jar) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

artifacts {
    archives javadocJar, sourcesJar
}

shadowJar {
    classifier = 'fat'
    manifest {
        attributes 'Main-Class': 'io.vertx.core.Starter'
        attributes 'Main-Verticle': 'com.cyngn.chrono.Server'
    }
    mergeServiceFiles {
        include 'META-INF/services/io.vertx.core.spi.VerticleFactory'
    }
}
```
