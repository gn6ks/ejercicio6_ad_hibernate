package ejercicio6;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Clase principal del programa que gestiona una filmoteca con opiniones de usuarios.
 * Permite cargar datos desde archivos CSV, mostrar películas y opiniones,
 * gestionar opiniones por usuario y exportarlas a un archivo.
 */
public class ejercicio6 {

    /**
     * Método principal que inicia la aplicación.
     * Configura Hibernate, borra datos anteriores, carga nuevos datos desde CSV
     * y muestra el menú principal.
     *
     * @param args argumentos de la línea de comandos (no se usan).
     */
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

    /**
     * Muestra un menú interactivo al usuario para navegar por la filmoteca.
     * El usuario puede ver todas las películas, ver sus opiniones o salir.
     *
     * @param session la sesión de Hibernate para acceder a la base de datos.
     * @param sessionFactory la fábrica de sesiones de Hibernate (se cierra al salir).
     */
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

    /**
     * Borra todos los datos existentes en las tablas 'peliculas' y 'opiniones'
     * usando comandos SQL directos (TRUNCATE).
     * Útil para empezar con una base de datos limpia cada vez que se ejecuta el programa.
     *
     * @param session la sesión de Hibernate para ejecutar las consultas.
     */
    public static void borrarDatosPrevios(Session session) {
        session.beginTransaction();

        session.createNativeQuery("TRUNCATE TABLE peliculas").executeUpdate();
        session.createNativeQuery("TRUNCATE TABLE opiniones").executeUpdate();

        System.err.println("Datos previos borrados");

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * Lee un archivo CSV y devuelve su contenido como una lista de filas.
     * Cada fila es un arreglo de strings separado por punto y coma (;).
     *
     * @param archivo el archivo CSV a leer.
     * @return una lista donde cada elemento es una fila del CSV dividida en columnas.
     */
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

    /**
     * Carga las películas desde el archivo 'peliculas.csv' y las guarda en la base de datos.
     * Cada línea del CSV debe tener: título;director;año.
     *
     * @param session la sesión de Hibernate para guardar los objetos Pelicula.
     */
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

    /**
     * Carga las opiniones desde el archivo 'opiniones.csv' y las guarda en la base de datos.
     * Cada línea del CSV debe tener: título_pelicula;usuario;opinion.
     *
     * @param session la sesión de Hibernate para guardar los objetos Opinion.
     */
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

    /**
     * Muestra todas las películas de la filmoteca junto con todas las opiniones que tienen.
     * Para cada película, se listan los usuarios y sus comentarios.
     *
     * @param session la sesión de Hibernate para consultar películas y opiniones.
     */
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
                }

