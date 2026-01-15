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
 * 🎬 EJERCICIO 6: MI FILMOTECA CON HIBERNATE
 */
public class ejercicio6 {

    /**
     * 🚀 MÉTODO PRINCIPAL: arranca toda la aplicación
     * - Configura Hibernate (la conexión a la base de datos)
     * - Borra datos viejos
     * - Carga nuevos datos desde archivos CSV
     * - Muestra el menú principal
     */
    public static void main(String[] args) {
        // 🔧 Paso 1: Configurar Hibernate (usa hibernate.cfg.xml)
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        configuration.addResource("Pelicula.hbm.xml");
        configuration.addResource("Opinion.hbm.xml");
        configuration.addResource("Cine.hbm.xml");
        ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        SessionFactory sessionFactory = configuration.buildSessionFactory(registry);

        // 📥 Abrir sesión para trabajar con la BD
        Session session = sessionFactory.openSession();

        // 🧹 Limpiar datos anteriores (para empezar limpio cada vez)
        borrarDatosPrevios(session);

        // 📂 Cargar datos desde los archivos CSV
        insertarDatosPeliculas(session);
        insertarDatosOpiniones(session);
        insertarDatosCines(session);

        // 🎮 Mostrar el menú interactivo
        menuBiblioteca(session, sessionFactory);
    }

