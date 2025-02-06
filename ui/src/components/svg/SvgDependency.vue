<script>
import SvgCircle from "@/components/svg/SvgCircle.vue";
import SvgText from "@/components/svg/SvgText.vue";
import {DEPENDENCY} from "@/colors/Svg.js";

export default {
  components: {SvgText, SvgCircle},
  props: {
    name: {
      type: String,
      required: true
    },
    x: {
      type: String,
      required: true
    },
    y: {
      type: String,
      required: true
    },
    radius: {
      type: String,
      required: false,
      default: 6
    },
    color: {
      type: String,
      required: false,
      default: DEPENDENCY.NO_CHILD
    }
  },
  data() {
    return {
      textX: String(Number(this.x) + this.radius * 2),
      textY: String(Number(this.y) + this.radius / 3 * 2)
    };
  },
  methods: {}
}
</script>

<template>
  <g>
    <SvgCircle :x="x" :y="y" :radius="radius" :color="color"/>
    <SvgText :x="textX" :y="textY" :text="name"/>
  </g>
</template>