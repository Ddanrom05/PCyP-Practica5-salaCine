
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class SalaCine {

    private static final int FILAS = 8;
    private static final int COLUMNAS = 10;

    private final Asiento[][] sala;

    // Mutex de la sala.
    private final ReentrantLock mutex = new ReentrantLock();

    public SalaCine() {

        sala = new Asiento[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                sala[i][j] = new Asiento();
            }
        }
    }

    // Validar posiciones.
    private boolean posicionValida(int fila, int columna) {

        return fila >= 1 && fila <= FILAS && columna >= 1 && columna <= COLUMNAS;
    }

    // RESERVAR ASIENTO

    public boolean reservar(
            int fila, int columna, String nombre, int edad) {

        // Intentar adquirir el mutex sin esperar.
        if (!mutex.tryLock()) {
            System.out.println("Sistema ocupado. Intente nuevamente.");
            return false;
        }

        try {

            if (!posicionValida(fila, columna)) {
                System.out.println("Asiento inválido.");
                return false;
            }

            if (nombre == null || nombre.isBlank() || edad < 0) {
                System.out.println("Nombre o edad inválidos.");
                return false;
            }

            Asiento asiento = sala[fila - 1][columna - 1];

            if (asiento.isOcupado()) {
                System.out.println(
                        "El asiento ya está ocupado.");
                return false;
            }

            asiento.ocupar(nombre.trim(), edad);

            System.out.printf("Reserva realizada: %s, %d años. " + "Asiento [%d,%d].%n", nombre.trim(), edad, fila, columna);

            return true;

        } finally {
            // Liberar siempre el mutex.
            mutex.unlock();
        }
    }

    // LIBERAR ASIENTO

    public boolean liberar(int fila, int columna) {

        if (!mutex.tryLock()) {
            System.out.println("Sistema ocupado. Intente nuevamente.");
            return false;
        }

        try {

            if (!posicionValida(fila, columna)) {
                System.out.println("Asiento inválido.");
                return false;
            }

            Asiento asiento = sala[fila - 1][columna - 1];

            if (!asiento.isOcupado()) {
                System.out.println("El asiento ya está libre.");
                return false;
            }

            System.out.printf("Se liberó el asiento [%d,%d], " + "reservado por %s.%n", fila, columna, asiento.getNombre());

            asiento.liberar();

            return true;

        } finally {
            mutex.unlock();
        }
    }

    // OBTENER COPIA CONSISTENTE DE LA SALA

    private Asiento[][] obtenerCopia() {

        Asiento[][] copia = new Asiento[FILAS][COLUMNAS];

        // Esperar a que termine cualquier modificación.
        mutex.lock();

        try {

            for (int i = 0; i < FILAS; i++) {

                for (int j = 0; j < COLUMNAS; j++) {

                    Asiento original = sala[i][j];

                    copia[i][j] = new Asiento();

                    if (original.isOcupado()) {

                        copia[i][j].ocupar(
                                original.getNombre(),
                                original.getEdad());
                    }
                }
            }

        } finally {
            mutex.unlock();
        }

        return copia;
    }

    // BUSCAR POR NOMBRE CON DOS HILOS

    public List<String> buscarPorNombreConcurrente(
            String nombre) {

        if (nombre == null || nombre.isBlank()) {
            return Collections.emptyList();
        }

        // Los hilos trabajarán sobre esta copia.
        Asiento[][] copia = obtenerCopia();

        // Lista compartida y sincronizada.
        List<String> resultados = Collections.synchronizedList(new ArrayList<>());

        String nombreBuscado = nombre.trim();

        // Primer hilo: filas 1 a 4.
        Thread hilo1 = new Thread(() -> buscarEnFilas(copia, 0, 4, nombreBuscado, resultados));

        // Segundo hilo: filas 5 a 8.
        Thread hilo2 = new Thread(() -> buscarEnFilas(copia, 4, 8, nombreBuscado, resultados));

        hilo1.start();
        hilo2.start();

        // Esperar a que ambos terminen.
        esperar(hilo1);
        esperar(hilo2);

        return new ArrayList<>(resultados);
    }

    // Trabajo que realiza cada hilo.
    private void buscarEnFilas(
            Asiento[][] copia,
            int inicio,
            int fin,
            String nombre,
            List<String> resultados) {

        for (int i = inicio; i < fin; i++) {

            for (int j = 0; j < COLUMNAS; j++) {

                Asiento asiento = copia[i][j];

                if (asiento.isOcupado() && asiento.getNombre().equalsIgnoreCase(nombre)) {

                    resultados.add(String.format("%s, %d años, asiento [%d,%d]", asiento.getNombre(), asiento.getEdad(), i + 1, j + 1));
                }
            }
        }
    }

    // BUSCAR POR ASIENTO

    public void buscarPorAsiento(int fila, int columna) {

        if (!posicionValida(fila, columna)) {
            System.out.println("Asiento inválido.");
            return;
        }

        // Consultar una copia consistente.
        Asiento[][] copia = obtenerCopia();

        Asiento asiento = copia[fila - 1][columna - 1];

        if (!asiento.isOcupado()) {

            System.out.printf(
                    "El asiento [%d,%d] está libre.%n",
                    fila, columna);

            return;
        }

        System.out.printf(
                "Información del asiento [%d,%d]:%n",
                fila, columna);

        System.out.println(
                "Nombre: " + asiento.getNombre());

        System.out.println(
                "Edad: " + asiento.getEdad());
    }

    // MOSTRAR SALA

    public void mostrarSala() {

        // Se obtiene una copia consistente.
        Asiento[][] copia = obtenerCopia();

        System.out.println("\n===== SALA DE CINE =====");

        System.out.println("    1 2 3 4 5 6 7 8 9 10");

        for (int i = 0; i < FILAS; i++) {

            System.out.printf("F%2d ", i + 1);

            for (int j = 0; j < COLUMNAS; j++) {

                System.out.print(copia[i][j].isOcupado() ? " X" : " O");
            }

            System.out.println();
        }

        System.out.println("\nO = Libre");
        System.out.println("X = Ocupado");
    }

    // CONTAR ASIENTOS DISPONIBLES CON DOS HILOS

    public int contarDisponiblesConcurrente() {

        Asiento[][] copia = obtenerCopia();

        // Cada hilo tendrá su propia posición.
        int[] conteos = new int[2];

        Thread hilo1 = new Thread(() -> conteos[0] = contarFilas(copia, 0, 4));

        Thread hilo2 = new Thread(() -> conteos[1] = contarFilas(copia, 4, 8));

        hilo1.start();
        hilo2.start();

        esperar(hilo1);
        esperar(hilo2);

        return conteos[0] + conteos[1];
    }

    private int contarFilas(
            Asiento[][] copia, int inicio, int fin) {

        int disponibles = 0;

        for (int i = inicio; i < fin; i++) {

            for (int j = 0; j < COLUMNAS; j++) {

                if (!copia[i][j].isOcupado()) {
                    disponibles++;
                }
            }
        }

        return disponibles;
    }

    // ESPERAR A QUE TERMINE UN HILO

    private void esperar(Thread hilo) {

        try {

            hilo.join();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException("Se interrumpió la espera del hilo.", e);
        }
    }
}
