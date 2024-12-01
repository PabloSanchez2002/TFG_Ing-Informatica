<script setup lang="ts">
import { format } from 'date-fns';
import Button from "#components/Button.vue";
import KeyCloakService from "#security/KeycloakService";
import { ref, onMounted, h } from "vue";
import axios from "axios";
import { NButton, NTag, useMessage } from "naive-ui";
import ButtonWarning from "#components/ButtonWarning.vue";
import { FontAwesomeIcon } from "@fortawesome/vue-fontawesome";
import { faArrowsRotate, faBookOpen, faPlay } from "@fortawesome/free-solid-svg-icons";
import {
    Despliegue,
    Group,
    Host,
    Project,
    ProjectType,
} from "#types/Despliegue";
import { faBook } from "@fortawesome/free-solid-svg-icons/faBook";

const despliegues = ref<Despliegue[]>([]);
const availableHost = ref<Host[]>([]);
const availableProjects = ref<Project[]>([]);
const availableProjectTypes = ref<ProjectType[]>([]);
const availableGroups = ref<Group[]>([]);

const userName = ref();
const roles = ref();
const isAdmin = ref();

const targetRow = ref();
const showModalDelete = ref(false);
const showModalCreate = ref(false);
const showModalDetails = ref(false);
const showModalLogs = ref(false);
const message = useMessage();

const columns = [
    {
        title: "ID",
        key: "id",
        minWidth: "48px"
    },
    {
        title: "Nombre",
        key: "name",
        minWidth: "100px",
        sortable: true,
        resizeable: true,
    },
    {
        title: "Grupo",
        key: "group",
        minWidth: "100px",
        sortable: true,
        resizeable: true,
    },
    {
        title: "Descripción",
        key: "description",
        minWidth: "100px",
        resizeable: true,
    },
    {
        title: "Host",
        key: "hostId",
        minWidth: "100px",
        resizeable: true,

        render(row: any) {
            //return row.hostId value on a tag borderless
            // return h(NTag, { bordered: false }, () => row.hostId);
            if (row.hostId === 'error') {
                return h(NTag, { type: "error" }, () => " ⚠️ Host eliminado");
            } else {
                return h(NTag, { bordered: false }, () => row.hostId);
            }
        },
    },
    {
        title: "Proyecto",
        key: "projectId",
        minWidth: "100px",
        resizeable: true,

        render(row: any) {
            //return row.projectId value on a tag
            if (row.projectId === 'error') {
                return h(NTag, { type: "error" }, () => "⚠️ Proyecto eliminado");
            } else {
                return h(NTag, { bordered: false }, () => row.projectId);
            }
        }
    },
    {
        title: "Estado desarrollo",
        key: "devState",
        minWidth: "100px",
        resizeable: true,
    },
    {
        title: "Tipo",
        key: "deployType",
        minWidth: "100px",
        resizeable: true,

        render(row: any) {
            //return row.deployType value on a tag
            return h(NTag, { type: "info" }, () => row.deployType);
        }
    },
    {
        title: "Fecha",
        key: "deployDate",
        minWidth: "100px",

        resizeable: true,

        render(row: any) {
            //format data from 2024-05-21T14:16:31.398 to 21/05/2024 - 14:16:31

            if (row.deployDate !== "-") {
                const formattedDate = format(new Date(row.deployDate), "dd/MM/yyyy - HH:mm:ss");
                return formattedDate;
            } else {
                return row.deployDate;
            }
        }
    },
    {
        title: "Desplegado por",
        key: "deployedBy",
        minWidth: "100px",
        resizeable: true,
    },
    {
        title: "Estado",
        key: "deployStatus",
        minWidth: "100px",
        resizeable: true,

        render(row: any) {
            //return row.deployStatus value on a tag
            // return row.deployStatus ? h(NTag, { type: "success" }, ()=> row.deployStatus) : h(NTag, { type: "error" }, ()=> "N/A");
            if (row.deployStatus === "Deployed") {
                return h(NTag, { type: "success" }, () => "Éxito");
            } else if (row.deployStatus === "Failed") {
                return h(NTag, { type: "error" }, () => "Fallido");
            } else if (row.deployStatus === "Deploying...") {
                return h(NTag, { type: "warning" }, () => "Desplegando...");
            } else {
                return h(NTag, { type: "warning" }, () => "N/A");
            }
        },
    },
    {
        title: "Log",
        key: "deployLog",
        minWidth: "100px",
        resizeable: true,

        // if logs are empty, return "No logs", si hay logs, return button button with this icon: <font-awesome-icon :icon="['fas', 'book-open']" />
        render(row: any) {
            return row.deployLog ? h(
                NButton,
                {
                    color: "#61C7Ec",
                    style: { boxShadow: "0 0 16px #61C7EC" },
                    disabled: row.deployStatus === 'Deploying...',
                    onClick: () => showLogs(row)
                },
                () => [
                    h(FontAwesomeIcon, { icon: faBookOpen }),
                    h('div', { style: { marginLeft: "8px" } }, ["Leer logs"])
                ]
            ) : "No logs";
        },
    },
    {
        title: "Acciones",
        key: "actions",
        minWidth: "100px",

        resizeable: true,

        render(row: any) {
            return h("div", { style: { display: "flex", gap: "8px" } }, [
                h(
                    NButton,
                    {
                        color: "#61C7EC",
                        style: { boxShadow: "0 0 16px #61C7EC" },
                        disabled: row.deployStatus === 'Deploying...' || row.hostId === 'error' || row.projectId === 'error',
                        onClick: () => deployDespliegue(row),
                    },
                    () => [
                    // if deployStatus is either "Success" or "Failed", return <font-awesome-icon :icon="['fas', 'rotate-right']" spin /> and "Reintentar", else return <font-awesome-icon :icon="['fas', 'play']" beat /> and "Desplegar"
                        row.deployStatus === "Deployed" || row.deployStatus === "Failed" ? h(FontAwesomeIcon, { icon: faArrowsRotate }) : h(FontAwesomeIcon, { icon: faPlay, beat: true }),
                        row.deployStatus === "Deployed" || row.deployStatus === "Failed" ? h('div', { style: { marginLeft: "8px" } }, ["Reintentar"]) : h('div', { style: { marginLeft: "8px" } }, ["Desplegar"]),

                        // h(FontAwesomeIcon, { icon: faPlay, beat: true }),
                        // h('div', { style: { marginLeft: "8px" } }, ["Desplegar"])
                    ]
                ),
                h(
                    NButton,
                    {
                        disabled: row.deployStatus === 'Deploying...' || row.hostId === 'error' || row.projectId === 'error',
                        onClick: () => editDespliegue(row),
                    },
                    () => "Editar"
                ),
                h(
                    ButtonWarning,
                    {
                        onClick: () => deleteDespliegue(row),
                    },
                    () => "Eliminar"
                ),
            ]);
        },
    },
];

