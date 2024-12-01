#!/bin/bash

# Function to setup target server
setup_server(){
    username=$1
    password=$2
    host=$3

    # Creamos el directorio keys
    mkdir -p ./keys

    # Generamos par de claves RSA 
    #  ssh-keygen -t rsa -b 2048 -f ./keys/username -q  only if it does not exist
    if [ ! -f ./keys/$username ]; then
        # ssh-keygen -t rsa -b 2048 -f ./keys/$username -q
        ssh-keygen -t rsa -b 2048 -f ./keys/$username -q -N ""
    fi


    # Copiamos la clave publica al servidor remoto
    #   sshpass -p $password ssh-copy-id -i /path/to/your/key_filename.pub username@remote_host
    sshpass -p $password ssh-copy-id -i ./keys/$username.pub $username@$host

    # Configyuramos el servidor remoto para que no pida contraseña
    #  ssh -i delonia delonia@192.168.222.130 'echo 'delonia' | sudo -S visudo -f /etc/sudoers.d/delonia_conf && echo "delonia ALL=(ALL) NOPASSWD: ALL" | sudo -S tee -a /etc/sudoers.d/delonia_conf'
    ssh -i ./keys/$username $username@$host 'echo '$password' | sudo -S visudo -f /etc/sudoers.d/'$username'_conf && echo "'$username' ALL=(ALL) NOPASSWD: ALL" | sudo -S tee -a /etc/sudoers.d/'$username'_conf'
    
    exit 0
}

# Function to check connection using password
check_password() {
    username=$1
    password=$2
    host=$3

    # echo all variables
    echo "Username: $username"
    echo "Password: $password"
    echo "Host: $host"

    sshpass -p $password ssh $username@$host "echo Connection successful using password authentication." >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "Connection successful using password authentication."
        exit 0
    else
        echo "Connection failed using password authentication."
        exit 1
    fi
}

# Function to check connection using key
check_key() {
    username=$1
    host=$2
    key_file="./keys/$username"

    ssh -o BatchMode=yes -i "$key_file" "$username@$host" "echo Connection successful using key authentication." >/dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "Connection successful using key authentication."
        exit 0
    else
        echo "Connection failed using key authentication."
        exit 1
    fi
}

# Main script

if [ $# -ne 4 ]; then
    echo "Usage: $0 <function> <host> <username> <password>"
    exit 1
fi

funct=$1
username=$2
password=$3
host=$4

# Function to execute is in the $1 variable, can be setup_server, check_password or check_key
if [ "$funct" == "setup_server" ]; then
    setup_server "$username" "$password" "$host"
elif [ "$funct" == "check_password" ]; then
    check_password "$username" "$password" "$host"
elif [ "$funct" == "check_key" ]; then
    check_key "$username" "$host"
else
    echo "Invalid function. Use setup_server, check_password, or check_key."
    exit 1
fi
