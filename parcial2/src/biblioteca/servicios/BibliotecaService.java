package biblioteca.servicios;

import biblioteca.entidades.Libro;
import biblioteca.entidades.Prestamo;
import biblioteca.entidades.SolicitudLibro;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class BibliotecaService {
    private final Stack<Libro> pilaLibrosDisponibles = new Stack<>();
    private final Queue<Prestamo> colaPrestamosActivos = new LinkedList<>();
    private final List<Prestamo> historialDevoluciones = new ArrayList<>();

    public void cargarLibrosDisponibles(List<Libro> librosComprados) {
        pilaLibrosDisponibles.clear();
        for (Libro libro : librosComprados) {
            pilaLibrosDisponibles.push(libro);
        }
        System.out.printf("%nSe han cargado %d libros disponibles desde Compras.txt.%n", pilaLibrosDisponibles.size());
    }

    public void registrarNuevaSolicitud(SolicitudLibro solicitud, Path rutaCompras, Path rutaSolicitudes) throws IOException {
        ValidadorExistencias validador = new ValidadorExistencias();
        List<Libro> comprasActuales = validador.cargarCompras(rutaCompras);

        boolean existe = comprasActuales.stream()
                .anyMatch(libro -> libro.getId() == solicitud.getId() || libro.getTitulo().equalsIgnoreCase(solicitud.getTitulo()));

        if (existe) {
            System.out.println("\nERROR: No se puede solicitar el libro '" + solicitud.getTitulo() + "' porque ya existe en el catálogo de compras.");
            return;
        }

        // --- CORRECCIÓN AQUÍ: Usamos los getters correspondientes ---
        String nuevaLinea = String.format("%d;%s;%s;%s;%s;%s;%.2f",
                solicitud.getId(),
                solicitud.getTitulo(),
                solicitud.getAutor(),
                solicitud.getGenero(), // getter
                solicitud.getFechaPublicacion().format(DateTimeFormatter.ISO_LOCAL_DATE), // getter
                solicitud.getEditorial(), // getter
                solicitud.getPrecio()      // getter
        );

        // Añadir con salto de línea para evitar que se pegue al contenido existente
        Files.writeString(rutaSolicitudes, System.lineSeparator() + nuevaLinea, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        System.out.println("\nSOLICITUD REGISTRADA: El libro '" + solicitud.getTitulo() + "' ha sido añadido a la lista de solicitudes.");
    }

    public void realizarCompraDeSolicitudes(Path rutaSolicitudes, Path rutaCompras) throws IOException {
        ValidadorExistencias validador = new ValidadorExistencias();
        List<SolicitudLibro> solicitudes = validador.cargarSolicitudes(rutaSolicitudes);

        if (solicitudes.isEmpty()) {
            System.out.println("\nNo hay solicitudes pendientes para realizar.");
            return;
        }

        List<String> lineasParaEscribir = new ArrayList<>();
        for (SolicitudLibro solicitud : solicitudes) {
            // --- CORRECCIÓN AQUÍ: Usamos los getters correspondientes ---
            String nuevaLinea = String.format("%d;%s;%s;%s;%s;%s;%.2f",
                    solicitud.getId(),
                    solicitud.getTitulo(),
                    solicitud.getAutor(),
                    solicitud.getGenero(),
                    solicitud.getFechaPublicacion().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    solicitud.getEditorial(),
                    solicitud.getPrecio()
            );
            lineasParaEscribir.add(nuevaLinea);
            pilaLibrosDisponibles.push(solicitud);
        }

        // Añadir las líneas al archivo de compras
        Files.write(rutaCompras, lineasParaEscribir, StandardOpenOption.APPEND);

        // Limpiar el archivo de solicitudes (dejando solo el encabezado)
        Files.writeString(rutaSolicitudes, "ID;Título;Autor;Género;Fecha de publicación;Editorial;Precio", StandardOpenOption.TRUNCATE_EXISTING);

        System.out.printf("\nCOMPRA REALIZADA: Se han procesado y añadido %d libros nuevos al catálogo.", solicitudes.size());
        System.out.println("\nEstos libros ahora están disponibles para ser prestados.");
    }

    // --- El resto de los métodos (prestar, devolver, reporte) no necesitaban esta corrección y permanecen igual ---

    public List<Libro> getLibrosDisponibles() {
        return new ArrayList<>(pilaLibrosDisponibles);
    }

    public boolean prestarLibro(Libro libroSeleccionado, String usuarioId) {
        boolean removed = pilaLibrosDisponibles.remove(libroSeleccionado);
        if (removed) {
            Prestamo nuevoPrestamo = new Prestamo(libroSeleccionado, usuarioId);
            colaPrestamosActivos.add(nuevoPrestamo);
            System.out.printf("\nPRÉSTAMO: El libro '%s' ha sido prestado a %s.%n", libroSeleccionado.getTitulo(), usuarioId);
            return true;
        } else {
            System.out.println("\nError: El libro seleccionado ya no se encuentra disponible.");
            return false;
        }
    }

    public void devolverLibro() {
        if (colaPrestamosActivos.isEmpty()) {
            System.out.println("\nNo hay préstamos activos para devolver.");
            return;
        }
        Prestamo prestamo = colaPrestamosActivos.poll();
        prestamo.registrarDevolucion();
        historialDevoluciones.add(prestamo);

        Libro libroDevuelto = prestamo.getLibro();
        pilaLibrosDisponibles.push(libroDevuelto);

        System.out.printf("\nDEVOLUCIÓN: Se ha devuelto el libro '%s'.%n", prestamo.getLibro().getTitulo());
        System.out.printf("  - Días de retraso: %d%n", prestamo.getDiasRetraso());
        System.out.printf("  - Multa a pagar: $%.2f%n", prestamo.getMulta());
        System.out.printf("  - ¡El libro '%s' ahora está disponible nuevamente para préstamo!%n", libroDevuelto.getTitulo());
    }

    public void generarReporteDePrestamos(Path carpetaSalida) throws IOException {
        if (historialDevoluciones.isEmpty() && colaPrestamosActivos.isEmpty()) {
            System.out.println("\nNo hay préstamos (activos o devueltos) para generar un reporte.");
            return;
        }

        String fecha = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String nombreArchivo = String.format("Reporte_General_Prestamos_%s.csv", fecha);
        Path rutaReporte = carpetaSalida.resolve(nombreArchivo);

        Files.createDirectories(carpetaSalida);

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(rutaReporte, StandardCharsets.UTF_8))) {
            writer.println("ID Libro;Titulo;Autor;Usuario;Fecha Prestamo;Fecha Devolucion;Dias Retraso;Multa;Estado");

            for (Prestamo prestamo : historialDevoluciones) {
                writer.printf("%d;%s;%s;%s;%s;%s;%d;%.2f;Devuelto%n",
                        prestamo.getLibro().getId(), prestamo.getLibro().getTitulo(), prestamo.getLibro().getAutor(),
                        prestamo.getUsuarioId(), prestamo.getFechaPrestamo(), prestamo.getFechaDevolucion(),
                        prestamo.getDiasRetraso(), prestamo.getMulta());
            }

            for (Prestamo prestamo : colaPrestamosActivos) {
                writer.printf("%d;%s;%s;%s;%s;%s;%s;%.2f;%s%n",
                        prestamo.getLibro().getId(),
                        prestamo.getLibro().getTitulo(),
                        prestamo.getLibro().getAutor(),
                        prestamo.getUsuarioId(),
                        prestamo.getFechaPrestamo(),
                        "Pendiente",
                        "N/A",
                        0.00,
                        "Activo"
                );
            }
        }
        System.out.printf("\nREPORTE GENERADO: Se ha guardado el reporte general de préstamos en '%s'%n", rutaReporte.toAbsolutePath());
    }
}