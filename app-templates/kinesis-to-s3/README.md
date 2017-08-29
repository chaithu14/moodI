
## Description

The Kinesis to Amazon S3 application template demonstrates continuous big data sync from a source to destination while reading data messages from a configured Kinesis stream. This could be easily utilized and extended by any developer to create a fast,  fault tolerant and scalable Big Data Sync or Retention Application to serve business with continuous data.

It ingests records separated by delimiter '|' from configured AWS Kinesis streams and writes each message as a record in Amazon S3 bucket. This application uses PoJoEvent as an example schema, which can be customized to use custom schema based on business requirements.

- The application scales linearly with number of Kinesis shards.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performance and can perform as fast as the network bandwidth allows.
- The only configuration user needs to provide is source Kinesis credentials, topic name and destination S3 bucket name, path and credentials.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppFactory and launch it to ingest records separated by '|' from configured AWS Kinesis Stream and writes each message as a record in Amazon S3. Please follow the walkthrough document below to understand the configurable properties. Moreover, one could easily add customized business logic to further process the data during sync.

## Implementation details
- Logical flow diagram
![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/04/Kinesis_to_S3_DAG.jpg)

- It uses following operators
  - KinesisByteArrayInputOperator Input operator (Read from AWS Kinesis)
  - S3MetricsTupleOutputModule Output Operator (Write to Amazon S3)

  - Supported data source
      - AWS Kinesis version 1.10.x
      - Tested with AWS Kinesis driver: com.amazonaws:aws-java-sdk-kinesis:jar:1.10.73
  - Supported sinks
      - AWS S3 version 1.10.x
      - Tested with AWS Redshift JDBC driver: com.amazonaws:aws-java-sdk-s3:jar:1.10.73

## Supported visualizations
|Metric|Widget|
|------|------|
|Bytes read per second from Kinesis |Line Chart|
|Bytes written per second to S3 |Line Chart|
|Events read per second from Kinesis |Line Chart|
|Events written per second to S3 |Line Chart|
|Total events read from Kinesis |Single Value|
|Total events written to S3 |Single Value|
|Total bytes read from Kinesis |Single Value|
|Total bytes written to S3 |Single Value|


## Resources

- Detailed documentation for this application template is available at :
  [http://docs.datatorrent.com/app-templates/0.10.0/kinesis-to-s3](http://docs.datatorrent.com/app-templates/0.10.0/kinesis-to-s3)

- Source code for this app-template is available at :
  [https://github.com/DataTorrent/moodI/tree/master/app-templates/kinesis-to-s3](https://github.com/DataTorrent/moodI/tree/master/app-templates/kinesis-to-s3)

- Please send feedback or feature requests to :
    <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

- Join our user discussion group at :
    <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
