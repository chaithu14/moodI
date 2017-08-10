/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */
package com.datatorrent.moodi.lib.io.fs.s3;

import javax.validation.constraints.NotNull;

import org.apache.hadoop.conf.Configuration;

import com.google.common.base.Preconditions;

import com.datatorrent.api.DAG;
import com.datatorrent.moodi.io.fs.FSInputModule;
import com.datatorrent.moodi.io.fs.FSSliceReader;

/**
 * S3InputModule is used to read files/list of files (or directory) from S3 bucket. <br/>
 * Module emits, <br/>
 * 1. FileMetadata 2. BlockMetadata 3. Block Bytes.<br/><br/>
 * For more info about S3 scheme protocals, please have a look at
 * <a href="https://wiki.apache.org/hadoop/AmazonS3">https://wiki.apache.org/hadoop/AmazonS3.</a>
 *
 * The module reads data in parallel, following parameters can be configured<br/>
 * 1. files: List of file(s)/directories to read. files would be in the form of
 *           SCHEME://AccessKey:SecretKey@BucketName/FileOrDirectory ,
 *           SCHEME://AccessKey:SecretKey@BucketName/FileOrDirectory , ....
 *           where SCHEME is the protocal scheme for the file system.
 *                 AccessKey is the AWS access key and SecretKey is the AWS Secret Key<br/>
 * 2. filePatternRegularExp: Files names matching given regex will be read<br/>
 * 3. scanIntervalMillis: interval between two scans to discover new files in input directory<br/>
 * 4. recursive: if scan recursively input directories<br/>
 * 5. blockSize: block size used to read input blocks of file<br/>
 * 6. readersCount: count of readers to read input file<br/>
 * 7. sequentialFileRead: Is emit file blocks in sequence?
 *
 * @since 3.5.0
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class S3InputModule extends FSInputModule
{
  @NotNull
  private String accessKey;
  @NotNull
  private String secretKey;
  @NotNull
  private String bucketName;
  @NotNull
  private String s3Schema = "s3n";
  /**
   * Endpoint for S3
   */
  private String s3EndPoint;

  /**
   * Creates the block reader for reading s3 blocks
   * @return S3BlockReader
   */
  @Override
  public FSSliceReader createBlockReader()
  {
    // Set the s3 bucket name, accessKey, SecretAccessKey to the block reader
    S3BlockReader reader = new S3BlockReader();
    reader.setBucketName(S3BlockReader.extractBucket(getFiles()));
    reader.setAccessKey(S3BlockReader.extractAccessKey(getFiles()));
    reader.setSecretAccessKey(S3BlockReader.extractSecretAccessKey(getFiles()));
    return reader;
  }


  /**
   * Convert the the input files to S3URI and set it to files.
   */
  private void generateAndSetS3URIToInput()
  {
    String inputURI = s3Schema + "://" + accessKey + ":" + secretKey + "@" + bucketName + "/";
    String uriFiles = "";
    String[] inputFiles = super.getFiles().split(",");
    for (int i = 0; i < inputFiles.length; i++) {
      uriFiles += inputURI + inputFiles[i];
      if (i != inputFiles.length - 1) {
        uriFiles += ",";
      }
    }
    this.setFiles(uriFiles);
  }

  @Override
  public void populateDAG(DAG dag, Configuration configuration)
  {
    generateAndSetS3URIToInput();
    super.populateDAG(dag, configuration);
  }

  /**
   * Set the S3 endpoint to use
   *
   * @param s3EndPoint
   */
  public void setS3EndPoint(String s3EndPoint)
  {
    this.s3EndPoint = s3EndPoint;
  }

  /**
   * Returns the s3 endpoint
   *
   * @return s3EndPoint
   */
  public String getS3EndPoint()
  {
    return s3EndPoint;
  }

  /**
   * Return the S3 access key
   * @return accessKey
   */
  public String getAccessKey()
  {
    return accessKey;
  }

  /**
   * Set the S3 access key
   * @param accessKey accessKey
   */
  public void setAccessKey(String accessKey)
  {
    this.accessKey = Preconditions.checkNotNull(accessKey);
  }

  /**
   * Return the S3 secret Key
   * @return secretKey
   */
  public String getSecretKey()
  {
    return secretKey;
  }

  /**
   * Set the S3 secret Key
   * @param secretKey secretKey
   */
  public void setSecretKey(String secretKey)
  {
    this.secretKey = Preconditions.checkNotNull(secretKey);
  }

  /**
   * Return the bucket name
   * @return bucketName
   */
  public String getBucketName()
  {
    return bucketName;
  }

  /**
   * Set the bucket name
   * @param bucketName bucketName
   */
  public void setBucketName(String bucketName)
  {
    this.bucketName = Preconditions.checkNotNull(bucketName);
  }

  /**
   * Get the schema for s3 file system
   * @return s3Schema
   */
  public String getS3Schema()
  {
    return s3Schema;
  }

  /**
   * Set the schema for s3 file system
   * @param s3Schema s3Schema
   */
  public void setS3Schema(String s3Schema)
  {
    this.s3Schema = Preconditions.checkNotNull(s3Schema);
  }

}
