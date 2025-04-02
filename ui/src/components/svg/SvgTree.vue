<script>
import SvgLine from "@/components/svg/SvgLine.vue";
import SvgCircle from "@/components/svg/SvgCircle.vue";
import SvgText from "@/components/svg/SvgText.vue";
import SvgDependency from "@/components/svg/SvgDependency.vue";
import {DEPENDENCY} from "@/colors/Svg.js";

export default {
  computed: {
    DEPENDENCY() {
      return DEPENDENCY
    }
  },
  components: {
    SvgDependency,
    SvgText,
    SvgCircle,
    SvgLine,
  },
  props: {
    tree: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      // list of SvgDependencies
      treeData: [],
      // list of SvgLines
      linesData: [],
      // node list with hidden children
      hidden: [],
      betweenSize: 40,
      height: null,
      width: window.screen.width,
      viewBox: "0 0 " + window.screen.width + " 0"
    };
  },
  mounted() {
    this.drawTree();
  },
  methods: {
    drawTree: function () {
      this.height = this.calculateHeight(this.tree);
      this.viewBox = "0 0 " + this.width + " " + this.height;
      this.treeData = [];
      this.linesData = [];

      this.positionNodes(this.tree, 6, this.height / 2, this.height, 0);
    },

    positionNodes(node, x, y, parentStep, level = 0) {
      node.x = x.toString();
      node.y = y.toString();
      node.id = crypto.randomUUID();

      this.treeData.push(node);

      if (!node.children || node.children.length === 0 || this.hidden.includes(node)) {
        return;
      }
      const totalLeaves = this.countLeaves(node);

      const offsets = []
      node.children.forEach((child, index) => {
        const childLeaves = this.countLeaves(child);
        const multiple = childLeaves / totalLeaves;
        const childHeight = parentStep * multiple
        const childX = x + this.calculateNameLength(node.name)
        const childY = y - parentStep / 2 + offsets.reduce((sum, a) => sum + a, 0) + (multiple * parentStep) / 2
        this.linesData.push({
          id: crypto.randomUUID(),
          x1: x.toString(),
          y1: y.toString(),
          x2: childX.toString(),
          y2: childY.toString(),
        })
        this.positionNodes(child, childX, childY, childHeight, level + 1);
        offsets.push(childHeight);
      })
    },

    calculateNameLength: function (name) {
      return name.length * 7;
    },
    countLeaves: function (node) {
      if (!node.children || node.children.length === 0 || this.hidden.includes(node)) {
        return 1;
      }
      return node.children.reduce((sum, child) => sum + this.countLeaves(child), 0);
    },
    calculateHeight: function (node) {
      let leaves = this.countLeaves(node)
      return (leaves + 2) * this.betweenSize;
    },
    onClick(node) {
      if (this.hidden.includes(node)) {
        this.hidden.splice(this.hidden.indexOf(node), 1);
      } else {
        this.hidden.push(node);
      }
      this.drawTree();
    }
  }
}
</script>

<template>
    <svg v-if="width!=null" :width="width" :height="height" :viewBox="viewBox" class="media tree-svg">
      <SvgLine v-for="line in linesData" :y2="line.y2" :x2="line.x2" :y1="line.y1" :x1="line.x1"
               :key="line.id"
      />
      <SvgDependency
          v-for="dep in treeData" :x="dep.x" :y="dep.y"
          :key="dep.id"
          :name="dep.name"
          :color="dep.children? DEPENDENCY.HAVE_CHILD : DEPENDENCY.NO_CHILD"
          :class="dep.children? 'is-clickable' : ''"
          @click="dep.children? this.onClick(dep): {}"
      />
    </svg>
</template>

<style scoped>
.tree-svg {
  width: 100%;
}
</style>