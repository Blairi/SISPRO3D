package com.sispro3d.unam;

import com.sispro3d.unam.user.controller.AccountController;
import com.sispro3d.unam.user.domain.Role;
import com.sispro3d.unam.user.dto.AccountRequest;
import com.sispro3d.unam.user.dto.AccountResponse;

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
        AccountRequest newAccount = AccountRequest.builder()
                .name("Axel")
                .lastName("Nuevo")
                .email("axel@unam.mx")
                .phone("+52 55321233")
                .password("dummy@password")
                .role(Role.ADMIN)
                .build();
        System.out.println("Nueva cuenta creada: newAccount = " + newAccount);
        accountController.createAccount(newAccount);

        // c. Listar registros para verificar que existe un nuevo registro
        accountController.displayAllAccounts();

        // d. Editar algún registro
        List<AccountResponse> accounts = accountController.getAllAccounts();
        AccountResponse accountToEdit = accounts.getLast();
        System.out.println("Editando una cuenta accountToEdit = " + accountToEdit);
        AccountRequest updatedRequest = AccountRequest.builder()
                .name("NOBRE NUEVO EDITADO")
                .lastName(accountToEdit.getLastName())
                .email(accountToEdit.getEmail())
                .phone(accountToEdit.getPhone())
                .password(accountToEdit.getPassword())
                .role(accountToEdit.getRole())
                .build();
        accountController.updateAccount(accountToEdit.getIdUser(), updatedRequest);

        // e. Listar registros para verificar la edición
        accountController.displayAllAccounts();

        // f. Eliminar algún registro
        System.out.println("Eliminando accountToEdit = " + accountToEdit);
        accountController.deleteAccount(accountToEdit.getIdUser());

        // g. Listar registros para verificar la eliminación
        accountController.displayAllAccounts();
    }
}