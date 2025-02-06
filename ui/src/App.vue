<script>
import Navbar from './components/Navbar.vue'
import ConfigurationPanel from './components/ConfigurationPanel.vue'
import {provide} from "vue";
import {flatDependenciesCache} from "./cache/flatDependenciesCache.js";

export default {
  components: {
    Navbar,
    ConfigurationPanel,
  },
  data() {
    return {
      configuration: null
    }
  },
  setup() {
    flatDependenciesCache.loadInitialCache();
    provide("cache", flatDependenciesCache);
    console.log("provided");
    return {}
  },

  methods: {
    onConfigurationSelected(configuration) {
      console.log(configuration)
      this.configuration = configuration;
    }
  }
}
</script>


<template>
  <Navbar @select="onConfigurationSelected"/>
  <ConfigurationPanel v-if="configuration !=null" :configuration="this.configuration"/>
</template>
