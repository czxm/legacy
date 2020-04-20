ADD JAR /usr/lib/hive/lib/hive-contrib.jar;

-- basic filtering
-- SELECT a.uri FROM apachelog a WHERE a.method='GET' AND a.status='200';

-- determine popular URLs (for caching purposes)

CREATE EXTERNAL TABLE IF NOT EXISTS apachelog_${hiveconf:user} (remoteHost STRING, remoteLogname STRING, user STRING, time STRING, method STRING, uri STRING, proto STRING, status STRING, bytes STRING, referer STRING, userAgent STRING) ROW FORMAT SERDE 'org.apache.hadoop.hive.contrib.serde2.RegexSerDe' WITH SERDEPROPERTIES ("input.regex" = "^([^ ]*) +([^ ]*) +([^ ]*) +\\[([^]]*)\\] +\\\"([^ ]*) ([^ ]*) ([^ ]*)\\\" ([^ ]*) ([^ ]*) (?:\\\"-\\\")*\\\"(.*)\\\" (.*)$", "output.format.string" = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s %9$s %10$s %11$s") STORED AS TEXTFILE LOCATION '${hiveconf:TESTROOT}/hive/hivedata';

CREATE TABLE IF NOT EXISTS histogram_${hiveconf:user} (hits STRING, uris INT);


INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_uri_hits' SELECT a.uri, "\t", COUNT(*) FROM apachelog_${hiveconf:user} a GROUP BY a.uri ORDER BY uri;

INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_uris_to_cache_report' SELECT t1.uri, t1.total FROM (SELECT uri, count(1) AS total from apachelog_${hiveconf:user} GROUP by uri) t1 where t1.total>5;

INSERT OVERWRITE TABLE histogram_${hiveconf:user} SELECT hits, COUNT(*) FROM (SELECT COUNT(a.uri) AS hits FROM apachelog_${hiveconf:user} a GROUP BY a.uri) t3 GROUP BY hits;
INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_historgram' SELECT * from histogram_${hiveconf:user};

INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_low_hits' SELECT h.uris FROM histogram_${hiveconf:user} h WHERE h.hits == 1;
INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_high_hits' SELECT SUM(h.hits * h.uris) FROM histogram_${hiveconf:user} h WHERE h.hits > 1;
INSERT OVERWRITE DIRECTORY '${hiveconf:TESTROOT}/hive/${hiveconf:user}/hive_uris_to_cache' SELECT SUM(h.uris) FROM histogram_${hiveconf:user} h WHERE h.hits > 1;

DROP TABLE IF EXISTS apachelog_${hiveconf:user};

DROP TABLE IF EXISTS histogram_${hiveconf:user};
