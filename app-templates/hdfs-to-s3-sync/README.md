### Description
The HDFS S3 Sync Application Template continuously ingests files as blocks and backup hadoop HDFS data to Amazon S3 for data upload from hadoop to Amazon.
- The application scales linearly with number of block readers.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can perform as fast as the network bandwidth allows.
- The only configuration user needs to provide is source HDFS path and destination S3 bucket name, path and credentials.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppHub and launch it to ingest data from HDFS to Amazon S3. Follow the tutorial videos or walkthrough document below to launch the template and run it.

### Logical Plan

Here is a preview of the logical plan of the application template

![Logical Plan](https://www.datatorrent.com/wp-content/uploads/2017/08/hdfs-to-s3-LogicalDAG-1.png)

### Launch App Properties

Here is a preview of the properties to be set at application launch

![Launch App Properties](https://www.datatorrent.com/wp-content/uploads/2017/08/hdfs-to-s3-Launch.png)

### Resources

Please find the walkthrough docs for app template as follows:

&nbsp; <a href="http://docs.datatorrent.com/app-templates/hdfs-to-s3-sync/"  class="docs" id="docs" ga-track="docs" target="_blank">http://docs.datatorrent.com/app-templates/hdfs-to-s3-sync/</a>

Please find the GitHub URL for app template as follows:

&nbsp; <a href="https://github.com/DataTorrent/moodI/tree/master/app-templates/hdfs-to-s3-sync"  class="github" id="github" ga-track="github" target="_blank">https://github.com/DataTorrent/moodI/tree/master/app-templates/hdfs-s3-sync</a>

Please send feedback or feature requests to:

&nbsp; <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

Please join the user discussion groups:

&nbsp; <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
