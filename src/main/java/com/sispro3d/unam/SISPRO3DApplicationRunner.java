package com.sispro3d.unam;

import com.sispro3d.unam.user.controller.AccountController;
import com.sispro3d.unam.user.domain.UserType;
import com.sispro3d.unam.user.dto.AccountDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class SISPRO3DApplicationRunner implements CommandLineRunner {

    @Autowired
    private AccountController accountController;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("===== SISPRO3D & Spring Boot =====");

        accountController.displayAllAccounts();

        AccountDTO newAccount = new AccountDTO(
                0, "Axel",
                "Nuevo", "axel@unam.mx",
                "+52 55321233", "dummy@password",
                UserType.ADMIN, LocalDateTime.now()
        );
        accountController.createAccount(newAccount);
        accountController.displayAllAccounts();

        // Editar algún registro
        List<AccountDTO> accounts = accountController.getAllAccounts();
        AccountDTO accountToEdit = accounts.getLast();
        System.out.println("Editando una cuenta accountToEdit = " + accountToEdit);
        accountToEdit.setName("NOBRE NUEVO EDITADO");
        accountController.updateAccount(accountToEdit.getIdUser(), accountToEdit);

        // Listar registros para verificar la edición
        accountController.displayAllAccounts();

        // Eliminar algún registro
        System.out.println("Eliminando accountToEdit = " + accountToEdit);
        accountController.deleteAccount(accountToEdit.getIdUser());

        // Listar registros para verificar la eliminación
        accountController.displayAllAccounts();
    }
}
