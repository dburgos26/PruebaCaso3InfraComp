package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.SecretKey;

public class ClienteThread extends Thread {

    private Socket sc = null;
    private PrintWriter ac = null;
    private BufferedReader dc = null;
    private String id = null;
    private SecurityFunctions sf = new SecurityFunctions();
    private PublicKey publicaServidor = null;
    private String nombre = null;


    public ClienteThread(Socket sc, String id) throws IOException {
        this.sc = sc;
        this.id = id;
        this.nombre = "Cliente " + id + ": ";
    }

    public void run() {

        try {

            //Generar lineas de comunicacion

            ac = new PrintWriter(sc.getOutputStream() , true);
            dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            publicaServidor = sf.read_kplus("datos_asim_srv.pub", "concurrent server " + id + ": ");

            //Envio de Secure init

            ac.println("SECURE INIT");

            //Recibir    

            String G = dc.readLine();
            String P = dc.readLine();
            String Gx = dc.readLine();
            String Fk_w = dc.readLine();

            String mensaje = G.toString()+","+P.toString()+","+Gx.toString();

            boolean verificacion = sf.checkSignature(publicaServidor, str2byte(Fk_w), mensaje);

            //Manejo de la primeraa respuesta (validacion y envio)

            if(verificacion){
                ac.println("OK");
                System.out.println(nombre + "se valido correctamente la firma");
            } else {
                ac.println("ERROR");
                System.out.println(nombre + "se encontro un error la firma");
                //Falta matar el cliente
            }

            //Calcular y enviar Gy

            SecureRandom r = new SecureRandom();
			int y = Math.abs(r.nextInt());
    		Long longy = Long.valueOf(y);
    		BigInteger biy = BigInteger.valueOf(longy);

            BigInteger bigP = new BigInteger(P);
            BigInteger bigG = new BigInteger(G);

            BigInteger Gy = G2Y(bigG, biy, bigP);

            System.out.println(nombre + "Se genero Gy: " + Gy);

            ac.println(Gy.toString());

            //Generar Key

            BigInteger llave_maestra = calcular_llave_maestra(Gy,biy,new BigInteger(P));
    		String str_llave = llave_maestra.toString();

            SecretKey sk_srv = sf.csk1(str_llave);
			SecretKey sk_mac = sf.csk2(str_llave);

            //Envio de mensaje y el hmac

            //Recibir respuesta y hmac

            //Validar integridad del mensaje

            //Verificar mensaje

            //Enviar ultima respuesta

            //Cerrar conexion

            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public byte[] str2byte( String ss)
	{	
		// Encapsulamiento con hexadecimales
		byte[] ret = new byte[ss.length()/2];
		for (int i = 0 ; i < ret.length ; i++) {
			ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
		}
		return ret;
	}
	
	public String byte2str( byte[] b )
	{	
		// Encapsulamiento con hexadecimales
		String ret = "";
		for (int i = 0 ; i < b.length ; i++) {
			String g = Integer.toHexString(((char)b[i])&0x00ff);
			ret += (g.length()==1?"0":"") + g;
		}
		return ret;
	}

    private BigInteger G2Y(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente,modulo);
	}

    private BigInteger calcular_llave_maestra(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente, modulo);
	}
    
}
