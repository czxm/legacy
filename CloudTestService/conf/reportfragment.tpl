<HTML>
<HEAD>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
<STYLE type="text/css">
/*
cruise Modifications
 */
    body, table, div.cruiseBuildInfo {
        font: normal 10pt Verdana, Arial, Helvetica;
    }

    div.cruiseBuildInfo td, div.cruiseBuildInfo th {
    font-size: 10pt;
    margin: 1px;
    padding: 5px;
    }
    table#buildResults, table#buildLog, table#sourceModifications {
    margin-left: -2px
    }
    h1 {
    font-size: 12pt;
    margin-top: 1em;
    margin-bottom: 0.5em;
    }
    h2 {
    font-size: 11pt;
    margin-top: 0.75em;
    margin-bottom: 0.25em;
    margin-right: 4px;
    /*
    padding-bottom: 3px;
    border-bottom: solid thin blue;

    background-color:#000066;
    color: #FFFFFF;
    padding: 3px;
    */
    }
    h3 {
    font-size: 10pt;
    margin-top: 0.5em;
    margin-bottom: 0.25em;
    margin-right: 4px;
    }

    div.cruiseBuildInfo th {
    font-weight: bold;
    background: #a6caf0;
    color: black;
    vertical-align: top;
    }
    div.cruiseBuildInfo td {
    background: #eeeee0;
    }

    div.cruiseBuildInfo tbody th, div.cruiseBuildInfo tfoot th {
    text-align: right;
    }
    div.cruiseBuildInfo tfoot th, div.cruiseBuildInfo tfoot td {
    border-top: solid 3px white;
    }

    div.cruiseBuildInfo table#rtestSummary td, div.cruiseBuildInfo table#rtestFailures td, div.cruiseBuildInfo table#buildInfo td {
    text-align: right;
    }

    table#changes, table#details, table.modifications, table.modifications table, table#buildResults, table#buildLog {
    width: 99%;
    }

    col.type, col.modification-info {
    width: 10em;
    }
    colgroup.numbers, col.numbers {
    width: 5em;
    }


    /* Modifications */
    table#sourceModifications table.modification-info {
    margin-left: 0;
    }
    table#sourceModifications table td, p.modification-comment {
    font-size: 8pt;
    }
    p.modification-comment {
    margin-left: 2px;
    margin-bottom: 0.5em;
    }
    table#sourceModifications td {
    vertical-align: top;
    background: #eeeee0;
    }
    col.modification-info {
    width: 10em
    }

    table#sourceModifications table.modification-info td {
    margin: 0px;
    padding: 1px 0px 1px 0px;
    }

    table#sourceModifications table.modification-files td {
    padding-top: 0px;
    padding-bottom: 0px;
    }

    table#sourceModifications table.modification-files tr.odd-row td {
    background-color: #FFFFDD;
    }
    table#sourceModifications table.modification-files tr.even-row td {
    background-color: #FFFFEE;
    }

    col.modification-action {
    width: 5em;
    }

    table#sourceModifications table td {
    background-color: transparent;
    }
    table#sourceModifications td.modification-info {
    background-color: #a6caf0;
    }

    .FAILED {
    font-weight: bold;
    color: red;
    }
    .OK {
    font-weight: bold;
    color: green;
    }

    .failedTest a:link {
    color: maroon;
    }
    .failedTest a:visited {
    color: purple;
    }
    .failedTest a:hover, .failedTest a:active {
    color: red;
    }

    tr.rtestInfo td, tr.rtestInfo th {
    border-top: 5px solid white;
    }

    .SQLDB {
    float: right;
    padding-left: 5px;
    }

    /*
    --------------------------
    */

    body {
    padding-bottom: 10px;
    }

    div.cruiseBuildInfo {
    margin-left: 7px;
    }

    p.makeLog {
    margin-top: 0;
    margin-bottom: 0;
    margin-left: 3em;
    text-indent: -3em;
    font-family: 'Lucida Console', 'Courier New', monospace;
    /* Verdana, Arial,helvetica,sans-serif; */
    font-size:8pt;
    }

    div#buildLog {
    margin-left: 7px;
    }

    div.cruiseBuildInfo th#buildFailed, th#buildSucceeded {
    text-align: left;
    font-size: 11pt;
    font-weight: bold;
    background-color: transparent;
    }

    table#buildResults th#buildFailed {
    color: red;
    }

    table#buildResults th#buildSucceeded {
    color: green;
    }


    /*
    Standard CruiseControl Stylesheet
    */

    .white { color:#FFFFFF }

    .index { background-color:#FFFFFF }
    .index-passed { color:#004400 }
    .index-failed { color:#FF0000; font-weight:bold }
    .index-header { font-weight:bold }

    .link { font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; text-decoration:none; }

    .tab-table { margin: 0em 0em 0.5em 0em; }
    .tabs { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; font-weight:bold; padding: 0em 2em; background-color:#EEEEEE; }
    .tabs-link { color:#000000; text-decoration:none; }
    .tabs-link:visited { color:#000000; text-decoration:none; }
    .tabs-selected { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; font-weight:bold; padding: 0em 2em; }
    .tabs-selected { border: inset; }

    table.header { font-family:arial,helvetica,sans-serif; font-size:10pt; color:#000000; }
    table.header th { text-align:left; vertical-align:top; white-space:nowrap; }
    table.header th.big { font-size:12pt; }

    .modifications-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .modifications-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }
    .modifications-oddrow { background-color:#CCCCCC }
    .modifications-evenrow { background-color:#FFFFCC }

    .changelists-oddrow { background-color:#CCCCCC }
    .changelists-evenrow { background-color:#FFFFCC }
    .changelists-file-spacer { background-color:#FFFFFF }
    .changelists-file-evenrow { background-color:#EEEEEE }
    .changelists-file-oddrow { background-color:#FFFFEE }
    .changelists-file-header { background-color:#666666; font-family:arial,helvetica,sans-serif; font-size:8pt; color:#FFFFFF; }

    .compile-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .compile-error-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#FF0000; }
    .compile-warn-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#CC9900; }
    .compile-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }

    .distributables-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .distributables-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }
    .distributables-oddrow { background-color:#CCCCCC }

    .unittests-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }
    .unittests-oddrow { background-color:#CCCCCC }
    .unittests-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .unittests-error { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#901090; }
    .unittests-failure { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#FF0000; }
    .unittests-title { font-family:arial,helvetica,sans-serif; font-size:9pt; font-weight: bold; color:#000080; background-color:#CCDDDD; }
    .unittests-error-title { font-family:arial,helvetica,sans-serif; font-size:9pt; font-weight: bold; color:#901090; background-color:#CCDDDD; }
    .unittests-failure-title { font-family:arial,helvetica,sans-serif; font-size:9pt; color:#FF0000; font-weight: bold; background-color:#CCDDDD; }

    .checkstyle-oddrow { background-color:#CCCCCC }
    .checkstyle-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .checkstyle-warning { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .checkstyle-error { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#FF0000; }
    .checkstyle-fileheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; font-weight:bold; color:#FFFFFF; }
    .checkstyle-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }

    .macker-oddrow { background-color:#CCCCCC }
    .macker-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .macker-data-error { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#FF0000; }
    .macker-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }

    a img { border: 0 }

    .hidden { visibility: hidden }

    .config-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }
    .config-result-message { font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FF0000; }

    .differences-sectionheader { background-color:#000066; font-family:arial,helvetica,sans-serif; font-size:10pt; color:#FFFFFF; }
    .differences-data { font-family:arial,helvetica,sans-serif; font-size:8pt; color:#000000; }
    .differences-oddrow { background-color:#CCCCCC }
    .differences-evenrow { background-color:#FFFFCC }

    .testresults-output-div { border:solid 1px; font-size: 9pt; font-family:monospace; overflow: auto; }

    .junit-test-info { background-color: #a6caf0; }

    .console-info { display: none; }



    table.detail tr th{
    font-weight: bold;
    text-align:left;
    background:#a6caf0;
    }
    table.detail tr td{
    background:#eeeee0;
    }
    h4 {
    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
    }
    h5 {
    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
    }
    h6 {
    margin-bottom: 0.5em; font: bold 100% verdana,arial,helvetica
    }
    .Error {
    font-weight:bold; color:red;background:#C0C0C0;
    }
    .Failure {
    font-weight:bold; color:purple;background:#C0C0C0;
    }
    .Pass {
    font-weight:bold; color:black;background:#C0C0C0;
    }
    .NoRegression {
    font-weight:bold; color:blue;background:#C0C0C0;
    }

    .Properties {
    text-align:right;
    }

body {	font-family: Verdana, Arial, sans-serif;	font-size: 10pt;	background-color: white;	color: black;}
table {	font-size: 10pt;}h1 {	font-size: 12pt;	font-weight: bold;	margin-bottom: 0.25em;}
h2 {	font-size: 11pt;	font-weight: bold;	margin-bottom: 0.25em;}
h1, h2 {	/* Account for table border */	margin-left: 3px;}
th {	font-weight: bold;	background-color: #a6caf0;	vertical-align: top;}
td {	background-color: #eeeee0;}
th, td {	padding: 3px 5px;}
tbody th, tfoot th {	text-align: right;}
table#details th {  text-align: left;}
pre{margin-left:3px;margin-bottom:10pt;line-height:115%;}
p{margin-left:3px;margin-bottom:10pt;line-height:17pt;}
</STYLE>
</HEAD>
<BODY>
$CONTENT$
<P><EM>Powered by Cloud Test Service</EM></P>
</BODY>
</HTML>