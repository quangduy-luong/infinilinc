<template>
  <v-container fluid py-1 px-3>
    <v-layout row wrap>
      <v-flex xs8 :offset-xs4="this.message.user === this.$store.getters.user.id">
        <v-card :color="color">
          <v-card-title><span :class="formatting">{{ username }}: {{ message.text }}</span></v-card-title>
        </v-card>
      </v-flex>
    </v-layout>
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
      },
      username () {
        if (this.$store.getters.links[this.message.user] === null || this.$store.getters.links[this.message.user] === undefined) {
          return 'Me'
        }
        return this.$store.getters.links[this.message.user].username
      }
    },
    methods: {
    }
  }
</script>