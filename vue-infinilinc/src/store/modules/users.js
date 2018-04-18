import * as firebase from 'firebase'

const state = {
  user: null,
  loading: false,
  error: null
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
  getAccountList (state) {
    return state.accountList
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
  updateUsername ({commit}, payload) {
    commit('setLoading', true)
    commit('clearError')
    var user = firebase.auth().currentUser
    user.updateProfile(payload.username)
      .then(
        () => {
          console.log('Update successful')
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
  /* updateUsername ({commit}, payload) {
    commit('setLoading', true)
    commit('clearError')
    var user = firebase.auth().currentUser
    debugger
    console.log('payload' + payload.username)
    user.updateEmail(payload.email)
      .then(
        () => {
          const updateUser = {
            id: user.uid,
            email: payload.email,
            username: payload.username,
            defaultName: 0
          }
          firebase.database().ref('users/' + user.uid).set(updateUser)
            .then(
              data => {
                commit('setUser', updateUser)
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
  }, */
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
