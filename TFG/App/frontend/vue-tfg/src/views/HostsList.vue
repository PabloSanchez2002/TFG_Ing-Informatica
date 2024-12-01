<script setup lang="ts">
    import Button from '#components/Button.vue';
    import KeyCloakService from '#security/KeycloakService';
    import { ref, onMounted, h, render } from 'vue';
    import axios from 'axios';
    import { NButton, NTag, NTooltip, useMessage } from 'naive-ui';
    import ButtonWarning from '#components/ButtonWarning.vue';
    import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
    import { faArrowsRotate } from "@fortawesome/free-solid-svg-icons";

    type Host = {
        id: number;
        name: string;
        ip: string;
        user: string;
        status: string;
    };

    const roles = ref();
    const isAdmin = ref();
    const hosts = ref<Host[]>();
    const columns = [
        {
            title: 'ID',
            key: 'id',
            label: 'ID',
            sortable: true,
            minWidth: "48px"
        },
        {
            title: 'Name',
            key: 'name',
            label: 'Name',
            sortable: true,
            minWidth: "48px"
        },
        {
            title: 'Ip',
            key: 'ip',
            label: 'Description',
            sortable: true,
            minWidth: "48px"
        },
        {
            title: 'Usuario',
            key: 'user',
            label: 'user',
            minWidth: "48px"
        },
        {
            title: 'Estado',
            key: 'status',
            label: 'status',
            minWidth: "48px",

            render(row: any) {
                // return h('div', row.status === 'ok' ? h(NTag, {type: 'success'}, ()=> 'En linea') : h(NTag, {type: 'error'}, ()=> 'No accesible'));
                statusCheck(row.ip, row.user)
                return h('div', row.status ? (row.status === 'ok' ? h(NTag, {type: 'success'}, ()=> 'En linea') : h(NTag, {type: 'error'}, ()=> 'No accesible')) : h(NTag, {type: 'warning'}, ()=> 'Cargando...'));
            }
        },
        {
            title: 'Acciones',
            key: 'actions',
            label: 'Acciones',
            minWidth: "110px",
            
            render(row: any) {
                return h('div',{style: {display: "flex", gap: "8px"}}, [
                    h(
                        NButton,
                        {   
                            onClick: () => editProyect(row),
                            disabled: !isAdmin.value
                        },
                        ()=>'Editar'
                    ),
                    h(
                        ButtonWarning,
                        {
                            onClick: () => deleteProyect(row),
                            disabled: !isAdmin.value
                        },
                        ()=>'Eliminar'
                    )
                ]);
            }
        }
    ];
    const targetRow = ref();
    const showModalDelete = ref(false);
    const showModalCreate = ref(false);
    const showModalDetails = ref(false);
    const message = useMessage();

    //Data for the form 
    const createFormModel = ref({
        name: null,
        ip: null,
        user: null,
        password: null
    });

    // const sshModel = ref({
    //     ip: '',
    //     user: '',
    //     password: ''
    // });

    //Rules for form validation
    const createFormRules = ref({
        name: [{ required: true, message: 'Nombre es requerido', trigger: 'blur' }],
        ip: [{ required: true, message: 'Ip es requerido', trigger: 'blur' }],
        user: [{ required: true, message: 'User es requerido', trigger: 'blur' }],
        password: [{ required: true, message: 'Password es requerido', trigger: 'blur' }]
    });


    //Function to create a new host
    function createHost() {
        console.log("Creating host");
        showModalCreate.value = true;
    }

    //Function to edit a host
    function editProyect(row: any) {
        console.log("Editing host", {...row});
        //copy ONLY the values of the row to the targetRow
        targetRow.value = {...row};
        showModalDetails.value = true;
    }

    //Function to delete a host
    function deleteProyect(row: any) {
        console.log("Deleting host",{...row});
        targetRow.value = {...row};
        showModalDelete.value = true;
    }


    // Function to handle submit action for creating a host
    function submitCallbackCrear() {
        console.log("Submitting form", createFormModel.value);
    
        axios.post('http://127.0.0.1:9090/host', createFormModel.value)
            .then(response => {
                console.log(response);
                message.success('Host creado');
                showModalCreate.value = false;
                reloadHosts();
            })
            .catch(error => {
                console.log(error);
                message.error('Error al crear el host');
            });
        
        // Reset form
        createFormModel.value = {
                        name: null,
                        ip: null,
                        user: null,
                        password: null                    
                    };
    }

    //Function to handle submit action for editing a host
    function submitCallbackEditar() {
        axios.put('http://127.0.0.1:9090/host/' + targetRow.value.id, targetRow.value)
            .then(response => {
                console.log(response);
                message.success('Host actualizado');
                showModalDetails.value = false;
                reloadHosts();
            })
            .catch(error => {
                const errorMessage = error.response.data.message;
                message.error('Error: ' + errorMessage);
            });
    }

    //Function to reload hosts
    async function reloadHosts() {
        try {

            const response = await axios.get('http://127.0.0.1:9090/host');
            hosts.value = response.data.map((host: any) => {
                // const status = ref(statusCheck(host.ip, host.user).then((status) => status));
                return {
                    id: host.id,
                    name: host.name,
                    ip: host.ip,
                    user: host.user,
                    status:undefined
                };
            });
            message.success('Hosts recargados');
            console.log("Hosts: ", hosts.value);
        } catch (error) {
            console.log('Error al recargar los hosts', error);
            message.error('Error al recargar los hosts');
        }
    }

    //Function to handle cancel action for creating a host
    function cancelCallbackEliminar() {
        console.log("Cancelado");
        showModalCreate.value = false;
        createFormModel.value = {
            name: null,
            ip: null,
            user: null,
            password: null
        };
    }

    //Function to handle submit action for deleting a host
    function submitCallbackEliminar() {
        console.log("Deleting host", targetRow.value);
        axios.delete('http://127.0.0.1:9090/host/' + targetRow.value.id)
            .then(response => {
                console.log(response);
                message.success('Host eliminado');
                showModalDelete.value = false;
                reloadHosts();
            })
            .catch(error => {
                const errorMessage = error.response.data.message;
                if (error.response && error.response.status === 404) {
                     // Show Naive UI alert for 404 error
                    message.error('Error 404: ' + errorMessage);
                } else {
                    message.error('Error: ' + errorMessage);
                }
            }).finally(() => {
                showModalDelete.value = false;
            });
    }

    // Function to handle statusCheck for servers adn return symbol according to status
    async function statusCheck(ip: string, user: string){

        const sshData = {
            hostname: ip,
            username: user,
        };
        try {
            const response = await axios.post('http://127.0.0.1:9090/testssh/key', sshData);
            // if response body is 0, then status is UP, if its 1, then status is DOWN
            console.log(response.data);
            if (hosts.value ){
                hosts.value.find((host: any) => {
                if (host.ip === ip && host.user === user) {
                    host.status = response.data === 0 ? "ok" : "Error";
                    }
                });
            }

        }catch (error) {
            console.log(error);
            return "Error";
        }
    }

    // Function to ping a host with ssh connection with username and password
    function pingHost(ip: string, user: string, password: string) {
        const sshData = {
            hostname: ip,
            username: user,
            password: password
        };

        console.log("Pinging host", sshData);
        try{
            axios.post('http://127.0.0.1:9090/testssh/password', sshData)
            .then(response => {
                    console.log(response.data);
                    if (response.data === 0) {
                        message.success('Ping exitoso');
                    } else {
                        message.error('Error al hacer ping');
                    }
                })
        } catch (error) {
            console.log(error);
            message.error('Error al hacer ping');
        }

    }


    onMounted(async () => {
        try {
            roles.value = await KeyCloakService.getUserRoles();
            console.log("Roles: ", roles.value);
            isAdmin.value = roles.value.includes("AdministrarHosts");
            reloadHosts();
        } catch (error) {
            console.error("Error mounting component:", error);
        }
    });

