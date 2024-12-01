import Keycloak, { KeycloakResourceAccess } from "keycloak-js";
// import router from "#router";

const keycloakInstance = new Keycloak();

interface CallbackOneParam<T1 = void, T2 = void> {
  (param1: T1): T2;
}

const initKeycloak = async (onInitCallback: CallbackOneParam) => {
  try {
    keycloakInstance.authenticated = await keycloakInstance.init({ onLoad: "login-required" })
    onInitCallback()
  } catch (error) {
    console.error("Keycloak init failed")
    console.error(error)
  }
};

const getAuthenticated = () => {
  return keycloakInstance.authenticated;
}

const login = async (onAuthenticatedCallback: CallbackOneParam) => {
  keycloakInstance
    .init({ onLoad: "login-required" }).then(function (authenticated) {
    keycloakInstance.authenticated ? onAuthenticatedCallback() : keycloakInstance.login();
      })
    .catch((e) => {
      console.dir(e);
      console.log(`keycloak init exception: ${e}`);
    });
};

function logout(url: any) {
  keycloakInstance.logout({ redirectUri: url });
}


const useAccountManagement = () => {
  const accountManagement = () => {
    keycloakInstance.accountManagement();
  };

  return {
    accountManagement,
  };
};

const token = (): string | undefined => keycloakInstance?.token;


const getUserName = (): string | undefined => keycloakInstance?.tokenParsed?.preferred_username;

const getIdProfile = async () => {
  const id:string = await keycloakInstance.loadUserProfile().then((profile) => {
  console.log(profile);
   return profile;
  });

  return id;
};

const getUserRoles = (): string[] | undefined => {
  return keycloakInstance?.tokenParsed?.realm_access?.roles;
};

const getResourceAccess = (): KeycloakResourceAccess | undefined => {
  return keycloakInstance?.tokenParsed?.resource_access;
};

const isLoggedIn = () => !!keycloakInstance.token;

const KeyCloakService = {
  token,
  getAuthenticated,
  getUserName,
  getIdProfile,
  getUserRoles,
  getResourceAccess,
  initKeycloak,
  login,
  logout,
  useAccountManagement,
};


export default KeyCloakService;