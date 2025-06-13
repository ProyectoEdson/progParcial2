package biblioteca.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Libro {
    protected int id;
    protected String titulo;
    protected String autor;
    protected String genero;
    protected LocalDate fechaPublicacion;
    protected String editorial;
    protected BigDecimal precio;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Libro(int id, String titulo, String autor, String genero, String fechaPublicacion, String editorial, BigDecimal precio) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.genero = genero;
        // Se mantiene el parseo de la fecha en el constructor
        this.fechaPublicacion = LocalDate.parse(fechaPublicacion, DATE_FORMATTER);
        this.editorial = editorial;
        this.precio = precio;
    }

    // --- GETTERS PARA TODAS LAS PROPIEDADES (AQUÍ LA CORRECCIÓN) ---
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getGenero() { return genero; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public String getEditorial() { return editorial; }
    public BigDecimal getPrecio() { return precio; }

    @Override
    public String toString() {
        return String.format("ID: %d, Título: %s, Autor: %s", id, titulo, autor);
    }
}