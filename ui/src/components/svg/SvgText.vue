<script>
export default {
  props: {
    text: {
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
    }
  }
}
</script>

<template>
  <text :x="x" :y="y" text-anchor="start" class="svg-stroke">{{ text }}</text>
  <text :x="x" :y="y" text-anchor="start" class="svg-text">{{ text }}</text>
</template>

<style scoped>
.svg-stroke {
  font-size: 12px;
  stroke: white;
  stroke-width: 4px;
  stroke-linejoin: round;
}
.svg-text {
  font-size: 12px;
}
</style>