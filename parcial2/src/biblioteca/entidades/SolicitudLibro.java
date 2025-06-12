package biblioteca.entidades;

import java.math.BigDecimal;

public class SolicitudLibro extends Libro {
    public SolicitudLibro(int id, String titulo, String autor, String genero, String fechaPublicacion, String editorial, BigDecimal precio) {
        super(id, titulo, autor, genero, fechaPublicacion, editorial, precio);
    }
}