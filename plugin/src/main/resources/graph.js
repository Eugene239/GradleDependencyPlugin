class Graph {

    data = {};
    filteredData = {};
    conflicts = {};
    filterTerm = "";
    CONFLICT_ATTR = "dep-name";
    elem = null
    name = null

    init(elem, name) {
        this.elem = elem;
        this.name = name;
    }

    filter(term) {
        this.filteredData = JSON.parse(JSON.stringify(this.data))
        this.filterTree(this.filteredData, term);
        this.clear();
        this.drawTree(this.filteredData);
        var children = this.filteredData.children
        return children ? children.length : 0;  
    }

    filterTree(tree, term) {
        this.filterTerm = term;
        if (!term || term.trim().length === 0) {
            return true;
        }
        if (tree.name.includes(term)) {
            // tree.children = null;
            return true
        }
        if (tree.children && tree.children.length > 0) {
            tree.children = tree.children.filter(child => this.filterTree(child, term));
            return tree.children && tree.children.length > 0;
        }
        return false;
    }

    display(isShowing) {
        if (isShowing) {
            this.elem.classList.remove("is-hidden")
        } else {
            this.elem.classList.add("is-hidden")
        }
    }

    clear() {
        this.elem.innerHTML = "";
    }

    checkNode(node) {
        if (!this.isCorrectVersion(node)) {
            var split = node.name.split(":")
            var dependency = split[0] + ":" + split[1]
            var versions = conflicts[dependency] ? conflicts[dependency] : new Set();
            versions.add(split[2]);
            versions.add(node.pomName.split(":")[2])
            conflicts[dependency] = versions;
        }
        if (node.children && node.children.length > 0) {
            node.children.forEach(child => {
                this.checkNode(child);
            })
        }

    }

    isCorrectVersion(node) {
        return !node.pomName || node.pomName == node.name
    }


    width = window.outerWidth
    marginTop = 10;
    marginRight = 10;
    marginBottom = 10;
    marginLeft = 5;
    root = null
    tree = null
    svg = null
    gLink= null
    gNode = null


    drawTree(data) {
        // Rows are separated by dx pixels, columns by dy pixels. These names can be counter-intuitive
        // (dx is a height, and dy a width). This because the tree must be viewed with the root at the
        // “bottom”, in the data domain. The width of a column is based on the tree’s height.
        this.root = d3.hierarchy(data);
        const dx = 20;
        //const dy = 200;
        const dy = (this.width - this.marginRight - this.marginLeft) / (1 + this.root.height); // todo calculate

        // Define the tree layout and the shape for links.
        this.tree = d3.tree().nodeSize([dx, dy]);
        this.diagonal = d3.linkHorizontal().x(d => d.y).y(d => d.x);

        // Create the SVG container, a layer for the links and a layer for the nodes.
        this.svg = d3.select(this.elem)
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
            d.id = i;
            d._children = d.children;
            // if (d.depth && d.depth > 2) d.children = null;
            // console.log(d.depth && d.data.name.length !== 6);
            //if (d.depth && d.data.name.length !== 3) d.children = null;
        });

        this.update(null, this.root);

    };

    update(event, source) {
        const duration = event?.altKey ? 2500 : 250; // hold the alt key to slow down the transition
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
                event.children = event.children ? null : event._children;
                this.update(event, d);
            });

        nodeEnter.append("circle")
            .attr("r", 4)
            .attr("fill", d => d._children ? "#5A5" : "#A55")
            .attr("stroke-width", 8);

        nodeEnter.append("text")
            .attr("dy", "0.31em")
            .attr("x", d => 8)
            .classed("node-conflict", d => {
                var data = d.data
                var latest = latestVersions[data.name]
                if (latest == null) return false;
                var version = data.versions ? (data.versions.actual ? data.versions.actual : data.versions.resolved) : "";
                return !latest || latest!== version;
            })
            .attr("text-anchor", d =>  /*d._children ? "end" : */ "start")
            .text(d => {
                var data = d.data;
                var latest = latestVersions[data.name]
                var version = data.versions ? (data.versions.actual ? data.versions.actual : data.versions.resolved) : "";
                var text = data.pomName ? data.pomName : data.name;
                return text + ":" + version;
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