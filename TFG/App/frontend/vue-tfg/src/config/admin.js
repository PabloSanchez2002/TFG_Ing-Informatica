const baseUrl = 'http://127.0.0.1:8080';

export default {
  baseUrl: baseUrl,
  token: {
    username: 'pablo',
    password: 'pablo',
    grant_type: 'password',
    client_id: 'vue-tfg',
    realmName: 'TFG_Pablo'
  },
  adminClient: {
    baseUrl: baseUrl,
    realmName: 'TFG_Pablo',
    username: 'pablo',
    password: 'pablo',
    grantType: 'password',
    clientId: 'vue-tfg'
  }
};