interface CreateFormModel {
    name: string;
    group: string;
    hostId: number | null;
    description: string;
    projectId: number | null;
    devState: string;
    deployType: string;
    deployDate: string;
    deployedBy: string;
    deployStatus: string;
    deployLog: string;
}

// Data for the form
const createFormModel = ref<CreateFormModel>({
    name: "",
    group: "",
    hostId: null,
    description: "",
    projectId: null,
    devState: "",
    deployType: "",
    deployDate: "",
    deployedBy: "",
    deployStatus: "",
    deployLog: "",
});

// Rules for form validation
const createFormRules = {
    name: [
        { required: true, message: "El nombre es requerido", trigger: "blur" },
    ],
    group: [
        { required: true, message: "El grupo es requerido", trigger: "blur" },
    ],
    // hostId: [
    //     { required: true, message: "El host es requerido", trigger: "blur" },
    // ],
    description: [
        { required: true, message: "La descripción es requerida", trigger: "blur" },
    ],
    // projectId: [
    //     { required: true, message: "El proyecto es requerido", trigger: "blur" },
    // ],
    deployType: [
        {
            required: true,
            message: "El tipo de despliegue es requerido",
            trigger: "blur",
        },
    ],
};

// Function to reload hosts
async function getHosts() {
    try {
        const response = await axios.get("http://127.0.0.1:9090/host");
        availableHost.value = response.data.map((host: any) => {
            return {
                label: host.name + " - " + host.ip,
                value: host.id,
            };
        });

        despliegues.value.forEach((despliegue) => {
            try {
                despliegue.hostId = availableHost.value?.find(
                    (host) => host.value === despliegue.hostId
                ).label;
            } catch (error) {
                message.error("Error al recargar el host " + despliegue.hostId);
                despliegue.hostId = "error";
            }
        });

        console.log("Hosts: ", availableHost.value);
    } catch (error) {
        console.log("Error al recargar los hosts", error);
        message.error("Error al recargar los hosts");
    }
}

