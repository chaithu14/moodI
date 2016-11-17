/**
 * Put your copyright and license info here.
 */
package com.datatorrent.apps;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.datatorrent.api.LocalMode;
import com.datatorrent.lib.db.jdbc.JdbcTransactionalStore;

public class ApplicationTest
{
  private static final String DB_DRIVER = "org.postgresql.Driver";
  private static final String URL = "jdbc:postgresql://localhost:5432/testdb?user=postgres&password=postgres";
  private static final String TABLE_NAME = "test_event_input_table";
  private static final String OUTPUT_TABLE_NAME = "test_event_output_table";

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

  @Test
  public void testApplication() throws Exception
  {
    try {
      LocalMode lma = LocalMode.newInstance();
      Configuration conf = new Configuration(false);
      conf.addResource(this.getClass().getResourceAsStream("/META-INF/properties-test.xml"));
      lma.prepareDAG(new Application(), conf);
      LocalMode.Controller lc = lma.getController();
      lc.runAsync();
      // wait for output files to roll
      Thread.sleep(5000);
      try {

        /*
         * Initialization of database connection.
         */
        Class.forName(DB_DRIVER).newInstance();
        Connection conn = DriverManager.getConnection(URL);
        Statement stmt = conn.createStatement();

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
        Assert.assertTrue("Number of rows mismatch", !(inputTuples.equals(ouputTuples)));

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

        for (int i = 0; i < getNumOfEventsInStore(); i++) {
          Assert.assertTrue("Row mismatch", (inputTuples.get(i).hashCode() != ouputTuples.get(i).hashCode()));
        }
        DbUtils.close(conn);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
    cleanTable();
  }
}
