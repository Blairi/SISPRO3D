package com.sispro3d.unam;

import com.sispro3d.unam.controller.AccountController;
import com.sispro3d.unam.domain.UserType;
import com.sispro3d.unam.dto.AccountDTO;

import java.time.LocalDateTime;
import java.util.List;

/*
Aplicacion principal del sistema SISPRO3D
Autor: Axel Fernando Montiel Aviles
 */

public class Main {
    /*
    3. Crear un programa en Java que acceda a la base de datos e implemente listado, alta, edición y
    eliminación de alguna de las entidades de su proyecto.
     */
    public static void main(String[] args) {
        // a. Listar registros de la entidad.
        AccountController accountController = new AccountController();
        accountController.displayAllAccounts();

        // b. Agregar un registro
        AccountDTO newAccount = new AccountDTO(
                0, "Axel",
                "Nuevo", "axel@unam.mx",
                "+52 55321233", "dummy@password",
                UserType.ADMIN, LocalDateTime.now()
        );
        System.out.println("Nueva cuenta creada: newAccount = " + newAccount);
        accountController.createAccount(newAccount);

        // c. Listar registros para verificar que existe un nuevo registro
        accountController.displayAllAccounts();

        // d. Editar algún registro
        List<AccountDTO> accounts = accountController.getAllAccounts();
        AccountDTO accountToEdit = accounts.getLast();
        System.out.println("Editando una cuenta accountToEdit = " + accountToEdit);
        accountToEdit.setName("NOBRE NUEVO EDITADO");
        accountController.updateAccount(accountToEdit.getIdUser(), accountToEdit);

        // e. Listar registros para verificar la edición
        accountController.displayAllAccounts();

        // f. Eliminar algún registro
        System.out.println("Eliminando accountToEdit = " + accountToEdit);
        accountController.deleteAccount(accountToEdit.getIdUser());

        // g. Listar registros para verificar la eliminación
        accountController.displayAllAccounts();
    }
}