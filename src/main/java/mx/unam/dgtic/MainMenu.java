package mx.unam.dgtic;

import mx.unam.dgtic.controller.AccountController;
import mx.unam.dgtic.controller.CategoryController;
import mx.unam.dgtic.controller.ServiceController;

import java.math.BigDecimal;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static AccountController accountController = new AccountController();
    static ServiceController serviceController = new ServiceController();
    static CategoryController categoryController = new CategoryController();

    public static void main(String[] args) {
        // info util para usar el menu...
        accountController.displayAccount(2);
        categoryController.displayAllCategories();

        int opcion;
        do {
            System.out.println("\n===== SISPRO3D - Servicios =====");
            System.out.println("1. Crear servicio");
            System.out.println("2. Ver servicio por ID");
            System.out.println("3. Ver todos los servicios");
            System.out.println("4. Actualizar servicio");
            System.out.println("5. Eliminar servicio");
            System.out.println("6. Ver servicios por experto");
            System.out.println("7. ¿Servicio aprobado por admin?");
            System.out.println("0. Salir");
            System.out.print("Opción: ");
            opcion = scanner.nextInt();

            switch (opcion) {
                case 1 -> crearServicio();
                case 2 -> {
                    System.out.print("ID servicio: ");
                    serviceController.displayServiceById(scanner.nextInt());
                }
                case 3 -> serviceController.displayAllServices();
                case 4 -> actualizarServicio();
                case 5 -> {
                    System.out.print("ID servicio a eliminar: ");
                    serviceController.deleteServiceById(scanner.nextInt());
                    System.out.println("Servicio eliminado.");
                }
                case 6 -> {
                    System.out.print("ID experto: ");
                    serviceController.displayAllServicesFromExpertId(scanner.nextInt());
                }
                case 7 -> {
                    System.out.print("ID servicio: ");
                    int id = scanner.nextInt();
                    boolean aprobado = serviceController.serviceIsApprovedByAdmin(id);
                    System.out.println("El servicio " + id + " fue aprobado? -> " + aprobado);
                }
                case 0 -> System.out.println("Hasta luego!");
                default -> System.out.println("Opción inválida");
            }
        } while (opcion != 0);
    }

    static void crearServicio() {
        scanner.nextLine();
        System.out.print("ID experto: ");       int expertId = scanner.nextInt();  scanner.nextLine();
        System.out.print("Nombre: ");           String nombre = scanner.nextLine();
        System.out.print("Descripción: ");      String desc = scanner.nextLine();
        System.out.print("Precio: ");           BigDecimal precio = scanner.nextBigDecimal(); scanner.nextLine();
        System.out.print("ID categoría: ");     int catId = scanner.nextInt(); scanner.nextLine();
        System.out.print("Dias de entrega: ");  int delivDays = scanner.nextInt(); scanner.nextLine();

        Integer idCreado = serviceController.createNewServiceByExpertId(
                expertId, nombre, desc, precio, catId, delivDays
        );
        System.out.println("idCreado = " + idCreado);
        serviceController.displayServiceById(idCreado);
    }

    static void actualizarServicio() {
        scanner.nextLine();
        System.out.print("ID servicio: ");      int id = scanner.nextInt();        scanner.nextLine();
        System.out.print("ID experto: ");       int expertId = scanner.nextInt();  scanner.nextLine();
        System.out.print("Nombre: ");           String nombre = scanner.nextLine();
        System.out.print("Descripción: ");      String desc = scanner.nextLine();
        System.out.print("Precio: ");           BigDecimal precio = scanner.nextBigDecimal(); scanner.nextLine();
        System.out.print("ID categoría: ");     int catId = scanner.nextInt();     scanner.nextLine();
        System.out.print("Dias de entrega: ");  int delivDays = scanner.nextInt(); scanner.nextLine();

        serviceController.updateServiceById(id, expertId, nombre, desc, precio, catId, delivDays);
        serviceController.displayServiceById(id);
    }
}