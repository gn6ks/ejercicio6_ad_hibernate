package ejercicio6;

public class Cine {

    private int id;
    private String cine;
    private String titulo;

    public Cine() {}

    public Cine(String cine, String titulo) {
        this.cine = cine;
        this.titulo = titulo;
    }

    public Cine(int id, String cine, String titulo) {
        this.id = id;
        this.cine = cine;
        this.titulo = titulo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCine() {
        return cine;
    }

    public void setCine(String cine) {
        this.cine = cine;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public String toString() {
        return "Cine {" +
                "id=" + id +
                ", cine='" + cine + '\'' +
                ", titulo='" + titulo + '\'' +
                '}';
    }
}
