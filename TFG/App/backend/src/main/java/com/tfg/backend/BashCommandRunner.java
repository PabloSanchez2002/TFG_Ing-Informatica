package com.tfg.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import com.tfg.backend.global.utils.Tuple;
import com.tfg.backend.global.utils.DirectoryDeleter;




public class BashCommandRunner {     
    static String username = "gestordespliegues";
    static String passwd = "delonia123";
    static String keypath = new File("keys/").getAbsolutePath();
    // static String keypath = "./keys/";

    public static int runCommand(String command, String ip, String username, String password ) throws IOException, InterruptedException {
        String target = "./SetupServer.sh " + command + " " + username + " " + password + " " + ip ;
        int exitStatus = 0;
        try {
            System.out.println(target);
            ProcessBuilder pb = new ProcessBuilder(target.split(" "));
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            exitStatus = p.waitFor();
            p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return exitStatus;
    }

    public static Tuple<Integer, String> startDeployment(String ip, String user, String repo, String mode) throws IOException, InterruptedException {
        String repoUrl = "https://scm.delonia.com/scm-git/" + repo;
        File cloneDir = new File("./repos/" + repo);

        StringBuilder message = new StringBuilder("####### Deployment process #######\n----- Comprobación de acceso a la maquina -----\n");

        // Check if the host is accessible
        if (runCommand("check_key", ip, user, "") != 0) {
            message.append("Error: No se ha podido acceder a la máquina con la ip: " + ip + "\n");
            return new Tuple<>(1, message.toString()); // Error accessing the host
        }else {
            message.append("Acceso a la máquina con la ip: " + ip + " establecido correctamente.\n----- Comprobación de acceso al repositprio -----\n");
        }
        // Check if the repository is accessible with the provided credentials
        if (isRepoAccessible(repoUrl)) {
            // Clone the repository
            try {
                // check if cloneDir exists
                if (cloneDir.exists()) {
                    System.out.println("Deleting the existing directory");
                    if (DirectoryDeleter.deleteDirectory(cloneDir) == false) {
                        message.append("Failed to delete the old proyect directory:").append(cloneDir.getAbsolutePath());
                        return new Tuple<>(1, message.toString()); // Error deleting the existing directory
                    }
                }

                Git.cloneRepository()
                        .setURI(repoUrl)
                        .setDirectory(cloneDir)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, passwd))
                        .call();
                message.append("Repository cloned successfully! :) \n----- Deployment process started -----\n");
                // return startAnsible(message, repoUrl, cloneDir, mode, ip, user);

            } catch (GitAPIException e) {
                message.append("Failed to clone repository:").append(e.getMessage());
                return new Tuple<>(1, message.toString()); // Error during cloning
            }

            return startAnsible(message, repo, cloneDir, mode, ip, user);

        } else {
            message.append("Repository is not accessible with the provided credentials.");
            return new Tuple<>(2, message.toString()); // Repository not accessible
        }
        // Start the deployment process
    }

    private static boolean isRepoAccessible(String repoUrl) {
        try {
            Git.lsRemoteRepository()
                    .setHeads(true)
                    .setTags(true)
                    .setRemote(repoUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, passwd))
                    .call();
            return true; // Repository is accessible
        } catch (GitAPIException e) {
            return false; // Repository is not accessible
        }
    }

    public static Tuple<Integer, String> startAnsible(StringBuilder message, String repo, File cloneDir, String mode, String ip, String user) {
        try {
            String keypath = new File("./keys/" + user).toPath().toString();
            String setupYamlPath = new File(cloneDir, "ansible/Setup.yaml").toPath().toString();
            System.out.println("Constructing the ansible-playbook command");
            String FinalRepoUrl = "https://" + username + ":" + passwd + "@scm.delonia.com/scm-git/" + repo;
            System.out.println("Repo URL ----> " + FinalRepoUrl);

            String command = String.format(
                "./RunAnsible.sh %s %s %s %s %s %s",
                ip,
                keypath,
                user,
                mode,
                FinalRepoUrl,
                setupYamlPath
            );

            System.out.println(command);
            ProcessBuilder pb = new ProcessBuilder(command.split(" "));

            System.out.println("Executing the ansible-playbook command");
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                message.append(line).append("\n");
                System.out.println(line);
            }
            return new Tuple<>(p.waitFor(), message.toString());
        } catch (IOException e) {
            message.append("Error starting Ansible: ").append(e.getMessage());
            return new Tuple<>(1, message.toString());
        } catch (InterruptedException e) {
            message.append("Error starting Ansible: ").append(e.getMessage());
            return new Tuple<>(1, message.toString());
        }
    }   
}
