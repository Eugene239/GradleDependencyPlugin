import {reactive} from 'vue';

export const flatDependenciesCache = reactive({
    data: {},

    async loadInitialCache() {
        const cache = await fetch("flat-dependencies.json");
        this.data = await cache.json();
    },

    getDependencies(configuration, topDependencies) {
        return topDependencies.map(dep => {
            return this.makeNode(dep, 4);
        });
    },

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

    loadChildren(libName) {
        return this.data[libName] ? this.data[libName]?.map(lib => this.makeNode(lib, 1)) : null;
    }
})