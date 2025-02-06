<script>
import {inject, ref} from "vue";
import TreeRow from "@/components/TreeRow.vue";
import SvgTree from "@/components/svg/SvgTree.vue";

export default {
  components: {TreeRow, SvgTree},
  props: {
    configuration: {
      type: Object,
      required: true
    }
  },
  setup() {
    const cache = inject("cache");
    return {
      cache
    }
  },
  watch: {
    configuration: function (newVal) {
      this.loadConfiguration()
    },
    filterInput: debounce(function () {
      if (this.isInputFunctionRunning) {
        return;
      }
      this.isInputFunctionRunning = true;
      this.filter = this.filterInput;
      this.filterDependencies();
      this.isInputFunctionRunning = false;
    }, 1000)

  },
  data() {
    return {
      treeList: null,
      filteredList: null,
      filterInput: "",
      filter: "",
      isInputFunctionRunning: false,
    };
  },

  mounted() {
    this.loadConfiguration();
  },

  methods: {
    ref,
    async loadConfiguration() {
      const response = await fetch(this.configuration.name + "/top-dependencies.json")
      const data = await response.json();
      this.treeList = this.cache.getDependencies(this.configuration, data).map(entry => {
        entry.id = crypto.randomUUID();
        return entry;
      })
      this.filterDependencies();
    },

    filterDependencies() {
      let copy = JSON.parse(JSON.stringify(this.treeList));

      if (!this.filter || this.filter.trim().length === 0) {
        this.filteredList = copy;
        return;
      }

      this.filteredList = copy.filter(item => {
        return this.filterTree(item, this.filter);
      });
    },

    filterTree(tree, term) {
      if (tree.name.includes(term)) {
        return true
      }
      if (tree.children && tree.children.length > 0) {
        tree.children = tree.children.filter(child => {
          return this.filterTree(child, term)
        });
        if (tree.children.length > 0) {
        }
        return tree.children.length > 0;
      }
      return false;
    }
  }
}

function debounce(fn, delay) {
  let timeoutID = null;
  return function () {
    clearTimeout(timeoutID)
    const args = arguments;
    const that = this;
    timeoutID = setTimeout(function () {
      fn.apply(that, args)
    }, delay)
  }
}
</script>

<template>
  <nav class="panel m-4 is-flex is-flex-direction-column">
    <div class="panel-heading">
      <div class="columns is-gap-4">
        <div v-if="configuration!=null" class="column is-narrow" style="align-self: center;">
          {{ configuration.name }}
        </div>
        <p class="control column is-6">
          <input class="input" type="text" placeholder="Search" name="dependency-filter" v-model="filterInput"/>
        </p>
      </div>
    </div>

    <div class="m-4 p-4">
      <SvgTree v-if="filteredList!=null" v-for="tree in filteredList" :key="tree.name" :tree="tree" />
    </div>
  </nav>
</template>