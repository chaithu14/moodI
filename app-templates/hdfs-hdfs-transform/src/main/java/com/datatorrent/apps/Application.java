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

import java.util.Map;

import org.apache.apex.malhar.lib.fs.FSRecordReaderModule;
import org.apache.apex.malhar.lib.fs.GenericFileOutputOperator.StringFileOutputOperator;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.filter.FilterOperator;
import com.datatorrent.lib.transform.TransformOperator;

@ApplicationAnnotation(name="HDFS-Transform-HDFS")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    FSRecordReaderModule recordReader = dag.addModule("recordReader", FSRecordReaderModule.class);
    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    FilterOperator filterOperator = dag.addOperator("filter", new FilterOperator());
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());
    Map<String, String> expMap = Maps.newHashMap();
    expMap.put("amount", "{$.amount} + 10");
    transform.setExpressionMap(expMap);
    CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());

    StringFileOutputOperator fileOutput = dag.addOperator("fileOutput", new StringFileOutputOperator());

    dag.addStream("record", recordReader.records, csvParser.in);
    dag.addStream("pojo", csvParser.out, filterOperator.input);
    dag.addStream("filteredPojo", filterOperator.truePort, transform.input);
    dag.addStream("transformedPojo", transform.output, formatter.in);
    dag.addStream("string", formatter.out, fileOutput.input);

    /*
     * To add custom logic to your DAG, add your custom operator here with
     * dag.addOperator api call and connect it in the dag using the dag.addStream
     * api call.
     *
     * For example:
     *
     * To add another transformation operator in the DAG, use the following block of
     * code.
     *
     * TransformOperator transformName = dag.addOperator("transformName", new TransformOperator());
     * Map<String, String> expMapName = Maps.newHashMap();
     * expMapName.put("fname", "{$.name}.split(\"\\\\s+\")[0]");
     * expMapName.put("lname", "{$.name}.split(\"\\\\s+\")[1]");
     * transformName.setExpressionMap(expMapName);
     *
     * And to connect it in the DAG as follows:
     * recordReader --> csvParser --> filter --> TransformAmount --> TransformName --> formatter --> fileOutput
     *
     * Replace the following line:
     * dag.addStream("transformedPojo", transform.output, formatter.in);
     *
     * with the following two lines:
     * dag.addStream("transformedAmount", transform.output, transformName.input);
     * dag.addStream("transformedName", transformName.output, formatter.in);
     *
     * In ApplicationTest.java
     * Replace the following line:
     * File outputfile = FileUtils.getFile(outputDir, "output.txt_7.0");
     * with the following line:
     * File outputfile = FileUtils.getFile(outputDir, "output.txt_8.0");
     *
     * In ApplicationTest.java
     * Replace the following line:
     * FileUtils.contentEquals(FileUtils.getFile("src/test/resources/test_event_data_transformed.txt"), outputfile));
     * with the following line:
     * FileUtils.contentEquals(FileUtils.getFile("src/test/resources/test_event_data_transformed_custom.txt"), outputfile));
     *
     * In properties.xml
     * Replace the value of property "apex.app-param.csvFormatterSchema" to
     *"separator": "|",
      "quoteChar": "\"",
      "lineDelimiter": "",
      "fields": [
      {
      "name": "accountNumber",
      "type": "Integer"
      },
      {
      "name": "fname",
      "type": "String"
      },
      {
      "name": "lname",
      "type": "String"
      },
      {
      "name": "amount",
      "type": "Integer"
      }
      ]

     *
     * In properties.xml add the following
     *  <property>
          <name>dt.operator.transformName.port.input.attr.TUPLE_CLASS</name>
          <value>com.datatorrent.apps.PojoEvent</value>
        </property>
        <property>
          <name>dt.operator.transformName.port.output.attr.TUPLE_CLASS</name>
          <value>com.datatorrent.apps.PojoEvent2</value>
        </property>
      *
      * In properties.xml replace  value of property "apex.app-param.tupleClassNameForFormatterInput" to
        com.datatorrent.apps.PojoEvent2
      *

     */
  }
}
