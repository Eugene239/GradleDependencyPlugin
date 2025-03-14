<script>
import {useRoute} from "vue-router";
import router from "@/router.js";
import {Navigator} from "@/navigation/Navigator.js";

export default {
  methods: {
    navigateToDependencies() {
      Navigator.navigateToDependencies(this.configurationName);
    },
    navigateToConflicts() {
      Navigator.navigateToConflicts(this.configurationName);
    }
  },
  data() {
    return {
      configurationName: useRoute().params.configuration
    }
  },
}
</script>

<template>
  <div class="configuration-screen">
    <nav class="breadcrumb mt-4 ml-4" aria-label="breadcrumbs">
      <ul>
        <li>
          <router-link to="/"><span class="material-symbols-outlined">home</span></router-link>
        </li>
        <li class="is-active"><a href="#" aria-current="page">{{ configurationName }}</a></li>
      </ul>
    </nav>
    <div class="container pt-4">
      <div class="pt-4">
        <div class="tabs  is-medium is-boxed">
          <ul>
            <li :class="{'is-active': $route.path.endsWith('conflicts')}" v-on:click="navigateToConflicts()">
              <a><span class="material-symbols-outlined pr-2">data_alert</span>Conflicts</a></li>
            <li :class="{'is-active': $route.path.endsWith('dependencies')}" v-on:click="navigateToDependencies()">
              <a><span class="material-symbols-outlined pr-2">list</span>Dependencies</a>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <RouterView :configuration="configurationName"/>
  </div>
</template>