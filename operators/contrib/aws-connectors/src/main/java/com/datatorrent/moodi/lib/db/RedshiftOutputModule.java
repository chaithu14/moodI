/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.moodi.lib.db;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.conf.Configuration;

import com.google.common.base.Preconditions;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.Module;
import com.datatorrent.lib.db.jdbc.JdbcTransactionalStore;
import com.datatorrent.moodi.lib.io.fs.FSRecordCompactionOperator;
import com.datatorrent.moodi.lib.io.fs.s3.S3MetricsTupleOutputModule;

import static com.datatorrent.api.Context.OperatorContext.TIMEOUT_WINDOW_COUNT;
import static com.datatorrent.moodi.lib.db.RedshiftOutputModule.READER_MODE.READ_FROM_S3;

/**
 * Functionality of RedshiftOutputModule is load data into Redshift table. Data intermediately writes to HDFS/S3 and
 * rolling files will load into Redshift table using copy command.
 * By default, it load files from S3 into Redshfit table. If the file is located in EMR, then specify "readFromS3" parameter to false.
 *
 *
 * @since 3.7.0
 */
@InterfaceStability.Evolving
public class RedshiftOutputModule implements Module
{
  protected static final String DEFAULT_REDSHIFT_DELIMITER = "|";
  @NotNull
  private String tableName;
  @NotNull
  private String accessKey;
  @NotNull
  private String secretKey;
  private String region;
  private String bucketName;
  private String directoryName;
  private String emrClusterId;
  @NotNull
  private String redshiftDelimiter = DEFAULT_REDSHIFT_DELIMITER;
  /**
   * Specified as count of streaming windows. This value will set to the operators in this module because
   * the operators in this module is mostly interacts with the Amazon Redshift.
   * Due to this reason, window id of these operators might be lag behind with the upstream operators.
   */
  @Min(120)
  private int timeOutWindowCount = 1200;
  protected static enum READER_MODE
  {
    READ_FROM_S3, READ_FROM_EMR;
  }

  private READER_MODE readerMode = READ_FROM_S3;
  private int batchSize = 100;
  private Long maxLengthOfRollingFile;
  private JdbcTransactionalStore store = new JdbcTransactionalStore();

  public final transient ProxyInputPort<byte[]> input = new ProxyInputPort<byte[]>();

  public void populateDAG(DAG dag, Configuration conf)
  {
    if (readerMode == READ_FROM_S3) {
      S3MetricsTupleOutputModule.S3BytesOutputModule tupleBasedS3 = dag.addModule("S3Compaction", new S3MetricsTupleOutputModule.S3BytesOutputModule());
      tupleBasedS3.setAccessKey(accessKey);
      tupleBasedS3.setSecretAccessKey(secretKey);
      tupleBasedS3.setBucketName(bucketName);
      tupleBasedS3.setOutputDirectoryPath(directoryName);
      tupleBasedS3.setCompactionParallelPartition(true);
      if (maxLengthOfRollingFile != null) {
        tupleBasedS3.setMaxLength(maxLengthOfRollingFile);
      }

      input.set(tupleBasedS3.input);

      org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator redshiftOutput = dag.addOperator("LoadToRedshift", createRedshiftOperator());
      dag.setAttribute(redshiftOutput, TIMEOUT_WINDOW_COUNT, timeOutWindowCount);

      dag.addStream("load-to-redshift", tupleBasedS3.output, redshiftOutput.input);
    } else {
      FSRecordCompactionOperator<byte[]> hdfsWriteOperator = dag.addOperator("WriteToHDFS", new FSRecordCompactionOperator<byte[]>());
      hdfsWriteOperator.setConverter(new GenericFileOutputOperator.NoOpConverter());
      if (maxLengthOfRollingFile != null) {
        hdfsWriteOperator.setMaxLength(maxLengthOfRollingFile);
      }
      dag.setInputPortAttribute(hdfsWriteOperator.input, Context.PortContext.PARTITION_PARALLEL, true);
      input.set(hdfsWriteOperator.input);

      org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator redshiftOutput = dag.addOperator("LoadToRedshift", createRedshiftOperator());
      dag.setAttribute(redshiftOutput, TIMEOUT_WINDOW_COUNT, timeOutWindowCount);
      dag.addStream("load-to-redshift", hdfsWriteOperator.output, redshiftOutput.input);
    }
  }

  /**
   * Create the RedshiftJdbcTransactionableOutputOperator instance
   * @return RedshiftJdbcTransactionableOutputOperator object
   */
  protected org.apache.apex.malhar.lib.db.redshift.RedshiftJdbcTransactionableOutputOperator createRedshiftOperator()
  {
    RedshiftJdbcTransactionableOutputOperator redshiftOutput = new RedshiftJdbcTransactionableOutputOperator();
    redshiftOutput.setAccessKey(accessKey);
    redshiftOutput.setSecretKey(secretKey);
    if (bucketName != null) {
      redshiftOutput.setBucketName(bucketName);
    }
    redshiftOutput.setTableName(tableName);
    if (emrClusterId != null) {
      redshiftOutput.setEmrClusterId(emrClusterId);
    }
    redshiftOutput.setReaderMode(readerMode.toString());
    redshiftOutput.setStore(store);
    redshiftOutput.setBatchSize(batchSize);
    redshiftOutput.setRedshiftDelimiter(redshiftDelimiter);
    if (region != null) {
      redshiftOutput.setRegion(region);
    }
    return redshiftOutput;
  }

