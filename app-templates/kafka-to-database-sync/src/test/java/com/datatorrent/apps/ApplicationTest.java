/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.datatorrent.apps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.LocalMode;
import com.datatorrent.lib.db.jdbc.JdbcTransactionalStore;

import info.batey.kafka.unit.KafkaUnit;
import info.batey.kafka.unit.KafkaUnitRule;
import kafka.producer.KeyedMessage;

public class ApplicationTest
{
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);
  private static final int zkPort = 12181;
  private static final int brokerPort = 19092;
  private static final String DB_DRIVER = "org.postgresql.Driver";
  private static final String URL = "jdbc:postgresql://localhost:5432/testdb?user=postgres&password=postgres";
  private static final String OUTPUT_TABLE_NAME = "test_event_output_table";

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
    }

  }

  @Rule
  public TestMeta testMeta = new TestMeta();

  @Before
  public void setup() throws Exception
  {

    try {
      Class.forName(DB_DRIVER).newInstance();

      Connection con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();

      String createMetaTable = "CREATE TABLE IF NOT EXISTS " + JdbcTransactionalStore.DEFAULT_META_TABLE + " ( "
          + JdbcTransactionalStore.DEFAULT_APP_ID_COL + " VARCHAR(100) NOT NULL, "
          + JdbcTransactionalStore.DEFAULT_OPERATOR_ID_COL + " INT NOT NULL, "
          + JdbcTransactionalStore.DEFAULT_WINDOW_COL + " BIGINT NOT NULL, " + "UNIQUE ("
          + JdbcTransactionalStore.DEFAULT_APP_ID_COL + ", " + JdbcTransactionalStore.DEFAULT_OPERATOR_ID_COL + ", "
          + JdbcTransactionalStore.DEFAULT_WINDOW_COL + ") " + ")";

      LOG.debug(createMetaTable);
      stmt.executeUpdate(createMetaTable);

      String createTable;

      createTable = "CREATE TABLE IF NOT EXISTS " + OUTPUT_TABLE_NAME + "  (ACCOUNT_NO integer PRIMARY KEY NOT NULL,"
          + "  NAME varchar(255) DEFAULT NULL," + "  AMOUNT integer DEFAULT NULL)";
      stmt.executeUpdate(createTable);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // test messages
  private static String[] lines = { "1|User_1|1000", "2|User_2|2000", "3|User_3|3000", "4|User_4|4000", "5|User_5|5000",
      "6|User_6|6000", "7|User_7|7000", "8|User_8|8000", "9|User_9|9000", "10|User_10|10000" };

  //test messages
  private static String[] lines_transformed = { "1|USER_1|1000", "2|USER_2|2000", "3|USER_3|3000", "4|USER_4|4000",
      "5|USER_5|5000", "6|USER_6|6000", "7|USER_7|7000", "8|USER_8|8000", "9|USER_9|9000", "10|USER_10|10000" };

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
      waitForOutputTuples();

      // compare output lines to input
      compare();
      cleanTable();
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
    topic = conf.get("dt.operator.kafkaInput.prop.topics");

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

  private int getNumberOfTuplesProcessed(Connection conn, String tableName)
  {
    String sqlQuery = "select * FROM " + tableName;
    ResultSetHandler resultSet = new ArrayListHandler();
    QueryRunner run = new QueryRunner();
    try {
      List<Object[]> ouputTuples = (ArrayList<Object[]>)run.query(conn, sqlQuery, resultSet);
      return ouputTuples.size();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return 0;
  }

  private void waitForOutputTuples() throws Exception
  {
    final int MAX_TIMEOUT = 60;
    int numOfOutputTuples = 0;
    Class.forName(DB_DRIVER).newInstance();
    Connection conn = DriverManager.getConnection(URL);

    for (int i = 0; i < MAX_TIMEOUT; ++i) {
      numOfOutputTuples = getNumberOfTuplesProcessed(conn, OUTPUT_TABLE_NAME);
      if (lines.length != numOfOutputTuples) {
        LOG.debug("Sleeping, Iteration({}): Output Tuples Found {} Total Tuples {}", i, numOfOutputTuples,
            lines.length);
        Thread.sleep(1000);
      } else {
        break;
      }
    }
    if (lines.length != numOfOutputTuples) {
      String msg = String.format("Error: Output tuples expected %d found %d after %d seconds%n", lines.length,
          numOfOutputTuples, MAX_TIMEOUT);
      throw new RuntimeException(msg);
    }
  }

  private void compare() throws Exception
  {
    try {
      String[] inputLines = lines;

      /*
       * Initialization of database connection.
       */
      Class.forName(DB_DRIVER).newInstance();
      Connection conn = DriverManager.getConnection(URL);
      Statement stmt = conn.createStatement();

      String outputSql = "select * FROM " + OUTPUT_TABLE_NAME;
      ResultSetHandler outputResultSet = new ArrayListHandler();
      QueryRunner run = new QueryRunner();
      List<Object[]> ouputTuples = (ArrayList<Object[]>)run.query(conn, outputSql, outputResultSet);

      /*
       * Validate number of tuples of input and output operators.
       */
      Assert.assertTrue("Number of rows mismatch", inputLines.length == ouputTuples.size());

      /*
       * Validate tuple contents of input and output operators.
       */
      Comparator comparator = new Comparator<Object[]>()
      {
        @Override
        public int compare(Object[] o1, Object[] o2)
        {
          return ((Integer)o1[0]).compareTo((Integer)o2[0]);
        }
      };
      Collections.sort(ouputTuples, comparator);

      for (int i = 0; i < inputLines.length; i++) {
        String[] fields = inputLines[i].split("\\|");
        Assert.assertEquals(Integer.parseInt(fields[0]), ouputTuples.get(i)[0]);
        Assert.assertEquals(fields[1], ouputTuples.get(i)[1]);
        Assert.assertEquals(Integer.parseInt(fields[2]), ouputTuples.get(i)[2]);
      }
      DbUtils.close(conn);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @After
  public void cleanTable()
  {
    try {
      Connection con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();
      String cleanOutputTable = "delete from " + OUTPUT_TABLE_NAME;
      stmt.executeUpdate(cleanOutputTable);

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int getNumOfEventsInStore()
  {
    Connection con;
    try {
      con = DriverManager.getConnection(URL);
      Statement stmt = con.createStatement();

      String countQuery = "SELECT count(*) from " + OUTPUT_TABLE_NAME;
      ResultSet resultSet = stmt.executeQuery(countQuery);
      resultSet.next();
      return resultSet.getInt(1);
    } catch (SQLException e) {
      throw new RuntimeException("fetching count", e);
    }
  }

}
