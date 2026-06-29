import Vue from 'vue'
import Vuex from 'vuex'
import { logout } from '@/api/api'
import msg from './msg.js'
import plan from './plan.js'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    HOST: '/api',
  },
  getters: {
  },
  mutations: {
  },
  actions: {
    logout({ commit }) {
      return new Promise(resolve => {
        logout().then(res => {
          
        })
        resolve()
      })
    }
  },
  modules: {
    msg,
    plan
  }
})
