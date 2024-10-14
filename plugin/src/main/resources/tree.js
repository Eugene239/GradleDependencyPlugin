class Tree {

    children = []
    elem = null

    init(elem) {
        this.elem = elem
    }

    draw(tree) {
        tree.children.forEach(element => {
            this.drawElem(element);
        });
    }

    // draw(tree) {
    //     this.drawElem(tree.children[0]);
    // }

    drawElem(tree) {
        var graph = new Graph();
        this.children.push(graph);
        var svgContainer = document.createElement("div")
        svgContainer.setAttribute("id", tree.name);
        svgContainer.classList.add("media");
        this.elem.appendChild(svgContainer);
        graph.data  = tree;
        graph.init(svgContainer, tree.name);
        graph.drawTree(tree);
    }

    filter(term) {
        console.log("tree filter", term);
        this.children.forEach(child=> {
            var size = child.filter(term);
            child.display(term ? term.length == 0 || child.name.includes(term)  || size> 0 : true);
        })
    }

    clear() {
        if (this.elem != null) {
            this.children = [];
            this.elem.innerHTML = "";
        }
    }

}