// Dunction to reload Projects
async function getProjects() {
    try {
        const response = await axios.get("http://127.0.0.1:9090/proyecto");
        availableProjects.value = response.data.map((project: any) => {
            return {
                label: project.name,
                value: project.id,
            };
        });

        despliegues.value.forEach((despliegue) => {
            console.log("Despliegue: ", despliegue);
            console.log("Available Projects: ", availableProjects.value);
            try {
                despliegue.projectId = availableProjects.value?.find(
                    (project) => project.value === despliegue.projectId
                ).label;
            } catch (error) {
                message.error("Error al recargar el proyecto " + despliegue.projectId);
                despliegue.projectId = "error"; 
            }
            
        });
        console.log("Projects: ", availableProjects.value);
    } catch (error) {
        console.log("Error al recargar los proyectos", error);
        message.error("Error al recargar los proyectos");
    }
}

async function updateTypes(value: number) {
    try {
        createFormModel.value.projectId = value;
        createFormModel.value.deployType = "";
        const response = await axios.get("http://127.0.0.1:9090/proyecto/" + value);
        availableProjectTypes.value = response.data.types.map((type: string) => {
            return {
                label: type,
                value: type,
            };
        });
        console.log("Types: ", availableProjectTypes.value);
    } catch (error) {
        console.log("Error al recargar los tipos", error);
        message.error("Error al recargar los tipos");
    }
}

function showLogs(row: any) {
    console.log("Showing logs", { ...row });
    targetRow.value = { ...row };
    showModalLogs.value = true;
}

function copyLogs() {
    console.log("Copying logs");
    navigator.clipboard.writeText(targetRow.value.deployLog);
    message.success("Logs copiados al portapapeles");
}

// Function to create a new despliegue
function createDespliegue() {
    console.log("Creating despliegue");
    showModalCreate.value = true;
}

// Function to edit a despliegue
function editDespliegue(row: any) {
    console.log("Editing despliegue", { ...row });
    targetRow.value = { ...row };
    showModalDetails.value = true;
}

// Function to delete a despliegue
function deleteDespliegue(row: any) {
    console.log("Deleting despliegue", { ...row });
    targetRow.value = { ...row };
    showModalDelete.value = true;
}

// Function to handle submit action for creating a despliegue
function submitCallbackCrear() {
    console.log("Submitting form");
    console.log(createFormModel.value);

    axios
        .post("http://127.0.0.1:9090/despliegue", createFormModel.value)
        .then((response) => {
            console.log(response);
            message.success("Despliegue creado exitosamente");
            showModalCreate.value = false;
            reloadDespliegues();
        })
        .catch((error) => {
            console.log(error);
            message.error("Error al crear despliegue");
        });

    // Reset form
    createFormModel.value = {
        name: "",
        group: "",
        hostId: null,
        description: "",
        projectId: null,
        devState: "",
        deployType: "",
        deployDate: "",
        deployedBy: "",
        deployStatus: "",
        deployLog: "",
    };
}

// Function to handle submit action for editing a despliegue
function submitCallbackEditar() {
    console.log("Submitting form: ", targetRow.value);
    
    targetRow.value.hostId = availableHost.value.find(
        (host) => host.label === targetRow.value.hostId
    ).value;
    targetRow.value.projectId = availableProjects.value.find(
        (project) => project.label === targetRow.value.projectId
    ).value;

    axios
        .put(
            "http://127.0.0.1:9090/despliegue/" + targetRow.value.id,
            targetRow.value
        )
        .then((response) => {
            console.log(response);
            message.success("Despliegue editado exitosamente");
        })
        .catch((error) => {
            console.log(error);
            message.error("Error al editar despliegue");
        }).finally(() => {
            showModalDetails.value = false;
            reloadDespliegues();
        });
}

// Function to reload despliegues
async function reloadDespliegues() {
    try {
        const response = await axios.get("http://127.0.0.1:9090/despliegue");
        despliegues.value = response.data.map((despliegue: any) => {
            return {
                id: despliegue.id,
                name: despliegue.name,
                group: despliegue.group,
                hostId: despliegue.hostId,
                description: despliegue.description,
                projectId: despliegue.projectId,
                devState: despliegue.devState,
                deployType: despliegue.deployType,
                deployDate: despliegue.deployDate ? despliegue.deployDate : "-",
                deployedBy: despliegue.deployedBy ? despliegue.deployedBy : "-",
                deployStatus: despliegue.deployStatus,
                deployLog: despliegue.deployLog,
            };
        });
        message.success("Despliegues cargados exitosamente");
        getHosts();
        getProjects();
        console.log(availableProjects.value);
    } catch (error) {
        console.log(error);
        message.error("Error al cargar despliegues");
    }
}

