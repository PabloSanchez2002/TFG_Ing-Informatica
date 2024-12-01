package com.tfg.backend;
import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.tfg.backend.SSHCredentials;
import com.tfg.backend.CRUD.entity.Despliegue;
import com.tfg.backend.BashCommandRunner;
import com.tfg.backend.CRUD.service.DespliegueService;



@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/")
public class tfgAPI {    
    @PostMapping("/testssh/password")
    public int testSSH(@RequestBody SSHCredentials credentials) {
        // return true if the password is correct, false otherwise
        int status = 1;
        try {
            status = BashCommandRunner.runCommand("check_password", credentials.getHostname(), credentials.getUsername(), credentials.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    @PostMapping("/testssh/key")
    public int testSSHKey(@RequestBody SSHCredentials credentials) {
        // return true if the key is correct, false otherwise
        int status = 1;

        try {
            status = BashCommandRunner.runCommand("check_key", credentials.getHostname(), credentials.getUsername(), credentials.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

}

   

