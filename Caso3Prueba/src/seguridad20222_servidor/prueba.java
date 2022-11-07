package seguridad20222_servidor;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class prueba {

    private Socket sc = null;


    public prueba(Socket sc) {
        this.sc = sc;
    }

    public void iniciar() throws IOException, InterruptedException{

        PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
		BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

        ac.println(1);

        String mensaje = dc.readLine();

        System.out.println("1: " + mensaje);

        mensaje = dc.readLine();

        System.out.println("2: " +mensaje);

        mensaje = dc.readLine();

        System.out.println("3: " +mensaje);

        mensaje = dc.readLine();

        System.out.println("4: " +mensaje);

        ac.println(mensaje);

        sc.close();
    }

    public static void main(String[] args) {
        try {
            Socket sc = new Socket("localhost", 4030);
            prueba p = new prueba(sc);
            p.iniciar();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
