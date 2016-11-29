### Description
This Kafka to HDFS Filter Application continuously reads string messages separated by '|' from configured kafka topic(s), filters based on the filter criteria and writes each message as a line in HDFS file(s). This application uses PoJoEvent as an example schema, this can be customized to use custom schema based on specific needs.

- The application scales linearly with the number of Kafka brokers and Kafka topics.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can process faster than Kafka broker can produce per topic.
- It is extremely easy to add custom logic to get your business value without worrying about connectivity and operational details of Kafka consumer and HDFS writer.
- The only configuration user needs to provide is source Kafka broker list, topic list, filter criteria and destination HDFS path, filename.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppHub and launch it to read your data from Kafka topics, filters based on the filter criteria and write to HDFS. Follow the tutorial videos or walkthrough document below to launch the template and add custom logic to process the data during ingestion.

### Jumpstart
Import and run application template as an operable proof of concept. Please watch the [walkthrough video](https://www.youtube.com/watch?v=d0RSeazfjN8) to import and launch the application.

<iframe src="https://www.youtube.com/embed/d0RSeazfjN8?enablejsapi=1" allowfullscreen="allowfullscreen" class="video" id="basicVideo" ga-track="basicVideo"></iframe>

### Productize
Add custom logic to the application template and launch. Please watch the [walkthrough video](https://www.youtube.com/watch?v=ftMiPECgyDQ) to add custom logic to the application template.

<iframe src="https://www.youtube.com/embed/ftMiPECgyDQ?enablejsapi=1" allowfullscreen="allowfullscreen" class="video" id="advancedVideo" ga-track="advancedVideo"></iframe>

### Logical Plan

Here is a preview of the logical plan of the application template

![Logical Plan](https://lh6.googleusercontent.com/n2UQ9vKyfhYXbHHk7rixm3Plj1BQFfYBWo7lw6GS1vI_9fhnc4eAo4ZI4xoSBRMzsJiHotwr0dLGAb8=w1887-h985)

### Launch App Properties

Here is a preview of the properties to be set at application launch

![Launch App Properties](https://lh6.googleusercontent.com/U62sNo00kJzaeiz4ZFNBZCNyIooCDsVYo8686bYIbNEOmL-q8dauf86Si-82fQw-BFr0hekTmyoDkmE=w1887-h985)

### Resources

Please find the walkthrough docs for app template as follows:

&nbsp; <a href="http://docs.datatorrent.com/app-templates/kafka-to-hdfs-filter"  class="docs" id="docs" ga-track="docs" target="_blank">http://docs.datatorrent.com/app-templates/kafka-to-hdfs-filter</a>

Please find the GitHub URL for app template as follows:

&nbsp; <a href="https://github.com/DataTorrent/app-templates/tree/master/kafka-to-hdfs-filter"  class="github" id="github" ga-track="github" target="_blank">https://github.com/DataTorrent/app-templates/tree/master/kafka-to-hdfs-filter</a>

Please send feedback or feature requests to:

&nbsp; <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

Please join the user discussion groups:

&nbsp; <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