  /**
   * Get the table name from database
   * @return tableName
   */
  public String getTableName()
  {
    return tableName;
  }

  /**
   * Set the name of the table as it stored in redshift
   * @param tableName given tableName
   */
  public void setTableName(@NotNull String tableName)
  {
    this.tableName = Preconditions.checkNotNull(tableName);
  }

  /**
   * Get the AWS Access key
   * @return accessKey
   */
  public String getAccessKey()
  {
    return accessKey;
  }

  /**
   * Set the AWS Access Key
   * @param accessKey accessKey
   */
  public void setAccessKey(@NotNull String accessKey)
  {
    this.accessKey = Preconditions.checkNotNull(accessKey);
  }

  /**
   * Get the AWS secret key
   * @return secretKey
   */
  public String getSecretKey()
  {
    return secretKey;
  }

  /**
   * Set the AWS secret key
   * @param secretKey secretKey
   */
  public void setSecretKey(@NotNull String secretKey)
  {
    this.secretKey = Preconditions.checkNotNull(secretKey);
  }

  /**
   * Get the AWS region from where the input file resides.
   * @return region
   */
  public String getRegion()
  {
    return region;
  }

  /**
   * Set the AWS region from where the input file resides.
   * This is mandatory property if S3/EMR and Redshift runs in different regions.
   * @param region given region
   */
  public void setRegion(String region)
  {
    this.region = region;
  }

  /**
   * Get the bucket name only if the input files are located in S3.
   * @return bucketName
   */
  public String getBucketName()
  {
    return bucketName;
  }

  /**
   * Set the bucket name only if the input files are located in S3.
   * @param bucketName bucketName
   */
  public void setBucketName(@NotNull String bucketName)
  {
    this.bucketName = Preconditions.checkNotNull(bucketName);
  }

  /**
   * Return the directory name under S3 bucket
   * @return directoryName
   */
  public String getDirectoryName()
  {
    return directoryName;
  }

  /**
   * Set the directory name under S3 bucket.
   * @param directoryName directoryName
   */
  public void setDirectoryName(@NotNull String directoryName)
  {
    this.directoryName = Preconditions.checkNotNull(directoryName);
  }

  /**
   * Get the EMR cluster id
   * @return emrClusterId
   */
  public String getEmrClusterId()
  {
    return emrClusterId;
  }

  /**
   * Set the EMR cluster id
   * @param emrClusterId emrClusterId
   */
  public void setEmrClusterId(@NotNull String emrClusterId)
  {
    this.emrClusterId = Preconditions.checkNotNull(emrClusterId);
  }

  /**
   * Return the delimiter character which is used to separate fields from input file.
   * @return redshiftDelimiter
   */
  public String getRedshiftDelimiter()
  {
    return redshiftDelimiter;
  }

  /**
   * Set the delimiter character which is used to separate fields from input file.
   * @param redshiftDelimiter redshiftDelimiter
   */
  public void setRedshiftDelimiter(@NotNull String redshiftDelimiter)
  {
    this.redshiftDelimiter = Preconditions.checkNotNull(redshiftDelimiter);
  }

  /**
   * Specifies whether the input files read from S3 or emr
   * @return readerMode
   */
  public String getReaderMode()
  {
    return readerMode.toString();
  }

  /**
   * Set the readFromS3 which indicates whether the input files read from S3 or emr
   * @param readerMode Type of reader mode
   */
  public void setReaderMode(@Pattern(regexp = "READ_FROM_S3|READ_FROM_EMR", flags = Pattern.Flag.CASE_INSENSITIVE) String readerMode)
  {
    this.readerMode = READER_MODE.valueOf(readerMode);
  }

  /**
   * Get the size of a batch operation.
   * @return batchSize
   */
  public int getBatchSize()
  {
    return batchSize;
  }

  /**
   * Sets the size of a batch operation.
   * @param batchSize batchSize
   */
  public void setBatchSize(int batchSize)
  {
    this.batchSize = batchSize;
  }

  /**
   * Get the maximum length in bytes of a rolling file.
   * @return maxLengthOfRollingFile
   */
  public Long getMaxLengthOfRollingFile()
  {
    return maxLengthOfRollingFile;
  }

  /**
   * Set the maximum length in bytes of a rolling file.
   * @param maxLengthOfRollingFile maxLengthOfRollingFile
   */
  public void setMaxLengthOfRollingFile(Long maxLengthOfRollingFile)
  {
    this.maxLengthOfRollingFile = maxLengthOfRollingFile;
  }

  /**
   * Get the JdbcTransactionalStore of a RedshiftJdbcTransactionableOutputOperator
   * @return JdbcTransactionalStore
   */
  public JdbcTransactionalStore getStore()
  {
    return store;
  }

  /**
   * Set the JdbcTransactionalStore
   * @param store store
   */
  public void setStore(@NotNull JdbcTransactionalStore store)
  {
    this.store = Preconditions.checkNotNull(store);
  }

  /**
   * Get the number of streaming windows for the operators which have stalled processing.
   * @return the number of streaming windows
   */
  public int getTimeOutWindowCount()
  {
    return timeOutWindowCount;
  }

  /**
   * Set the number of streaming windows.
   * @param timeOutWindowCount given number of streaming windows for time out.
   */
  public void setTimeOutWindowCount(int timeOutWindowCount)
  {
    this.timeOutWindowCount = timeOutWindowCount;
  }
}
