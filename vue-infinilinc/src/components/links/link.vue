<template>
  <v-container fluid py-0 px-0>
    <v-card color="gray lighten-3">
      <v-card-title><span class="title">{{ link.username }}</span></v-card-title>
      <v-card-actions>
        <v-container fluid py-0 px-0 grid-list-md>
          <v-layout row wrap>
            <v-flex xs8>
              <v-btn color="primary" block @click="onChat">Chat</v-btn>
            </v-flex>
            <v-flex xs4>
              <v-btn color="error">Remove</v-btn>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-actions>
    </v-card>
  </v-container>
</template>

<script>
  export default {
    props: ['link'],
    methods: {
      onChat () {
        this.$store.commit('setCurrentChat', this.link.chat)
        this.$router.push('/chat')
      }
    }
  }
</script>