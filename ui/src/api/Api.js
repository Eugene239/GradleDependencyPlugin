export const Api = {

    async configurations() {
        return await ((await fetch("configurations.json")).json())
    },

    async flatDependencies(configuration) {
        return await ((await fetch(`${configuration}/flat-dependencies.json`)).json())
    },

    async topDependencies(configuration) {
        return await (await fetch(`${configuration}/top-dependencies.json`)).json();
    },

    async conflicts(configuration) {
        return await (await fetch(`${configuration}/conflicts.json`)).json();
    },

    async latestVersions() {
        return await ((await fetch(`latest-versions.json`)).json())
    },

    async libSizes() {
        return await ((await fetch(`lib-sizes.json`)).json())
    },
}