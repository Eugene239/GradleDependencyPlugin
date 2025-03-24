<script>
import {inject} from "vue";
import {Navigator} from "@/navigation/Navigator.js";

export default {
  data() {
    return {
    }
  },

  setup() {
    const cache = inject("configurationsCache");
    return {
      cache
    }
  },

  methods: {
    onConfigurationSelected(configuration) {
      Navigator.navigateToConfiguration(configuration.name);
    }
  }
}
</script>

<template>
  <section class="hero is-fullheight-with-navbar">
    <div class="hero-body is-flex is-flex-direction-column ">
      <h1 class="title">Select configuration</h1>
        <div class="columns is-gap-4 mt-4 is-multiline">
          <div v-for="conf in cache.configurations" class="column is-narrow is-clickable" v-on:click="onConfigurationSelected(conf)">
            <div class="box">
              <div class="content p-4">
              <p class="title is-5 ">{{ conf.name }}</p>
              <p class="subtitle is-6 mt-2">{{ conf.description }}</p>
              </div>
            </div>
          </div>
        </div>
    </div>
  </section>
</template>