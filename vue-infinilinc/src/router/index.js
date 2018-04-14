import Vue from 'vue'
import Router from 'vue-router'
import homepage from '@/components/homepage'
import profile from '@/components/users/profile'
import signin from '@/components/users/signin'
import signup from '@/components/users/signup'
<<<<<<< Updated upstream
import conversation from '@/components/conversations/conversation'
=======
import chat from '@/components/conversations/chat'
>>>>>>> Stashed changes

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'homepage',
      component: homepage
    },
    {
      path: '/signup',
      name: 'signup',
      component: signup
    },
    {
      path: '/signin',
      name: 'signin',
      component: signin
    },
    {
      path: '/profile',
      name: 'profile',
      component: profile
    },
    {
<<<<<<< Updated upstream
      path: '/conversation',
      name: 'conversation',
      component: conversation
=======
      path: '/chat',
      name: 'chat',
      component: chat
>>>>>>> Stashed changes
    }
  ],
  mode: 'history'
})