// Function to handle cancel action for creating a despliegue
function cancelCallbackCrear() {
    console.log("Canceling form");
    showModalCreate.value = false;
    createFormModel.value = {
        name: "",
        group: "",
        hostId: null,
        description: "",
        projectId: null,
        devState: "",
        deployType: "",
        deployDate: "",
        deployedBy: "",
        deployStatus: "",
        deployLog: "",
    };
}

//Function to handle submit action for deleting a host
function submitCallbackEliminar() {
    console.log("Deleting despliegue", targetRow.value);

    axios
        .delete("http://127.0.0.1:9090/despliegue/" + targetRow.value.id)
        .then((response) => {
            console.log(response);
            message.success("Despliegue eliminado exitosamente");
            showModalDelete.value = false;
            reloadDespliegues();
        })
        .catch((error) => {
            const errorMessage = error.response.data.message;
            if (error.response && error.response.status === 404) {
                // Show Naive UI alert for 404 error
                message.error("Error 404: " + errorMessage);
            } else {
                message.error("Error: " + errorMessage);
            }
        })
        .finally(() => {
            showModalDelete.value = false;
        });
}

// Function to handle cancel action for deleting a host
function cancelCallbackEliminar() {
    console.log("Canceling delete");
    showModalDelete.value = false;
    createFormModel.value = {
        name: "",
        group: "",
        hostId: null,
        description: "",
        projectId: null,
        devState: "",
        deployType: "",
        deployDate: "",
        deployedBy: "",
        deployStatus: "",
        deployLog: "",
    };
}

// Function to deploy a despliegue
function deployDespliegue(row: any) {
    console.log("Deploying despliegue", { ...row });
    targetRow.value = { ...row };
    row.deployStatus = "Deploying...";
    row.deployedBy = userName; 
    axios //send username to backend on body to save who deployed the despliegue
        .post("http://127.0.0.1:9090/despliegue/deploy/" + targetRow.value.id, userName.value)
        .then((response) => {
            console.log(response);
            if (response.data === "Success") {
                message.success("Despliegue exitoso");
            } else {
                message.error("Error al desplegar, consulta los logs");
            }
        }).catch((error) => {
            const errorMessage = error.response.data.message;
            if (error.response && error.response.status === 404) {
                // Show Naive UI alert for 404 error
                message.error("Error 404: " + errorMessage);
            } else {
                message.error("Error: " + errorMessage);
            }
        }).finally(() => {
            reloadDespliegues();
        });
}

onMounted(async () => {
    try {
        roles.value = await KeyCloakService.getUserRoles();
        userName.value = await KeyCloakService.getUserName();
        console.log("Roles: ", roles.value);
        isAdmin.value = roles.value.includes("AdministrarDespliegues");
        reloadDespliegues();
        // update all the hostIp and project names for each despliegue in the table to show the actual names instead of the ids
        despliegues.value.forEach((despliegue) => {
            despliegue.hostId = availableHost.value.find(
                (host) => host.value === despliegue.hostId
            ).label;
            despliegue.projectId = availableProjects.value.find(
                (project) => project.value === despliegue.projectId
            ).label;
        });

    } catch (error) {
        console.error("Error mounting component:", error);
    }
});
</script>

