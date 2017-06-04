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

import java.util.List;
import java.util.Map;

import org.apache.apex.malhar.kafka.KafkaSinglePortInputOperator;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.db.jdbc.JdbcFieldInfo;
import com.datatorrent.lib.db.jdbc.JdbcPOJOInsertOutputOperator;
import com.datatorrent.lib.db.jdbc.JdbcPOJOPollInputOperator;
import com.datatorrent.lib.db.jdbc.JdbcStore;
import com.datatorrent.lib.db.jdbc.JdbcTransactionalStore;
import com.datatorrent.lib.transform.TransformOperator;
import com.datatorrent.lib.util.FieldInfo;

@ApplicationAnnotation(name="Kafka-to-Database-Sync")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    KafkaSinglePortInputOperator kafkaInputOperator = dag.addOperator("kafkaInput", KafkaSinglePortInputOperator.class);
    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    JdbcPOJOInsertOutputOperator jdbcOutputOperator = dag.addOperator("JdbcOutput", new JdbcPOJOInsertOutputOperator());

    /*
     * Custom field mapping(DB ColumnName -> PojoFieldExpression) provided to JdbcOutput Operator.
     */
    JdbcTransactionalStore outputStore = new JdbcTransactionalStore();
    jdbcOutputOperator.setStore(outputStore);
    jdbcOutputOperator.setFieldInfos(addFieldInfos());

    /*
     * Connecting JDBC operators and using parallel partitioning for input port.
     */
    dag.addStream("record", kafkaInputOperator.outputPort, csvParser.in);
    dag.addStream("pojo", csvParser.out, jdbcOutputOperator.input);
    dag.setInputPortAttribute(csvParser.in, Context.PortContext.PARTITION_PARALLEL, true);
    dag.setInputPortAttribute(jdbcOutputOperator.input, Context.PortContext.PARTITION_PARALLEL, true);

    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call.
     *
     * For example:
     *
     * To add the transformation operator in the DAG, use the following block of
     * code.
     *
     * TransformOperator transform = dag.addOperator("transform", new TransformOperator());
     * Map<String, String> expMap = Maps.newHashMap();
     * expMap.put("name", "{$.name}.toUpperCase()");
     * transform.setExpressionMap(expMap);
     *
     * And to connect it in the DAG as follows:
     * kafkaInput --> csvParser --> Transform --> JdbcOutput
     *
     * Replace the following line:
     * dag.addStream("pojo", csvParser.out, jdbcOutputOperator.input);
     *
     * with the following two lines:
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, jdbcOutputOperator.input);
     * dag.setInputPortAttribute(transform.input, Context.PortContext.PARTITION_PARALLEL, true);
     *
     * In ApplicationTest.java
     *
     * Replace following line in compare() method:
     * String[] inputLines = lines;
     * with this lines:
     * String[] inputLines = lines_transformed;
     *
     */
  }

  /**
   * This method can be modified to have field mappings based on used defined
   * class for inserting to database.
   */
  private List<JdbcFieldInfo> addFieldInfos()
  {
    List<JdbcFieldInfo> fieldInfos = Lists.newArrayList();

    /*
     * To use this application with custom schema add field info mapping as shown
     * on the following line:
     * fieldInfos.add(new JdbcFieldInfo("DATABASE_COLUMN_NAME", "pojoFieldName", SupportType.DATABASE_COLUMN_TYPE, sqlType));
     *
     * Also, update TUPLE_CLASS property from the xml configuration files.
     */
    fieldInfos.add(new JdbcFieldInfo("account_no", "accountNumber", FieldInfo.SupportType.INTEGER, 0));
    fieldInfos.add(new JdbcFieldInfo("name", "name", FieldInfo.SupportType.STRING, 0));
    fieldInfos.add(new JdbcFieldInfo("amount", "amount", FieldInfo.SupportType.INTEGER, 0));
    return fieldInfos;
  }
}
