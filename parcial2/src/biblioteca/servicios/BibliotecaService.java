package biblioteca.servicios;

import biblioteca.entidades.Prestamo;
import biblioteca.entidades.SolicitudLibro;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class BibliotecaService {
    private final Stack<SolicitudLibro> pilaSolicitudes = new Stack<>();
    private final Queue<Prestamo> colaPrestamosActivos = new LinkedList<>();
    private final List<Prestamo> historialDevoluciones = new ArrayList<>();

    public void cargarSolicitudesEnPila(List<SolicitudLibro> solicitudes) {
        for (SolicitudLibro solicitud : solicitudes) {
            pilaSolicitudes.push(solicitud);
        }
        System.out.printf("%nSe han cargado %d solicitudes válidas en la pila.%n", pilaSolicitudes.size());
    }

    public List<SolicitudLibro> getSolicitudesDisponibles() {
        return new ArrayList<>(pilaSolicitudes);
    }

    public boolean prestarLibro(SolicitudLibro solicitudSeleccionada, String usuarioId) {
        boolean removed = pilaSolicitudes.remove(solicitudSeleccionada);

        if (removed) {
            Prestamo nuevoPrestamo = new Prestamo(solicitudSeleccionada, usuarioId);
            colaPrestamosActivos.add(nuevoPrestamo);
            System.out.printf("\nPRÉSTAMO: El libro '%s' ha sido prestado a %s.%n", solicitudSeleccionada.getTitulo(), usuarioId);
            return true;
        } else {
            System.out.println("\nError: El libro seleccionado ya no se encuentra disponible.");
            return false;
        }
    }

    /**
     * Procesa la devolución de un libro.
     * El libro vuelve a estar disponible en la pila de solicitudes.
     */
    public void devolverLibro() {
        if (colaPrestamosActivos.isEmpty()) {
            System.out.println("\nNo hay préstamos activos para devolver.");
            return;
        }
        Prestamo prestamo = colaPrestamosActivos.poll();
        prestamo.registrarDevolucion();
        historialDevoluciones.add(prestamo);

        // --- MEJORA 1: Devolver el libro a la pila de disponibles ---
        // Se extrae el libro original del préstamo.
        SolicitudLibro libroDevuelto = (SolicitudLibro) prestamo.getLibro();
        // Se añade de nuevo a la pila para que pueda ser prestado otra vez.
        pilaSolicitudes.push(libroDevuelto);
        // --- Fin de la mejora ---

        System.out.printf("\nDEVOLUCIÓN: Se ha devuelto el libro '%s'.%n", prestamo.getLibro().getTitulo());
        System.out.printf("  - Días de retraso: %d%n", prestamo.getDiasRetraso());
        System.out.printf("  - Multa a pagar: $%.2f%n", prestamo.getMulta());
        System.out.printf("  - ¡El libro '%s' ahora está disponible nuevamente para préstamo!%n", libroDevuelto.getTitulo());
    }

    /**
     * Genera un reporte CSV que incluye tanto los préstamos devueltos como los activos.
     * @param carpetaSalida La ruta donde se guardará el reporte.
     * @throws IOException Si ocurre un error de escritura.
     */
    public void generarReporteDePrestamos(Path carpetaSalida) throws IOException {
        if (historialDevoluciones.isEmpty() && colaPrestamosActivos.isEmpty()) {
            System.out.println("\nNo hay préstamos (activos o devueltos) para generar un reporte.");
            return;
        }

        // El nombre del archivo ahora es más descriptivo.
        String fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String nombreArchivo = String.format("Reporte_General_Prestamos_%s.csv", fecha);
        Path rutaReporte = carpetaSalida.resolve(nombreArchivo);

        Files.createDirectories(carpetaSalida);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(rutaReporte, StandardCharsets.UTF_8))) {
            // Se añade una columna "Estado" para mayor claridad.
            writer.println("ID Libro;Titulo;Autor;Usuario;Fecha Prestamo;Fecha Devolucion;Dias Retraso;Multa;Estado");

            // --- MEJORA 2: Escribir ambos tipos de préstamos en el reporte ---
            // 1. Escribir los libros ya devueltos del historial.
            for (Prestamo prestamo : historialDevoluciones) {
                writer.printf("%d;%s;%s;%s;%s;%s;%d;%.2f;Devuelto%n",
                        prestamo.getLibro().getId(),
                        prestamo.getLibro().getTitulo(),
                        prestamo.getLibro().getAutor(),
                        prestamo.getUsuarioId(),
                        prestamo.getFechaPrestamo(),
                        prestamo.getFechaDevolucion(),
                        prestamo.getDiasRetraso(),
                        prestamo.getMulta()
                );
            }

            // 2. Escribir los préstamos que siguen activos.
            for (Prestamo prestamo : colaPrestamosActivos) {
                writer.printf("%d;%s;%s;%s;%s;%s;%s;%s;Activo%n",
                        prestamo.getLibro().getId(),
                        prestamo.getLibro().getTitulo(),
                        prestamo.getLibro().getAutor(),
                        prestamo.getUsuarioId(),
                        prestamo.getFechaPrestamo(),
                        "Pendiente",  // Fecha de devolución aún no existe.
                        "N/A",        // Días de retraso no aplican.
                        "0.00",       // Multa no aplica.
                        "Activo"      // Estado del préstamo.
                );
            }
            // --- Fin de la mejora ---
        }
        System.out.printf("\nREPORTE GENERADO: Se ha guardado el reporte general de préstamos en '%s'%n", rutaReporte.toAbsolutePath());
    }
}