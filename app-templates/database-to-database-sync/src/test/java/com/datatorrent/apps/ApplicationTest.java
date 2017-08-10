/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.validation.ConstraintViolationException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.LocalMode;
import com.datatorrent.lib.db.jdbc.JdbcTransactionalStore;

@org.apache.hadoop.classification.InterfaceStability.Evolving
public class ApplicationTest
{
  private static final String DB_DRIVER = "org.postgresql.Driver";
  private static final String URL = "jdbc:postgresql://localhost:5432/testdb?user=postgres&password=postgres";
  private static final String TABLE_NAME = "test_event_input_table";
  private static final String OUTPUT_TABLE_NAME = "test_event_output_table";
  private static final Logger LOG = LoggerFactory.getLogger(ApplicationTest.class);

  @BeforeClass
  public static void setup()
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

      System.out.println(createMetaTable);
      stmt.executeUpdate(createMetaTable);

      String createTable;
      createTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
        "  (ACCOUNT_NO integer PRIMARY KEY NOT NULL," +
        "  NAME varchar(255) DEFAULT NULL," +
        "  AMOUNT integer DEFAULT NULL)";
      stmt.executeUpdate(createTable);
      insertEventsInTable(10, 0);

      createTable = "CREATE TABLE IF NOT EXISTS " + OUTPUT_TABLE_NAME +
        "  (ACCOUNT_NO integer PRIMARY KEY NOT NULL," +
        "  NAME varchar(255) DEFAULT NULL," +
        "  AMOUNT integer DEFAULT NULL)";
      stmt.executeUpdate(createTable);

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
      String cleanOutputTable = "delete from " + OUTPUT_TABLE_NAME;
      stmt.executeUpdate(cleanOutputTable);

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

  @Test
  public void testApplication() throws Exception
  {
    try {
      Configuration conf = new Configuration(false);
      conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));

      /*
       * Run the application asynchronously and keep polling for results till timeout.
       */
      LocalMode.Controller lc = asyncRun(conf);
      waitForOutputTuples();

      /*
       * Validate the data contents of results.
       */
      validateTuples();

      lc.shutdown();
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  @AfterClass
  public static void teardown() {
    cleanTable();
  }

  private LocalMode.Controller asyncRun(Configuration conf) throws Exception
  {
    LocalMode lma = LocalMode.newInstance();
    lma.prepareDAG(new Application(), conf);
    LocalMode.Controller lc = lma.getController();
    lc.runAsync();
    return lc;
  }

  private void validateTuples()
  {
    try {

      /*
       * Initialization of database connection.
       */
      Class.forName(DB_DRIVER).newInstance();
      Connection conn = DriverManager.getConnection(URL);

      String inputSql = "select * FROM " + TABLE_NAME;
      String outputSql = "select * FROM " + OUTPUT_TABLE_NAME;
      ResultSetHandler inputResultSet = new ArrayListHandler();
      ResultSetHandler outputResultSet = new ArrayListHandler();
      QueryRunner run = new QueryRunner();
      List<Object[]> inputTuples = (ArrayList<Object[]>)run.query(conn, inputSql, inputResultSet);
      List<Object[]> ouputTuples = (ArrayList<Object[]>)run.query(conn, outputSql, outputResultSet);

      /*
       * Validate number of tuples of input and output operators.
       */
      Assert.assertEquals("Number of rows mismatch", inputTuples.size(), ouputTuples.size());

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
      Collections.sort(inputTuples,comparator);
      Collections.sort(ouputTuples,comparator);

      for (int i = 0; i < inputTuples.size(); i++) {
        Assert.assertEquals("Row mismatch", inputTuples.get(i)[0], ouputTuples.get(i)[0]);
      }
      DbUtils.close(conn);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
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
    final int numOfInputTuples = getNumOfEventsInStore(TABLE_NAME);
    int numOfOutputTuples = 0;
    Class.forName(DB_DRIVER).newInstance();
    Connection conn = DriverManager.getConnection(URL);

    for (int i = 0; i < MAX_TIMEOUT ; ++i) {
      numOfOutputTuples = getNumberOfTuplesProcessed(conn, OUTPUT_TABLE_NAME);
      if (numOfInputTuples != numOfOutputTuples) {
        LOG.debug("Sleeping, Iteration({}): Output Tuples Found {} Total Tuples {}", i, numOfOutputTuples, numOfInputTuples);
        Thread.sleep(1000);
      } else {
        break;
      }
    }
    if (numOfInputTuples != numOfOutputTuples) {
      String msg = String.format("Error: Output tuples expected %d found %d after %d seconds%n", numOfInputTuples, numOfOutputTuples, MAX_TIMEOUT);
      throw new RuntimeException(msg);
    }
  }
}
