<script setup lang="ts">
// import { RouterView } from 'vue-router';
// import router from './router'
import Navegacion from './Navegacion.vue';
// import KeyCloakService from '../security/KeycloakService';

</script>

<template>
  <main>
    <Navegacion />
    <div className="content__container">
      <div className="main__container">
        <router-view />
      </div>
    </div>
  </main>
</template>

<style scoped>

  .content__container{
    width: 100%;
    display: flex;
    justify-content: center;
  }

  .main__container{
    max-width: 2100px;	
		}    


</style>
