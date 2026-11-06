
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SalaCine {

    private static final int FILAS = 8;
    private static final int COLUMNAS = 10;

    private final Asiento[][] sala;

    public SalaCine() {
        sala = new Asiento[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                sala[i][j] = new Asiento();
            }
        }
    }

    private boolean posicionValida(int fila, int columna) {
        return fila >= 1 && fila <= FILAS
                && columna >= 1 && columna <= COLUMNAS;
    }

    // Operación protegida por el monitor de SalaCine.
    public synchronized boolean reservar(
            int fila, int columna, String nombre, int edad) {

        if (!posicionValida(fila, columna)) {
            System.out.println("Fila o columna inválida.");
            return false;
        }

        if (nombre == null || nombre.isBlank() || edad < 0) {
            System.out.println("Nombre o edad inválidos.");
            return false;
        }

        Asiento asiento = sala[fila - 1][columna - 1];

        if (asiento.isOcupado()) {
            System.out.println("El asiento ya está ocupado.");
            return false;
        }

        asiento.ocupar(nombre.trim(), edad);

        System.out.printf(
                "Reserva realizada: %s, %d años. Asiento [%d,%d].%n",
                nombre.trim(), edad, fila, columna);

        return true;
    }

    public synchronized boolean liberar(int fila, int columna) {

        if (!posicionValida(fila, columna)) {
            System.out.println("Fila o columna inválida.");
            return false;
        }

        Asiento asiento = sala[fila - 1][columna - 1];

        if (!asiento.isOcupado()) {
            System.out.println("El asiento ya está libre.");
            return false;
        }

        System.out.printf(
                "Se liberó el asiento [%d,%d], reservado por %s.%n",
                fila, columna, asiento.getNombre());

        asiento.liberar();
        return true;
    }

    public synchronized void buscarPorAsiento(
            int fila, int columna) {

        if (!posicionValida(fila, columna)) {
            System.out.println("Fila o columna inválida.");
            return;
        }

        Asiento asiento = sala[fila - 1][columna - 1];

        if (asiento.isOcupado()) {
            System.out.printf(
                    "Asiento [%d,%d]: %s, %d años.%n",
                    fila, columna,
                    asiento.getNombre(), asiento.getEdad());
        } else {
            System.out.printf(
                    "El asiento [%d,%d] está libre.%n",
                    fila, columna);
        }
    }

    /*
     * Crea una copia consistente mientras se posee el monitor.
     * Los hilos de búsqueda trabajan después sobre esta copia,
     * sin modificar la sala original.
     */
    private synchronized String[][] obtenerCopia() {

        String[][] copia = new String[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Asiento asiento = sala[i][j];

                if (asiento.isOcupado()) {
                    copia[i][j] = asiento.getNombre()
                            + "|" + asiento.getEdad();
                } else {
                    copia[i][j] = null;
                }
            }
        }

        return copia;
    }

    public List<String> buscarPorNombreConcurrente(String nombre) {

        String[][] copia = obtenerCopia();
        List<String> resultados =
                java.util.Collections.synchronizedList(
                        new ArrayList<>());

        String nombreBuscado = nombre.trim();

        Thread hilo1 = new Thread(() ->
                buscarEnFilas(copia, 0, 4,
                        nombreBuscado, resultados));

        Thread hilo2 = new Thread(() ->
                buscarEnFilas(copia, 4, 8,
                        nombreBuscado, resultados));

        hilo1.start();
        hilo2.start();

        esperar(hilo1);
        esperar(hilo2);

        return new ArrayList<>(resultados);
    }

    private void buscarEnFilas(
            String[][] copia,
            int inicio,
            int fin,
            String nombre,
            List<String> resultados) {

        for (int i = inicio; i < fin; i++) {
            for (int j = 0; j < COLUMNAS; j++) {

                String dato = copia[i][j];

                if (dato != null) {
                    String[] partes = dato.split("\\|", 2);

                    if (partes[0].equalsIgnoreCase(nombre)) {
                        resultados.add(String.format(
                                "%s, %s años, asiento [%d,%d]",
                                partes[0], partes[1], i + 1, j + 1));
                    }
                }
            }
        }
    }

    public int contarDisponiblesConcurrente() {

        String[][] copia = obtenerCopia();
        int[] conteos = new int[2];

        Thread hilo1 = new Thread(() ->
                conteos[0] = contarFilas(copia, 0, 4));

        Thread hilo2 = new Thread(() ->
                conteos[1] = contarFilas(copia, 4, 8));

        hilo1.start();
        hilo2.start();

        esperar(hilo1);
        esperar(hilo2);

        return conteos[0] + conteos[1];
    }

    private int contarFilas(
            String[][] copia, int inicio, int fin) {

        int disponibles = 0;

        for (int i = inicio; i < fin; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                if (copia[i][j] == null) {
                    disponibles++;
                }
            }
        }

        return disponibles;
    }

    public synchronized void mostrarSala() {

        System.out.println("\n          PANTALLA");
        System.out.println("    1 2 3 4 5 6 7 8 9 10");

        for (int i = 0; i < FILAS; i++) {
            System.out.printf("F%2d ", i + 1);

            for (int j = 0; j < COLUMNAS; j++) {
                System.out.print(
                        sala[i][j].isOcupado() ? " X" : " O");
            }

            System.out.println();
        }

        System.out.println("\nO = Libre | X = Ocupado");
    }

    /*
     * Ejecuta dos hilos que intentan reservar el mismo asiento.
     * Sirve para probar la sincronización.
     */
    public void probarReservasConcurrentes(
            int fila, int columna) {

        Thread hilo1 = new Thread(() ->
                reservar(fila, columna, "Usuario A", 25));

        Thread hilo2 = new Thread(() ->
                reservar(fila, columna, "Usuario B", 30));

        hilo1.start();
        hilo2.start();

        esperar(hilo1);
        esperar(hilo2);

        System.out.println(
                "Prueba terminada: solo una reserva debe tener éxito.");
    }

    private void esperar(Thread hilo) {
        try {
            hilo.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Se interrumpió la espera de un hilo.", e);
        }
    }
}
