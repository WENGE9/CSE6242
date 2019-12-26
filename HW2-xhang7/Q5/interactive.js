//Load csv data and draw charts
d3.csv("./state-year-earthquakes.csv").then(function(data) {

    var measureElm = document.getElementById('dummy');
    var responsiveWidth = measureElm.offsetWidth;

    var margin = {top: 50, right: 50, bottom: 50, left: 80}
        , width = measureElm.offsetWidth - margin.left - margin.right
        , height = 600 - margin.top - margin.bottom;


    var years = getUniqueYears(data);

    years.sort(function(a, b){return a-b});

    //Total by region and year
    var byRegionByYearCount = d3.nest()
        .key(function(d) { return d.region; }).sortKeys(d3.ascending)
        .key(function(d) { return d.year; }).sortKeys(d3.ascending)
        .rollup(function(leaves) { return d3.sum(leaves, function(d) {return +d.count;}); })
        .entries(data);

    var byRegionByYearAllState = d3.nest()
        .key(function(d) { return d.region; }).sortKeys(d3.ascending)
        .key(function(d) { return d.year; }).sortKeys(d3.ascending)
        .sortValues(function(a, b){

            if (a.count === b.count) {
                return b.state > a.state ? 1 : -1;
            }
            return b.count - a.count;
        })
        .entries(data);

    var colorDomain = byRegionByYearCount.map(function (obj) { return obj.key; });

    var totalArr = [];

    byRegionByYearCount.forEach(function (obj, idx) {

        var arr = obj.values;

        arr.forEach(function (item, idx2) {
            item['orig'] = obj.key;
            item['origIdx'] = idx;

            totalArr.push(item.value)
        });
    });

    var extentDom = d3.extent(totalArr);

    //Setting the scales
    var xScale = d3.scalePoint().domain(years).range([0, width]);
    var yScale = d3.scaleLinear().domain(extentDom).range([height, 0]);
    var color = d3.scaleOrdinal().range(d3.schemeCategory10).domain(colorDomain);
    var line = d3.line().curve(d3.curveLinear) //Used linear curve
        .x(function(d) {
            return xScale(d["key"]);
        })
        .y(function(d) {
            return yScale(d["value"]);
        });

    var svg = d3.select("body").select("#line-chart").append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.axisBottom(xScale));

    svg.append("g")
        .attr("class", "y axis")
        .call(d3.axisLeft(yScale));

    //Creating line
    var regionLine = svg
        .selectAll(".category")
        .data(byRegionByYearCount)
        .enter()
        .append("g")
        .attr("class", "category");

    regionLine
        .append("path")
        .attr("class", "line")
        .attr("d", function(d) {
            return line(d.values);
        })
        .style("stroke", function(d) {
            return color(d.key);
        });

    //Creating circle on the each line
    regionLine.selectAll("circle")
        .data(function(d, i){ return d.values})
        .enter()
        .append("circle")
        .attr("class", "dot")
        .attr("cx", function(d) { return xScale(d["key"]); })
        .attr("cy", function(d) { return yScale(d["value"]); })
        .attr("r", 4)
        .style("fill", function(d,i,j) { return color(d.orig); })
        .on("mouseover", function(a, b, c) {

            d3.select(this).attr("r", 8);

            showSubChart(a.orig, a.key, a.origIdx, byRegionByYearAllState[a.origIdx], responsiveWidth);
        })
        .on("mouseout", function(a, b, c) {

            d3.select(this).attr("r", 4);

            document.getElementById('ttl-2').innerText = '';
            d3.select("#bar-chart").select("svg").selectAll('g').remove();
        });

    //Creating legend
    var legend = svg.selectAll('.legend')
        .data(byRegionByYearCount)
        .enter()
        .append('g')
        .attr('class', 'legend');

    legend.append('circle')
        .attr('cx', (width - 20)-50)
        .attr('cy', function(d, i) {
            return i * 20;
        })
        .attr('r',5)
        .style('fill', function(d) {
            return color(d.key);
        });

    legend.append('text')
        .attr('x', (width - 8)-50)
        .attr('y', function(d, i) {
            return (i * 20) + 5;
        })
        .text(function(d) {
            return d.key;
        });

});

//Creating sub bar chart
function showSubChart(forRegion, forYear, regionIdx, dataCache, givenWd) {

    d3.select("#bar-chart").select("svg").selectAll('g').remove();

    document.getElementById('ttl-2').innerText = forRegion + 'ern Region Earthquakes ' + forYear;

    var valArray = [];

    dataCache['values'].forEach(function (obj, idx) {

        if(obj.key == forYear) valArray = obj.values;
    });


    var margin = {top: 50, right: 50, bottom: 50, left: 80}
        , width = givenWd - margin.left - margin.right
        , height = 600 - margin.top - margin.bottom;

    var subChart = d3.select("#bar-chart").select("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var allCount    = valArray.map(function (d){ return +d.count;});
    var extentDom   = d3.extent(allCount);

    var X = d3.scaleLinear().range([0, width]).domain(extentDom).nice();
    var Y = d3.scaleBand().domain(valArray.map(function (d){ return d.state;})).range([0, height]).padding(0.1);

    subChart.append("g").attr("class", "x axis").attr("transform", "translate(0," + height + ")").call(d3.axisBottom(X).tickSize(-height));
    subChart.append("g").attr("class", "y axis").call(d3.axisLeft(Y));

    var bars = subChart.selectAll(".bar")
        .data(valArray)
        .enter()
        .append("g");

    bars.append("rect")
        .attr("class", "bar")
        .attr("y", function (d) {
            return Y(d.state);
        })
        .attr("height", Y.bandwidth())
        .attr("x", 0)
        .attr("width", function (d) {
            return X(d.count);
        });
}
//Get unique date from data
function getUniqueYears(data) {

    return d3.map(data, function(d){return d.year;}).keys();
}

function getMaxCount($byRegByYear) {

    return d3.max($byRegByYear, function (obj) {
        return d3.max(obj.values, function(d) { return +d.value; });
    });
}