package biblioteca.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Prestamo {
    private final Libro libro;
    private final String usuarioId;
    private final LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;

    // Constantes para el cálculo de la multa
    private static final int DIAS_PRESTAMO_ESTANDAR = 15;
    private static final BigDecimal MULTA_POR_DIA = new BigDecimal("0.75");

    public Prestamo(Libro libro, String usuarioId) {
        this.libro = libro;
        this.usuarioId = usuarioId;
        this.fechaPrestamo = LocalDate.now();
    }

    public void registrarDevolucion() {
        this.fechaDevolucion = LocalDate.now();
    }

    public long getDiasRetraso() {
        if (fechaDevolucion == null) return 0; // Aún no se ha devuelto

        long diasTranscurridos = ChronoUnit.DAYS.between(fechaPrestamo, fechaDevolucion);
        long diasRetraso = diasTranscurridos - DIAS_PRESTAMO_ESTANDAR;

        return Math.max(0, diasRetraso); // Retorna 0 si no hay retraso
    }

    public BigDecimal getMulta() {
        long diasRetraso = getDiasRetraso();
        return MULTA_POR_DIA.multiply(new BigDecimal(diasRetraso));
    }

    // Getters para el reporte
    public Libro getLibro() { return libro; }
    public String getUsuarioId() { return usuarioId; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
}