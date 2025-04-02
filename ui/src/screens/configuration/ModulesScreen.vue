<script>
import {Navigator} from "@/navigation/Navigator.js";
import {Api} from "@/api/Api.js";

export default {
  name: "ModulesScreen",
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
      base: null,
    }
  },
  mounted() {
    console.log("ModulesScreen component mounted.", this.configuration);
    this.fetchData()
  },

  watch: {
    filterInput(val) {
      this.data = this.base.filter((item) => item.name.toLowerCase().includes(val.toLowerCase()));
    }
  },
  methods: {
    async fetchData() {
      this.response = await Api.topDependencies(this.configuration);
      this.fillData();
    },
    fillData() {
      if (!this.response) return;
      this.data = this.response
          .filter((item) => {
            let version = item.split(":")[2];
            return version === "unspecified"
          })
          .map(item => {
            return {
              name: item.split(":")[1],
              fullName: item
            }
          });
      this.base = JSON.parse(JSON.stringify(this.data));
    },
    navigateToDependency(item) {
      Navigator.navigateToDependency(this.configuration, this.getLibKey(item.fullName));
    },
    getLibKey(item) {
      return item.split(":", 2).join(":");
    }
  }
}
</script>

<template>
  <div class="modules-screen container mt-5">
    <p class="control mb-5 box has-background-success">
      <input class="input" type="text" placeholder="Search" v-model="filterInput"/>
    </p>
    <table class="table is-striped is-fullwidth">
      <thead>
      <tr>
        <th class="is-flex-grow-1 ">Name</th>
        <th class="has-text-right mr-6 pr-5" >Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in data">
        <td>
          {{ item.name }}
        </td>
        <td class="has-text-right mr-6">
          <div class="button" v-on:click="navigateToDependency(item)">Details</div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>