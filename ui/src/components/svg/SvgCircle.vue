<script>
export default {
  props: {
    radius: {
      type: String,
      required: false,
      default: "6",
    },
    x: {
      type: String,
      required: true,
    },
    y: {
      type: String,
      required: true,
    },
    color: {
      type: String,
      required: false,
      default: 'green',
    }
  }
}

</script>

<template>
  <circle :r="radius" :cx="x" :cy="y" :fill="color" />
</template>