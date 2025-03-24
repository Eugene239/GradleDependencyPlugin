<script>
import {useRoute} from "vue-router";
import {Navigator} from "@/navigation/Navigator.js";
import router from "@/router.js";

export default {

  data() {
    return {
      configurationName: useRoute().params.configuration,
      dependency: useRoute().params.dependency,
    }
  },
  watch: {
    dependency: function (newVal) {
      console.log("DependencyScreen newVal", newVal);
    },
  },
  methods: {
    useRoute,
    navigateToConfiguration() {
      Navigator.navigateToConfiguration(this.configurationName);
    },
    navigateToUsage() {
      Navigator.navigateToUsage(this.configurationName, router.currentRoute.value.params.dependency);
    },
    navigateToGraph() {
      Navigator.navigateToGraph(this.configurationName, router.currentRoute.value.params.dependency);
    },
    navigateToDependencies() {
      Navigator.navigateToSubDependencies(this.configurationName, router.currentRoute.value.params.dependency);
    }
  },
}
</script>

<template>
  <div class="dependency-screen">
    <nav class="breadcrumb mt-4 ml-4" aria-label="breadcrumbs">
      <ul>
        <li>
          <router-link to="/"><span class="material-symbols-outlined">home</span></router-link>
        </li>
        <li><a v-on:click="navigateToConfiguration" aria-current="page">{{ configurationName }}</a></li>
        <li class="is-active"><a href="#" aria-current="page">{{ useRoute().params.dependency }}</a></li>
      </ul>
    </nav>
    <div class="container pt-4">
      <div class="pt-4">
        <div class="tabs  is-medium is-boxed">
          <ul>
            <li :class="{'is-active': $route.path.endsWith('usage')}" v-on:click="navigateToUsage()">
              <a> <span class="material-symbols-outlined pr-2">supervisor_account</span> Used in</a>
            </li>
            <li :class="{'is-active': $route.path.endsWith('dependencies')}" v-on:click="navigateToDependencies()">
              <a> <span class="material-symbols-outlined pr-2">user_attributes</span> Dependencies</a>
            </li>
            <li :class="{'is-active': $route.path.endsWith('graph')}" v-on:click="navigateToGraph()">
              <a><span class="material-symbols-outlined pr-2">flowchart</span> Graph</a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <RouterView :configuration="configurationName" :dependency="useRoute().params.dependency"/>
  </div>

</template>