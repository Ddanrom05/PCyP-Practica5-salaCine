
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner teclado =
            new Scanner(System.in);

    public static void main(String[] args) {

        SalaCine cine = new SalaCine();

        int opcion;

        do {

            System.out.println("\n==============================");
            System.out.println("       SISTEMA DE CINE");
            System.out.println("==============================");
            System.out.println("1. Reservar asiento");
            System.out.println("2. Liberar asiento");
            System.out.println("3. Buscar por nombre");
            System.out.println("4. Buscar por asiento");
            System.out.println("5. Mostrar sala");
            System.out.println("6. Contar asientos disponibles");
            System.out.println("0. Salir");

            opcion = leerEntero("Selecciona una opción: ");

            switch (opcion) {

                case 1 -> {

                    System.out.print("Nombre: ");
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

                    System.out.print("Nombre a buscar: ");
                    String nombre = teclado.nextLine().trim();

                    List<String> resultados = cine.buscarPorNombreConcurrente(nombre);

                    if (resultados.isEmpty()) {

                        System.out.println("Usuario no encontrado.");

                    } else {

                        System.out.println("\nUsuarios encontrados:");

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

                    int disponibles = cine.contarDisponiblesConcurrente();

                    System.out.println("\nAsientos disponibles: " + disponibles);

                    System.out.println("Asientos ocupados: " + (80 - disponibles));
                }

                case 0 -> System.out.println("Fin del programa.");

                default -> System.out.println("Opción inválida.");
            }

        } while (opcion != 0);

        teclado.close();
    }

    // Lectura segura de números enteros.
    private static int leerEntero(String mensaje) {

        while (true) {

            System.out.print(mensaje);

            String entrada = teclado.nextLine().trim();

            try {

                return Integer.parseInt(entrada);

            } catch (NumberFormatException e) {

                System.out.println("Error: introduce un número entero.");
            }
        }
    }
}
