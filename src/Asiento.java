
public class Asiento {

    private boolean ocupado;
    private String nombre;
    private int edad;

    public Asiento() {
        this.ocupado = false;
        this.nombre = "";
        this.edad = 0;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public String getNombre() {
        return nombre;
    }

    public int getEdad() {
        return edad;
    }

    public void ocupar(String nombre, int edad) {
        this.ocupado = true;
        this.nombre = nombre;
        this.edad = edad;
    }

    public void liberar() {
        this.ocupado = false;
        this.nombre = "";
        this.edad = 0;
    }
}
