import Vue from 'vue'
import Router from 'vue-router'
import homepage from '@/components/homepage'
import profile from '@/components/users/profile'
import signin from '@/components/users/signin'
import signup from '@/components/users/signup'
import chat from '@/components/conversations/chat'
import chats from '@/components/conversations/chats'
import links from '@/components/links/links'
import connect from '@/components/links/connect'
import AuthGuard from './auth-guard'
import ErrorGuard from './error-guard'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'homepage',
      component: homepage,
      beforeEnter: ErrorGuard
    },
    {
      path: '/signup',
      name: 'signup',
      component: signup,
      beforeEnter: ErrorGuard
    },
    {
      path: '/signin',
      name: 'signin',
      component: signin,
      beforeEnter: ErrorGuard
    },
    {
      path: '/links',
      name: 'links',
      component: links,
      beforeEnter: AuthGuard
    },
    {
      path: '/profile',
      name: 'profile',
      component: profile,
      beforeEnter: AuthGuard
    },
    {
      path: '/chat',
      name: 'chat',
      component: chat,
      beforeEnter: AuthGuard
    },
    {
      path: '/chats',
      name: 'chats',
      component: chats,
      beforeEnter: AuthGuard
    },
    {
      path: '/connect',
      name: 'connect',
      component: connect,
      beforeEnter: AuthGuard
    }
  ],
  mode: 'history'
})
