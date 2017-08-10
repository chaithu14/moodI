/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.validation.ConstraintViolationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.LocalMode;
import com.datatorrent.apps.Application;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ApplicationTest
{
  private static final String DB_DRIVER = "org.postgresql.Driver";
  private static final String URL = "jdbc:postgresql://localhost:5432/testdb?user=postgres&password=postgres";
  private static final String TABLE_NAME = "test_event_table";
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

  private String outputDir;

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
  public void setupOutputDir() throws Exception
  {
    outputDir = testMeta.baseDirectory + File.separator + "output";
  }

  @BeforeClass
  public static void setup()
  {
    try {
      Class.forName(DB_DRIVER).newInstance();
      Connection con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();

      String createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
          + " (ACCOUNT_NO INTEGER, NAME VARCHAR(255),AMOUNT INTEGER)";
      stmt.executeUpdate(createTable);
      cleanTable();
      insertEventsInTable(10, 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void cleanTable()
  {
    try {
      Connection con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();
      String cleanTable = "delete from " + TABLE_NAME;
      stmt.executeUpdate(cleanTable);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void insertEventsInTable(int numEvents, int offset)
  {
    try {
      Connection con = DriverManager.getConnection(URL);
      String insert = "insert into " + TABLE_NAME + " values (?,?,?)";
      PreparedStatement stmt = con.prepareStatement(insert);
      for (int i = 0; i < numEvents; i++, offset++) {
        stmt.setInt(1, offset);
        stmt.setString(2, "Account_Holder-" + offset);
        stmt.setInt(3, (offset * 1000));
        stmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private LocalMode.Controller asyncRun(Configuration conf) throws Exception
  {
    LocalMode lma = LocalMode.newInstance();
    lma.prepareDAG(new Application(), conf);
    LocalMode.Controller lc = lma.getController();
    lc.runAsync();
    return lc;
  }

  @Test
  public void testApplication() throws Exception
  {
    try {
      Configuration conf = new Configuration(false);
      conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
      conf.set("dt.operator.fileOutput.prop.filePath", outputDir);

      /*
       * Run the application asynchronously and keep polling for results till timeout.
       */
      LocalMode.Controller lc = asyncRun(conf);
      waitForOutputTuples();

      cleanTable();
      lc.shutdown();
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  private int getNumberOfTuplesProcessed()
  {
    String[] extensions = { "dat.0", "tmp" };
    Collection<File> list = FileUtils.listFiles(new File(outputDir), extensions, false);
    int recordsCount = 0;
    for (File file : list) {
      try {
        recordsCount += FileUtils.readLines(file).size();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return recordsCount;
  }

  private void waitForOutputTuples() throws Exception
  {
    final int MAX_TIMEOUT = 60;
    final int numOfInputTuples = getNumOfEventsInStore(TABLE_NAME);
    int numOfOutputTuples = 0;

    /*
     * Wait till output directory is created.
     */
    File directory = (new File(outputDir));
    for (int i = 0; i < MAX_TIMEOUT; i++) {
      if (!directory.exists()) {
        LOG.debug("Waiting for output directory {} to be created.", directory.getName());
        Thread.sleep(1000);
      } else {
        LOG.debug("Output directory {} is created.", directory.getName());
        break;
      }
    }

    /*
     * Periodically check for number of records processed in a file.
     */
    for (int i = 0; i < MAX_TIMEOUT; i++) {
      numOfOutputTuples = getNumberOfTuplesProcessed();
      if (numOfInputTuples != numOfOutputTuples) {
        LOG.debug("Sleeping, Iteration({}): Records Found {} Total records {}", i, numOfOutputTuples, numOfInputTuples);
        Thread.sleep(1000);
      } else {
        LOG.debug("Total Records Found {} Total Expected Records {}", numOfOutputTuples, numOfInputTuples);
        break;
      }
    }

    /*
     * Verification of number of records processed in a file.
     */
    if (numOfInputTuples != numOfOutputTuples) {
      String msg = String.format("Error: Records in file expected %d found %d after %d seconds%n", numOfInputTuples, numOfOutputTuples, MAX_TIMEOUT);
      Assert.assertEquals("Records in file", numOfInputTuples, numOfOutputTuples);
      throw new RuntimeException(msg);
    }
  }

  public int getNumOfEventsInStore(String tableName)
  {
    Connection con;
    try {
      con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();

      String countQuery = "SELECT count(*) from " + tableName;
      ResultSet resultSet = stmt.executeQuery(countQuery);
      resultSet.next();
      return resultSet.getInt(1);
    } catch (SQLException e) {
      throw new RuntimeException("fetching count", e);
    }
  }
}
