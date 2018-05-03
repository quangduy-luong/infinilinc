<template>
  <div>
    <v-list-tile avatar @click="onChat">
      <v-list-tile-avatar>
        <img :src="image">
      </v-list-tile-avatar>
      <v-list-tile-content>
        <v-list-tile-title>{{ names }}</v-list-tile-title>
        <v-list-tile-sub-title>{{ lastMessage }}</v-list-tile-sub-title>
      </v-list-tile-content>
    </v-list-tile>
    <v-divider inset></v-divider>
  </div>
</template>

<script>
  import * as firebase from 'firebase'
  export default {
    data () {
      return {
        image: '',
        names: '',
        interval1: {},
        interval2: {},
        lastTime: null,
        ref: null
      }
    },
    computed: {
      lastMessage () {
        if (this.message === undefined || this.message.length < 1) {
          return 'No messages found.'
        } else {
          return this.message
        }
      }
    },
    methods: {
      onChat () {
        this.$store.commit('setCurrentChat', this.chat.key)
        this.$router.push('/chat')
      }
    },
    props: ['chat', 'users', 'usernames', 'message'],
    /* eslint-disable */
    mounted () {
      var self = this
      self.interval1 = setInterval (function () {
        if (self.chat.users !== undefined && self.chat.users.length === 1) {
          firebase.database().ref('users/' + self.chat.users[0].id).once('value', (snapshot) => {
            self.image = snapshot.val().imageUrl
          })
        }
      }, 100)
      self.interval2 = setInterval (function () {
        if (self.chat.usernames !== undefined && self.chat.usernames.length > 1) {
          self.$data.names = self.chat.usernames
        }
      }, 100)
    }
  }
</script>