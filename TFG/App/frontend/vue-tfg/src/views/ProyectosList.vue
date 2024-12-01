<script setup lang="ts">
    import Button from '#components/Button.vue';
    import KeyCloakService from '#security/KeycloakService';
    import { ref, onMounted, h } from 'vue';
    import axios from 'axios';
    import { NButton, NTag, useMessage } from 'naive-ui';
    import ButtonWarning from '#components/ButtonWarning.vue';
    import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
    import { faArrowsRotate } from "@fortawesome/free-solid-svg-icons";

    const roles = ref();
    const isAdmin = ref();
    const projects = ref();
    const columns = [
        {
            title: 'ID',
            key: 'id',
            label: 'ID',
            sortable: true,
            minWidth: "48px"
        },
        {
            title: 'Nombre',
            key: 'name',
            label: 'Nombre',
            sortable: true,
            resizable: true,
            minWidth: "110px"
        },
        {
            title: 'Descripción',
            key: 'description',
            label: 'Descripción',
            sortable: true,
            resizable: true,
            minWidth: "110px"
        },
        {
            title: 'Repositorio',
            key: 'repository',
            label: 'Repositorio de scripts',
            sortable: true,
            resizable: true,
            minWidth: "110px",

            // render 'https://scm.delonia.com/scm-git/' to the beginning of the repository but its not a ref nor a link
            render(row: any) {
                return h('div',{style: {display: "flex", gap: "8px"}}, 'https://scm.delonia.com/scm-git/' + row.repository
                );
            }


        },
        {
            title: 'Tipo de despliegues',
            key: 'types',
            label: 'Tipos de despliegue',
            resizable: true,
            minWidth: "110px",
            render (row: { types: any[]; }) {
                const types = row.types.map((typesKey) => {
                    return h(
                        NTag,
                        {type: 'info', bordered: false},
                        {default: () => typesKey}
                    )
                })
                return h('div',{style: {display: "flex", flexWrap: "wrap", gap: "8px"}}, types)
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


    // Data for the form
    const createFormModel = ref({
    name: null,
    description: null,
    repository: null,
    types: []
    });

    // Rules for form validation
    const createFormRules = ref({
    name: [{ required: true, message: 'Nombre es requerido', trigger: 'blur' }],
    description: [{ required: true, message: 'Descripción es requerida', trigger: 'blur' }],
    repository: [{ required: true, message: 'Repositorio es requerido', trigger: 'blur' }],
    types: [{ required: true, message: 'Tipos de despliegue es requerido', trigger: 'blur' }]
    });


    // Function to create project
    function createProyect() {
        console.log("Create project");
        showModalCreate.value = true;
    }

    // Function to edit project
    function editProyect(row: any) {
        console.log("Edit project: ", {...row});
        targetRow.value = {...row};
        showModalDetails.value = true;
    }

    // Function to delete project
    function deleteProyect(row: any) {
        console.log("Delete project: ", {...row});
        targetRow.value = {...row};
        showModalDelete.value = true;
    }




    // Function to handle submit action for creating project
    function submitCallbackCrear() {
    console.log("Form submitted:", createFormModel.value);
    try {
        axios.post('http://127.0.0.1:9090/proyecto', createFormModel.value)
            .then(response => {
                console.log(response.data);
                // Reload projects after successful creation
                reloadProjects();
                message.success('Proyecto creado');
                showModalCreate.value = false;
                // clear modal form
                createFormModel.value = {
                    name: null,
                    description: null,
                    repository: null,
                    types: []
                };
            })
            .catch(error => {
                const errorMessage = error.response.data.message;
                message.error('Error: ' + errorMessage);
            })
            .finally(() => {
                
            });
    } catch (error) {
        console.error("Error creating project:", error);
        }
    }

    // Function to handle submit action for editing project
    function submitCallbackEdit() {
        console.log("Form submitted:", targetRow.value);
        try {
            axios.put('http://127.0.0.1:9090/proyecto/' + targetRow.value.id, targetRow.value)
                .then(response => {
                    console.log(response.data);
                    // Reload projects after successful creation
                    reloadProjects();
                    message.success('Proyecto actualizado');
                    showModalDetails.value = false;
                    createFormModel.value = {
                        name: null,
                        description: null,
                        repository: null,
                        types: []
                    };
                })
                .catch(error => {
                    const errorMessage = error.response.data.message;
                    message.error('Error: ' + errorMessage);
                })
                .finally(() => {
                    
                });
        } catch (error) {
            console.error("Error updating project:", error);
        }
    }


    // Function to reload the projects
    async function reloadProjects() {
        try {
            const response = await axios.get('http://127.0.0.1:9090/proyecto');
            projects.value = response.data;
            message.success('Proyectos recargados');
            console.log("Projects: ", projects.value);
        } catch (error) {
            message.error('Error al recargar proyectos');
            console.error("Error reloading projects:", error);
        }
    }



    // Function to handle cancel action
    function cancelCallbackEliminar() {
        console.log("Cancelado");
        showModalDelete.value = false;
        createFormModel.value = {
            name: null,
            description: null,
            repository: null,
            types: []
        };
    }

    // Function to handle submit action for deleting project
    function submitCallbackEliminar() {
        console.log("Eliminado");
        console.log('http://127.0.0.1:9090/proyecto/' + targetRow.value.id);
        axios.delete('http://127.0.0.1:9090/proyecto/' + targetRow.value.id)
            .then(response => {
                console.log(response.data);
                // console.log('http://127.0.0.1:9090/proyecto/' + targetRow.value.id);
                // Reload projects after successful deletion
                reloadProjects();
                message.success('Proyecto eliminado');
            })
            .catch(error => {
                const errorMessage = error.response.data.message;
                if (error.response && error.response.status === 404) {
                     // Show Naive UI alert for 404 error
                    message.error('Error 404: ' + errorMessage);
                } else {
                    message.error('Error: ' + errorMessage);
                }
            })
            .finally(() => {
                showModalDelete.value = false;
            });
    }

    onMounted(async () => {
        // Fetch projects on component mount
        try {
            roles.value = await KeyCloakService.getUserRoles();
            console.log("Roles: ", roles.value);
            isAdmin.value = roles.value.includes("AdministrarProyectos");
            reloadProjects();
        } catch (error) {
            console.error("Error mounting component:", error);
        }
    });
</script>

<template>

    <div className="container">
        <div v-if="isAdmin">
            <Button @click="createProyect()">
                <strong>Crear nuevo proyecto</strong>
            </Button>
        </div>
        <div v-else>
            <h1>No tienes permisos para administrar proyectos</h1>
        </div>
        <div class="menu-div">
            <div>        
                <h1>Proyectos disponibles</h1>
            </div>
            <div>    
                <n-space>
                    <n-button  @click="reloadProjects()">
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
            :data= "projects"
            :bordered="true"
            :stripe="true"
        />
        </n-space>
    </div>

    <!-- Modal for creating -->
    <n-modal
    v-model:show="showModalCreate"
    preset="dialog"
    title="Crear proyecto"
    >
        <n-form :model="createFormModel" :rules="createFormRules" ref="createFormRef">
            <n-form-item label="Nombre" path="name" rules="required">
                <n-input v-model:value="createFormModel.name" />
            </n-form-item>
            <n-form-item label="Descripción" path="description" rules="required">
                <n-input v-model:value="createFormModel.description" />
            </n-form-item>
            <n-form-item label="Repositorio" path="repository" rules="required" :feedback="'https://scm.delonia.com/scm-git/' + createFormModel.repository" >
                <n-input v-model:value="createFormModel.repository"/>
            </n-form-item>
            <br>
            <n-form-item label="Tipo de despliegue" path="types" rules="required">
                <n-dynamic-tags v-model:value="createFormModel.types" />
            </n-form-item>
            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end">
                        <n-button
                            :disabled="createFormModel.name === '' || createFormModel.description === '' || createFormModel.repository === '' || createFormModel.types.length < 1"
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
        title="Detalles del proyecto"   
    >
        <n-form :model="targetRow" :rules="createFormRules" ref="detailsFormRef">
            <n-form-item label="Nombre" path="name">
                <n-input v-model:value="targetRow.name" />
            </n-form-item>
            <n-form-item label="Descripción" path="description">
                <n-input v-model:value="targetRow.description" />
            </n-form-item>
            <n-form-item label="Repositorio" path="repository" rules="required" :feedback="'https://scm.delonia.com/scm-git/' + targetRow.repository" >
                <n-input v-model:value="targetRow.repository"/>
            </n-form-item>
            <n-form-item label="Tipo de despliegue" path="types">
                <n-dynamic-tags v-model:value="targetRow.types" />
            </n-form-item>
            <n-row :gutter="[0, 24]">
                <n-col :span="24">
                    <div style="display: flex; justify-content: flex-end">
                        <n-button
                            :disabled="targetRow.name === '' || targetRow.description === '' || targetRow.repository === '' || targetRow.types.length < 1"    
                            type="primary"
                            @click="submitCallbackEdit()"
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
    ¿Borrar el proyecto <strong>{{ targetRow.name }}</strong>?
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