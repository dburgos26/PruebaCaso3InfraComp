package seguridad20222_servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
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
            //prueba 2

            ac = new PrintWriter(sc.getOutputStream() , true);
            dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));
            publicaServidor = sf.read_kplus("datos_asim_srv.pub" , "concurrent server " + id + ": ");

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

            if(verificacion)
            {
                ac.println("OK");
                System.out.println(nombre + "se valido correctamente la firma que envio el servidor");
                //Calcular y enviar Gy

                SecureRandom r = new SecureRandom();
                int y = Math.abs(r.nextInt());
                Long longy = Long.valueOf(y);
                BigInteger biy = BigInteger.valueOf(longy);
                
                BigInteger bigP = new BigInteger(P);
                BigInteger bigG = new BigInteger(G);
                BigInteger bigGx = new BigInteger(Gx);
                
                BigInteger Gy = G2Y(bigG, biy, bigP);
                
                System.out.println(nombre + "Se genero Gy: " + Gy);
                
                ac.println(Gy.toString());
                
                //Generar Key
                
                BigInteger llave_maestra = calcular_llave_maestra(bigGx,biy,new BigInteger(P));
                String str_llave = llave_maestra.toString();
                SecretKey sk_cli = sf.csk1(str_llave);
                SecretKey sk_mac = sf.csk2(str_llave);
                
                //Generar HMAC
                byte[] iv1 = generateIvBytes();
                String str_iv1 = byte2str(iv1);
                IvParameterSpec ivSpec1 = new IvParameterSpec(iv1);
                
                //Envio de mensaje, iv y el hmac
                SecureRandom srcon = new SecureRandom();
                int n = Math.abs(srcon.nextInt()); //Se genera un numero random para realizar la consulta
                System.out.println("Cliente "+this.id+ ": Consulta a realizar: "+ n);

                String consulta = Integer.toString(n);
                byte[] byte_consulta = consulta.getBytes();
                byte[] enc_consulta = sf.senc(byte_consulta, sk_cli,ivSpec1, "Cliente");
                String str_consulta = byte2str(enc_consulta);
                
                byte [] consulta_mac = sf.hmac(byte_consulta, sk_mac);
                String str_consulta_mac = byte2str(consulta_mac);
                
                ac.println(str_consulta);
                ac.println(str_consulta_mac);
                ac.println(str_iv1);
                
                //Recibir respuesta y hmac
                String conf_srv = dc.readLine();
                if (!conf_srv.equals("OK"))
                {
                    System.out.println("==========> Server couldnt verify query integrity");
                    //Matar cliente
                }
                else
                {
                    //Validar integridad del mensaje
                    System.out.println("==========> Server could verify query integrity).");
                    String str_rta_consulta = dc.readLine();
                    String str_rta_mac = dc.readLine();
                    String str_iv2 = dc.readLine();
                    byte[] byte_rta_consulta = str2byte(str_rta_consulta);
                    byte[] byte_rta_mac = str2byte(str_rta_mac);
                            
                    byte[] iv2 = str2byte(str_iv2);
                    IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);
                    byte[] descifrado = sf.sdec(byte_rta_consulta, sk_cli,ivSpec2);
                    boolean verificar = sf.checkInt(descifrado, sk_mac, byte_rta_mac);
                    System.out.println("Cliente "+ this.id + ": Integrity check:" + verificar); 
                
                    //Verificar mensaje
                    if (verificar) 
                    {
                        System.out.println("==========> Server sends matching answer and MAC");
                
                        String str_original = new String(descifrado, StandardCharsets.UTF_8);
                        System.out.println("Cliente "+this.id+": Respuesta del servidor: " + str_original);

                        //Enviar ultima respuesta
                        ac.println("OK");
                    }
                    else
                    {
                        // In this case, the server send query and MAC that do not check
                        //Enviar ultima respuesta 
                        ac.println("ERROR");
                        System.out.println("==========> Server sends not matching answer and MAC");
                    }
                
                } 
            } 
            else 
            {
                ac.println("ERROR");
                System.out.println(nombre + "se encontro un error la firma que envio el servidor");
            }
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

    private byte[] generateIvBytes() {
	    byte[] iv = new byte[16];
	    new SecureRandom().nextBytes(iv);
	    return iv;
	}

    private BigInteger calcular_llave_maestra(BigInteger base, BigInteger exponente, BigInteger modulo) {
		return base.modPow(exponente, modulo);
	}
    
}
