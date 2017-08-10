/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.io.File;
import java.io.IOException;

import javax.validation.ConstraintViolationException;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.batey.kafka.unit.KafkaUnitRule;
import info.batey.kafka.unit.KafkaUnit;

import kafka.producer.KeyedMessage;

import com.datatorrent.api.LocalMode;

/**
 * Test the DAG declaration in local mode.
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ApplicationTest
{
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);
  private static final int zkPort = 12181;
  private static final int brokerPort = 19092;
  private String outputDir;
  private String outputFileName;
  private String topic;

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
  public void setup() throws Exception
  {
    outputDir = testMeta.baseDirectory + File.separator + "output";
  }

  // test messages
  private static String[] lines = { "1|User_1|1000", "2|User_2|2000", "3|User_3|3000", "4|User_4|4000", "5|User_5|5000",
      "6|User_6|6000", "7|User_7|7000", "8|User_8|8000", "9|User_9|9000", "10|User_10|10000" };

  //test messages
  private static String[] lines_transformed = { "1|USER_1|1000", "2|USER_2|2000", "3|USER_3|3000", "4|USER_4|4000", "5|USER_5|5000",
      "6|USER_6|6000", "7|USER_7|7000", "8|USER_8|8000", "9|USER_9|9000", "10|USER_10|10000" };

  // broker port must match properties.xml
  @Rule
  public KafkaUnitRule kafkaUnitRule = new KafkaUnitRule(zkPort, brokerPort);

  @Test
  public void testApplication() throws Exception
  {
    try {

      // write messages to Kafka topic
      Configuration conf = getConfig();

      writeToTopic();

      // run app asynchronously; terminate after results are checked
      LocalMode.Controller lc = asyncRun(conf);

      // check for presence of output file
      chkOutput();

      // compare output lines to input
      compare();

      lc.shutdown();
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  private void writeToTopic()
  {
    KafkaUnit ku = kafkaUnitRule.getKafkaUnit();
    ku.createTopic(topic);
    for (String line : lines) {
      KeyedMessage<String, String> kMsg = new KeyedMessage<>(topic, line);
      ku.sendMessages(kMsg);
    }
    LOG.debug("Sent messages to topic {}", topic);
  }

  private Configuration getConfig()
  {
    Configuration conf = new Configuration(false);
    conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
    conf.set("dt.operator.fileOutput.prop.filePath", outputDir);
    topic = conf.get("dt.operator.kafkaInput.prop.topics");
    outputFileName = conf.get("dt.operator.fileOutput.prop.outputFileName");

    return conf;
  }

  private LocalMode.Controller asyncRun(Configuration conf) throws Exception
  {

    LocalMode lma = LocalMode.newInstance();
    lma.prepareDAG(new Application(), conf);
    LocalMode.Controller lc = lma.getController();
    lc.runAsync();
    return lc;
  }

  private void chkOutput() throws Exception
  {
    final int MAX = 60;
    boolean isFileExist = false;
    for (int i = 0; i < MAX; ++i) {
      LOG.debug("Sleeping, i = {}", i);
      Thread.sleep(1000);
      File[] files = new File(outputDir).listFiles();
      if (files == null) {
        continue;
      }
      for (File file : files) {
        if (file.isFile() && file.getName().contains(outputFileName)
          && !file.getName().endsWith(".tmp")) {
          isFileExist = true;
        }
      }
      if (isFileExist) {
        break;
      }
    }
    if (!isFileExist) {
      String msg = String.format("Error: %s not found after %d seconds%n", outputFileName, MAX);
      throw new RuntimeException(msg);
    }
  }

  private void compare() throws Exception
  {
    // Get the output file
    String outputFilePath = "";
    File[] files = new File(outputDir).listFiles();
    for (File file : files) {
      if (file.isFile() && file.getName().contains(outputFileName)) {
        outputFilePath = file.getPath();
      }
    }
    // read output file
    File file = new File(outputFilePath);
    LOG.info("Compare: {} -> {}", file.getPath(), file.getName());
    String output = FileUtils.readFileToString(file);
    Assert.assertArrayEquals(lines, output.split("\\n"));
  }
}
