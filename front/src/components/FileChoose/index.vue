<template>
  <div class="file-choose">
    <Input class="file-choose-input"
      :value="value" @input="setPath($event)"
      />
  </div>
</template>

<script>

export default {
  props: {
    value: {
      type: String
    },
    mode: {
      type: String,
      default: 'dir'
    },
    disabled: {
      type: Boolean
    }
  },
  data() {
    return {
      chooserWait: false
    }
  },
  methods: {
    setPath(event){
      this.$emit('input',event)
    }
  }
}
</script>

<style scoped>
.file-choose {
  display: inline-block;
  width: 100%;
}
.file-choose-input {
  width: 85%;
  padding-right: 3px;
}
.file-choose-button {
  width: 15%;
}
</style>
