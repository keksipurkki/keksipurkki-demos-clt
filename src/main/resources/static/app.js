import { Client } from '@stomp/stompjs';
import * as d3 from "d3";

const brokerURL = 'ws://localhost:8080/ws';
const maxLevels = 5;

const screenWidth = Math.round(window.innerWidth / maxLevels);

// set the dimensions and margins of the graph
const margin = {top: 10, right: 5, bottom: 30, left: 5};
const width = screenWidth - margin.left - margin.right;
const height = width - margin.top - margin.bottom;

const svgs = [];

for (var i = 0; i < maxLevels; i++) {
   const svg = d3.select(`#histogram_${i + 1}`).append("svg").attr("height", height + margin.top + margin.bottom);
   svgs.push(svg);
}

function update(svg, values) {

  const maxCount = Math.max(...values.map(({ count }) => count));

  const x = d3.scaleBand()
      .range([0, width])
      .domain(values.map(({ min, max }) => ((max + min) / 2).toFixed(2)));

  svg.append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

  svg.append("g")
      .attr("transform", "translate(0," + height + ")")
      .call(d3.axisBottom(x));

  const y = d3.scaleLinear()
    .domain([0, 2 * Math.max(maxCount, 50)])
    .range([height, 0]);

  svg.append("g")
    .call(d3.axisLeft(y))

  svg.selectAll("rect")
    .data(values)
    .enter()
    .append("rect")
      .attr("x", function({ max, min }) { return x( ((max + min) / 2).toFixed(2) ); })
      .attr("y", function({ count }) { return y(count); })
      .attr("width", x.bandwidth())
      .attr("height", function({ count }) { return height - y(count); })
      .attr("fill", "#69b3a2");
}

const client = new Client({
    brokerURL,
    onConnect: () => {

      console.log("Connected to", brokerURL);

      client.subscribe('/topic/histograms', message => {
        const { histograms } = JSON.parse(message.body);
        d3.selectAll("svg > *").remove();
        histograms.forEach((histogram, level) => {
          console.log(`Updating histogram at level ${level + 1}`);
          update(svgs[level], histogram);
        });
      });
    },
});

client.activate();