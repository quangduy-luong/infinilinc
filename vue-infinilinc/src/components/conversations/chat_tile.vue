<template>
  <v-layout row wrap>
    <v-list-tile avatar @click="onChat">
      <v-list-tile-avatar>
        <v-icon large>{{ icon }}</v-icon>
      </v-list-tile-avatar>
      <v-list-tile-content>
        <v-list-tile-title>{{ names }}</v-list-tile-title>
        <v-list-tile-sub-title>{{ lastMessage }}</v-list-tile-sub-title>
      </v-list-tile-content>
    </v-list-tile>
    <v-divider inset></v-divider>
  </v-layout>
</template>

<script>
  import * as firebase from 'firebase'
  export default {
    data () {
      return {
        icon: 'add',
        names: '',
        interval1: {},
        interval2: {},
        lastMessage: 'No messages found.',
        ref: null
      }
    },
    methods: {
      newMessage (snapshot) {
        this.lastMessage = snapshot.val().text
        if (this.lastMessage.length > 30) {
          this.lastMessage = this.lastMessage.substring(0, 30) + '...'
        }
      },
      onChat () {
        this.$store.commit('setCurrentChat', this.chat.key)
        this.$router.push('/chat')
      }
    },
    props: ['chat', 'users', 'usernames'],
    /* eslint-disable */
    mounted () {
      this.ref = firebase.database().ref('chats').child(this.chat.key).child('messages').limitToLast(1)
      this.ref.on('child_added', this.newMessage)
      var self = this
      self.interval1 = setInterval (function () {
        if (self.chat.users !== undefined && self.chat.users.length === 1) {
          self.$data.icon = 'person'
        } else {
          self.$data.icon = 'people'
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