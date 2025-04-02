<script>
import Navbar from './components/Navbar.vue'
import {provide} from "vue";
import {flatDependenciesCache} from "./cache/flatDependenciesCache.js";
import {configurationsCache} from "@/cache/configurationsCache.js";

export default {
  components: {
    Navbar,
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
    }
  }
}
</script>

<template>
  <Navbar/>
  <RouterView />
</template>
