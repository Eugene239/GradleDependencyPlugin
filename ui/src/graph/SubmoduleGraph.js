export class SubmoduleGraph {

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
        console.log("SubmoduleGraph", "getGraphData", this.dependencyName);
        let topParents = new Set(
            this._getTopParents(this.dependencyName)
                .filter(item => this.cache.isSubmodule(item))
        )

        return Array.from(topParents)
            .filter(lib => this.hasSelectedDependency(lib))
            .map(dep => {
                return this.makeGraphNode(dep, 100);
            });
    }

    makeGraphNode(library, limit) {
        let childrenDependencies = null;
        if (limit > 0) {
            childrenDependencies = (this.data[library] || [])
                .filter(item => this.cache.isSubmodule(item))
                .filter(lib => this.hasSelectedDependency(lib))
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
            .filter(child => this.hasSelectedDependency(child, selectedLibrary, parent));
        this.dictionary[lib] = result.length > 0;
        return result.length > 0;
    }

    hasSelectedDependency(lib) {
        if (typeof lib !== "string") {
            return false;
        }
        if (this.dictionary[lib]) {
            return this.dictionary[lib];
        }
        if (lib.includes(this.dependencyName)) {
            return true
        }
        let dependencies = this.data[lib] || []
        let result = dependencies
            .filter(child => this.hasSelectedDependency(child, this.dependencyName));
        this.dictionary[lib] = result.length > 0;
        return result.length > 0;
    }

    _getTopParents(libName) {
        let parents = this._getParents(libName);
        if (parents && parents.length === 0) {
            return [libName];
        }
        return [...new Set(parents.flatMap(p => this._getTopParents(p)))];
    }

    _getParents(libName) {
        let name = libName.split(":").length === 3 ? libName.split(":", 2).join(":") : libName;
        let array = Array();
        Object.entries(this.data).forEach(([key, value]) => {
            let librariesWithoutVersions = value.map((item) => item.split(":", 2).join(":"));
            let index = librariesWithoutVersions.indexOf(name)
            if (index !== -1) {
                array.push(key);
            }
        });
        return array;
    }

    _mapToLowerIfNeeded(libName) {
        let dependencies = this.data[libName] || []
        let hasDirectChild = dependencies.filter(item => item.includes(this.dependencyName)).length > 0;
        console.log("mapToLowerIfNeeded", libName, hasDirectChild);
        return libName;
    }
}