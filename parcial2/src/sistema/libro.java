package sistema;

public class libro {
        private String id; // Agregamos un ID para la validación
        private String titulo;
        private String autor;
        // Constructor, Getters y Setters
        public libro(String id, String titulo, String autor) {
            this.id = id;
            this.titulo = titulo;
            this.autor = autor;

        }
        // Getters para todos los campos
        public String getId() { return id; }
        public String getTitulo() { return titulo; }

        @Override
        public String toString() {
            return "Libro [ID=" + id + ", Titulo=" + titulo + ", Autor=" + autor + "]";
        }
}
