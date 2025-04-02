export class DependencyGraph {

    constructor(
        flatDependencies,
        topDependencies,
        dependencyName,
        cache
    ) {
        this.data = flatDependencies;
        this.topDependencies = topDependencies;
        this.dependencyName = dependencyName;
        this.cache = cache;
        this.dictionary = {}
    }


    async getGraphData() {
        let start = Date.now();
        console.log("DependencyGraph", "getGraphData", this.dependencyName);

        let indices = await Promise.all(this.topDependencies.map((dep) => this.hasSelectedDependencyAsync(dep)));
        let filtered = this.topDependencies.filter((_, index) => indices[index]);
        console.log("filtered", this.topDependencies.length, filtered.length);
        let result = filtered
            .filter(lib => this._hasSelectedDependency(lib))
            .map(dep => {
                let node = this.makeGraphNode(dep, 100)
                console.log(node);
                return node;
            })
        console.log("end", Date.now() - start);
        return result
    }

    makeGraphNode(library, limit) {
        let childrenDependencies = null;
        if (limit > 0) {
            childrenDependencies = (this.data[library] || [])
                .filter(item => !this.cache.isSubmodule(item))
                .filter(lib => this._hasSelectedDependency(lib))
                .map(lib => this.makeGraphNode(lib, limit - 1))

        } else {
            childrenDependencies = null;
        }
        if (childrenDependencies && childrenDependencies.length === 0) {
            childrenDependencies = null;
        }
        return {
            name: library,
            children: childrenDependencies
        };
    }

    async hasSelectedDependencyAsync(lib, selectedLibrary, parent) {
        if (typeof lib !== "string") {
            return false;
        }
        if (this.dictionary[lib]) {
            return this.dictionary[lib];
        }
        if (lib.includes(selectedLibrary)) {
            return true
        }
        let dependencies = this.data[lib] || []
        let result = dependencies
            .filter(item => !this.cache.isSubmodule(item))
            .filter(child => this._hasSelectedDependency(child, selectedLibrary, parent));
        this.dictionary[lib] = result.length > 0;
        return result.length > 0;
    }

    _hasSelectedDependency(lib) {
        if (this.dictionary[lib]) {
            return this.dictionary[lib];
        }
        if (typeof lib !== "string") {
            return false;
        }
        if (lib.includes(this.dependencyName)) {
            return true
        }
        let dependencies = this.data[lib] || []
        let result = dependencies
            .filter(item => !this.cache.isSubmodule(item))
            .filter(child => this._hasSelectedDependency(child, this.dependencyName));
        this.dictionary[lib] = result.length > 0;
        return result.length > 0;
    }


}