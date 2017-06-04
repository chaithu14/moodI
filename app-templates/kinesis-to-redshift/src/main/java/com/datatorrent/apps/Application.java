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

import org.apache.apex.malhar.lib.db.redshift.RedshiftOutputModule;
import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.kinesis.KinesisByteArrayInputOperator;

@ApplicationAnnotation(name="Kinesis-to-Redshift")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    // Operator used to get the data from AWS kinesis streams in form of byte array.
    KinesisByteArrayInputOperator inputOperator = dag.addOperator("KinesisInput", new KinesisByteArrayInputOperator());

    // Operator used to insert the values from AWS Kinesis streams to AWS Redshift database.
    RedshiftOutputModule jdbcOutputOperator = dag.addModule("JdbcOutput", new RedshiftOutputModule());

    // connect the above operators.
    dag.addStream("KinesisToRedshift", inputOperator.outputPort, jdbcOutputOperator.input);

    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call.
     *
     * For example:
     *
     * To add the transformation operator in the DAG, use the following block of code:
     * CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
     * TransformOperator transform = dag.addOperator("transform", new TransformOperator());
     * Map<String, String> expMap = Maps.newHashMap();
     * expMap.put("name", "{$.name}.toUpperCase()");
     * transform.setExpressionMap(expMap);
     * CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
     * StringToByteArrayConverterOperator converterOp = dag.addOperator("converter", new StringToByteArrayConverterOperator());
     *
     * And to connect it in the DAG as follows:
     * kinesisInput --> csvParser --> Transform --> csvFormatter --> byteArrayConverter -> redshiftJdbcOutput
     *
     * Replace the following line:
     * dag.addStream("KinesisToRedshift", inputOperator.outputPort, jdbcOutputOperator.input);
     *
     * with the following two lines:
     * dag.addStream("data", inputOperator.outputPort, csvParser.in);
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * dag.addStream("string", formatter.out, converterOp.input).setLocality(DAG.Locality.THREAD_LOCAL);
     * dag.addStream("writeToJDBC", converterOp.output, jdbcOutputOperator.input);
     */
  }
}
