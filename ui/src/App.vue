<script>
import Navbar from './components/Navbar.vue'
import ConfigurationPanel from './components/ConfigurationPanel.vue'
import {provide} from "vue";
import {flatDependenciesCache} from "./cache/flatDependenciesCache.js";
import {configurationsCache} from "@/cache/configurationsCache.js";

export default {
  components: {
    Navbar,
    ConfigurationPanel,
  },
  data() {
    return {}
  },

  setup() {
    provide("configurationsCache", configurationsCache);
    provide("flatDependenciesCache", flatDependenciesCache);
    return {}
  },
  mounted() {
    this.loadCaches()
  },
  methods: {
    async loadCaches() {
       await configurationsCache.loadInitialCache();
       await flatDependenciesCache.loadInitialCache();
    }
  }
}
</script>

<template>
  <Navbar/>
  <RouterView />
</template>
