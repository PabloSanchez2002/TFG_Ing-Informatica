<script setup lang="ts">
    import { onMounted, ref } from 'vue';
    import KeyCloakService from '../security/KeycloakService';
        
    const username = ref();
    const accessToken = ref();
    const params = ref();
    const roles = ref();
    const resources = ref();


    onMounted(async () => {
        console.log("Profile Page Mounted");
        username.value = KeyCloakService.getUserName();
        accessToken.value =KeyCloakService.token();
        params.value = await KeyCloakService.getIdProfile();
        roles.value = await KeyCloakService.getUserRoles();
        resources.value = await KeyCloakService.getResourceAccess();
        
        console.log("Roles: ", roles.value);
        console.log("Resources: ", resources.value);
    });
</script>

<template>
    <div style="word-wrap: break-word;">
        <h2>Username: {{ username }}</h2>
        <!-- <h2>Access Token {{ accessToken }}</h2>
        <h2>Params: {{ params }}</h2> -->
        <!-- insert <h2>Roles: {{ roles }}</h2> but each one is in a naive ui tag -->
        <h2>Roles:</h2>
        <div style="display: flex; align-items: center; justify-content: center; gap: 8px;">
            <n-tag v-for="role in roles" :key="role" :bordered="false">
                {{ role }}
            </n-tag>
        </div>

        <!-- <h2>Resources: {{ resources }}</h2> -->
        <!-- <button @click="useAccountManagement">Account Management</button> -->
    </div>
</template>

<style>
/*     
    div { 
         display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;

    } */

</style>