    /**
     * 📋 MENÚ PRINCIPAL: panel de control del usuario
     * Permite navegar entre las distintas funcionalidades:
     * 1. Ver todas las películas
     * 2. Iniciar sesión y ver tus opiniones
     * 3. Ver cartelera de todos los cines (agrupada)
     * 4. Buscar un cine específico
     * 5. Salir
     */
    public static void menuBiblioteca(Session session, SessionFactory sessionFactory) {
        Scanner sc = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("==================== 🎥 Filmoteca Florida 🎥 ====================");
            System.out.println("1. Mostrar Filmoteca completa");
            System.out.println("2. Iniciar sesión y ver MIS opiniones");
            System.out.println("3. Ver cartelera de TODOS los cines");
            System.out.println("4. Buscar cartelera de un CINE");
            System.out.println("5. Salir");
            System.out.println("==================================================================");
            System.out.print("👉 Elige una opción (1-5): ");
            opcion = sc.nextInt();
            sc.nextLine(); // Consumir el salto de línea

            switch (opcion) {
                case 1 -> mostrarFilmoteca(session);
                case 2 -> iniciarSesion(session);
                case 3 -> mostrarTitulosCines(session);
                case 4 -> mostrarTitulosCineConcreto(session);
                case 5 -> {
                    System.out.println("👋 ¡Hasta pronto!");
                    session.close();
                    sessionFactory.close();
                    System.exit(0);
                }
                default -> System.out.println("❌ Opción inválida. Inténtalo de nuevo.");
            }
        } while (opcion != 5);
    }

    /**
     * 🧹 BORRAR DATOS ANTERIORES
     * Usa TRUNCATE para vaciar las tablas antes de cargar nuevos datos.
     * Así evitamos duplicados cada vez que ejecutamos el programa.
     */
    public static void borrarDatosPrevios(Session session) {
        session.beginTransaction();

        session.createNativeQuery("TRUNCATE TABLE peliculas").executeUpdate();
        session.createNativeQuery("TRUNCATE TABLE opiniones").executeUpdate();
        session.createNativeQuery("TRUNCATE TABLE cines").executeUpdate();

        System.err.println("🧹 Datos anteriores borrados de la base de datos.");

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * 📖 LEER ARCHIVO CSV
     * Toma un archivo .csv y lo convierte en una lista de líneas.
     * Cada línea se divide usando ";" como separador.
     * Ej: "Matrix;Lana Wachowski;1999" → ["Matrix", "Lana Wachowski", "1999"]
     */
    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(";"));
            }
        } catch (Exception e) {
            System.err.println("❌ Error al leer el archivo: " + archivo.getName());
            e.printStackTrace();
        }
        return lineas;
    }

    /**
     * 📥 INSERTAR PELÍCULAS
     * Lee 'peliculas.csv' y guarda cada película en la base de datos.
     * Formato esperado: Título;Director;Año
     */
    public static void insertarDatosPeliculas(Session session) {
        session.beginTransaction();

        File archivo = new File("C:\\Users\\pablo\\Documents\\_estudios\\_dam2\\ejercicio6_ad_hibernate\\src\\main\\java\\recursos\\peliculas.csv");
        ArrayList<String[]> contenido = leerArchivoCSV(archivo);

        for (String[] linea : contenido) {
            String titulo = linea[0];
            String director = linea[1];
            int anyo = Integer.parseInt(linea[2]);
            Pelicula p = new Pelicula(titulo, director, anyo);
            session.save(p);
        }

        session.getTransaction().commit();
        session.clear();
        System.out.println("✅ Películas cargadas desde CSV.");
    }

    /**
     * 💬 INSERTAR OPINIONES
     * Lee 'opiniones.csv' y guarda cada opinión.
     * Formato: Título;Usuario;Opinión
     */
    public static void insertarDatosOpiniones(Session session) {
        session.beginTransaction();

        File archivo = new File("C:\\Users\\pablo\\Documents\\_estudios\\_dam2\\ejercicio6_ad_hibernate\\src\\main\\java\\recursos\\opiniones.csv");
        ArrayList<String[]> contenido = leerArchivoCSV(archivo);

        for (String[] linea : contenido) {
            String titulo = linea[0];
            String usuario = linea[1];
            String opinion = linea[2];
            Opinion o = new Opinion(titulo, usuario, opinion);
            session.save(o);
        }

        session.getTransaction().commit();
        session.clear();
        System.out.println("✅ Opiniones cargadas desde CSV.");
    }

    /**
     * 🏢 INSERTAR CINES
     * Lee 'cines.csv' y guarda cada asociación cine-película.
     * Formato: Nombre del cine;Título de la película
     * ⚠️ Cada fila es UNA relación (no un cine único).
     */
    public static void insertarDatosCines(Session session) {
        session.beginTransaction();

        File archivo = new File("C:\\Users\\pablo\\Documents\\_estudios\\_dam2\\ejercicio6_ad_hibernate\\src\\main\\java\\recursos\\cines.csv");
        ArrayList<String[]> contenido = leerArchivoCSV(archivo);

        for (String[] linea : contenido) {
            String nombreCine = linea[0];
            String tituloPelicula = linea[1];
            Cine c = new Cine(nombreCine, tituloPelicula);
            session.save(c);
        }

        session.getTransaction().commit();
        session.clear();
        System.out.println("✅ Asociaciones cine-película cargadas desde CSV.");
    }

    /**
     * 👀 MOSTRAR FILMOTECA COMPLETA
     * Muestra todas las películas + todas las opiniones que tienen.
     * Para cada película, busca manualmente sus opiniones comparando el título.
     */
    public static void mostrarFilmoteca(Session session) {
        session.beginTransaction();

        List<Pelicula> peliculas = session.createQuery("FROM ejercicio6.Pelicula", Pelicula.class).list();
        List<Opinion> opiniones = session.createQuery("FROM ejercicio6.Opinion", Opinion.class).list();

        if (peliculas.isEmpty()) {
            System.err.println("📭 La filmoteca está vacía.");
            session.getTransaction().commit();
            return;
        }

        System.out.println("🎬 Mi filmoteca:");
        for (Pelicula p : peliculas) {
            System.out.println("\n" + p.getId() + ". " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ")");
            System.out.println("💬 Opiniones:");

            List<String> usuarios = new ArrayList<>();
            List<String> textos = new ArrayList<>();

            // 🔍 Buscar opiniones que coincidan con el título de esta película
            for (Opinion o : opiniones) {
                if (o.getTitulo().equals(p.getTitulo())) {
                    usuarios.add(o.getUsuario());
                    textos.add(o.getOpinion());
                }
            }

            if (usuarios.isEmpty()) {
                System.out.println("  - Aún no hay opiniones sobre esta película.");
            } else {
                for (int i = 0; i < usuarios.size(); i++) {
                    System.out.println("  - " + usuarios.get(i) + ": " + textos.get(i));
                }
            }
        }

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * 🏢 MOSTRAR CARTELERA DE TODOS LOS CINES (AGRUPADA)
     * Agrupa las películas por nombre de cine (sin repetir nombres).
     * Ej: Todas las pelis de "Cine Galaxy" aparecen juntas.
     */
    public static void mostrarTitulosCines(Session session) {
        session.beginTransaction();

        List<Cine> cines = session.createQuery("FROM ejercicio6.Cine", Cine.class).list();
        List<Pelicula> peliculas = session.createQuery("FROM ejercicio6.Pelicula", Pelicula.class).list();

        if (cines.isEmpty()) {
            System.err.println("📭 No hay cines registrados.");
            session.getTransaction().commit();
            return;
        }

        // 🧠 Paso 1: Obtener nombres únicos de cines (sin repetir)
        List<String> nombresUnicos = new ArrayList<>();
        for (Cine c : cines) {
            String nombre = c.getCine();
            if (!nombresUnicos.contains(nombre)) {
                nombresUnicos.add(nombre);
            }
        }

        System.out.println("📽️ Cartelera de todos los cines:");

        // 🧠 Paso 2: Para cada nombre único, mostrar sus películas
        for (String nombreCine : nombresUnicos) {
            System.out.println("\n🏢 Cartelera de '" + nombreCine + "':");

            boolean tienePeliculas = false;

            // Buscar todas las filas de 'Cine' que pertenezcan a este nombre
            for (Cine c : cines) {
                if (c.getCine().equals(nombreCine)) {
                    // Encontrar la película completa por su título
                    for (Pelicula p : peliculas) {
                        if (p.getTitulo().equals(c.getTitulo())) {
                            System.out.println("   - " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ")");
                            tienePeliculas = true;
                            break; // Una película por registro
                        }
                    }
                }
            }

            if (!tienePeliculas) {
                System.out.println("   - No hay películas en cartelera.");
            }
        }

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * 👤 INICIAR SESIÓN COMO USUARIO
     * Te pide tu nombre y muestra SOLO tus opiniones.
     * Al final, te permite modificar, borrar o exportar.
     */
    public static void iniciarSesion(Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        System.out.print("👤 Usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.println("✅ Sesión iniciada como '" + usuario + "'.");

        List<Pelicula> peliculas = session.createQuery("FROM ejercicio6.Pelicula", Pelicula.class).list();
        List<Opinion> opiniones = session.createQuery("FROM ejercicio6.Opinion", Opinion.class).list();

        // 📦 Guardamos tus opiniones aquí para poder exportarlas después
        List<String> titulosExportar = new ArrayList<>();
        List<String> opinionesExportar = new ArrayList<>();

        boolean hayOpiniones = false;

        for (Pelicula p : peliculas) {
            List<String> misOpiniones = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            boolean tieneOpinion = false;

            // Buscar solo TUS opiniones sobre esta película
            for (Opinion o : opiniones) {
                if (o.getUsuario().equals(usuario) && o.getTitulo().equals(p.getTitulo())) {
                    tieneOpinion = true;
                    misOpiniones.add(o.getOpinion());
                    ids.add(o.getId());

                    // Guardar para exportar
                    titulosExportar.add(o.getTitulo());
                    opinionesExportar.add(o.getOpinion());
                }
            }

            if (tieneOpinion) {
                hayOpiniones = true;
                System.out.println("\n" + p.getId() + ". " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ")");
                System.out.println("💬 Tus opiniones:");

                for (int i = 0; i < misOpiniones.size(); i++) {
                    System.out.println("   - ID " + ids.get(i) + ": " + misOpiniones.get(i));
                }
            }
        }

        if (!hayOpiniones) {
            System.err.println("📭 No tienes opiniones registradas.");
        }

        session.getTransaction().commit();
        session.clear();

        // 📤 ¿Quieres exportar o gestionar?
        System.out.print("¿Deseas gestionar tus opiniones? (S/N/E=Exportar): ");
        String opcion = sc.nextLine().trim().toUpperCase();

        switch (opcion) {
            case "S" -> gestionarOpiniones(session);
            case "E" -> {
                if (!opinionesExportar.isEmpty()) {
                    exportarNuevoCSV(usuario, titulosExportar, opinionesExportar);
                } else {
                    System.out.println("📭 No hay opiniones para exportar.");
                }
            }
            case "N" -> {
            }
            default -> System.out.println("❓ Opción no reconocida.");
        }
    }

    /**
     * 🔍 BUSCAR CARTELERA DE UN CINE ESPECÍFICO
     * El usuario escribe el nombre del cine y se muestran sus películas.
     */
    public static void mostrarTitulosCineConcreto(Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        System.out.print("🏢 Nombre del cine: ");
        String nombreCine = sc.nextLine().trim();
        System.out.println("🔍 Buscando cartelera de '" + nombreCine + "'...");

        List<Cine> cines = session.createQuery("FROM ejercicio6.Cine", Cine.class).list();
        List<Pelicula> peliculas = session.createQuery("FROM ejercicio6.Pelicula", Pelicula.class).list();

        List<Pelicula> cartelera = new ArrayList<>();
        boolean encontrado = false;

        // Buscar todas las películas asociadas a este cine
        for (Cine c : cines) {
            if (c.getCine().equals(nombreCine)) {
                encontrado = true;
                for (Pelicula p : peliculas) {
                    if (p.getTitulo().equals(c.getTitulo())) {
                        cartelera.add(p);
                        break;
                    }
                }
            }
        }

        if (!encontrado || cartelera.isEmpty()) {
            System.err.println("❌ No se encontró el cine o no tiene películas.");
        } else {
            System.out.println("✅ Cartelera de '" + nombreCine + "':");
            for (Pelicula p : cartelera) {
                System.out.println("   - " + p.getTitulo() + " (" + p.getDirector() + ", " + p.getAnyo() + ")");
            }
        }

        session.getTransaction().commit();
        session.clear();

        // 💾 S para exportar la cartelera de 'Cine'
        System.out.print("¿Deseas exportar esta cartelera? (S/N): ");
        String opcion = sc.nextLine().trim().toUpperCase();

        if ("S".equals(opcion)) {
            exportarCineCSV(nombreCine, cartelera);
        } else if (!"N".equals(opcion)) {
            System.out.println("❓ Opción no válida.");
        }
    }

    /**
     * ✏️ GESTIONAR OPINIONES (menú secundario)
     * Permite elegir si borrar o modificar una opinión por su ID.
     */
    public static void gestionarOpiniones(Session session) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n🛠️ Gestión de opiniones");
        System.out.print("ID de la opinión: ");
        int id = sc.nextInt();
        System.out.print("(0) Borrar / (1) Modificar: ");
        int accion = sc.nextInt();

        if (accion == 0) {
            borrarOpinion(id, session);
        } else if (accion == 1) {
            modificarOpinion(id, session);
        } else {
            System.out.println("❌ Acción no soportada.");
        }
    }

    /**
     * 🗑️ BORRAR UNA OPINIÓN POR ID
     */
    public static void borrarOpinion(int id, Session session) {
        session.beginTransaction();
        Opinion o = session.get(Opinion.class, id);

        if (o == null) {
            System.err.println("❌ Opinión con ID " + id + " no encontrada.");
        } else {
            System.out.println("🗑️ Borrando opinión: \"" + o.getOpinion() + "\"");
            session.delete(o);
            System.out.println("✅ Opinión eliminada.");
        }

        session.getTransaction().commit();
        session.clear();
    }

    /**
     * ✍️ MODIFICAR UNA OPINIÓN POR ID
     */
    public static void modificarOpinion(int id, Session session) {
        session.beginTransaction();
        Scanner sc = new Scanner(System.in);

        Opinion o = session.get(Opinion.class, id);
        if (o == null) {
            System.err.println("❌ Opinión con ID " + id + " no encontrada.");
            session.getTransaction().commit();
            return;
        }

        System.out.println("✏️ Opinión actual: \"" + o.getOpinion() + "\"");
        System.out.print("Nueva opinión: ");
        String nueva = sc.nextLine().trim();
        o.setOpinion(nueva);

        session.getTransaction().commit();
        session.clear();
        System.out.println("✅ Opinión actualizada.");
    }

    /**
     * 💾 EXPORTAR OPINIONES DE UN USUARIO A CSV
     * Genera un archivo como: Ana92_20260115_143022.csv
     * Formato: Título;Opinión
     */
    public static void exportarNuevoCSV(String usuario, List<String> titulos, List<String> opiniones) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = usuario + "_" + timestamp + ".csv";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            for (int i = 0; i < titulos.size(); i++) {
                bw.write(titulos.get(i) + ";" + opiniones.get(i));
                bw.newLine();
            }
            System.out.println("📤 Opiniones exportadas a: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("❌ Error al guardar el archivo CSV.");
            e.printStackTrace();
        }
    }

    /**
     * 💾 EXPORTAR CARTELERA DE UN CINE A CSV
     * Formato: Cine;Título;Director;Año
     */
    public static void exportarCineCSV(String cine, List<Pelicula> peliculas) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = cine.replace(" ", "_") + "_" + timestamp + ".csv";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            for (Pelicula p : peliculas) {
                bw.write(cine + ";" + p.getTitulo() + ";" + p.getDirector() + ";" + p.getAnyo());
                bw.newLine();
            }
            System.out.println("📤 Cartelera exportada a: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("❌ Error al guardar el archivo CSV.");
            e.printStackTrace();
        }
    }
}