<script>
export default {
  props: {
    x1: {
      type: String,
      required: true
    },
    y1: {
      type: String,
      required: true
    },
    x2: {
      type: String,
      required: true
    },
    y2: {
      type: String,
      required: true
    }
  },
  data: function() {
    return {
      line: "M " + this.x1 + "," + this.y1 + " C" + this.x1 + "," + this.y1 + " " + this.x1 + "," + this.y2 + " " + this.x2 + "," + this.y2
    }
  },
}
</script>

<template>
  <path :d="line" stroke="gray" fill="transparent"></path>

</template>
