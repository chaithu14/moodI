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

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;

import com.google.common.collect.Maps;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.contrib.formatter.CsvFormatter;
import com.datatorrent.contrib.parser.CsvParser;
import com.datatorrent.lib.transform.TransformOperator;
import com.datatorrent.moodi.io.fs.StringFileOutputOperator;
import com.datatorrent.moodi.lib.io.fs.FSRecordReaderModule;

@ApplicationAnnotation(name="HDFS-line-copy")
public class Application implements StreamingApplication
{

  @Override
  public void populateDAG(DAG dag, Configuration conf)
  {
    FSRecordReaderModule recordReader = dag.addModule("recordReader", FSRecordReaderModule.class);
    CsvParser csvParser = dag.addOperator("csvParser", CsvParser.class);
    CsvFormatter formatter = dag.addOperator("formatter", new CsvFormatter());
    StringFileOutputOperator fileOutput = dag.addOperator("fileOutput", new StringFileOutputOperator());
    
    dag.addStream("record", recordReader.records, csvParser.in);
    dag.addStream("pojo", csvParser.out, formatter.in);
    dag.addStream("string", formatter.out, fileOutput.input);

    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
    
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
     * recordReader --> csvParser --> Transform --> formatter --> fileOutput
     *
     * Replace the following line:
     * dag.addStream("pojo", csvParser.out, formatter.in);
     * 
     * with the following two lines:
     * dag.addStream("pojo", csvParser.out, transform.input);
     * dag.addStream("transformed", transform.output, formatter.in);
     * 
     * In ApplicationTest.java
     * Replace the following line:
     * File outputfile = FileUtils.getFile(outputDir, "output.txt_5.0");
     * with the following line:
     * File outputfile = FileUtils.getFile(outputDir, "output.txt_6.0");

     * In ApplicationTest.java
     * Replace the following line:
     * FileUtils.contentEquals(FileUtils.getFile("src/test/resources/test_event_data.txt"), outputfile));
     * with the following line:
     * FileUtils.contentEquals(FileUtils.getFile("src/test/resources/test_event_data_transformed.txt"), outputfile));
     */

  }
}
