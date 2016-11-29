### Description
The Database to Database Sync Application records from configured source PostgreSQL table and writes them in destination PostgreSQL table. It first does a bulk upload from the configured table parallely and continuously polls at configured poll interval for new records and writes them in destination table.
- The application scales linearly with the number of poller partitions.
- The application is fault tolerant and can withstand node and cluster outages without data loss.
- The application is also highly performant and can process as fast as the network allows.
- It is extremely easy to add custom logic to get your business value without worrying about database connectivity and operational details of database poller and database writer.
- The only configuration user needs to provide is source database connection details, table.
- This enterprise grade application template will dramatically reduce your time to market and cost of operations.

Import the application from DataTorrent AppHub and launch it to ingest your data from your PostgreSQL table and insert into output PostgreSQL table. Follow the tutorial videos or walkthrough document below to launch the template and add custom logic to process the data during ingestion.

### Jumpstart
Import and run application template as an operable proof of concept. Please watch the [walkthrough video](https://www.youtube.com/watch?v=F0arSlih73A) to import and launch the application.

<iframe src="https://www.youtube.com/embed/F0arSlih73A?enablejsapi=1" allowfullscreen="allowfullscreen" class="video" id="basicVideo" ga-track="basicVideo"></iframe>

### Productize
Add custom logic to the application template and launch. Please watch the [walkthrough video](https://www.youtube.com/watch?v=702HBqsLgJ4) to add custom logic to the application template.

<iframe src="https://www.youtube.com/embed/702HBqsLgJ4?enablejsapi=1" allowfullscreen="allowfullscreen" class="video" id="advancedVideo" ga-track="advancedVideo"></iframe>

### Logical Plan

Here is a preview of the logical plan of the application template

![Logical Plan](https://lh4.googleusercontent.com/ndjrVaSvD3-pzWA6sdyuNjYuJQzyYNgM8Tn0zQ6PKVE3bV99Pv0Y1QCEexU4snLC_-RY_HXCcRBYUik=w1887-h985)

### Launch App Properties

Here is a preview of the properties to be set at application launch

![Launch App Properties](https://lh5.googleusercontent.com/2ApMgu81aMs9Vgcdr5i1G6-MvpCW7_MtG92sI-j7QB3gt4OWMYfRghrjhRF-Pv1hYdBuiC2twnwIjE8=w1887-h985)

### Resources

Please find the walkthrough docs for app template as follows:

&nbsp; <a href="http://docs.datatorrent.com/app-templates/database-to-database/"  class="docs" id="docs" ga-track="docs" target="_blank">http://docs.datatorrent.com/app-templates/database-to-database/</a>

Please find the GitHub URL for app template as follows:

&nbsp; <a href="https://github.com/DataTorrent/app-templates/tree/master/database-to-database"  class="github" id="github" ga-track="github" target="_blank">https://github.com/DataTorrent/app-templates/tree/master/database-to-database</a>

Please send feedback or feature requests to:

&nbsp; <a href="mailto:feedback@datatorrent.com"  class="feedback" id="feedback" ga-track="feedback">feedback@datatorrent.com</a>

Please join the user discussion groups:

&nbsp; <a href="mailto:dt-users@googlegroups.com"  class="maillist" id="maillist" ga-track="maillist">dt-users@googlegroups.com</a>
