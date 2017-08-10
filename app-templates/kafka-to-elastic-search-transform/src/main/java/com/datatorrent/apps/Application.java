/*
 * Copyright (c) 2012-2017 DataTorrent, Inc.
 * All Rights Reserved.
 * The use of this source code is governed by the Limited License located at
 * https://www.datatorrent.com/datatorrent-openview-software-license/
 */

package com.datatorrent.apps;

import org.apache.hadoop.conf.Configuration;

import com.datatorrent.api.Context;
import com.datatorrent.api.DAG;
import com.datatorrent.api.StreamingApplication;
import com.datatorrent.api.annotation.ApplicationAnnotation;
import com.datatorrent.elasticsearch.operator.ElasticSearchOutputOperator;
import com.datatorrent.lib.schemaAware.FilterOperator;
import com.datatorrent.lib.schemaAware.JsonFormatter;
import com.datatorrent.lib.schemaAware.JsonParser;
import com.datatorrent.lib.schemaAware.TransformOperator;
import com.datatorrent.moodi.kafka.KafkaSinglePortInputOperator;

@org.apache.hadoop.classification.InterfaceStability.Evolving
@ApplicationAnnotation(name="KafkaToElasticSearch")
public class Application implements StreamingApplication
{

  public void populateDAG(DAG dag, Configuration conf)
  {
    // This kafka input operator takes input from specified Kafka brokers.
    KafkaSinglePortInputOperator kafkaInputOperator = dag.addOperator("kafkaInput", KafkaSinglePortInputOperator.class);

    // Parses a json string tuple against a specified json schema and emits JSONObject.
    JsonParser jsonParser = dag.addOperator("JsonParser", JsonParser.class);

    // Filters the tuple as per specified condition by user.
    FilterOperator filterOperator = dag.addOperator("filter", new FilterOperator());

    // Transforms the tuple value to user logic. Note logic may be modified.
    TransformOperator transform = dag.addOperator("transform", new TransformOperator());

    // Format the transformed logic into JSON format.
    JsonFormatter jsonFormatter = dag.addOperator("JsonFormatter", JsonFormatter.class);

    // Use elastic search as a store.
    ElasticSearchOutputOperator elasticSearchOutput = dag.addOperator("ElasticStore", ElasticSearchOutputOperator.class);

    // Now create the streams to complete the dag or application logic.
    // Most of the operators are kept THREAD_LOCAL for optimizing the local resources. As latest elastic search supports java 1.8,
    // so most of the clusters are not on java 1.8. If hadoop cluster is migrated to java 1.8, one can change the locality as
    // per the requirement.
    dag.addStream("KafkaToJsonParser", kafkaInputOperator.outputPort, jsonParser.in).setLocality(DAG.Locality.CONTAINER_LOCAL);
    dag.addStream("JsonParserToFilter", jsonParser.out, filterOperator.input).setLocality(DAG.Locality.THREAD_LOCAL);
    dag.addStream("FilterToTransform", filterOperator.truePort, transform.input).setLocality(DAG.Locality.THREAD_LOCAL);
    dag.addStream("TransformToJsonFormatter", transform.output, jsonFormatter.in).setLocality(DAG.Locality.THREAD_LOCAL);

    dag.addStream("JsonToElasticStore", jsonFormatter.out, elasticSearchOutput.input);
    dag.setAttribute(Context.DAGContext.METRICS_TRANSPORT, null);
  }
}
