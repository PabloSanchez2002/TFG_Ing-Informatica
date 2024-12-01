import KcAdminClient from '@keycloak/keycloak-admin-client';
import config from '#config/admin.js';

const adminClient = new KcAdminClient(config.adminClient)

await adminClient.login(config.adminUsername, config.adminPassword);

await adminClient.auth(config.adminClient);

export default adminClient;