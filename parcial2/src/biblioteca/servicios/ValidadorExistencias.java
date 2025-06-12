package biblioteca.servicios;

import biblioteca.entidades.Libro;
import biblioteca.entidades.SolicitudLibro;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidadorExistencias {

    // Método genérico para leer libros de un archivo (Compras o Solicitudes)
    private List<Libro> leerLibrosDesdeArchivo(Path rutaArchivo, String delimitador) throws IOException {
        List<Libro> libros = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(rutaArchivo)) {
            String linea;
            // Omitir encabezados si los hubiera
            reader.readLine();
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(delimitador);
                if (datos.length >= 7) {
                    try {
                        libros.add(new Libro(
                                Integer.parseInt(datos[0].trim()),
                                datos[1].trim(),
                                datos[2].trim(),
                                datos[3].trim(),
                                datos[4].trim(),
                                datos[5].trim(),
                                new BigDecimal(datos[6].trim())
                        ));
                    } catch (Exception e) {
                        System.err.println("Error al procesar línea (se omite): " + linea);
                    }
                }
            }
        }
        return libros;
    }

    public List<SolicitudLibro> cargarSolicitudes(Path rutaArchivo) throws IOException {
        List<SolicitudLibro> solicitudes = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(rutaArchivo)) {
            String linea;

            // LÍNEA AÑADIDA PARA SOLUCIONAR EL ERROR
            // Esto lee y descarta la primera línea (el encabezado).
            reader.readLine();

            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length >= 7) {
                    try {
                        solicitudes.add(new SolicitudLibro(
                                Integer.parseInt(datos[0].trim()),
                                datos[1].trim(),
                                datos[2].trim(),
                                datos[3].trim(),
                                datos[4].trim(),
                                datos[5].trim(),
                                new BigDecimal(datos[6].trim())
                        ));
                    } catch (Exception e) {
                        System.err.println("Error al procesar línea de solicitud (se omite): " + linea);
                    }
                }
            }
        }
        return solicitudes;
    }

    public List<Libro> cargarCompras(Path rutaArchivo) throws IOException {
        // Reutiliza el método genérico de lectura
        return leerLibrosDesdeArchivo(rutaArchivo, ";");
    }

    public List<SolicitudLibro> obtenerSolicitudesValidas(List<SolicitudLibro> solicitudes, List<Libro> compras) {
        // Usamos Sets para una búsqueda eficiente de duplicados (O(1) en promedio)
        Set<Integer> idsExistentes = compras.stream().map(Libro::getId).collect(Collectors.toSet());
        Set<String> titulosExistentes = compras.stream().map(libro -> libro.getTitulo().toLowerCase()).collect(Collectors.toSet());

        List<SolicitudLibro> solicitudesValidas = new ArrayList<>();
        System.out.println("\n--- Validación de Solicitudes ---");
        for (SolicitudLibro solicitud : solicitudes) {
            if (idsExistentes.contains(solicitud.getId()) || titulosExistentes.contains(solicitud.getTitulo().toLowerCase())) {
                System.out.printf("DUPLICADO: La solicitud '%s' (ID: %d) ya existe en el catálogo de compras.%n", solicitud.getTitulo(), solicitud.getId());
            } else {
                System.out.printf("VÁLIDA: La solicitud '%s' (ID: %d) es nueva.%n", solicitud.getTitulo(), solicitud.getId());
                solicitudesValidas.add(solicitud);
            }
        }
        System.out.println("---------------------------------");
        return solicitudesValidas;
    }
}