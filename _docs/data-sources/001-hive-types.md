title: "Hive-to-Drill Data Type Mapping"
parent: "Data Sources"
---
Using Drill you can read tables created in Hive that use data types compatible with Drill. Drill currently does not support writing Hive tables. The following table shows Drill support for Hive primitive types:
<table>
  <tr>
    <th>SQL Type</th>
    <th>Hive Type</th>
    <th>Drill Description</th>
  </tr>
  <tr>
    <td>BIGINT</td>
    <td>BIGINT</td>
    <td>8-byte signed integer</td>
  </tr>
  <tr>
    <td>BOOLEAN</td>
    <td>BOOLEAN</td>
    <td>TRUE (1) or FALSE (0)</td>
  </tr>
  <tr>
    <td>N/A</td>
    <td>CHAR</td>
    <td>Same as Varchar but having a fixed-length max 255</td>
  </tr>
  <tr>
    <td>DATE</td>
    <td>DATE</td>
    <td>Years months and days in the form in the form YYYY-­MM-­DD</td>
  </tr>
  <tr>
    <td>DECIMAL</td>
    <td>DECIMAL</td>
    <td>38-digit precision</td>
  </tr>
  <tr>
    <td>FLOAT</td>
    <td>FLOAT</td>
    <td>4-byte single precision floating point number</td>
  </tr>
  <tr>
    <td>DOUBLE</td>
    <td>DOUBLE</td>
    <td>8-byte double precision floating point number</td>
  </tr>
  <tr>
    <td>INTEGER</td>
    <td>INT</td>
    <td>4-byte signed integer</td>
  </tr>
  <tr>
    <td>INTERVAL</td>
    <td>N/A</td>
    <td>Integer fields representing a period of time depending on the type of interval</td>
  </tr>
  <tr>
    <td>INTERVALDAY</td>
    <td>N/A</td>
    <td>Integer fields representing a day</td>
  </tr>
  <tr>
    <td>INTERVALYEAR</td>
    <td>N/A</td>
    <td>Integer fields representing a year</td>
  </tr>
  <tr>
    <td>SMALLINT</td>
    <td>SMALLINT</td>
    <td>2-byte signed integer</td>
  </tr>
  <tr>
    <td>TIME</td>
    <td>N/A</td>
    <td>Hours minutes seconds 24-hour basis</td>
  </tr>
  <tr>
    <td>TIMESTAMP</td>
    <td>N/A</td>
    <td>Conventional UNIX Epoch timestamp.</td>
  </tr>
  <tr>
    <td>N/A</td>
    <td>TIMESTAMP</td>
    <td>JDBC timestamp in yyyy-mm-dd hh:mm:ss format</td>
  </tr>
  <tr>
    <td>TIMESTAMPTZ</td>
    <td>N/A</td>
    <td>Hours ahead of or behind Coordinated Universal Time (UTC) or regional hours and minutes</td>
  </tr>
  <tr>
    <td>N/A</td>
    <td>STRING</td>
    <td>Binary string (16)</td>
  </tr>
  <tr>
    <td>BINARY</td>
    <td>BINARY</td>
    <td>Binary string</td>
  </tr>
  <tr>
    <td>VARCHAR</td>
    <td>VARCHAR</td>
    <td>Character string variable length</td>
  </tr>
</table>

## Unsupported Types
The following Hive types are not supported:

* LIST
* MAP
* STRUCT
* TIMESTAMP (Unix Epoch format)
* UNION

The Hive version used in MapR supports the Hive timestamp in Unix Epoch format. Currently, the Apache Hive version used by Drill does not support this timestamp format. The workaround is to use the JDBC format for the timestamp, which Hive accepts and Drill uses, as shown in the type mapping example. The timestamp value appears in the CSV file in JDBC format: 2015-03-25 01:23:15. The Hive table defines column i as a timestamp column. The Drill extract function verifies that Drill interprets the timestamp correctly.

## Type Mapping Example
This example demonstrates the mapping of Hive data types to Drill data types. Using a CSV that has the following contents, you create a Hive table having values of different supported types:

     100005,true,3.5,-1231.4,3.14,42,"SomeText",2015-03-25,2015-03-25 01:23:15 

The example assumes that the CSV resides on the MapR file system (MapRFS) in the Drill sandbox: `/mapr/demo.mapr.com/data/`
 
In Hive, you define an external table using the following query:

    hive> CREATE EXTERNAL TABLE types_demo ( 
          a bigint, 
          b boolean, 
          c DECIMAL(3, 2), 
          d double, 
          e float, 
          f INT, 
          g VARCHAR(64), 
          h date,
          i timestamp
          ) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' 
          LINES TERMINATED BY '\n' 
          STORED AS TEXTFILE LOCATION '/mapr/demo.mapr.com/data/mytypes.csv';

You check that Hive mapped the data from the CSV to the typed values as as expected:

    hive> SELECT * FROM types_demo;
    OK
    100005	true	3.5	-1231.4	3.14	42	"SomeText"	2015-03-25   2015-03-25 01:23:15
    Time taken: 0.524 seconds, Fetched: 1 row(s)

In Drill, you use the Hive storage plugin that has the following definition.

	{
	  "type": "hive",
	  "enabled": true,
	  "configProps": {
	    "hive.metastore.uris": "thrift://localhost:9083",
	    "hive.metastore.sasl.enabled": "false"
	  }
	}

Using the Hive storage plugin connects Drill to the Hive metastore containing the data.
	
	0: jdbc:drill:> USE hive;
	+------------+------------+
	|     ok     |  summary   |
	+------------+------------+
	| true       | Default schema changed to 'hive' |
	+------------+------------+
	1 row selected (0.067 seconds)
	
The data in the Hive table shows the expected values.
	
	0: jdbc:drill:> SELECT * FROM hive.`types_demo`;
	+--------+------+------+---------+------+----+------------+------------+-----------+
	|   a    |   b  |  c   |     d   |  e   | f  |     g      |     h      |     i     |
	+------------+---------+---------+------+----+------------+------------+-----------+
	| 100005 | true | 3.50 | -1231.4 | 3.14 | 42 | "SomeText" | 2015-03-25 | 2015-03-25 01:23:15.0 |
	+--------+------+------+---------+------+----+------------+------------+-----------+
	1 row selected (1.262 seconds)
	
To validate that Drill interprets the timestamp in column i correctly, use the extract function to extract part of the date:

    0: jdbc:drill:> select extract(year from i) from hive.`types_demo`;
    +------------+
    |   EXPR$0   |
    +------------+
    | 2015       |
    +------------+
    1 row selected (0.387 seconds)