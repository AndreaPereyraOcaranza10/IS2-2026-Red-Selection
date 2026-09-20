/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9973334189476774, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET Login-1"], "isController": false}, {"data": [1.0, 500, 1500, "GET Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "GET Inicio"], "isController": false}, {"data": [0.9954151934705746, 500, 1500, "GET Foto Perfil"], "isController": false}, {"data": [0.9913071527823728, 500, 1500, "POST Login"], "isController": false}, {"data": [0.9947213882748837, 500, 1500, "GET Explorar Mascotas"], "isController": false}, {"data": [1.0, 500, 1500, "GET Login"], "isController": false}, {"data": [0.9945328525110831, 500, 1500, "POST Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "GET Explorar Mascotas-1"], "isController": false}, {"data": [1.0, 500, 1500, "POST Login-1"], "isController": false}, {"data": [1.0, 500, 1500, "GET Explorar Mascotas-0"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 545080, 0, 0.0, 87.73743670653887, 0, 1212, 51.0, 284.0, 334.9500000000007, 472.0, 4537.002355565543, 31662.759571305796, 1082.5436362274327], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET Login-1", 60341, 0, 0.0, 23.90946454317973, 0, 131, 35.0, 48.0, 52.0, 64.0, 503.1225767720311, 1516.597426075913, 86.47419288269283], "isController": false}, {"data": ["GET Login-0", 60341, 0, 0.0, 26.91834739232027, 0, 133, 37.0, 48.0, 52.0, 63.0, 503.1225767720311, 91.22456652881192, 85.9828622413139], "isController": false}, {"data": ["GET Inicio", 60653, 0, 0.0, 27.25133134387441, 0, 127, 37.0, 48.0, 53.0, 64.0, 505.3995500374969, 2430.118583868011, 83.77483842492292], "isController": false}, {"data": ["GET Foto Perfil", 60526, 0, 0.0, 146.13817202524368, 2, 1041, 150.0, 294.0, 368.0, 485.0, 504.07668668226825, 20382.49644064184, 90.57627963822009], "isController": false}, {"data": ["POST Login", 60452, 0, 0.0, 182.28273671673375, 3, 1212, 186.0, 337.0, 404.0, 532.9900000000016, 503.47716729547176, 1609.0087799124879, 234.52989140619144], "isController": false}, {"data": ["GET Explorar Mascotas", 60622, 0, 0.0, 150.8668800105596, 1, 1080, 153.0, 298.0, 371.0, 494.0, 504.8299523666766, 2420.492805101533, 94.09656729872422], "isController": false}, {"data": ["GET Login", 60641, 0, 0.0, 50.700301776026535, 1, 208, 67.0, 91.0, 99.0, 121.0, 505.4891009877881, 1616.6829338895718, 172.70899161995166], "isController": false}, {"data": ["POST Login-0", 60452, 0, 0.0, 151.91191358433218, 2, 1174, 153.0, 297.0, 370.0, 498.0, 503.485553899073, 91.30789833749074, 147.9972184800986], "isController": false}, {"data": ["GET Explorar Mascotas-1", 300, 0, 0.0, 7.503333333333332, 0, 48, 3.0, 31.0, 38.0, 44.0, 20.06957452502007, 73.08539394902328, 3.4298589276157343], "isController": false}, {"data": ["POST Login-1", 60452, 0, 0.0, 30.291785218024227, 0, 133, 38.0, 49.0, 54.95000000000073, 66.0, 503.5065216304909, 1517.7908894206744, 86.54018340524063], "isController": false}, {"data": ["GET Explorar Mascotas-0", 300, 0, 0.0, 7.776666666666668, 0, 53, 2.0, 33.0, 39.94999999999999, 44.0, 20.122073915084847, 3.6353356194245086, 3.7335879334630087], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 545080, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
