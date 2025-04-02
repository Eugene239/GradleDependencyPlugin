import {reactive} from "vue";
import {Api} from "@/api/Api.js";

export const configurationsCache = reactive({
    data: {},
    configurations: [],
    latestVersions: {},
    libSizes: {},
    isLibSizesEnabled: false,
    isLatestVersionsEnabled: false,

    async loadInitialCache() {
        this.data = await Api.configurations()
        this.configurations = this.data["configurations"]
        this.isLibSizesEnabled = this.data.startupFlags && this.data.startupFlags.fetchLibSize === true
        this.isLatestVersionsEnabled = this.data.startupFlags && this.data.startupFlags.fetchVersions === true
        if (this.isLatestVersionsEnabled) {
            this.latestVersions = await Api.latestVersions();
        }
        if (this.isLibSizesEnabled) {
            this.libSizes = await Api.libSizes();
        }
    },

    latestVersion(key) {
        if (this.isLatestVersionsEnabled) {
            return this.latestVersions[key]
        }
    },
    libSize(library) {
        if (this.isLibSizesEnabled) {
            const size = this.libSizes[library];
            if (size) {
                return (size / 1024).toFixed(2)
            }
        }
    }
})