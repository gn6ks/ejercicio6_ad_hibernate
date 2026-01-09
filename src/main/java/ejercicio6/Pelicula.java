package ejercicio6;

public class Pelicula {
    private int id;
    private String titulo;
    private String director;
    private int anyo;

    public Pelicula() {}

    public Pelicula(String titulo, String director, int anyo) {
        this.titulo = titulo;
        this.director = director;
        this.anyo = anyo;
    }

    public Pelicula(int id, String titulo, String director, int anyo) {
        this.id = id;
        this.titulo = titulo;
        this.director = director;
        this.anyo = anyo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public int getAnyo() {
        return anyo;
    }

    public void setAnyo(int anyo) {
        this.anyo = anyo;
    }

    @Override
    public String toString() {
        return "Pelicula {" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", director='" + director + '\'' +
                ", anyo=" + anyo +
                '}';
    }
}
