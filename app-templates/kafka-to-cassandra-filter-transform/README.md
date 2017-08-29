## Description

The Kafka to Cassandra (including Filtering, Transformation) application template demonstrates continuous big data preparation while reading data messages from a configured Kafka topic. This data is considered to be in JSON format, which is further filtered, transformed based on configurable properties. Finally, this prepared message is written to a Cassandra  . This could be easily utilized and extended by any developer to create a fast, fault tolerant and scalable Big Data Application to serve business with rich data.

It continuously ingests messages based on a configured topic from a Kafka cluster. This application uses PojoEvent as an example schema, however, it can be customized to use custom schema based on business needs.

- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can process as fast as the network allows.
- It is extremely easy to add custom logic to get your business value without worrying about database connectivity and operational details of database writer.
- The only configuration user needs to provide is source Kafka topics, Kafka broker lists and Cassandra database details like node, table and keyspace.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppFactory and launch it to ingest messages in JSON string format from configured Kafka topic. It would write each transformed message as a record in Cassandra store. Please follow the walkthrough document below to understand the configurable properties. Moreover, one could easily add customized business logic to process the data during ingestion.

## Implementation details
- Logical flow diagram
![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/08/kafka-to-cassandra-transform-dag.png)

- It uses following operators
  - KafkaSinglePortInputOperator Input operator (Read from source Kafka Cluster)
  - JsonParser Operator (Parser)
  - FilterOperator Operator (Filter)
  - Transform Operator (Transform)
  - CassandraOutputOperator Output Operator (Write to destination Cassandra Cluster)  
- Supported data source
  - Apache Kafka version 0.9.0.x
  - Tested with kafka client library: org.apache.kafka:kafka_2.11:0.9.0.1
- Supported sinks
  - Apache Cassandra version 3.1.x
  - Tested with cassandra library: com.datastax.cassandra:cassandra-driver-core:jar:3.1.0  

## Resources

  - Detailed documentation for this application template is available at:

    [http://docs.datatorrent.com/app-templates/0.10.0/kafka-to-cassandra-filter-transform](http://docs.datatorrent.com/app-templates/0.10.0/kafka-to-cassandra-filter-transform)

  - Source code for this app-template is available at :

    [https://github.com/DataTorrent/moodI/tree/master/app-templates/kafka-to-cassandra-filter-transform](https://github.com/DataTorrent/moodI/tree/master/app-templates/kafka-to-cassandra-filter-transform)

  - Please send feedback or feature requests to :
      <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

  - Join our user discussion group at :
      <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
