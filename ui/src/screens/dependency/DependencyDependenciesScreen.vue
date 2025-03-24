<script>
import {inject} from "vue";
import {Navigator} from "@/navigation/Navigator.js";

export default {
  name: "DependencyDependenciesScreen.vue",
  props: {
    configuration: {
      type: String,
      required: true,
    },
    dependency: {
      type: String,
      required: true,
    }
  },
  data() {
    return {
      data: {

      }
    }
  },
  setup() {
    const cache = inject("flatDependenciesCache");
    return {
      cache
    }
  },
  mounted() {
    console.log("DependencyDependenciesScreen mounted", this.configuration, this.dependency);
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.data = await this.cache.getSubDependencies(this.dependency);
      console.log(this.data);
    },
    navigateToDependency(item) {
      let dependency = item.split(":",2).join(":");
      Navigator.navigateToDependency(this.configuration, dependency);
    }
  }
}
</script>

<template>
  <div class="dependency-dependencies-screen container mt-5">
    <div v-for="(item, version) in data" :key="version" class="message">
      <div class="message-header is-flex is-align-items-center">
        <div class="title-3  message-title ">{{ dependency }}</div>
        <div class="tags is-clickable mx-3 m-0 is-flex-grow-1">
          <span class="tag is-primary is-light">{{ version }}</span>
        </div>
      </div>
      <div class="message-body">
        <table class="table is-striped is-fullwidth">
          <tbody>
          <tr v-for="library in item">
            <td class="is-flex is-align-items-center">
              <div class="is-flex-grow-1">{{ library }}</div>
              <div class="button button cell" v-on:click="navigateToDependency(library)">Details</div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>