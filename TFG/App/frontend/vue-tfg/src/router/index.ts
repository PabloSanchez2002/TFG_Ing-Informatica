import Dashboard from '#views/Dashboard.vue'
import Home from '#views/Home.vue'
import Profile from '#views/Profile.vue'
import ProyectosList from '#views/ProyectosList.vue'
import HostsList from '#views/HostsList.vue'
import Despliegues from '#views/Despliegues.vue'
import { createRouter, createWebHistory } from 'vue-router'



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/dashboard',
      name: 'dashboard',
      component: Dashboard,
      children: [
        {
          path: '/profile',
          name: 'profile',
          component: Profile
        },
        {
          path: '/projects',
          name: 'projects',
          component: ProyectosList
        },
        {
          path: '/hosts',
          name: 'hosts',
          component: HostsList
        },
        {
          path: '/despliegues',
          name: 'despliegues',
          component: Despliegues
        }
      ],
    },
    {
      path: '/',
      // name: 'login',
      component: Home
    }
    // {
		// path: '*',
		// name: 'default',
		// component: Dashboard
    // }
  ]
})

export default router