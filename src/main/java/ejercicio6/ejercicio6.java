package ejercicio6;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ejercicio6 {

    //TODO: documentar todo el ejercicio

    public static void main(String[] args) {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        configuration.addResource("Pelicula.hbm.xml");
        configuration.addResource("Opinion.hbm.xml");
        ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        SessionFactory sessionFactory = configuration.buildSessionFactory(registry);

        Session session = sessionFactory.openSession();

        borrarDatosPrevios(session);
        insertarDatosPeliculas(session);
        insertarDatosOpiniones(session);
        menuBiblioteca(session, sessionFactory);
    }

    public static void menuBiblioteca(Session session, SessionFactory sessionFactory) {
        Scanner sc = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("==================== Filmoteca Florida ====================");
            System.out.println("1. Mostrar Filmoteca");
            System.out.println("2. Mostrar Opiniones por Usuario");
            System.out.println("3. Salir");
            System.out.println("==================== Filmoteca Florida ====================");
            System.out.print("Ingrese opcion: (1-3)  ");
            opcion = sc.nextInt();

            switch (opcion) {
                case 1 -> mostrarFilmoteca(session);
                case 2 -> iniciarSesion(session);
                case 3 -> System.exit(0);
                default -> System.out.println("invalid option");
            }
        } while (opcion != 7);

        session.close();
        sessionFactory.close();
    }

    public static void borrarDatosPrevios(Session session) {
        session.beginTransaction();

        session.createNativeQuery("TRUNCATE TABLE peliculas").executeUpdate();
        session.createNativeQuery("TRUNCATE TABLE opiniones").executeUpdate();

        System.err.println("Datos previos borrados");

        session.getTransaction().commit();
        session.clear();
    }

    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(";"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineas;
    }

    public static void insertarDatosPeliculas(Session session) {
        session.beginTransaction();

        List<String> titulos = new ArrayList<>();
        List<String> directores = new ArrayList<>();
        List<Integer> anyos = new ArrayList<>();

        File archivoPeliculas = new File("src/main/java/recursos/peliculas.csv");
        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivoPeliculas);

        for (String[] linea : contenidoArchivo) {
            titulos.add(linea[0]);
            directores.add(linea[1]);
            anyos.add(Integer.parseInt(linea[2]));
        }

        for (int i = 0; i < titulos.size(); i++) {
            Pelicula p = new Pelicula(titulos.get(i), directores.get(i), anyos.get(i));
            session.save(p);
        }

        session.getTransaction().commit();
        session.clear();
    }

    public static void insertarDatosOpiniones(Session session) {
        session.beginTransaction();

        List<String> peliculas = new ArrayList<>();
        List<String> usuarios = new ArrayList<>();
        List<String> opiniones = new ArrayList<>();

        File archivoPeliculas = new File("src/main/java/recursos/opiniones.csv");
        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivoPeliculas);

        for (String[] linea : contenidoArchivo) {
            peliculas.add(linea[0]);
            usuarios.add(linea[1]);
            opiniones.add(linea[2]);
        }

        for (int i = 0; i < peliculas.size(); i++) {
            Opinion o = new Opinion(peliculas.get(i), usuarios.get(i), opiniones.get(i));
            session.save(o);
        }

        session.getTransaction().commit();
        session.clear();
    }

    public static void mostrarFilmoteca(Session session) {
        session.beginTransaction();

        List<Pelicula> lista = session.createQuery("From ejercicio6.Pelicula", Pelicula.class).list();
        List<Opinion> listaOpiniones = session.createQuery("From ejercicio6.Opinion", Opinion.class).list();

        if (!lista.isEmpty()) {
            System.out.println("Mi filmoteca:");
            for (Pelicula p : lista) {

                System.out.println(p.getId() + ". " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ") ");
                System.out.println("Opiniones:");

                List<String> usuarios = new ArrayList<>();
                List<String> opiniones = new ArrayList<>();

                if (!listaOpiniones.isEmpty()) {
                    for (Opinion o : listaOpiniones) {
                        if (o.getTitulo().equals(p.getTitulo())) {
                            usuarios.add(o.getUsuario());
                            opiniones.add(o.getOpinion());
                        }
                    }
                } else {
                    System.err.println("No hay opiniones sobre esta pelicula");
                }

                for (int i = 0; i < usuarios.size(); i++) {
                    System.out.println("- " + usuarios.get(i) + ": " + opiniones.get(i));
                }
            }

        } else {
            System.err.println("No hay nada en la filmoteca para mostrar");
        }

        session.getTransaction().commit();
        session.clear();
    }

    public static void iniciarSesion(Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        System.out.print("usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.println("Se ha iniciado sesion como '" + usuario + "'.");

        List<Pelicula> listaPeliculas = session.createQuery("From ejercicio6.Pelicula", Pelicula.class).list();
        List<Opinion> listaOpiniones = session.createQuery("From ejercicio6.Opinion", Opinion.class).list();

        if (!listaPeliculas.isEmpty()) {

            boolean mostrarPelicula = false;

            for (Pelicula p : listaPeliculas) {

                List<String> opinionUsuarioPelicula = new ArrayList<>();
                boolean tieneOpinionUsuario = false;

                if (!listaOpiniones.isEmpty()) {

                    for (Opinion o : listaOpiniones) {

                        if (o.getUsuario().equals(usuario) && o.getTitulo().equals(p.getTitulo())) {
                            tieneOpinionUsuario = true;
                            opinionUsuarioPelicula.add(o.getOpinion());
                        }
                    }
                }

                if (tieneOpinionUsuario) {
                    mostrarPelicula = true;
                    System.out.println(p.getId() + ". " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ") ");
                    System.out.println("Opiniones:");

                    for (int i = 0; i < opinionUsuarioPelicula.size(); i++) {
                        System.out.println("- " + usuario + ": " + opinionUsuarioPelicula.get(i));
                    }
                }
            }

            if (!mostrarPelicula) {
                System.err.println("No tienes opiniones registradas para ninguna película.");
            }

        } else {
            System.err.println("No hay nada en la filmoteca para mostrar");
        }

        session.getTransaction().commit();
        session.clear();
    }

    public static void gestionarOpiniones(Session session, List<String> opiniones) {
        //TODO: mirar si es por texto o si es por id
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        System.out.println("Gestionar opiniones:");
        System.out.print("ID pelicula opinion a modificar/borrar: ");
        int id = sc.nextInt();
        System.out.print("(0) Borrar / (1) Modificar: ");
        int opcion = sc.nextInt();

        switch (opcion) {
//            case 0 ->
//            case 1 ->
            default -> System.out.println("opcion no habilitada");
        }

        session.getTransaction().commit();
        session.clear();
    }

    public static void borrarOpinion(List<String> opiniones, int id) {
        //TODO: preguntar a roberto si esta es la manera de hacerlo segun el id que se muestra?
    }

    public static void modificarOpinion(List<String> opiniones, int id) {
        //TODO: preguntar a roberto si esta es la manera de hacerlo segun el id que se muestra?
    }
}
