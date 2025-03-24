<script>
import {inject} from "vue";

export default {
  data() {
    return {
      config: this.cache,
      selectedConfig: null
    };
  },

  setup() {
    const cache = inject("configurationsCache");
    return {
      cache
    }
  },
}
</script>

<template>
  <nav class="navbar is-success" role="navigation" aria-label="main navigation">
    <div class="navbar-brand">
      <img class="navbar-item" src="https://www.svgrepo.com/show/353831/gradle.svg" width="72px" height="72px"/>
    </div>

<!--    <div v-if="!loading" class="navbar-item has-dropdown is-hoverable">-->
<!--      <a class="navbar-link">-->
<!--        Graph-->
<!--      </a>-->
<!--      <div class="navbar-dropdown">-->
<!--        <a v-for="cfg in config.configurations" class="navbar-item" v-on:click="onSelect(cfg)" :class="{ 'is-active' : selectedConfig === cfg }">{{ cfg.name }}</a>-->
<!--      </div>-->
<!--    </div>-->
<!--    <a v-if="!loading && config.startupFlags.fetchVersions" class="navbar-item">-->
<!--      Latest versions-->
<!--    </a>-->
    <div v-if="config.data!=null" class="navbar-end">
      <div class="navbar-item">
        {{ config.data.version }}
      </div>
    </div>
  </nav>
</template>

<style scoped>

</style>