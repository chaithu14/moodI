/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */
package com.datatorrent.moodi.lib.io.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datatorrent.lib.db.TransactionableStore;

/**
 * <p>JdbcTransactionalStore class.</p>
 *
 * @since 0.9.4
 */
@org.apache.hadoop.classification.InterfaceStability.Evolving
public class JdbcTransactionalStore extends JdbcStore implements TransactionableStore
{
  private static final transient Logger LOG = LoggerFactory.getLogger(JdbcTransactionalStore.class);

  public static String DEFAULT_APP_ID_COL = "dt_app_id";
  public static String DEFAULT_OPERATOR_ID_COL = "dt_operator_id";
  public static String DEFAULT_WINDOW_COL = "dt_window";
  public static String DEFAULT_META_TABLE = "dt_meta";

  @NotNull
  protected String metaTableAppIdColumn;
  @NotNull
  protected String metaTableOperatorIdColumn;
  @NotNull
  protected String metaTableWindowColumn;
  @NotNull
  private String metaTable;

  private boolean inTransaction;
  protected transient PreparedStatement lastWindowFetchCommand;
  protected transient PreparedStatement lastWindowInsertCommand;
  protected transient PreparedStatement lastWindowUpdateCommand;
  protected transient PreparedStatement lastWindowDeleteCommand;

  public JdbcTransactionalStore()
  {
    super();
    metaTable = DEFAULT_META_TABLE;
    metaTableAppIdColumn = DEFAULT_APP_ID_COL;
    metaTableOperatorIdColumn = DEFAULT_OPERATOR_ID_COL;
    metaTableWindowColumn = DEFAULT_WINDOW_COL;
    inTransaction = false;
  }

  /**
   * Sets the name of the meta table.<br/>
   * <b>Default:</b> {@value #DEFAULT_META_TABLE}
   *
   * @param metaTable meta table name.
   */
  public void setMetaTable(@NotNull String metaTable)
  {
    this.metaTable = metaTable;
  }

  /**
   * Sets the name of app id column.<br/>
   * <b>Default:</b> {@value #DEFAULT_APP_ID_COL}
   *
   * @param appIdColumn application id column name.
   */
  public void setMetaTableAppIdColumn(@NotNull String appIdColumn)
  {
    this.metaTableAppIdColumn = appIdColumn;
  }

  /**
   * Sets the name of operator id column.<br/>
   * <b>Default:</b> {@value #DEFAULT_OPERATOR_ID_COL}
   *
   * @param operatorIdColumn operator id column name.
   */
  public void setMetaTableOperatorIdColumn(@NotNull String operatorIdColumn)
  {
    this.metaTableOperatorIdColumn = operatorIdColumn;
  }

  /**
   * Sets the name of the window column.<br/>
   * <b>Default:</b> {@value #DEFAULT_WINDOW_COL}
   *
   * @param windowColumn window column name.
   */
  public void setMetaTableWindowColumn(@NotNull String windowColumn)
  {
    this.metaTableWindowColumn = windowColumn;
  }

  @Override
  public void connect()
  {
    super.connect();
    try {
      String command = "select " + metaTableWindowColumn + " from " + metaTable + " where " + metaTableAppIdColumn +
          " = ? and " + metaTableOperatorIdColumn + " = ?";
      logger.debug(command);
      lastWindowFetchCommand = connection.prepareStatement(command);

      command = "insert into " + metaTable + " (" + metaTableAppIdColumn + ", " + metaTableOperatorIdColumn + ", " +
        metaTableWindowColumn + ") values (?,?,?)";
      logger.debug(command);
      lastWindowInsertCommand = connection.prepareStatement(command);

      command = "update " + metaTable + " set " + metaTableWindowColumn + " = ? where " + metaTableAppIdColumn + " = ? "
          + " and " + metaTableOperatorIdColumn + " = ?";
      logger.debug(command);
      lastWindowUpdateCommand = connection.prepareStatement(command);

      command = "delete from " + metaTable + " where " + metaTableAppIdColumn + " = ? and " + metaTableOperatorIdColumn
          + " = ?";
      logger.debug(command);
      lastWindowDeleteCommand = connection.prepareStatement(command);

      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void disconnect()
  {
    if (lastWindowUpdateCommand != null) {
      try {
        lastWindowUpdateCommand.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
    super.disconnect();
  }

  @Override
  public void beginTransaction()
  {
    inTransaction = true;
  }

  @Override
  public void commitTransaction()
  {
    try {
      connection.commit();
      inTransaction = false;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void rollbackTransaction()
  {
    try {
      connection.rollback();
      inTransaction = false;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isInTransaction()
  {
    return inTransaction;
  }

  @Override
  public long getCommittedWindowId(String appId, int operatorId)
  {
    Long lastWindow = getCommittedWindowIdHelper(appId, operatorId);

    try {
      if (lastWindow == null) {
        lastWindowInsertCommand.close();
        connection.commit();
      }

      lastWindowFetchCommand.close();
      LOG.debug("Last window id: {}", lastWindow);

      if (lastWindow == null) {
        return -1L;
      } else {
        return lastWindow;
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * This is a helper method for loading the committed window Id.
   * @param appId The application ID.
   * @param operatorId The operator ID.
   * @return The last committed window. If there is no previously committed window this will return null.
   */
  protected Long getCommittedWindowIdHelper(String appId, int operatorId)
  {
    try {
      lastWindowFetchCommand.setString(1, appId);
      lastWindowFetchCommand.setInt(2, operatorId);
      Long lastWindow = null;
      ResultSet resultSet = lastWindowFetchCommand.executeQuery();
      if (resultSet.next()) {
        lastWindow = resultSet.getLong(1);
      } else {
        lastWindowInsertCommand.setString(1, appId);
        lastWindowInsertCommand.setInt(2, operatorId);
        lastWindowInsertCommand.setLong(3, -1);
        lastWindowInsertCommand.executeUpdate();
      }
      return lastWindow;
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public void storeCommittedWindowId(String appId, int operatorId, long windowId)
  {
    try {
      lastWindowUpdateCommand.setLong(1, windowId);
      lastWindowUpdateCommand.setString(2, appId);
      lastWindowUpdateCommand.setInt(3, operatorId);
      lastWindowUpdateCommand.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeCommittedWindowId(String appId, int operatorId)
  {
    try {
      lastWindowDeleteCommand.setString(1, appId);
      lastWindowDeleteCommand.setInt(2, operatorId);
      lastWindowDeleteCommand.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
