<template>
  <v-container fluid class="text-xs-center">
    <v-layout row wrap>
      <v-flex xs12>
        <img src="https://i.imgur.com/5lmpNHy.png">
      </v-flex>
      <v-flex xs12>
        <span>Please tap the two phones together</span>
      </v-flex>
      <v-flex xs12>
        <v-container>
          <v-layout row wrap>
            <v-flex xs12 md6 lg4 offset-md3 offset-lg4>
              <v-text-field disabled label="Your display name" v-model="username"></v-text-field>
            </v-flex>
            <v-flex xs12 md6 lg4 offset-md3 offset-lg4>
              <v-text-field disabled label="Received user ID" v-model="newLink.id"></v-text-field>
            </v-flex>
          </v-layout>
        </v-container>
      </v-flex>
      <v-dialog v-model="nameDialog" persistent max-width="300">
        <v-card>
          <v-card-title>
            <span class="subheading">Please choose a display name for this connection:</span>
            <v-text-field v-model="username" label="New display name"></v-text-field>
          </v-card-title>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn @click="onNameConfirm" color="primary" large><v-icon>check</v-icon></v-btn>
            <v-spacer></v-spacer>
            <v-btn @click="onNameCancel" large><v-icon>close</v-icon></v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <v-dialog v-model="confirmDialog" persistent max-width="300">
        <v-card>
          <v-card-title>
            <span class="headline">Connect with {{ newLink.username }}?</span>
          </v-card-title>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn @click="onConfirm" color="primary" large><v-icon>check</v-icon></v-btn>
            <v-spacer></v-spacer>
            <v-btn @click="onCancel" large><v-icon>close</v-icon></v-btn>
            <v-spacer></v-spacer>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-layout>
  </v-container>
</template>

<script>
  export default {
    data () {
      return {
        newLink: '',
        confirmDialog: false,
        nameDialog: true,
        username: ''
      }
    },
    computed: {
      errorMessage () {
        return this.$store.getters.linkFeedback
      },
      links () {
        return this.$store.getters.links
      }
    },
    methods: {
      onCancel () {
        this.confirmDialog = false
      },
      onConfirm () {
        this.confirmDialog = false
        this.$store.dispatch('createLink', { originalUser: this.$store.getters.user.id, otherUser: this.newLink.id, originalUsername: this.username, otherUsername: this.newLink.username })
        this.$router.push('/')
      },
      onNameCancel () {
        this.nameDialog = false
        this.$router.push('/')
      },
      onNameConfirm () {
        this.nameDialog = false
      }
    },
    mounted () {
      /* eslint-disable */
      if (this.$store.getters.user !== null && this.$store.getters.user !== undefined) {
        this.username = this.$store.getters.user.username
      }
      nfc.enable()
      nfc.onConnect = () => {
        this.$store.commit('setLinkFeedback', 'Sending message now... ')
        var jString = JSON.stringify({ 'id': this.$store.getters.user.id, 'username': this.username })
        nfc.send(jString)
      }
      nfc.onSendComplete = () => {
        nfc.receive()
      }
      nfc.onReceive = (str) => {
        this.$store.commit('setLinkFeedback', 'Receiving message now... ')
        var jObj = JSON.parse(str)
        this.newLink = jObj
        this.$store.commit('setLinkFeedback', 'Message successfully received ')
      },
      nfc.onReset = () => {
        nfc.disable()
      }
    },
    beforeDestroy () {
      nfc.disable()
    },
    watch: {
      newLink: function (newStr, oldStr) {
        this.confirmDialog = true
      }
    }
  }
</script>