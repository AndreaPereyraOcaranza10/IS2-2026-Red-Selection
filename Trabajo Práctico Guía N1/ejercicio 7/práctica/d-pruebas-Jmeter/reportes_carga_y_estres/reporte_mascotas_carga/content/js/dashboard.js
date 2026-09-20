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
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [1.0, 500, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [1.0, 500, 1500, "GET Login-1"], "isController": false}, {"data": [1.0, 500, 1500, "GET Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "GET Inicio"], "isController": false}, {"data": [1.0, 500, 1500, "GET Foto Perfil"], "isController": false}, {"data": [1.0, 500, 1500, "POST Login"], "isController": false}, {"data": [1.0, 500, 1500, "GET Explorar Mascotas"], "isController": false}, {"data": [1.0, 500, 1500, "GET Login"], "isController": false}, {"data": [1.0, 500, 1500, "POST Login-0"], "isController": false}, {"data": [1.0, 500, 1500, "GET Explorar Mascotas-1"], "isController": false}, {"data": [1.0, 500, 1500, "POST Login-1"], "isController": false}, {"data": [1.0, 500, 1500, "GET Explorar Mascotas-0"], "isController": false}]}, function(index, item){
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
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 503839, 0, 0.0, 9.065455433183837, 0, 75, 5.0, 20.900000000001455, 25.0, 33.0, 4199.743267010645, 29316.406803528414, 1002.4852286059316], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["GET Login-1", 55962, 0, 0.0, 3.071280511775857, 0, 24, 3.0, 5.0, 6.0, 9.0, 466.65332466102967, 1406.6648855610313, 80.20604017611447], "isController": false}, {"data": ["GET Login-0", 55962, 0, 0.0, 2.2963260784103414, 0, 22, 2.0, 4.0, 5.0, 7.0, 466.65721600053365, 84.62929827240433, 79.7509890625912], "isController": false}, {"data": ["GET Inicio", 55992, 0, 0.0, 3.022860408629798, 0, 29, 3.0, 5.0, 6.0, 9.0, 466.76336717851245, 2244.343674582979, 77.47706814572601], "isController": false}, {"data": ["GET Foto Perfil", 55979, 0, 0.0, 14.337573018453304, 1, 75, 14.0, 25.0, 28.0, 38.0, 466.71335551053414, 18871.68013693223, 83.86255606829911], "isController": false}, {"data": ["POST Login", 55968, 0, 0.0, 16.96708833619218, 2, 75, 16.0, 27.0, 31.0, 41.0, 466.64498861902496, 1491.2706449531838, 217.37271442507316], "isController": false}, {"data": ["GET Explorar Mascotas", 55988, 0, 0.0, 19.566907194398947, 2, 70, 19.0, 30.0, 34.0, 45.0, 466.77282466422673, 2240.040498681188, 86.65098243911895], "isController": false}, {"data": ["GET Login", 55992, 0, 0.0, 5.4546899557079715, 1, 30, 5.0, 9.0, 10.0, 13.0, 466.8217403266552, 1491.961530034058, 159.9581578593582], "isController": false}, {"data": ["POST Login-0", 55968, 0, 0.0, 13.641598770726091, 1, 73, 13.0, 24.0, 28.0, 36.0, 466.6488794023479, 84.62968987105623, 137.16925068369795], "isController": false}, {"data": ["GET Explorar Mascotas-1", 30, 0, 0.0, 2.1999999999999993, 1, 7, 2.0, 4.900000000000002, 5.899999999999999, 7.0, 2.0768431983385254, 7.5630354361370715, 0.3549292575285566], "isController": false}, {"data": ["POST Login-1", 55968, 0, 0.0, 3.2364922813036148, 0, 20, 3.0, 5.0, 6.0, 9.0, 466.6644431844712, 1406.700304000392, 80.20795117233098], "isController": false}, {"data": ["GET Explorar Mascotas-0", 30, 0, 0.0, 1.5000000000000002, 0, 4, 1.0, 2.900000000000002, 3.4499999999999993, 4.0, 2.076986984214899, 0.375236906327887, 0.3853784443367488], "isController": false}]}, function(index, item){
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
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 503839, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
