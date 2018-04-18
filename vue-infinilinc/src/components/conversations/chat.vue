<template>
  <v-container fluid grid-list-sm fill-height>
    <v-layout row wrap>
      <v-flex xs12 style="height: calc(100vh - 13rem);">
        <div style="height: 100%; overflow-y: auto;" class="chat-box">
          <message v-for="(msg, i) in messages" :message="msg" :key="i"></message>
        </div>
      </v-flex>
      <v-flex xs12><v-divider></v-divider></v-flex>
      <v-flex xs12>
        <v-container py-0 px-2 grid-list-xs>
          <v-layout row wrap>
            <v-text-field placeholder="Type here" v-model="message" @keyup.enter="sendMessage"></v-text-field>
            <v-btn icon @click="sendMessage" :disabled="message.length < 1"><v-icon>send</v-icon></v-btn>
          </v-layout>
        </v-container>
      </v-flex>
    </v-layout>
  </v-container>
</template>

<script>
  import * as firebase from 'firebase'
  import message from './message'
  export default {
    data () {
      return {
        message: '',
        messages: [],
        ref: null,
        limit: 20
      }
    },
    computed: {
      currentChat () {
        return this.$store.getters.currentChat
      }
    },
    methods: {
      sendMessage () {
        if (this.message.length < 1) {
          return
        }
        var msg = {
          user: this.$store.getters.user.id,
          text: this.message,
          date: new Date().toString()
        }
        firebase.database().ref('chats').child(this.currentChat).child('messages').push(msg)
        this.message = ''
      },
      onChildAdded (snapshot) {
        this.messages.push(snapshot.val())
      }
    },
    components: {
      'message': message
    },
    watch: {
      'messages': function (value) {
        this.$nextTick(() => {
          var chatbox = this.$el.querySelector('.chat-box')
          chatbox.scrollTop = chatbox.scrollHeight
        })
      }
    },
    mounted () {
      this.ref = firebase.database().ref('chats').child(this.currentChat).child('messages').limitToLast(20)
      this.ref.on('child_added', this.onChildAdded)
    }
  }
</script>