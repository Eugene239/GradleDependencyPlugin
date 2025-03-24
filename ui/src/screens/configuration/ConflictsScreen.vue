<script>
import router from "@/router.js";
import {Navigator} from "@/navigation/Navigator.js";
import {Api} from "@/api/Api.js";

export default {
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
  name: "ConflictsScreen",
  mounted() {
    console.log("Component mounted.", this.configuration);
    this.fetchData()
  },
  watch: {
    filterInput(val) {
      this.data = Object.fromEntries(Object.entries(this.response)
          .filter(([key, value]) => key.toLowerCase().includes(val.toLowerCase()))
      )
    }
  },
  methods: {
    async fetchData() {
      this.isLoading = true;
      try {
        this.response = await Api.conflicts(this.configuration);
        this.data = JSON.parse(JSON.stringify(this.response));
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    getConflicts(item) {
      return item.set
          .filter((version) => {return  item.resolved !== version})
    },
    navigateToGraph(name) {
      Navigator.navigateToDependency(this.configuration, name);
    }
  },
}
</script>

<template>
  <div class="conflicts-screen container mt-5">
    <p class="control mb-5 box has-background-success">
      <input class="input" type="text" placeholder="Search" v-model="filterInput"/>
    </p>

    <div v-for="(item,name) in data" :key="name" class="message">
      <div class="message-header is-flex is-align-items-center">
        <div class="title-3  message-title ">{{ name }}</div>
        <div class="tags has-addons is-clickable mx-3 m-0 is-flex-grow-1">
          <span class="tag is-primary">{{ item.resolved }}</span>
          <span class="tag is-light">Resolved</span>
        </div>
        <div class="is-align-items-center">
          <div class="button is-success is-light" v-on:click="navigateToGraph(name)">
            Details
          </div>
        </div>
      </div>
      <div class="message-body">
        <div class="columns is-multiline">
          <div v-for="version in getConflicts(item)" class="column is-narrow is-clickable">
            <span class="tag is-white is-medium">{{ version }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
