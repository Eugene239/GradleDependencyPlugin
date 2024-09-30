var data = {};
var filteredData = {};
var globalTimeout = null;
var conflicts = {};
var filterTerm = "";

var CONFLICT_ATTR = "dep-name";

var selectedConfiguration = null;

function onLoad() {
  isLoading(true)
  fetch("/configurations.json")
    .then(response => response.json())
    .then(json => {
      setConfigurations(json);
      console.log(json);
      //       var configuration = json["configurations"][0].name
      //       fetch(configuration + "/dependencies.json")
      //           .then(response => response.json())
      //               .then(json => {
      //                   data = json
      //                   filteredData = json
      //                   fillConflicts()
      //                    drawTree()
      //     })
      //   })
      //   .catch(error => console.log(error))

      // document.getElementsByName("filter")[0].addEventListener('change', function (event) {
      //   event.preventDefault();
      //   if (globalTimeout != null) clearTimeout(globalTimeout);
      //   globalTimeout = setTimeout(function () { filter(data, event.target.value) }, 300);
    });
}

function setConfigurations(configurations) {
  var container = document.getElementsByName("configuration-list")[0];
  configurations.configurations.forEach(config => {
    container.innerHTML += '<a class="navbar-item" type="configuration-selector" config="' + config.name + '">' + config.name + '</a>';
  });
  var selectors = document.querySelectorAll('[type="configuration-selector"]');
  selectors.forEach(selector => {
    selector.addEventListener("click", function () { selectConfiguration(selector.attributes["config"].value) });
  });
  selectConfiguration(configurations.configurations[0].name);
}

function selectConfiguration(name) {
  var selectors = document.querySelectorAll('[type="configuration-selector"]');
  selectors.forEach(selector => {
    if (selector.attributes["config"].value == name) {
      selector.classList.add("is-active");
    } else {
      selector.classList.remove("is-active");
    }
  });
  if (selectedConfiguration !== name) {
    selectedConfiguration = name;
    fetchConfiguration(name);
  }
  console.log("select: " + name);
}

function fetchConfiguration(name) {
  isLoading(true);
  fetch(name + "/dependencies.json")
    .then(response => response.json())
    .then(json => {
      data = json
      filteredData = json
      fillConflicts()
      drawTree()
      isLoading(false);
    })
}


function filter(tree, term) {
  filteredData = JSON.parse(JSON.stringify(tree))
  filterTree(filteredData, term);
  drawTree();
}

function filterTree(tree, term) {
  filterTerm = term;
  if (!term || term.trim().length === 0) {
    return true;
  }
  if (tree.name.includes(term)) {
    // tree.children = null;
    return true
  }
  if (tree.children && tree.children.length > 0) {
    tree.children = tree.children.filter(child => filterTree(child, term));
    return tree.children && tree.children.length > 0;
  }
  return false;
}

function fillConflicts() {
  var layout = document.getElementById("conflicts")
  data.children.forEach(child => {
    checkNode(child);
  });
  for (var item in conflicts) {
    var domItem = document.createElement("div");
    domItem.className = "conflict-item"
    domItem.textContent = item + " (" + Array.from(conflicts[item]).join(", ") + ")";
    domItem.setAttribute(CONFLICT_ATTR, item);
    layout.appendChild(domItem);

  }
}

function checkNode(node) {
  if (!isCorrectVersion(node)) {
    var split = node.name.split(":")
    var dependency = split[0] + ":" + split[1]
    var versions = conflicts[dependency] ? conflicts[dependency] : new Set();
    versions.add(split[2]);
    versions.add(node.pomName.split(":")[2])
    conflicts[dependency] = versions;
  }
  if (node.children && node.children.length > 0) {
    node.children.forEach(child => {
      checkNode(child);
    })
  }

}

function isCorrectVersion(node) {
  return !node.pomName || node.pomName == node.name
}