<template>
    <div className="container">
        <div v-if="isAdmin">
            <Button @click="createDespliegue()">
                <strong>Crear nuevo despliegue</strong>
            </Button>
        </div>
        <div v-else>
            <h1>No tienes permisos para administrar despliegues</h1>
        </div>
        <div class="menu-div">
            <div>
                <h1>Despliegues disponibles</h1>
            </div>
            <div>
                <n-space>
                    <n-button @click="reloadDespliegues()">
                        <template #icon>
                            <FontAwesomeIcon :icon="faArrowsRotate" size="lg" />
                        </template>
                        Refrescar
                    </n-button>
                </n-space>
            </div>
        </div>

        <n-space vertical :size="12" className="table__container">
            <n-data-table ref="table" :columns="columns" :data="despliegues" :bordered="true" :single-column="false"
                :stripe="true" />
        </n-space>
    </div>

    <!-- Modal for create -->
    <n-modal v-model:show="showModalCreate" preset="dialog" title="Crear Despliegue">
        <n-form :model="createFormModel" :rules="createFormRules" ref="createFormRef">
            <n-form-item label="Nombre" path="name">
                <n-input v-model:value="createFormModel.name" />
            </n-form-item>
            <n-form-item label="Grupo" path="group">
                <n-input v-model:value="createFormModel.group" />
            </n-form-item>
            <n-form-item label="Descripción" path="description">
                <n-input v-model:value="createFormModel.description" />
            </n-form-item>
            <n-form-item label="Host" path="hostId">
                <n-select v-model:value="createFormModel.hostId" placeholder="Select" :options="availableHost" />
            </n-form-item>
            <n-form-item label="Proyecto" path="projectId">
                <n-select v-model:value="createFormModel.projectId" placeholder="Select" :options="availableProjects"
                    :on-update:value="updateTypes" />
            </n-form-item>
            <n-form-item label="Tipo" path="deployType">
                <n-select v-model:value="createFormModel.deployType" placeholder="Select"
                    :options="availableProjectTypes" />
            </n-form-item>
            <n-form-item label="Estado" path="devState">
                <n-input v-model:value="createFormModel.devState" />
            </n-form-item>

            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end; gap: 10px">
                        <n-button type="default" @click="cancelCallbackCrear()">Cancelar
                        </n-button>

                        <n-button type="primary" :disabled="createFormModel.name === '' ||
                            createFormModel.group === '' ||
                            createFormModel.hostId === null ||
                            createFormModel.description === '' ||
                            createFormModel.projectId === null ||
                            createFormModel.deployType === ''
                            " @click="submitCallbackCrear()">Crear
                        </n-button>
                    </div>
                </n-col>
            </n-row>
        </n-form>
    </n-modal>

    <!-- Modal for details -->
    <n-modal v-model:show="showModalDetails" preset="dialog" title="Detalles del despliegue">
        <n-form :model="targetRow" :rules="createFormRules" ref="detailsFormRef">
            <n-form-item label="Nombre" path="name">
                <n-input v-model:value="targetRow.name" />
            </n-form-item>
            <n-form-item label="Grupo" path="group">
                <n-input v-model:value="targetRow.group" />
            </n-form-item>
            <n-form-item label="Descripción" path="description">
                <n-input v-model:value="targetRow.description" />
            </n-form-item>
            <n-form-item label="Estado de desarrollo" path="devState">
                <n-input v-model:value="targetRow.devState" />
            </n-form-item>

            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end">
                        <n-button :disabled="targetRow.name === '' ||
                            targetRow.group === '' ||
                            targetRow.description === ''
                            " type="primary" @click="submitCallbackEditar()">
                            Guardar
                        </n-button>
                    </div>
                </n-col>
            </n-row>
        </n-form>
    </n-modal>

    <!-- Modal for deleting -->
    <n-modal v-model:show="showModalDelete" preset="dialog" title="⚠️ Eliminar ⚠️" positive-text="SI" negative-text="NO"
        @positive-click="submitCallbackEliminar()" @negative-click="cancelCallbackEliminar()">
        ¿Borrar el host <strong>{{ targetRow.name }}</strong>?
    </n-modal>

    <!-- Modal to read logs -->

    <n-modal v-model:show="showModalLogs" preset="dialog" title="Logs del despliegue"  
    style="max-width: 900px; width: 100%; max-height: 1000px; display: flex; flex-direction: column;">
    
        <n-card :style="{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column'}">
            <div ref="logContentContainer" :style="{ flex: 1, overflowY: 'auto', maxWidth: '900px', maxHeight: '800px'  }"> 
                <pre ref="logContent" class="log-content">{{ targetRow.deployLog }}</pre>
            </div>
        </n-card>
    
        <div :style="{ display: 'flex', justifyContent: 'flex-end', gap: '10px', paddingTop: '16px' }">
            <n-button type="default" @click="showModalLogs = false">Cerrar</n-button>
            <n-button type="primary" @click="copyLogs">Copiar</n-button>
        </div>
    </n-modal>
</template>



<style scoped>
.container {
    display: flex;
    /* width: 1000px; */
    flex-direction: column;
    justify-content: center;
    align-items: flex-start;
    padding: 30px 20px;
}

.menu-div {
    display: flex;
    /* Use flexbox */
    justify-content: space-between;
    /* Align items with space between them */
    align-items: center;
    /* Center items vertically */
    gap: 10px;
}

.table__container {
    width: 100%;
    /* overflow-x: scroll; */
}

.table {
    width: 100%;
    background-color: black;
}

.button__div {
    display: flex;
    gap: 4px;
}

.log-content {
  white-space: pre-wrap;
  word-wrap: break-word;
  overflow-y: auto;
  height: 100%;
  padding: 8px;
  /* background-color: #f5f5f5; */
  border-radius: 8px;
}
</style>