</script>

<template>
    <div className="container">
        <div v-if="isAdmin">
            <Button @click="createHost()">
                <strong>Crear nuevo host</strong>
            </Button>
        </div>
        <div v-else>
            <h1>No tienes permisos para administrar hosts</h1>
        </div>
        <div class="menu-div">
            <div>        
                <h1>Hosts disponibles</h1>
            </div>
            <div>    
                <n-space>
                    <n-button  @click="reloadHosts()">
                    <template #icon>
                        <FontAwesomeIcon :icon="faArrowsRotate" size="lg" />
                    </template>
                    Refrescar
                    </n-button>
                </n-space>    
            </div>
        </div>
        
        <n-space vertical :size="12" className="table__container">
        <n-data-table 
            ref="table"
            :columns="columns"
            :data= "hosts"
            :bordered="true"
            :stripe="true"
        />
        </n-space>
    </div>

    <!-- Modal for create -->
    <n-modal 
    v-model:show="showModalCreate" 
    preset="dialog" 
    title="Crear Host"
    >
        <n-form :model="createFormModel" :rules="createFormRules" ref="createFormRef">
            <n-form-item label="Nombre" path="name" rules="required">
                <n-input v-model:value="createFormModel.name" />
            </n-form-item>
            <n-form-item label="Ip" path="ip" rules="required">
                <n-input v-model:value="createFormModel.ip" />
            </n-form-item>
            <n-form-item label="Usuario" path="user" rules="required">
                <n-input v-model:value="createFormModel.user" />
            </n-form-item>

            <n-form-item label="Password" path="password" rules="required">
                <n-input v-model:value="createFormModel.password" type="password" show-password-on="click"/>
            </n-form-item>
            
        
            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end; gap: 10px;">
                        <n-button
                            :disabled="createFormModel.ip === '' || createFormModel.user === '' || createFormModel.password === ''"
                            type="default"
                            @click="pingHost(createFormModel.ip, createFormModel.user, createFormModel.password)"
                        >
                            Probar conexión SSH
                        </n-button>

                        <n-button
                            :disabled="createFormModel.name === '' || createFormModel.ip === '' || createFormModel.user === ''  "
                            type="primary"
                            @click="submitCallbackCrear()"
                        >
                            Crear
                        </n-button>
                        
                        

                    </div>
                </n-col>
            </n-row>
        
        </n-form>
    </n-modal>

    <!-- Modal for details -->
    <n-modal
        v-model:show="showModalDetails"
        preset="dialog"
        title="Detalles del host"   
    >
        <n-form :model="targetRow" :rules="createFormRules" ref="detailsFormRef">
            <n-form-item label="Nombre" path="name">
                <n-input v-model:value="targetRow.name" />
            </n-form-item>
            <n-form-item label="Ip" path="ip">
                <n-input v-model:value="targetRow.ip" />
            </n-form-item>
            <n-form-item label="Usuario" path="user">
                <n-input v-model:value="targetRow.user" />
            </n-form-item>
            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end">
                        <n-button
                            :disabled="targetRow.name === '' || targetRow.ip === '' || targetRow.user === ''"    
                            type="primary"
                            @click="submitCallbackEditar()"
                        >
                            Guardar
                        </n-button>
                    </div>
                </n-col>
            </n-row>       
        </n-form>   
    </n-modal>

    <!-- Modal for deleting -->
    <n-modal
        v-model:show="showModalDelete"
        preset="dialog"
        title="⚠️ Eliminar ⚠️"
        positive-text="SI"
        negative-text="NO"
        @positive-click="submitCallbackEliminar()"
        @negative-click="cancelCallbackEliminar()"
    > 
    ¿Borrar el host <strong>{{ targetRow.name }}</strong>?
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
    display: flex; /* Use flexbox */
    justify-content: space-between; /* Align items with space between them */
    align-items: center; /* Center items vertically */
    gap: 10px;
    }

    .table__container {
        width: 100%;
        /* padding-left: 20px;
        padding-right: 20px; */
    }

    .table {
        width: 100% ;
        background-color: black;
    }

    .button__div {
        display: flex;
        gap: 4px;
    }

</style>