function drawTree() {
  d3.selectAll("svg > *").remove();
  // Specify the charts’ dimensions. The height is variable, depending on the layout.
  const width = window.outerWidth
  const marginTop = 10;
  const marginRight = 10;
  const marginBottom = 10;
  const marginLeft = 200;

  // Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
  // (dx is a height, and dy a width). This because the tree must be viewed with the root at the
  // “bottom”, in the data domain. The width of a column is based on the tree’s height.
  const root = d3.hierarchy(filteredData);
  const dx = 20;
  //const dy = 200;
  const dy = (width - marginRight - marginLeft) / (1 + root.height); // todo calculate

  // Define the tree layout and the shape for links.
  const tree = d3.tree().nodeSize([dx, dy]);
  const diagonal = d3.linkHorizontal().x(d => d.y).y(d => d.x);

  // Create the SVG container, a layer for the links and a layer for the nodes.
  const svg = d3.select("svg")
    .attr("width", width)
    .attr("height", dx)
    .attr("viewBox", [-marginLeft, -marginTop, width, dx])
    .attr("style", "max-width: 100%;  font: 10px sans-serif; user-select: none;");

  const gLink = svg.append("g")
    .attr("fill", "none")
    .attr("stroke", "#555")
    .attr("stroke-opacity", 0.4)
    .attr("stroke-width", 1.5);

  const gNode = svg.append("g")
    .attr("cursor", "pointer")
    .attr("pointer-events", "all");

  function update(event, source) {
    const duration = event?.altKey ? 2500 : 250; // hold the alt key to slow down the transition
    const nodes = root.descendants().reverse();

    const links = root.links();

    // Compute the new tree layout.
    tree(root);

    let left = root;
    let right = root;
    root.eachBefore(node => {
      if (node.x < left.x) left = node;
      if (node.x > right.x) right = node;
    });

    const height = right.x - left.x + marginTop + marginBottom;

    const transition = svg.transition()
      .duration(duration)
      .attr("height", height)
      .attr("viewBox", [-marginLeft, left.x - marginTop, width, height])
      .tween("resize", window.ResizeObserver ? null : () => () => svg.dispatch("toggle"));

    // Update the nodes…
    const node = gNode.selectAll("g")
      .data(nodes, d => d.id);

    // Enter any new nodes at the parent's previous position.
    const nodeEnter = node.enter().append("g")
      .attr("transform", d => `translate(${source.y0},${source.x0})`)
      .attr("fill-opacity", 0)
      .attr("stroke-opacity", 0)
      .on("click", (event, d) => {
        event.children = event.children ? null : event._children;
        update(event, d);
      });

    nodeEnter.append("circle")
      .attr("r", 4)
      .attr("fill", d => d._children ? "#5A5" : "#A55")
      .attr("stroke-width", 8);

    nodeEnter.append("text")
      .attr("dy", "0.31em")
      .attr("x", d => d._children ? -6 : 6)
      .classed("node-conflict", d => {
        var data = d.data
        return !isCorrectVersion(data) && data.name.includes(filterTerm);
      })
      .attr("text-anchor", d => d._children ? "end" : "start")
      .text(d => {
        var data = d.data;
        var text = data.pomName ? data.pomName : data.name;
        return text;
      })
      .clone(true).lower()
      .attr("stroke-linejoin", "round")
      .attr("stroke-width", 3)
      .attr("stroke", "white");

    // Transition nodes to their new position.
    const nodeUpdate = node.merge(nodeEnter).transition(transition)
      .attr("transform", d => `translate(${d.y},${d.x})`)
      .attr("fill-opacity", 1)
      .attr("stroke-opacity", 1);

    // Transition exiting nodes to the parent's new position.
    const nodeExit = node.exit().transition(transition).remove()
      .attr("transform", d => `translate(${source.y},${source.x})`)
      .attr("fill-opacity", 0)
      .attr("stroke-opacity", 0);

    // Update the links…
    const link = gLink.selectAll("path")
      .data(links, d => d.target.id);

    // Enter any new links at the parent's previous position.
    const linkEnter = link.enter().append("path")
      .attr("d", d => {
        const o = { x: source.x0, y: source.y0 };
        return diagonal({ source: o, target: o });
      });

    // Transition links to their new position.
    link.merge(linkEnter).transition(transition)
      .attr("d", diagonal);

    // Transition exiting nodes to the parent's new position.
    link.exit().transition(transition).remove()
      .attr("d", d => {
        const o = { x: source.x, y: source.y };
        return diagonal({ source: o, target: o });
      });

    // Stash the old positions for transition.
    root.eachBefore(d => {
      d.x0 = d.x;
      d.y0 = d.y;
    });
  }

  // Do the first update to the initial configuration of the tree — where a number of nodes
  // are open (arbitrarily selected as the root, plus nodes with 7 letters).
  root.x0 = dy / 2;
  root.y0 = 0;
  root.descendants().forEach((d, i) => {
    d.id = i;
    d._children = d.children;
    // if (d.depth && d.depth > 2) d.children = null;
    // console.log(d.depth && d.data.name.length !== 6);
    //if (d.depth && d.data.name.length !== 3) d.children = null;
  });

  update(null, root);

}


document.addEventListener('click', function (event) {
  var target = event.target;
  var attribute = target.getAttribute(CONFLICT_ATTR);
  if (!attribute) return;
  event.preventDefault();
  // todo fix changing 
  document.getElementsByName("filter")[0].value = attribute;
  filter(data, attribute);
}, false);

function isLoading(bool) {
  var elem = document.getElementsByName("progress-bar")[0]
  if (bool) {
    elem.classList.remove("is-hidden")
  } else {
    elem.classList.add("is-hidden")
  }
}