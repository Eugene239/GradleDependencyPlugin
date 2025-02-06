<script>
export default {
  props: {
    treeData: {
      type: Object,
      required: true
    },
    // filter: {
    //   type: String,
    //   required: false
    // }
  },
  data() {
    return {
      width : window.outerWidth,
      marginTop : 10,
      marginRight : 10,
      marginBottom : 10,
      marginLeft : 5,
      root : null,
      tree : null,
      svg : null,
      gLink : null,
      gNode : null,
      //filteredData : this.treeData
    }
  },

  watch: {
    treeData: function(newVal) {
      this.clear();
      this.drawTree();
    },
    // filter: function (newVal) {
    //   this.filterData();
    // }
  },

  mounted() {
    this.drawTree(this.treeData);
  },

  methods: {
    clear() {
      const elem = this.$refs[this.treeData.name]
       if (elem) {
         elem.innerHTML = '';
       }
    },

    // filterData() {
    //   console.log("Filter data", this.filter);
    //
    // },

    drawTree() {
   //   console.log("Draw tree", this.treeData);
      // Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
      // (dx is a height, and dy a width). This because the tree must be viewed with the root at the
      // “bottom”, in the data domain. The width of a column is based on the tree’s height.
      this.root = d3.hierarchy(this.treeData);
      const dx = 20;
      //const dy = 200;
      const dy = (this.width - this.marginRight - this.marginLeft) / (1 + this.root.height); // todo calculate

      // Define the tree layout and the shape for links.
      this.tree = d3.tree().nodeSize([dx, dy]);
      this.diagonal = d3.linkHorizontal().x(d => d.y).y(d => d.x);

      // Create the SVG container, a layer for the links and a layer for the nodes.
      this.svg = d3.select(this.$refs[this.treeData.name])
          .append("svg")
          .attr("width", this.width)
          .attr("height", dx)
          .attr("style", "max-width: 100%;  font: 14px sans-serif; user-select: none;");

      //   .attr("viewBox", [-this.marginLeft, -this.marginTop, this.width, dx])
      //    .attr("style", "max-width: 100%;  font: 10px sans-serif; user-select: none;");
      this.gLink = this.svg.append("g")
          .attr("fill", "none")
          .attr("stroke", "#555")
          .attr("stroke-opacity", 0.4)
          .attr("stroke-width", 1.5);
      this.gNode = this.svg.append("g")
          .attr("cursor", "pointer")
          .attr("pointer-events", "all");
      // Do the first update to the initial configuration of the tree — where a number of nodes
      // are open (arbitrarily selected as the root, plus nodes with 7 letters).
      this.root.x0 = dy / 2;
      this.root.y0 = 0;
      this.root.descendants().forEach((d, i) => {
        //console.log("descendants", d, i)
        d.id = i;
//            d._children =d.children
        d._children =d.children
        if (d.depth > this.defaultDepth) {
          d.children = null;
        }
        // var children = flatDependencies[d.data];
        // d.children =
        //if (d.depth && d.depth > 2) d.children = null;
        // console.log(d.depth && d.data.name.length !== 6);
        //if (d.depth && d.data.name.length !== 3) d.children = null;
      });

      this.update(this.root);

    },

    update(source) {
      //  console.log("update", source);
      const duration = 250;
      const nodes = this.root.descendants().reverse();

      const links = this.root.links();

      // Compute the new tree layout.
      this.tree(this.root);

      let left = this.root;
      let right = this.root;
      this.root.eachBefore(node => {
        if (node.x < left.x) left = node;
        if (node.x > right.x) right = node;
      });
      const height = right.x - left.x + this.marginTop + this.marginBottom;


      const transition = this.svg.transition()
          .duration(duration)
          .attr("height", height)
          .attr("viewBox", [-this.marginLeft, left.x - this.marginTop, this.width, height])
          .tween("resize", window.ResizeObserver ? null : () => () => this.svg.dispatch("toggle"));

      // Update the nodes…
      const node = this.gNode.selectAll("g")
          .data(nodes, d => d.id);

      // Enter any new nodes at the parent's previous position.
      const nodeEnter = node.enter().append("g")
          .attr("transform", d => `translate(${source.y0},${source.x0})`)
          .attr("fill-opacity", 0)
          .attr("stroke-opacity", 0)
          .on("click", (event, d) => {
            console.log("click", event, d);
            event.children = event.children ? null : event._children;
            this.update(event);
          });

      nodeEnter.append("circle")
          .attr("r", 5)
          .attr("fill", d => {
            // console.log("circle", d.data.children);
            //var children = flatDependencies[d.data.name]? flatDependencies[d.data.name] : []
            //return children.length > 0 ? "#5A5" : "#A55"
            // todo
            return "#A55"
          })
          .attr("stroke-width", 8);

      nodeEnter.append("text")
          .attr("dy", "0.31em")
          .attr("x", d => 8)
          .classed("node-conflict", d => {
            var data = d.data
            var latest = null;// latestVersions[data.name]
            if (latest == null) return false;
            var version = data.versions ? (data.versions.actual ? data.versions.actual : data.versions.resolved) : "";
            return !latest || latest !== version;
          })
          .attr("text-anchor", d =>  /*d._children ? "end" : */ "start")
          .text(d => {
            // var data = d.data;
            // var latest = latestVersions[data.name]
            // var version = data.versions ? (data.versions.actual ? data.versions.actual : data.versions.resolved) : "";
            // var text = data.pomName ? data.pomName : data.name;
            // return text + ":" + version;
            return d.data.name;
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
      const link = this.gLink.selectAll("path")
          .data(links, d => d.target.id);

      // Enter any new links at the parent's previous position.
      const linkEnter = link.enter().append("path")
          .attr("d", d => {
            const o = { x: source.x0, y: source.y0 };
            return this.diagonal({ source: o, target: o });
          });

      // Transition links to their new position.
      link.merge(linkEnter).transition(transition)
          .attr("d", this.diagonal);

      // Transition exiting nodes to the parent's new position.
      link.exit().transition(transition).remove()
          .attr("d", d => {
            const o = { x: source.x, y: source.y };
            return this.diagonal({ source: o, target: o });
          });

      // Stash the old positions for transition.
      this.root.eachBefore(d => {
        d.x0 = d.x;
        d.y0 = d.y;
      });
    }
  }
}

</script>

<template>
<div class="media" v-if="this.treeData!=null" :ref="treeData.name" :name="treeData.name">

</div>
</template>