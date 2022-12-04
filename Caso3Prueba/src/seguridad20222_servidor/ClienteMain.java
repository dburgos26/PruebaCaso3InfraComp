package seguridad20222_servidor;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClienteMain {
    
    public static void main(String[] args) throws UnknownHostException, IOException {

        //Preguntar por la cantidad

        System.out.println ("Por favor introduzca una la cantidad de clientes que necesita:");
        String cantidadStr = null;
        try (Scanner entradaEscaner = new Scanner (System.in)) {
            cantidadStr = entradaEscaner.nextLine (); 
        }
        int cantidad = Integer.parseInt(cantidadStr);

        //Generar los clientes

        // prueba 2

        for(int i = 0; i < cantidad; i++){

            Socket sc = new Socket("localhost", 4030);
            ClienteThread t = new ClienteThread(sc, "" + i);
            t.start();

        }

    }

}