                if (usuarios.isEmpty()) {
                    System.out.println("- No hay opiniones sobre esta película.");
                } else {
                    for (int i = 0; i < usuarios.size(); i++) {
                        System.out.println("- " + usuarios.get(i) + ": " + opiniones.get(i));
                    }
                }
            }

        } else {
            System.err.println("No hay nada en la filmoteca para mostrar");
        }

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * Permite a un usuario identificarse y ver solo sus opiniones sobre las películas.
     * También permite modificar, borrar o exportar esas opiniones.
     *
     * @param session la sesión de Hibernate para consultar datos.
     */
    public static void iniciarSesion(Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        System.out.print("usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.println("Se ha iniciado sesion como '" + usuario + "'.");

        List<Pelicula> listaPeliculas = session.createQuery("From ejercicio6.Pelicula", Pelicula.class).list();
        List<Opinion> listaOpiniones = session.createQuery("From ejercicio6.Opinion", Opinion.class).list();

        // Listas para guardar TODAS las opiniones del usuario (solo para exportar al final)
        List<String> titulosParaExportar = new ArrayList<>();
        List<String> opinionesParaExportar = new ArrayList<>();

        if (!listaPeliculas.isEmpty()) {
            boolean mostrarPelicula = false;

            for (Pelicula p : listaPeliculas) {
                // Listas temporales solo para esta película
                List<String> opinionUsuarioPelicula = new ArrayList<>();
                List<Integer> idsOpiniones = new ArrayList<>();
                boolean tieneOpinionUsuario = false;

                for (Opinion o : listaOpiniones) {
                    if (o.getUsuario().equals(usuario) && o.getTitulo().equals(p.getTitulo())) {
                        tieneOpinionUsuario = true;
                        opinionUsuarioPelicula.add(o.getOpinion());
                        idsOpiniones.add(o.getId());

                        // Guardamos también para exportar después
                        titulosParaExportar.add(o.getTitulo());
                        opinionesParaExportar.add(o.getOpinion());
                    }
                }

                if (tieneOpinionUsuario) {
                    mostrarPelicula = true;
                    System.out.println(p.getId() + ". " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ") ");
                    System.out.println("Opiniones:");

                    for (int i = 0; i < opinionUsuarioPelicula.size(); i++) {
                        System.out.println("- ID " + idsOpiniones.get(i) + ": " + opinionUsuarioPelicula.get(i));
                    }
                    System.out.println();
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

        System.out.println("¿Desea cambiar alguna de sus opiniones? (S/N/E (Exportar))");
        String opcion = sc.nextLine().trim().toUpperCase();

        switch (opcion) {
            case "S" -> gestionarOpiniones(session);
            case "E" -> {
                if (!opinionesParaExportar.isEmpty()) {
                    exportarNuevoCSV(usuario, titulosParaExportar, opinionesParaExportar);
                } else {
                    System.out.println("No hay opiniones para exportar.");
                }
            }
            case "N" -> {}
            default -> System.out.println("opcion invalida");
        }
    }

    /**
     * Permite al usuario elegir si quiere borrar o modificar una opinión por su ID.
     *
     * @param session la sesión de Hibernate para acceder a la base de datos.
     */
    public static void gestionarOpiniones(Session session) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Gestionar opiniones:");
        System.out.print("ID opinion a modificar/borrar: ");
        int id = sc.nextInt();
        System.out.print("(0) Borrar / (1) Modificar: ");
        int opcion = sc.nextInt();

        switch (opcion) {
            case 0 -> borrarOpinion(id, session);
            case 1 -> modificarOpinion(id, session);
            default -> System.out.println("opcion no habilitada");
        }
    }

    /**
     * Elimina una opinión de la base de datos usando su ID.
     *
     * @param id el identificador único de la opinión a borrar.
     * @param session la sesión de Hibernate para realizar la operación.
     */
    public static void borrarOpinion(int id, Session session) {
        session.beginTransaction();

        Opinion opinion = session.get(Opinion.class, id);

        System.out.println("====== PAPELERA OPINIONES ======");
        System.out.println("Se va a borrar la opinion con el ID '" + id + "'");
        System.out.println("Borrando opinion '" + opinion.getOpinion() + "'");
        System.out.println("====== PAPELERA OPINIONES ======");

        session.delete(opinion);

        System.err.println("Borrado con éxito.");

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * Modifica el texto de una opinión existente usando su ID.
     *
     * @param id el identificador único de la opinión a modificar.
     * @param session la sesión de Hibernate para actualizar el dato.
     */
    public static void modificarOpinion(int id, Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        Opinion opinion = session.load(Opinion.class, id);

        System.out.println("====== MODIFICADOR ======");
        System.out.print("Nueva Opinion: ");
        String nuevaOpinion = sc.nextLine().trim();
        System.out.println("====== MODIFICADOR ======");

        opinion.setOpinion(nuevaOpinion);

        System.out.println("👌 Opinión actualizada con exito");

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * Exporta las opiniones de un usuario a un archivo CSV con nombre único.
     * El archivo se guarda en la carpeta del proyecto y contiene: título;opinion.
     *
     * @param nombreUsuario el nombre del usuario cuyas opiniones se exportan.
     * @param titulos la lista de títulos de películas con opiniones.
     * @param opiniones la lista de opiniones correspondientes a esos títulos.
     */
    public static void exportarNuevoCSV(String nombreUsuario, List<String> titulos, List<String> opiniones) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String nombreArchivo = nombreUsuario + "_" + ahora.format(formateador) + ".csv";
        File archivo = new File(nombreArchivo);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (int i = 0; i < titulos.size(); i++) {
                bw.write(titulos.get(i));
                bw.write(";");
                bw.write(opiniones.get(i));
                bw.newLine();
            }
            System.out.println("✅ Opiniones exportadas a " + nombreArchivo);
        } catch (Exception e) {
            System.err.println("❌ No se pudo guardar el archivo CSV.");
            e.printStackTrace();
        }
    }
}