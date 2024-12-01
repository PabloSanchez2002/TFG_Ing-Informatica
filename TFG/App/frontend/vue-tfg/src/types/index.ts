type Despliegue = {
    id: number;
    name: string;
    group: string;
    hostId: string;
    description: string;
    projectId: string;
    devState: string;
    deployType: string;
    deployDate: string;
    deployedBy: string;
    deployStatus: string;
    deployLog: string;
};

type Host = {
    label: string;
    value: number;
};

type Project = {
    label: string;
    value: number;
};

type ProjectType = {
    label: string;
    value: string;
};

type Group = {
    label: string;
    value: number;
};

export type { Despliegue, Host, Project, ProjectType, Group };