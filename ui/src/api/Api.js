export const Api = {

    async configurations() {
        return await ((await fetch("configurations.json")).json())
    },

    async flatDependencies() {
        return await ((await fetch("flat-dependencies.json")).json())
    },

    async topDependencies(configuration) {
        return await (await fetch(`${configuration}/top-dependencies.json`)).json();
    },

    async conflicts(configuration) {
        return await (await fetch(`${configuration}/conflicts.json`)).json();
    }
}