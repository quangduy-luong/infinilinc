import * as firebase from 'firebase'

const state = {
  user: null,
  loading: false,
  error: null,
  nfc: false
}

const getters = {
  user (state) {
    return state.user
  },
  loading (state) {
    return state.loading
  },
  error (state) {
    return state.error
  },
  nfc (state) {
    return state.nfc
  }
}

const mutations = {
  setUser (state, payload) {
    state.user = payload
    this.dispatch('loadLinks')
  },
  setLoading (state, payload) {
    state.loading = payload
  },
  setError (state, payload) {
    state.error = payload
  },
  clearError (state) {
    state.error = null
  },
  setNfc (state, payload) {
    state.nfc = payload
  }
}

const actions = {
  registerUser ({commit}, payload) {
    commit('setLoading', true)
    commit('clearError')
    firebase.auth().createUserWithEmailAndPassword(payload.email, payload.password)
      .then(
        user => {
          const newUser = {
            id: user.uid,
            email: user.email,
            username: payload.username
          }
          firebase.database().ref('users/' + user.uid).set(newUser)
            .then(
              data => {
                commit('setUser', newUser)
              })
            .catch(
              error => {
                console.log(error)
                commit('setError', error)
              })
          commit('setLoading', false)
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  updateUsername ({commit, state}, payload) {
    commit('setLoading', true)
    commit('clearError')
    var user = firebase.auth().currentUser
    firebase.database().ref('users/' + user.uid).update({username: payload.username})
            .then(
              data => {
                state.user.username = payload.username
              })
            .catch(
              error => {
                console.log(error)
                commit('setError', error)
              })
    commit('setLoading', false)
  },
  updateEmail ({commit, state}, payload) {
    commit('setLoading', true)
    commit('clearError')
    var user = firebase.auth().currentUser
    user.updateEmail(payload.email)
      .then(
        () => {
          firebase.database().ref('users/' + user.uid).update({email: payload.email})
            .then(
              data => {
                state.user.email = payload.email
              })
            .catch(
              error => {
                console.log(error)
                commit('setError', error)
              })
          commit('setLoading', false)
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  updatePassword ({commit}, payload) {
    commit('setLoading', true)
    commit('clearError')
    var user = firebase.auth().currentUser
    user.updatePassword(payload.password)
      .then(
        data => {
          console.log('Password updated.')
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  loginWithGoogle ({commit}) {
    commit('setLoading', true)
    commit('clearError')
    var provider = new firebase.auth.GoogleAuthProvider()
    firebase.auth().signInWithPopup(provider)
      .then(
        result => {
          console.log(result)
          firebase.database().ref('users/').once('value', function (snapshot) {
            if (snapshot.hasChild(result.user.uid)) {
              var currUser = snapshot.child(result.user.uid).val()
              currUser.id = result.user.uid
              commit('setUser', currUser)
            } else {
              const newUser = {
                id: result.user.uid,
                email: result.user.email,
                username: result.user.displayName
              }
              firebase.database().ref('users/' + result.user.uid).set(newUser)
                .then(
                  data => {
                    commit('setUser', newUser)
                  })
                .catch(
                  error => {
                    console.log(error)
                    commit('setError', error)
                  })
            }
          })
          commit('setLoading', false)
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  loginWithFacebook ({commit}) {
    commit('setLoading', true)
    commit('clearError')
    var provider = new firebase.auth.FacebookAuthProvider()
    firebase.auth().signInWithPopup(provider)
      .then(
        result => {
          firebase.database().ref('users/').once('value', function (snapshot) {
            if (snapshot.hasChild(result.user.uid)) {
              var currUser = snapshot.child(result.user.uid).val()
              currUser.id = result.user.uid
              commit('setUser', currUser)
            } else {
              const newUser = {
                id: result.user.uid,
                email: result.user.email,
                username: result.user.displayName
              }
              firebase.database().ref('users/' + result.user.uid).set(newUser)
                .then(
                  data => {
                    commit('setUser', newUser)
                  })
                .catch(
                  error => {
                    console.log(error)
                    commit('setError', error)
                  })
            }
          })
          commit('setLoading', false)
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  loginWithTwitter ({commit}) {
    commit('setLoading', true)
    commit('clearError')
    var provider = new firebase.auth.TwitterAuthProvider()
    firebase.auth().signInWithPopup(provider)
      .then(
        result => {
          firebase.database().ref('users/').once('value', function (snapshot) {
            if (snapshot.hasChild(result.user.uid)) {
              var currUser = snapshot.child(result.user.uid).val()
              currUser.id = result.user.uid
              commit('setUser', currUser)
            } else {
              const newUser = {
                id: result.user.uid,
                email: result.user.email,
                username: result.user.displayName
              }
              firebase.database().ref('users/' + result.user.uid).set(newUser)
                .then(
                  data => {
                    commit('setUser', newUser)
                  })
                .catch(
                  error => {
                    console.log(error)
                    commit('setError', error)
                  })
            }
          })
          commit('setLoading', false)
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  loginUser ({commit}, payload) {
    commit('setLoading', true)
    commit('clearError')
    firebase.auth().signInWithEmailAndPassword(payload.email, payload.password)
      .then(
        user => {
          firebase.database().ref('users/').once('value', function (snapshot) {
            var currUser = snapshot.child(user.uid).val()
            commit('setUser', currUser)
            commit('setLoading', false)
          })
        }
      )
      .catch(
        error => {
          commit('setLoading', false)
          commit('setError', error)
          console.log(error)
        }
      )
  },
  autoLogin ({commit}, payload) {
    firebase.database().ref('users/').once('value', function (snapshot) {
      var currUser = snapshot.child(payload.uid).val()
      commit('setUser', currUser)
      commit('setLoading', false)
    })
  },
  logout ({commit}) {
    firebase.auth().signOut()
    commit('setUser', null)
  },
  setError ({commit}, error) {
    commit('setError', error)
  },
  clearError ({commit}) {
    commit('clearError')
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
