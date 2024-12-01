<script lang="ts" setup>
    import router from '../router';
    import Button from '../components/Button.vue';
    import deloniaBlanco from '../images/deloniaBlanco.png';
    import deloniaNegro from '../images/deloniaNegro.png';
    import KeyCloakService from '../security/KeycloakService';

    const handleLogin = () => {
        KeyCloakService.login(() => {});
        router.push('/dashboard');
    };


</script>

<template>
    <div className="container">
        <img class="delonia__blanco imagen__home" :src=deloniaBlanco alt="Delonia Logo" />
        <img class="delonia__negro imagen__home" :src=deloniaNegro alt="Delonia Logo" />
        <h1 className="header"> <br />Herramienta para proyectos DevOps de Delonia <br /> <br /> </h1>
        
        <Button @click="handleLogin">
            <strong>Sign in with Keycloak</strong>
        </Button>
    </div>
</template>

<style scoped>
.container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: 70vh;
}

.imagen__home {
    width: 700px;
}

</style>