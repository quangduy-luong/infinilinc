import Vue from 'vue'
import App from './App'
import router from './router'
import Vuetify from 'vuetify'
import { store } from './store'
import sidebar from './components/core/sidebar'
import alert from './components/core/alert'
import 'vuetify/dist/vuetify.min.css'
import * as firebase from 'firebase'

Vue.use(Vuetify)

Vue.config.productionTip = false

// registering components
Vue.component('sidebar', sidebar)
Vue.component('app-alert', alert)

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App),
  created () {
    firebase.initializeApp({
      apiKey: 'AIzaSyChP8svYd4qhX0RonBSYyQhJbPL535Xat8',
      authDomain: 'infinilinc.firebaseapp.com',
      databaseURL: 'https://infinilinc.firebaseio.com',
      projectId: 'infinilinc',
      storageBucket: 'gs://infinilinc.appspot.com'
    })
    firebase.auth().onAuthStateChanged((user) => {
      if (user) {
        this.$store.dispatch('autoLogin', user)
      }
    })
    firebase.database().ref('links').on('value', (snapshot) => {
      this.$store.dispatch('loadLinks')
    })
  }
})
