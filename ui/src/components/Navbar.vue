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
  methods: {
    navigateToGradle() {
      window.location.href = "https://plugins.gradle.org/plugin/io.github.eugene239.gradle.plugin.dependency";
    },
    navigateToGithub() {
      window.location.href = "https://github.com/Eugene239/GradleDependencyPlugin"
    }
  }
}
</script>

<template>
  <nav class="navbar is-success" role="navigation" aria-label="main navigation">
    <div class="navbar-brand is-clickable" v-on:click="navigateToGithub()">
      <img class="navbar-item" src="https://www.svgrepo.com/show/512317/github-142.svg" width="62px" height="72px"/>
    </div>
    <div class="navbar-brand is-clickable" v-on:click="navigateToGradle()">
      <img class="navbar-item" src="https://www.svgrepo.com/show/353831/gradle.svg" width="72px" height="72px"/>
    </div>

    <div v-if="config.data!=null" class="navbar-end">
      <div class="navbar-item">
        {{ config.data.version }}
      </div>
    </div>
  </nav>
</template>

<style scoped>

</style>