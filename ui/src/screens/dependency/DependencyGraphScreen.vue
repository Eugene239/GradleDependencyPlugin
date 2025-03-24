<script>
import {inject} from "vue";
import SvgTree from "@/components/svg/SvgTree.vue";
import {Api} from "@/api/Api.js";

export default {
  name: "DependencyGraphScreen",
  components: {SvgTree},
  props: {
    configuration: {
      type: String,
      required: true,
    },
    dependency: {
      type: String,
      required: true,
    },
  },
  watch: {
    dependency: function (newValue) {
      this.fetchData();
    }
  },
  data() {
    return {
      treeList: null,
    }
  },
  created() {
    this.fetchData();
  },
  mounted() {
  },
  setup() {
    const cache = inject("flatDependenciesCache");
    return {
      cache
    }
  },
  methods: {
   async fetchData() {
      let topDependencies = await Api.topDependencies(this.configuration);
      this.treeList = await this.cache.getGraphData(topDependencies, this.dependency);
      console.log("treeList", this.treeList);
    }
  }
}

</script>

<template>
  <div class="dependency-graph-screen mt-4 pt-4 ml-3">
    <SvgTree v-if="treeList!=null" v-for="tree in treeList" :key="tree.name" :tree="tree" />
  </div>
</template>