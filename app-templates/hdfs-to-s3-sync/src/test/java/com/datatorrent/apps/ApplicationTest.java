/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */
package com.datatorrent.apps;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.datatorrent.api.LocalMode;
import com.datatorrent.stram.StramLocalCluster;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifies the S3OutputModule using the application. This reads the data from local file system
 * "input" directory and uploads the files into "output" directory.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ApplicationTest
{
  private String uploadId = "uploadfile";
  private static final String FILE_DATA = "Testing the application template HDFS-to-S3-sync. This File has more data hence more blocks.";
  private static final String FILE = "file.txt";
  private String inputDir;
  private String outputDir;
  private File inputFile;
  @Mock
  public static AmazonS3 client;
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

  public static class TestMeta extends TestWatcher
  {
    public String baseDirectory;

    @Override
    protected void starting(org.junit.runner.Description description)
    {
      this.baseDirectory = "target/" + description.getClassName() + "/" + description.getMethodName();
    }

    @Override
    protected void finished(Description description)
    {
      super.finished(description);
      try {
        FileUtils.forceDelete(new File(baseDirectory));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Rule
  public TestMeta testMeta = new TestMeta();

  @Before
  public void beforeTest() throws IOException
  {
    inputDir = testMeta.baseDirectory + File.separator + "input";
    outputDir = testMeta.baseDirectory + File.separator + "output";
    inputFile = new File(inputDir + File.separator + FILE);
    FileUtils.writeStringToFile(inputFile, FILE_DATA);
  }

  private CompleteMultipartUploadResult completeMultiPart() throws IOException
  {
    FileUtils.copyFile(inputFile, new File(outputDir + File.separator + FILE));
    CompleteMultipartUploadResult result = new CompleteMultipartUploadResult();
    result.setETag(outputDir);
    return result;
  }

  @Test
  public void testApplicationHDFSToS3() throws Exception
  {
    InitiateMultipartUploadResult result = new InitiateMultipartUploadResult();
    result.setUploadId(uploadId);

    PutObjectResult objResult = new PutObjectResult();
    objResult.setETag("SuccessFullyUploaded");

    UploadPartResult partResult = new UploadPartResult();
    partResult.setPartNumber(1);
    partResult.setETag("SuccessFullyPartUploaded");

    /*
     * Mock some methods used by S3 Output Module.
     */
    MockitoAnnotations.initMocks(this);
    when(client.initiateMultipartUpload(any(InitiateMultipartUploadRequest.class))).thenReturn(result);
    when(client.putObject(any(PutObjectRequest.class))).thenReturn(objResult);
    when(client.uploadPart(any(UploadPartRequest.class))).thenReturn(partResult);
    when(client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class))).thenReturn(completeMultiPart());

    Configuration conf = new Configuration(false);
    conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
    conf.set("dt.operator.HDFSInputModule.prop.files", inputDir);
    conf.set("dt.operator.S3OutputModule.prop.outputDirectoryPath", outputDir);

    Path outDir = new Path("file://" + new File(outputDir).getAbsolutePath());
    final Path outputFilePath =  new Path(outDir.toString() + File.separator + FILE);
    final FileSystem fs = FileSystem.newInstance(outDir.toUri(), new Configuration());

    /*
     * Run application asynchronously
     */
    LocalMode lma = LocalMode.newInstance();
    lma.prepareDAG(new Application(), conf);
    LocalMode.Controller lc = lma.getController();
    lc.setHeartbeatMonitoringEnabled(true);

    ((StramLocalCluster)lc).setExitCondition(new Callable<Boolean>()
    {
      @Override
      public Boolean call() throws Exception
      {
        return fs.exists(outputFilePath);
      }
    });
    lc.runAsync();

    /*
     * Wait till output results are generated in S3 mocked file/directory
     */
    final int MAX_TIMEOUT = 60;
    for (int i = 0; i < MAX_TIMEOUT; i++) {
      if (!fs.exists(outputFilePath)) {
        LOG.debug("Waiting for file(s)/directory {} to be created.", outputFilePath);
        Thread.sleep(1000);
      } else {
        LOG.debug("Output file(s)/directory {} is created.", outputFilePath);
        break;
      }
    }
    Assert.assertTrue("Unable to find output results even after maximum timeout", fs.exists(outputFilePath));
  }
}
