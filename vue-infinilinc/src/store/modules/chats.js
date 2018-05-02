import * as firebase from 'firebase'

const state = {
  linkFeedback: 'No connection attempt yet.',
  links: [],
  currentChat: null
}

const getters = {
  linkFeedback (state) {
    return state.linkFeedback
  },
  links (state) {
    return state.links
  },
  currentChat (state) {
    return state.currentChat
  }
}

const mutations = {
  setLinkFeedback (state, payload) {
    state.linkFeedback = payload
  },
  setLinks (state, payload) {
    state.links = payload
    console.log(payload)
  },
  setCurrentChat (state, payload) {
    state.currentChat = payload
  }
}

const actions = {
  createLink ({commit}, payload) {
    var myUsername = payload.originalUsername
    commit('setLinkFeedback', 'Attempting to link ' + payload.originalUser + ' and ' + payload.otherUser + '. ')
    firebase.database().ref('links/' + payload.originalUser).once('value', function (snapshot) {
      if (snapshot.hasChild(payload.otherUser)) {
        commit('setLinkFeedback', 'Failed to create a link. ')
        console.log('Failed to create a link.')
      } else {
        if (payload.originalUser < payload.otherUser) {
          var chat = firebase.database().ref('chats').push({users: [payload.originalUser, payload.otherUser]})
          firebase.database().ref('users').child(payload.otherUser).once('value', function (data) {
            var otherUsername = payload.otherUsername
            firebase.database().ref('links/' + payload.originalUser).child(payload.otherUser).set({chat: chat.key, user: payload.otherUser, username: otherUsername})
            firebase.database().ref('users/' + payload.originalUser).child('chats').child(chat.key).set(true)
            firebase.database().ref('links/' + payload.otherUser).child(payload.originalUser).set({chat: chat.key, user: payload.originalUser, username: myUsername})
            firebase.database().ref('users/' + payload.otherUser).child('chats').child(chat.key).set(true)
          })
          commit('setLinkFeedback', 'Link has been established by this user. ')
        }
        commit('setLinkFeedback', 'Link created. ')
        console.log('created link')
      }
    })
  },
  loadLinks ({commit}) {
    if (this.getters.user === null || this.getters.user === undefined) {
      commit('setLinks', [])
      return
    }
    const userID = this.getters.user.id
    firebase.database().ref('links/' + userID).once('value', function (snapshot) {
      commit('setLinks', snapshot.val())
    })
  },
  removeLink ({commit}, payload) {
    firebase.database().ref('chats/' + payload.chat).remove()
    firebase.database().ref('users/' + this.getters.user.id + '/chats/' + payload.chat).remove()
    firebase.database().ref('users/' + payload.user + '/chats/' + payload.chat).remove()
    firebase.database().ref('links/' + this.getters.user.id).child(payload.user).remove()
    firebase.database().ref('links/' + payload.user).child(this.getters.user.id).remove()
    this.dispatch('loadLinks')
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
