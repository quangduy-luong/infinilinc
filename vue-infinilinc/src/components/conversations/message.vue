<template>
  <v-container fluid py-1 px-3>
    <v-card :color="color">
      <v-card-title><span :class="formatting">{{ message.username }}: {{ message.text }}</span></v-card-title>
    </v-card>
  </v-container>
</template>

<script>
  export default {
    props: ['message'],
    computed: {
      color () {
        return this.message.user === this.$store.getters.user.id ? 'blue' : 'gray'
      },
      formatting () {
        return this.message.user === this.$store.getters.user.id ? 'white--text' : 'black--text'
      }
    },
    methods: {
    }
  }
</script>