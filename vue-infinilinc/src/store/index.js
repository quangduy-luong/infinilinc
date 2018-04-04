import Vue from 'vue'
import Vuex from 'vuex'
// Store Modules
import sidebar from './modules/sidebar'
import users from './modules/users'
import chats from './modules/chats'

Vue.use(Vuex)

export const store = new Vuex.Store({
  modules: {
    sidebar: sidebar,
    users: users,
    chats: chats
  }
})
