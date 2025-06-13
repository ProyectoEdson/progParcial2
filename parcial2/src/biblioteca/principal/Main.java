package biblioteca.principal;

import biblioteca.entidades.Libro;
import biblioteca.entidades.SolicitudLibro;
import biblioteca.servicios.BibliotecaService;
import biblioteca.servicios.ValidadorExistencias;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static BibliotecaService servicio;
    private static ValidadorExistencias validador;
    private static Path rutaSalida;
    private static Path rutaSolicitudes;
    private static Path rutaCompras;
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
        String userHome = System.getProperty("user.home");
        Path baseDirPath = Paths.get(userHome, "Parcial_2J2");
        Path bibliotecaPath = baseDirPath.resolve("Biblioteca");
        Path existenciaPath = baseDirPath.resolve("Existencia");
        Files.createDirectories(bibliotecaPath);
        Files.createDirectories(existenciaPath);

        rutaSolicitudes = bibliotecaPath.resolve("Solicitudes.txt");
        rutaCompras = existenciaPath.resolve("Compras.txt");
        rutaSalida = bibliotecaPath.resolve("Salida");

        // Asegurarse de que los archivos existen
        if (!Files.exists(rutaSolicitudes)) {
            Files.writeString(rutaSolicitudes, "ID;Título;Autor;Género;Fecha de publicación;Editorial;Precio");
        }
        if (!Files.exists(rutaCompras)) {
            Files.writeString(rutaCompras, "ID;Título;Autor;Género;Fecha de publicación;Editorial;Precio");
        }

        validador = new ValidadorExistencias();
        servicio = new BibliotecaService();
        scanner = new Scanner(System.in);

        // Lógica de inicialización cambiada: cargar libros desde Compras.txt
        List<Libro> librosComprados = validador.cargarCompras(rutaCompras);
        servicio.cargarLibrosDisponibles(librosComprados);

        System.out.println("\nSistema inicializado correctamente en: " + baseDirPath.toAbsolutePath());
    }

    private static void mostrarMenu() throws IOException {
        boolean salir = false;
        while (!salir) {
            System.out.println("\n===== SISTEMA DE BIBLIOTECA UNIVERSITARIA =====");
            System.out.println("1. Prestar libro");
            System.out.println("2. Devolver libro");
            System.out.println("3. Generar reporte de préstamos");
            System.out.println("4. Registrar nueva solicitud de libro");
            System.out.println("5. Realizar compra de solicitudes");
            System.out.println("6. Salir del sistema");
            System.out.print("\nSeleccione una opción: ");

            int opcion = 0;
            try {
                opcion = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debe ingresar un número válido.");
                continue;
            }

            switch (opcion) {
                case 1: prestarLibro(); break;
                case 2: devolverLibro(); break;
                case 3: generarReporte(); break;
                case 4: registrarSolicitud(); break;
                case 5: realizarSolicitudes(); break;
                case 6:
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
        List<Libro> librosDisponibles = servicio.getLibrosDisponibles();
        if (librosDisponibles.isEmpty()) {
            System.out.println("\nNo hay libros disponibles para prestar.");
            return;
        }

        System.out.println("\n--- Libros Disponibles para Préstamo ---");
        for (int i = 0; i < librosDisponibles.size(); i++) {
            Libro libro = librosDisponibles.get(i);
            System.out.printf("%d. %s (Autor: %s)\n", (i + 1), libro.getTitulo(), libro.getAutor());
        }
        System.out.println("----------------------------------------");

        int seleccion = -1;
        // Lógica de selección de libro... (sin cambios)
        System.out.print("Seleccione el número del libro que desea prestar: ");
        seleccion = Integer.parseInt(scanner.nextLine());

        System.out.print("Ingrese el ID del usuario (su nombre): ");
        String userId = scanner.nextLine().trim();

        Libro libroSeleccionado = librosDisponibles.get(seleccion - 1);
        servicio.prestarLibro(libroSeleccionado, userId);
    }

    private static void devolverLibro() {
        servicio.devolverLibro();
    }

    private static void generarReporte() throws IOException {
        servicio.generarReporteDePrestamos(rutaSalida);
    }

    private static void registrarSolicitud() throws IOException {
        System.out.println("\n--- Registrar Nueva Solicitud ---");
        try {
            System.out.print("ID del libro: ");
            int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Título: ");
            String titulo = scanner.nextLine();
            System.out.print("Autor: ");
            String autor = scanner.nextLine();
            System.out.print("Género: ");
            String genero = scanner.nextLine();
            System.out.print("Fecha de Publicación (YYYY-MM-DD): ");
            String fecha = scanner.nextLine();
            System.out.print("Editorial: ");
            String editorial = scanner.nextLine();
            System.out.print("Precio: ");
            BigDecimal precio = new BigDecimal(scanner.nextLine());

            SolicitudLibro nuevaSolicitud = new SolicitudLibro(id, titulo, autor, genero, fecha, editorial, precio);
            servicio.registrarNuevaSolicitud(nuevaSolicitud, rutaCompras, rutaSolicitudes);

        } catch (NumberFormatException e) {
            System.out.println("Error en el formato del número (ID o Precio). La solicitud ha sido cancelada.");
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error inesperado: " + e.getMessage());
        }
    }

    private static void realizarSolicitudes() throws IOException {
        servicio.realizarCompraDeSolicitudes(rutaSolicitudes, rutaCompras);
    }
}