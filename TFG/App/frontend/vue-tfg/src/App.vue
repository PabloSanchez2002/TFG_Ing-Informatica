<script setup lang='ts'>
import { darkTheme, lightTheme } from 'naive-ui'
import { ref } from 'vue'

const prefersDark = ref(window.matchMedia('(prefers-color-scheme: dark)').matches) 

</script>

<template>

    <n-config-provider :theme="prefersDark ? darkTheme : lightTheme">
        <n-message-provider>
            <router-view />    
        </n-message-provider>
    </n-config-provider>       
    
</template>