const state = {
  menuItems: [
    {
      icon: 'person_add',
      title: 'Sign Up',
      path: 'signup'
    },
    {
      icon: 'lock',
      title: 'Sign In',
      path: 'signin'
    }
  ],
  menuItemsUser: [
    {
      icon: 'person',
      title: 'Profile',
      path: 'profile'
    },
    {
      icon: 'chat',
      title: 'Chats',
      path: 'chats'
    },
    {
      icon: 'link',
      title: 'Links',
      path: 'links'
    }
  ],
  showSidebar: false
}

const getters = {
  getShowSidebar (state) {
    return state.showSidebar
  },
  getMenuItems (state) {
    return state.menuItems
  },
  getMenuItemsUser (state) {
    return state.menuItemsUser
  }
}

const actions = {
}

const mutations = {
  toggleSidebar (state) {
    state.showSidebar = !state.showSidebar
  },
  setShowSidebar (state, visible) {
    state.showSidebar = visible
  }
}

export default {
  state,
  getters,
  actions,
  mutations
}
