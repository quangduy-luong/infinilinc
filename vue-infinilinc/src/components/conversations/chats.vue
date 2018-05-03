<template>
  <v-container fluid py-0 px-0>
    <v-layout row wrap>
      <v-flex xs12 >
        <v-container v-if="chats === null || chats === undefined || chats.length < 1">
          <span>You don't have any active chats!</span>
        </v-container>
        <v-container v-else fluid py-0 px-0>
          <v-list two-line>
            <v-subheader >Recent</v-subheader>
            <chat-tile v-for="(chat, i) in chats" :key="i" :chat="chat" :users="chat.users" :usernames="chat.usernames"></chat-tile>
          </v-list>
        </v-container>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
  import * as firebase from 'firebase'
  import chatTile from './chat_tile'
  export default {
    data () {
      return {
        usersChatRef: null,
        chats: []
      }
    },
    components: {
      'chat-tile': chatTile
    },
    computed: {
      authenticated () {
        return this.$store.getters.user !== null
      }
    },
    methods: {
      onChildAdded (snapshot) {
        var chat = { key: snapshot.key }
        firebase.database().ref('chats').child(chat.key).child('users').once('value', (snapshot) => {
          chat.users = []
          for (var i = 0; i < snapshot.val().length; i++) {
            let user = snapshot.val()[i]
            let links = this.$store.getters.links
            let name = links[user] === null || links[user] === undefined ? 'User' : links[user].username
            if (user !== this.$store.getters.user.id) {
              chat.users.push({ id: user, username: name })
            }
          }
          chat.usernames = ''
          for (i = 0; i < chat.users.length; i++) {
            chat.usernames += chat.users[i].username
            if (i < chat.users.length - 1) {
              chat.usernames += ', '
            }
          }
        })
        this.chats.push(chat)
        console.log(chat)
      }
    },
    mounted () {
      this.usersChatRef = firebase.database().ref('users/' + this.$store.getters.user.id + '/chats')
      this.usersChatRef.on('child_added', this.onChildAdded)
    }
  }
</script>