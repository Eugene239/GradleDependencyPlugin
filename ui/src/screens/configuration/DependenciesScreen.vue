<script>
import {Navigator} from "@/navigation/Navigator.js";
import {Api} from "@/api/Api.js";

export default {
  name: "DependenciesScreen",
  props: {
    configuration: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      response: null,
      data: null,
      isLoading: false,
      filterInput: "",
    }
  },
  mounted() {
    console.log("DependenciesScreen mounted.", this.configuration);
    this.fetchData()
  },
  watch: {
    filterInput(val) {
      this.data = this.response.filter((item) => item.toLowerCase().includes(val.toLowerCase()));
    }
  },
  methods: {
    async fetchData() {
      this.isLoading = true;
      try {
        this.response = await Api.topDependencies(this.configuration);
        this.data = JSON.parse(JSON.stringify(this.response));
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    navigateToDependency(item) {
      let dependency = item.split(":",2).join(":");
      Navigator.navigateToDependency(this.configuration, dependency);
    }
  },
}
</script>

<template>
  <div class="dependencies-screen container mt-5">
    <p class="control mb-5 box has-background-success">
      <input class="input" type="text" placeholder="Search" v-model="filterInput"/>
    </p>
    <table class="table is-striped is-fullwidth">
      <tbody>
      <tr v-for="item in data">
        <td class="is-flex is-align-items-center">
          <div class="is-flex-grow-1">{{ item }}</div>
          <div class="button cell" v-on:click="navigateToDependency(item)">Details</div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>