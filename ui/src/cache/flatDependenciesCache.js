import {reactive} from 'vue';
import {Api} from "@/api/Api.js";
import {SubmoduleGraph} from "@/graph/SubmoduleGraph.js";
import {DependencyGraph} from "@/graph/DependencyGraph.js";

export const flatDependenciesCache = reactive({
    data: {},
    configuration: null,

    async loadInitialCache(configuration) {
        if (this.configuration !== configuration || Object.keys(this.data).length === 0) {
            this.configuration = configuration;
            this.data = await Api.flatDependencies(configuration);
        }
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

    async getGraphData(configuration, topDependencies, dependencyName) {
        await this.loadInitialCache(configuration)
        let builder = this.isSubmodule(dependencyName)
            ? new SubmoduleGraph(this.data, topDependencies, dependencyName, this)
            : new DependencyGraph(this.data, topDependencies, dependencyName, this);
        return await builder.getGraphData()
    },


    addAllChildren(set, node) {
        set.add(node.name);
        let children = node.children ? node.children : [];
        children.forEach((item) => {
            this.addAllChildren(set, item);
        });
    },


    isSubmodule(dependencyName) {
        if (dependencyName.split(":").length === 3) {
            let version = dependencyName.split(":")[2]
            if (version === "unspecified") {
                return true
            }
        }
        let dependencies = Object.entries(this.data).filter(([key, value]) => {
            let name = key.split(":", 2).join(":")
            if (name === dependencyName) {
                let version = key.split(":")[2]
                if (version === "unspecified") {
                    return true
                }
            }
            return false;
        });
        return dependencies.length > 0;
    },

    hasModules() {
        let dependencies = Object.entries(this.data).filter(([key, value]) => {
            let version = key.split(":")[2]
            return version === "unspecified";
        });
        return dependencies.length > 0;
    }

})