
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) {

        SalaCine cine = new SalaCine();
        int opcion;

        do {
            System.out.println("\n===== SISTEMA DE CINE =====");
            System.out.println("1. Reservar asiento");
            System.out.println("2. Liberar asiento");
            System.out.println("3. Buscar usuario por nombre");
            System.out.println("4. Buscar por asiento");
            System.out.println("5. Mostrar sala");
            System.out.println("6. Contar asientos disponibles");
            System.out.println("7. Probar reservas concurrentes");
            System.out.println("0. Salir");

            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {
                case 1 -> {
                    System.out.print("Nombre del usuario: ");
                    String nombre = teclado.nextLine().trim();

                    int edad = leerEntero("Edad: ");
                    int fila = leerEntero("Fila (1-8): ");
                    int columna = leerEntero("Columna (1-10): ");

                    cine.reservar(fila, columna, nombre, edad);
                }

                case 2 -> {
                    int fila = leerEntero("Fila (1-8): ");
                    int columna = leerEntero("Columna (1-10): ");

                    cine.liberar(fila, columna);
                }

                case 3 -> {
                    System.out.print("Nombre que deseas buscar: ");
                    String nombre = teclado.nextLine().trim();

                    List<String> resultados =
                            cine.buscarPorNombreConcurrente(nombre);

                    if (resultados.isEmpty()) {
                        System.out.println(
                                "No se encontró al usuario.");
                    } else {
                        System.out.println("Resultados:");

                        for (String resultado : resultados) {
                            System.out.println(resultado);
                        }
                    }
                }

                case 4 -> {
                    int fila = leerEntero("Fila (1-8): ");
                    int columna = leerEntero("Columna (1-10): ");

                    cine.buscarPorAsiento(fila, columna);
                }

                case 5 -> cine.mostrarSala();

                case 6 -> {
                    int disponibles =
                            cine.contarDisponiblesConcurrente();

                    System.out.println(
                            "Asientos disponibles: " + disponibles
                                    + " de 80.");

                    System.out.println(
                            "Asientos ocupados: " + (80 - disponibles));
                }

                case 7 -> {
                    int fila = leerEntero(
                            "Fila para la prueba (1-8): ");
                    int columna = leerEntero(
                            "Columna para la prueba (1-10): ");

                    cine.probarReservasConcurrentes(fila, columna);
                }

                case 0 ->
                        System.out.println("Programa finalizado.");

                default ->
                        System.out.println("Opción no válida.");
            }

        } while (opcion != 0);

        teclado.close();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = teclado.nextLine().trim();

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Error: introduce un número entero.");
            }
        }
    }
}
