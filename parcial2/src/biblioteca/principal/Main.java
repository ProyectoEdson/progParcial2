package biblioteca.principal;

import biblioteca.entidades.Libro;
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
        // Usar directorio home del usuario como base
        String userHome = System.getProperty("user.home");
        Path baseDirPath = Paths.get(userHome, "Parcial_2J2");

        // Crear estructura de directorios si no existe
        Path bibliotecaPath = baseDirPath.resolve("Biblioteca");
        Path existenciaPath = baseDirPath.resolve("Existencia");

        Files.createDirectories(bibliotecaPath);
        Files.createDirectories(existenciaPath);

        // Definir rutas de archivos
        Path rutaSolicitudes = bibliotecaPath.resolve("Solicitudes.txt");
        Path rutaCompras = existenciaPath.resolve("Compras.txt");
        rutaSalida = bibliotecaPath.resolve("Salida");

        // Crear archivos vacíos si no existen
        if (!Files.exists(rutaSolicitudes)) {
            Files.writeString(rutaSolicitudes,
                    "1;El principito;Antoine de Saint-Exupéry;Literatura;2000-01-01;Salamandra;15.99\n" +
                            "2;Cien años de soledad;Gabriel García Márquez;Novela;1995-06-15;Sudamericana;24.50");
            System.out.println("Archivo de solicitudes creado con datos de ejemplo en: " + rutaSolicitudes);
        }

        if (!Files.exists(rutaCompras)) {
            Files.writeString(rutaCompras,
                    "101;Don Quijote;Miguel de Cervantes;Clásico;1990-03-20;Alfaguara;30.75\n" +
                            "102;Rayuela;Julio Cortázar;Novela;1985-11-10;Cátedra;18.25");
            System.out.println("Archivo de compras creado con datos de ejemplo en: " + rutaCompras);
        }

        // Inicialización de servicios
        validador = new ValidadorExistencias();
        servicio = new BibliotecaService();
        scanner = new Scanner(System.in);

        // Carga inicial de datos
        List<Libro> compras = validador.cargarCompras(rutaCompras);
        List<SolicitudLibro> solicitudes = validador.cargarSolicitudes(rutaSolicitudes);
        List<SolicitudLibro> solicitudesValidas = validador.obtenerSolicitudesValidas(solicitudes, compras);

        // Cargar solicitudes válidas en la pila
        servicio.cargarSolicitudesEnPila(solicitudesValidas);
        System.out.println("\nSistema inicializado correctamente en: " + baseDirPath.toAbsolutePath());
    }

    private static void mostrarMenu() throws IOException {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n===== SISTEMA DE BIBLIOTECA UNIVERSITARIA =====");
            System.out.println("1. Prestar libro");
            System.out.println("2. Devolver libro");
            System.out.println("3. Generar reporte de devoluciones");
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
        System.out.print("Ingrese el ID del usuario: ");
        String userId = scanner.nextLine().trim();

        if (userId.isEmpty()) {
            System.out.println("Error: Debe ingresar un ID de usuario válido.");
            return;
        }

        servicio.prestarLibro(userId);
    }

    private static void devolverLibro() {
        servicio.devolverLibro();
    }

    private static void generarReporte() throws IOException {
        servicio.generarReporteDevoluciones(rutaSalida);
    }
}