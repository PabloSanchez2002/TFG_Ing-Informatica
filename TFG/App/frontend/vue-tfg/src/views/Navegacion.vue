<script setup lang='ts'>
 import router from '#router';
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { faHouse } from "@fortawesome/free-solid-svg-icons";
import Button from '#components/Button.vue';
import KeyCloakService from '../security/KeycloakService';


</script>


<template>
    <div class="navbar">
        <div class="navbar__container">
            <div class="navbar__elements">
                <div @click="router.push('/')">
                    <img class="delonia__blanco" src="../images/deloniaBlanco.png" width="300"alt="Delonia Logo" />
                    <img class="delonia__negro" src="../images/deloniaNegro.png"  width="300" alt="Delonia Logo" />
                </div>
                <div class="navbar__links">
                    <router-link class="navbar__links__item" to="/" color="foreground">
                        <FontAwesomeIcon :icon="faHouse" size="lg" />
                    </router-link>
                    
                    <router-link class="navbar__links__item" to="profile" color="foreground">
                        Perfil
                    </router-link>
                    
                    <router-link class="navbar__links__item" to="projects" color="foreground" >
                        Proyectos
                    </router-link>
                    <router-link class="navbar__links__item" to="hosts" color="foreground" >
                        Hosts
                    </router-link>

                    <router-link class="navbar__links__item" to="despliegues" color="foreground" >
                        Despliegues
                    </router-link>
                </div>
            </div>
            <Button @click="KeyCloakService.logout('')">
                Log Out
            </Button>
        </div>
    </div>
</template>

<style scoped>
.navbar {
    padding: 16px 32px;
    border-radius: 16px;
    background-color: #75757552;

}

.navbar__container {
    display: flex;
    align-items: center;
    /* gap: 16px; */
    justify-content: space-between;
}
.navbar__elements {
    display: flex;
    gap: 16px;
    align-items: center;
    
}

.navbar__links {
    display: flex;
    gap: 16px;
    font-size: 25px;
}

.navbar__links__item {
    color: #61c7ec;
    text-decoration: none;
}

.navbar__links__item:hover {
    color: #89d1eb;
}
</style>
