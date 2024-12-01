// import { createApp } from 'vue'
// import './style.css'
// import App from './App.vue'
// import router from './router'
// import KeyCloakService from "./security/KeycloakService";

// // KeyCloakService.initKeycloak(() => {console.log('Keycloak is initialized')});
// createApp(App).use(router).mount('#app')



import { createApp } from "vue";
import App from "./App.vue";
import naive from "naive-ui";
import './style.css'
import KeyCloakService from "./security/KeycloakService";
import router from '#router'

const renderApp = () => {
    createApp(App).use(router).use(naive).mount('#app');
};

KeyCloakService.login(renderApp);       
