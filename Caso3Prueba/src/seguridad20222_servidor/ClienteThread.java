package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteThread extends Thread {

    private Socket sc = null;
    private PrintWriter ac = null;
    private BufferedReader dc = null;



    public ClienteThread(Socket sc) {
        this.sc = sc;
    }

    public void run() {

        try {

            //Generar lineas de comunicacion

            ac = new PrintWriter(sc.getOutputStream() , true);
            dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

            //Envio de Secure Int

            ac.println(1);

            //Recibir

            String G = dc.readLine();
            String P = dc.readLine();
            String Gx = dc.readLine();
            String Fk_w = dc.readLine();

            //Manejo de la primeraa respuesta (validacion y envio)

            //Calcular Gy

            //Generar Key

            //Envio de mensaje y el hmac

            //Recibir respuesta y hmac

            //Validar integridad del mensaje

            //Verificar mensaje

            //Enviar ultima respuesta

            //Cerrar conexion

            sc.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
}
