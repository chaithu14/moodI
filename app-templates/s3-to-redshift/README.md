## Description

The Amazon S3 to Redshift application template demonstrates continuous big data sync from a source to destination while scanning data from a configured S3 bucket. This could be easily utilized and extended by any developer to create a fast,  fault tolerant and scalable Big Data Sync, Preparation or Retention Application to serve business with rich continuous data.

It ingests records by scanning data from configured S3 buckets, parses the record, transforms it based on a configured expression and finally writes the data as a record in AWS Redshift data warehouse. This application uses PoJoEvent as an example schema, which can be customized to use custom schema based on business requirements.

- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performance and can perform as fast as the network bandwidth allows.
- The only configuration user needs to provide is source S3 credentials, bucket name, directory to scan and destination Redshift credentials, table name.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppFactory and launch it to ingest records separated by '|' from configured S3 bucket and writes as a record in AWS Redshift data warehouse. Please follow the walkthrough document below to understand the configurable properties. Moreover, one could easily add customized business logic to further process the data during sync.

## Implementation details
- Logical flow diagram
![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/08/s3-to-redshift-logical-dag.png)

- It uses following operators
  - S3RecordReaderWrapperModule Input operator (Read from Amazon S3)
  - CsvParser Operator (Delimited Pojo)
  - TransformOperator Operator (Transform based on configured expression)
  - CsvFormatter Operator (Format Tuple)
  - RedshiftOutputModule Output Operator (Write to AWS Redshift)
- Supported data source
    - AWS S3 version 1.10.x
    - Tested with AWS Redshift JDBC driver: com.amazonaws:aws-java-sdk-s3:jar:1.10.73
- Supported sinks
    - AWS Redshift JDBC version 1.2.1.x
    - Tested with AWS Redshift JDBC driver: com.amazon.redshift:redshift-jdbc4:jar:1.2.1.1001

## Supported visualizations
|Metric|Widget|
|------|------|
|Bytes read per second from S3 |Line Chart|
|Bytes written per second to Redshift |Line Chart|
|Events read per second from S3 |Line Chart|
|Events written per second to Redshift |Line Chart|
|Total events read from S3 |Single Value|
|Total events written to Redshift |Single Value|
|Total bytes read from S3 |Single Value|
|Total bytes written to Redshift |Single Value|


## Resources

- Detailed documentation for this application template is available at:

  [http://docs.datatorrent.com/app-templates/0.10.0/s3-to-redshift](http://docs.datatorrent.com/app-templates/0.10.0/s3-to-redshift)

- Source code for this app-template is available at:

    [https://github.com/DataTorrent/moodI/tree/master/app-templates/s3-to-redshift](https://github.com/DataTorrent/moodI/tree/master/app-templates/s3-to-redshift)

- Please send feedback or feature requests to :
    <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

- Join our user discussion group at :
    <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
