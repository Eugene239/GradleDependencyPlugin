import {reactive} from "vue";
import {Api} from "@/api/Api.js";

export const configurationsCache = reactive({
    data: {},
    configurations: [],

    async loadInitialCache() {
        this.data = await Api.configurations()
        this.configurations = this.data["configurations"]
        console.log("loaded", this.configurations)
    },
})