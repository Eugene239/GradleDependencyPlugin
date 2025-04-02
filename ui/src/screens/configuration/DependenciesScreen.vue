<script>
import {Navigator} from "@/navigation/Navigator.js";
import {Api} from "@/api/Api.js";
import {computed, inject} from "vue";

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
      base: null,
      data: null,
      isLoading: false,
      filterInput: "",
      sortedBy: null,
      sortDirection: "desc",
      isLibSizeEnabled: computed(() => this.cache.isLibSizesEnabled),
      isLatestVersionsEnabled: computed(() => this.cache.isLatestVersionsEnabled),
    }
  },
  mounted() {
    console.log("DependenciesScreen mounted.", this.configuration);
    this.fetchData()
  },
  watch: {
    filterInput(val) {
      this.data = this.base.filter((item) => item.name.toLowerCase().includes(val.toLowerCase()));
    }
  },
  setup() {
    const cache = inject("configurationsCache");
    return {
      cache
    }
  },
  methods: {
    async fetchData() {
      this.isLoading = true;
      try {
        this.response = await Api.topDependencies(this.configuration);
        this.fillData();
      } catch (error) {
        console.error(error);
      } finally {
        this.isLoading = false;
      }
    },
    fillData() {
      if (!this.response) return;
      this.data = this.response
          .filter((item) => {
            let version = item.split(":")[2];
            return version!=="unspecified"
          })
          .map(item => {
            return {
              name: item,
              latestVersion: this.latestVersion(item),
              libSize: this.libSize(item),
              versionConflict: this.latestVersion(item) && this.latestVersion(item) !== item.split(":")[2],
            }
          });
      this.base = JSON.parse(JSON.stringify(this.data));
    },

    navigateToDependency(item) {
      Navigator.navigateToDependency(this.configuration, this.getLibKey(item.name));
    },
    latestVersion(item) {
      return this.cache.latestVersion(this.getLibKey(item));
    },
    libSize(item) {
      return this.cache.libSize(item)
    },
    sortBy(field) {
      if (this.sortedBy !== field) {
        this.sortedBy = field;
        this.sortDirection = "desc";
        this.data = this.data
            .sort((a, b) => {
              if (field === "name") {
                return this.getLibKey(a.name).localeCompare(this.getLibKey(b.name), undefined, {sensitivity: 'base'});
              } else {
                return (a.libSize || 0) - (b.libSize || 0)
              }
            });
      } else {
        if (this.sortDirection === "asc") {
          this.sortedBy = null;
          this.sortDirection = "desc";
          this.data = this.data.sort();
        } else {
          this.sortDirection = "asc";
          this.data = this.data
              .sort((a, b) => {
                if (field === "name") {
                  return this.getLibKey(b.name).localeCompare(this.getLibKey(a.name), undefined, {sensitivity: 'base'})
                } else {
                  return (b.libSize || 0) - (a.libSize || 0)
                }
              });
        }
      }
    },
    getLibKey(item) {
      return item.split(":", 2).join(":");
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
      <thead>
      <tr>
        <th class="is-flex-grow-1 is-clickable" v-on:click="sortBy('name')">
          Name
          <span v-if="sortedBy==='name' && sortDirection==='asc'">&#8593;</span>
          <span v-if="sortedBy==='name' && sortDirection==='desc'">&#8595;</span>
        </th>
        <th class="is-clickable" v-if="isLibSizeEnabled" v-on:click="sortBy('libSize')">
          Lib size in KB
          <span v-if="sortedBy==='libSize' && sortDirection==='desc'">&#8593;</span>
          <span v-if="sortedBy==='libSize' && sortDirection==='asc'">&#8595;</span>
        </th>
        <th v-if="isLatestVersionsEnabled">Latest version</th>
        <th class="has-text-right mr-6 pr-5">Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in data">
        <td>
          {{ item.name }}
        </td>
        <td v-if="isLibSizeEnabled">
          <span class="tag is-white is-medium" v-if="item.libSize">{{ item.libSize }}</span>
        </td>
        <td v-if="isLatestVersionsEnabled">
          <span v-if="item.versionConflict && item.latestVersion" class="tag is-warning is-medium"> {{
              item.latestVersion
            }}</span>
          <span v-if="!item.versionConflict && item.latestVersion" class="tag is-white is-medium"> {{
              item.latestVersion
            }}</span>
        </td>
        <td class="has-text-right mr-6">
          <div class="button " v-on:click="navigateToDependency(item)">Details</div>
        </td>
      </tr>
      </tbody>
    </table>
  </div>
</template>