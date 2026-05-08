package mx.unam.dgtic;

import mx.unam.dgtic.controller.AccountController;
import mx.unam.dgtic.controller.CategoryController;
import mx.unam.dgtic.controller.ServiceController;

import java.math.BigDecimal;

/*
Aplicacion principal del sistema SISPRO3D
Autor: Axel Fernando Montiel Aviles
 */
public class Main {
    /*
    En esta clase main se ejecutan las operaciones básicas CRUD con la
    arquitectura propuesta en el PDF. Se usá la entidad de dominio
    Service para el CRUD junto a un par de consultas de reglas de negocio
     */
    public static void main(String[] args) {
        AccountController accountController = new AccountController();
        ServiceController serviceController = new ServiceController();
        CategoryController categoryController = new CategoryController();

        accountController.displayAccount(2); // imprimir experto


        categoryController.displayAllCategories(); // imprimir todas las categorias

        Integer idCreado = serviceController.createNewServiceByExpertId(
                2,
                "Nuevo servicio",
                "Probando el controller",
                BigDecimal.valueOf(9999.999),
                1,
                2
        );

        System.out.println("idCreado = " + idCreado);
        serviceController.displayServiceById(idCreado);

        serviceController.displayAllServices();

        serviceController.updateServiceById(
                idCreado,
                2,
                "ACTUALIZADO",
                "Desc ACTUALIZADA",
                BigDecimal.valueOf(13.3),
                1,
                10
        );

        serviceController.displayServiceById(idCreado);

        // 2 consultas de negocio
        System.out.println("**** IMPRIMIENDO SERVICIOS DEL EXPERTO 2 ****");
        serviceController.displayAllServicesFromExpertId(2); // imprimir servicios del experto
        boolean resp = serviceController.serviceIsApprovedByAdmin(idCreado); // checar si un admin la aprobo
        System.out.println("El servicio " + idCreado + " fue aprobado? -> " + resp);

        // eliminar servicio
        serviceController.deleteServiceById(idCreado);
    }
}