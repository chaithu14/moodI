## Description

The HDFS to HDFS (including Filtering, Transformation) application template demonstrates continuous big data preparation while reading data from a source Hadoop cluster. This data is considered to be in delimited format, which is further filtered, transformed based on configurable properties. Finally, this prepared data is written back in a desired format to the destination Hadoop cluster. This could be easily utilized and extended by any developer to create a fast, fault tolerant and scalable Big Data Application to serve business with rich data.

It continuously ingests data files as lines from one Hadoop cluster to other retaining one-to-one file traceability.

- The application scales linearly with number of record readers.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can perform as fast as the network allows.
- It is extremely easy to add custom logic to get your business value without worrying about connectivity and operational details of HDFS reader or writer.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.
Import the application from DataTorrent AppFactory and launch it to ingest data from one Hadoop cluster to another. Follow the walkthrough document below to understand the configurable properties. Moreover, one could easily add customized business logic to process the data during ingestion.

## Implementation details

- Logical flow diagram
![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/08/hdfs-to-hdfs-filter-transform-dag.png)

- It uses following operators
  - FSRecordReaderModule Input operator (Read from source Hadoop Cluster)
  - CsvParser Operator (Parser)
  - FilterOperator Operator (Filter)
  - Transform Operator (Transform)
  - CsvFormatter Operator (Formatter)
  - StringFileOutputOperator Output Operator (Write to destination Hadoop Cluster)  
- Supported data source
  - Apache Hadoop HDFS 2.6.x
  - Tested with hadoop library: org.apache.hadoop:hadoop-common:jar:2.6.0
- Supported sinks
  - Apache Hadoop HDFS 2.6.x
  - Tested with hadoop library: org.apache.hadoop:hadoop-common:jar:2.6.0

## Resources

  - Detailed documentation for this application template is available at :
[http://docs.datatorrent.com/app-templates/0.10.0/hdfs-to-hdfs-filter-transform](http://docs.datatorrent.com/app-templates/0.10.0/hdfs-to-hdfs-filter-transform)

  - Source code for this app-template is available at :
  [https://github.com/DataTorrent/moodI/tree/master/app-templates/hdfs-to-hdfs-filter-transform](https://github.com/DataTorrent/moodI/tree/master/app-templates/hdfs-to-hdfs-filter-transform)

  - Please send feedback or feature requests to :
      <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

  - Join our user discussion group at :
      <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
