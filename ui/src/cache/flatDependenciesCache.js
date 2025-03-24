import {reactive} from 'vue';
import {Api} from "@/api/Api.js";

export const flatDependenciesCache = reactive({
    data: {},

    async loadInitialCache() {
        this.data = await Api.flatDependencies();
    },

    //@Deprecated("Used only for old graph")
    getDependencies(configuration, topDependencies) {
        return topDependencies.map(dep => {
            return this.makeNode(dep, 4);
        });
    },

    // @Deprecated("Used only for old graph")
    makeNode(library, limit) {
        let childrenDependencies = null;
        if (limit > 0) {
            childrenDependencies = this.data[library] ? this.data[library]?.map(lib => this.makeNode(lib, limit - 1)) : null
        } else {
            childrenDependencies = null;
        }
        if (childrenDependencies && childrenDependencies.length === 0) {
            childrenDependencies = null;
        }
        return {
            name: library,
            children: childrenDependencies
        }
    },

    // @Deprecated("Used only for old graph")
    loadChildren(libName) {
        return this.data[libName] ? this.data[libName]?.map(lib => this.makeNode(lib, 1)) : null;
    },

    async getUsage(libName) {
        let array = Array();
        Object.entries(this.data).forEach(([key, value]) => {
            let librariesWithoutVersions = value.map((item) => item.split(":", 2).join(":"));
            let index = librariesWithoutVersions.indexOf(libName)
            if (index !== -1) {
                array.push({
                    version: value[index],
                    parent: key
                });
            }
        });
        return array.reduce((acc, {version, parent}) => {
            const key = version.split(":")[2]
            if (!acc[key]) {
                acc[key] = new Set();
            }
            acc[key].add(parent);
            return acc;
        }, {});
    },

    async getSubDependencies(libName) {
        let result = {}
        Object.entries(this.data)
            .filter(([key, value]) => libName === key.split(":", 2).join(":"))
            .forEach(([key, value]) => {
                result[key.split(":")[2]] = value;
            });
        return result;
    },

    async getGraphData(topDependencies, dependencyName) {
        //console.log("getGraphData", dependencyName, topDependencies);
        let result = topDependencies
            .filter(lib => this.hasSelectedDependency(lib, dependencyName))
            .map(dep => this.makeGraphNode(dep, dependencyName, 100))
        //console.log("result: ", result);
        return result
    },

    makeGraphNode(library, selectedLibrary, limit) {
        let childrenDependencies = null;
        if (limit > 0) {
            childrenDependencies = this.data[library] ? this.data[library]
                    ?.map(lib => this.makeGraphNode(lib, selectedLibrary, limit - 1))
                    ?.filter(lib => this.hasSelectedDependency(lib.name, selectedLibrary))
                : null
        } else {
            childrenDependencies = null;
        }
        if (childrenDependencies && childrenDependencies.length === 0) {
            childrenDependencies = null;
        }
        return {
            name: library,
            children: childrenDependencies
        }
    },

    hasSelectedDependency(lib, selectedLibrary, parent) {
        if (typeof lib !== "string") {
            return false;
        }
        if (lib.includes(selectedLibrary)) {
            return true
        }
        let dependencies = this.data[lib] || []
        let result = dependencies
            .filter(child => this.hasSelectedDependency(child, selectedLibrary, parent));
            //.filter(async child => this.hasSelectedDependency(child, selectedLibrary, parent));
        return result.length > 0;
    }

})