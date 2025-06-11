package sistema;

public class solicitudLibro {
    private String id; // ID del libro solicitado
    private String titulo; // Título del libro solicitado
    private String estudiante; // Estudiante que solicita el libro

    // Constructor
    public solicitudLibro(String id, String titulo, String autor) {
        this.id = id;
        this.titulo = titulo;
        this.estudiante = autor;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }


    public String getAutor() {
        return estudiante;
    }

    @Override
    public String toString() {
        return "SolicitudLibro [ID=" + id + ", Titulo=" + titulo + ", Nonbre de estudiante=" + estudiante + "]";
    }
}
