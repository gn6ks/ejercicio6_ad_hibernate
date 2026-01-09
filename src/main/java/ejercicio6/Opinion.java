package ejercicio6;

public class Opinion {
    private int id;
    private String titulo;
    private String usuario;
    private String opinion;

    public Opinion() {}

    public Opinion(String titulo, String usuario, String opinion) {
        this.titulo = titulo;
        this.usuario = usuario;
        this.opinion = opinion;
    }

    public Opinion(int id, String titulo, String usuario, String opinion) {
        this.id = id;
        this.titulo = titulo;
        this.usuario = usuario;
        this.opinion = opinion;
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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    @Override
    public String toString() {
        return "Opinion {" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", usuario='" + usuario + '\'' +
                ", opinion='" + opinion + '\'' +
                '}';
    }
}
