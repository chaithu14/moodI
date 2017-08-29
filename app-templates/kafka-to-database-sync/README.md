
## Description

The Kafka to Database sync application template demonstrates continuous big data sync from a source to destination while reading data messages from a configured Kafka topic. This could be easily utilized and extended by any developer to create a fast,  fault tolerant and scalable Big Data Sync or Retention Application to serve business with continuous data.

The Kafka to Database Sync Application ingest string messages seperated by '|' from configured kafka topic and writes each message as a record in PostgreSQL DataBase. This application uses PoJoEvent as an example schema, this can be customized to use custom schema based on specific needs.

- The application scales linearly with the number of poller partitions.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can process as fast as the network allows.
- It is extremely easy to add custom logic to get your business value without worrying about database connectivity and operational details of database poller and database writer.
- The only configuration user needs to provide is source Kafka topics, Kafka broker lists and database connection details, table.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppFactory and launch it to ingest string messages seperated by '|' from configured kafka topic and writes each message as a record in PostgreSQL DataBase. Please follow the walkthrough document below to understand the configurable properties. Moreover, one could easily add customized business logic to further process the data during sync.

## Implementation details
- Logical flow diagram
![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/08/kafka-to-database-sync-dag.png)

- It uses following operators
  - KafkaSinglePortInputOperator Input operator (Read from source Kafka Cluster)
  - CsvParser Operator (Delimited Parser)
  - JdbcPOJOInsertOutputOperator Output Operator (Write to PostgreSQL)
- Supported data source
  - Apache Kafka version 0.9.0.x
  - Tested with kafka client library: org.apache.kafka:kafka_2.11:0.9.0.1
- Supported sinks
  - PostgreSQL version: 9.4.x
  - Tested with PostgreSQL client library: org.postgresql:postgresql:9.4.1208.jre7


## Resources

  - Detailed documentation for this application template is available at:   
    [http://docs.datatorrent.com/app-templates/0.10.0/kafka-to-database-sync](http://docs.datatorrent.com/app-templates/0.10.0/kafka-to-database-sync)

  - Source code for this app-template is available at:  
    [https://github.com/DataTorrent/moodI/tree/master/app-templates/kafka-to-database-sync](https://github.com/DataTorrent/moodI/tree/master/app-templates/kafka-to-database-sync)

  - Please send feedback or feature requests to:
      <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

  - Join our user discussion group at:
      <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
