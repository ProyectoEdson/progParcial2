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
        // Push items directly to the stack for true LIFO behavior
        for (SolicitudLibro solicitud : solicitudes) {
            pilaSolicitudes.push(solicitud);
        }
        System.out.printf("%nSe han cargado %d solicitudes válidas en la pila.%n", pilaSolicitudes.size());
    }

    public void prestarLibro(String usuarioId) {
        if (pilaSolicitudes.isEmpty()) {
            System.out.println("\nNo hay solicitudes de libros para prestar.");
            return;
        }
        SolicitudLibro solicitud = pilaSolicitudes.pop();
        Prestamo nuevoPrestamo = new Prestamo(solicitud, usuarioId);
        colaPrestamosActivos.add(nuevoPrestamo);
        System.out.printf("\nPRÉSTAMO: El libro '%s' ha sido prestado a %s.%n", solicitud.getTitulo(), usuarioId);
    }

    public void devolverLibro() {
        if (colaPrestamosActivos.isEmpty()) {
            System.out.println("\nNo hay préstamos activos para devolver.");
            return;
        }
        Prestamo prestamo = colaPrestamosActivos.poll();
        prestamo.registrarDevolucion(); // Simula la devolución en la fecha actual
        historialDevoluciones.add(prestamo);

        System.out.printf("\nDEVOLUCIÓN: Se ha devuelto el libro '%s'.%n", prestamo.getLibro().getTitulo());
        System.out.printf("  - Días de retraso: %d%n", prestamo.getDiasRetraso());
        System.out.printf("  - Multa a pagar: $%.2f%n", prestamo.getMulta());
    }

    public void generarReporteDevoluciones(Path carpetaSalida) throws IOException {
        if (historialDevoluciones.isEmpty()) {
            System.out.println("\nNo hay devoluciones para generar un reporte.");
            return;
        }

        // Crear el nombre del archivo con la fecha actual
        String fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String nombreArchivo = String.format("Reporte_devoluciones_%s.csv", fecha);
        Path rutaReporte = carpetaSalida.resolve(nombreArchivo);

        // Asegurarse de que el directorio de salida exista
        Files.createDirectories(carpetaSalida);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(rutaReporte, StandardCharsets.UTF_8))) {
            // Escribir encabezado del CSV
            writer.println("ID Libro;Titulo;Autor;Usuario;Fecha Prestamo;Fecha Devolucion;Dias Retraso;Multa");

            // Escribir cada registro de devolución
            for (Prestamo prestamo : historialDevoluciones) {
                writer.printf("%d;%s;%s;%s;%s;%s;%d;%.2f%n",
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
        }
        System.out.printf("\nREPORTE GENERADO: Se ha guardado el reporte en '%s'%n", rutaReporte.toAbsolutePath());
    }
}