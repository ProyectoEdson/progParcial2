package biblioteca.principal;

import biblioteca.entidades.SolicitudLibro;
import biblioteca.servicios.BibliotecaService;
import biblioteca.servicios.ValidadorExistencias;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static BibliotecaService servicio;
    private static ValidadorExistencias validador;
    private static Path rutaSalida;
    private static Scanner scanner;

    public static void main(String[] args) {
        try {
            inicializarSistema();
            mostrarMenu();
        } catch (IOException e) {
            System.err.println("Error de E/S al leer o escribir archivos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static void inicializarSistema() throws IOException {
        // (Este método no necesita cambios)
        String userHome = System.getProperty("user.home");
        Path baseDirPath = Paths.get(userHome, "Parcial_2J2");
        Path bibliotecaPath = baseDirPath.resolve("Biblioteca");
        Path existenciaPath = baseDirPath.resolve("Existencia");
        Files.createDirectories(bibliotecaPath);
        Files.createDirectories(existenciaPath);
        Path rutaSolicitudes = bibliotecaPath.resolve("Solicitudes.txt");
        Path rutaCompras = existenciaPath.resolve("Compras.txt");
        rutaSalida = bibliotecaPath.resolve("Salida");
        if (!Files.exists(rutaSolicitudes)) {
            Files.writeString(rutaSolicitudes,
                    "ID;Título;Autor;Género;Fecha de publicación;Editorial;Precio\n" +
                            "1;El principito;Antoine de Saint-Exupéry;Literatura;2000-01-01;Salamandra;15.99\n" +
                            "2;Cien años de soledad;Gabriel García Márquez;Novela;1995-06-15;Sudamericana;24.50");
            System.out.println("Archivo de solicitudes creado con datos de ejemplo en: " + rutaSolicitudes);
        }
        if (!Files.exists(rutaCompras)) {
            Files.writeString(rutaCompras,
                    "ID;Título;Autor;Género;Fecha de publicación;Editorial;Precio\n" +
                            "101;Don Quijote;Miguel de Cervantes;Clásico;1990-03-20;Alfaguara;30.75\n" +
                            "102;Rayuela;Julio Cortázar;Novela;1985-11-10;Cátedra;18.25");
            System.out.println("Archivo de compras creado con datos de ejemplo en: " + rutaCompras);
        }
        validador = new ValidadorExistencias();
        servicio = new BibliotecaService();
        scanner = new Scanner(System.in);
        List<SolicitudLibro> solicitudes = validador.cargarSolicitudes(rutaSolicitudes);
        List<SolicitudLibro> solicitudesValidas = validador.obtenerSolicitudesValidas(solicitudes, validador.cargarCompras(rutaCompras));
        servicio.cargarSolicitudesEnPila(solicitudesValidas);
        System.out.println("\nSistema inicializado correctamente en: " + baseDirPath.toAbsolutePath());
    }

    private static void mostrarMenu() throws IOException {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n===== SISTEMA DE BIBLIOTECA UNIVERSITARIA =====");
            System.out.println("1. Prestar libro");
            System.out.println("2. Devolver libro");
            // Texto del menú actualizado para mayor claridad
            System.out.println("3. Generar reporte de préstamos (activos y devueltos)");
            System.out.println("4. Salir del sistema");
            System.out.print("\nSeleccione una opción: ");

            int opcion = 0;
            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número válido.");
                continue;
            }

            switch (opcion) {
                case 1:
                    prestarLibro();
                    break;
                case 2:
                    devolverLibro();
                    break;
                case 3:
                    generarReporte();
                    break;
                case 4:
                    salir = true;
                    System.out.println("Gracias por utilizar el Sistema de Biblioteca Universitaria.");
                    break;
                default:
                    System.out.println("Opción no válida. Por favor, intente nuevamente.");
                    break;
            }
        }
    }

    private static void prestarLibro() {
        // (Este método no necesita cambios)
        List<SolicitudLibro> solicitudesDisponibles = servicio.getSolicitudesDisponibles();
        if (solicitudesDisponibles.isEmpty()) {
            System.out.println("\nNo hay solicitudes de libros disponibles para prestar.");
            return;
        }
        System.out.println("\n--- Libros Disponibles para Préstamo ---");
        for (int i = 0; i < solicitudesDisponibles.size(); i++) {
            SolicitudLibro solicitud = solicitudesDisponibles.get(i);
            System.out.printf("%d. %s (Autor: %s)\n", (i + 1), solicitud.getTitulo(), solicitud.getAutor());
        }
        System.out.println("----------------------------------------");
        int seleccion = -1;
        while (true) {
            try {
                System.out.print("Seleccione el número del libro que desea prestar: ");
                seleccion = Integer.parseInt(scanner.nextLine());
                if (seleccion >= 1 && seleccion <= solicitudesDisponibles.size()) {
                    break;
                } else {
                    System.out.println("Error: Selección fuera de rango. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número válido.");
            }
        }
        System.out.print("Ingrese el ID del usuario (su nombre): ");
        String userId = scanner.nextLine().trim();
        if (userId.isEmpty()) {
            System.out.println("Error: Debe ingresar un ID de usuario válido.");
            return;
        }
        SolicitudLibro libroSeleccionado = solicitudesDisponibles.get(seleccion - 1);
        servicio.prestarLibro(libroSeleccionado, userId);
    }


    private static void devolverLibro() {
        servicio.devolverLibro();
    }

    private static void generarReporte() throws IOException {
        // Se llama al nuevo método con el nombre actualizado.
        servicio.generarReporteDePrestamos(rutaSalida);
    }
}