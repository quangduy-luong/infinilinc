import Vue from 'vue'
import Router from 'vue-router'
import homepage from '@/components/homepage'
import profile from '@/components/users/profile/profile'
import changeEmail from '@/components/users/profile/changeEmail'
import changeUsername from '@/components/users/profile/changeUsername'
import changePassword from '@/components/users/profile/changePassword'
import signin from '@/components/users/signin'
import signup from '@/components/users/signup'
import chat from '@/components/conversations/chat'

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
      path: '/changeEmail',
      name: 'changeEmail',
      component: changeEmail
    },
    {
      path: '/changeUsername',
      name: 'changeUsername',
      component: changeUsername
    },
    {
      path: '/changePassword',
      name: 'changePassword',
      component: changePassword
    },
    {
      path: '/chat',
      name: 'chat',
      component: chat
    }
  ],
  mode: 'history'
})
