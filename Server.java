import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 5000;
        System.out.println("Servidor escuchando en el puerto " + port);
        getLastNameFile();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket = serverSocket.accept();
            System.out.println("Cliente conectado.");
            
            // Flujos de entrada y salida
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            // Recibir la clave pública del cliente
            byte[] clientPublicKeyBytes = new byte[in.readInt()];
            in.readFully(clientPublicKeyBytes);
            PublicKey clientPublicKey = KeyFactory.getInstance("DH").generatePublic(new X509EncodedKeySpec(clientPublicKeyBytes));
            
            // Generar la clave compartida
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(clientPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();
            SecretKey aesKey = new SecretKeySpec(sharedSecret, 0, 32, "AES");

            // Enviar la clave pública del servidor al cliente
            out.writeInt(keyPair.getPublic().getEncoded().length);
            out.write(keyPair.getPublic().getEncoded());
            
            // Preparar el descifrado
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            int addToPath=getLastNameFile();
            String path="response\\archivo_recibido"+(addToPath== 0 ? "":addToPath)+".txt";
            // Recibir y descifrar el archivo
            FileOutputStream fileOut = new FileOutputStream(path);
            CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher);
            
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                cipherOut.write(buffer, 0, bytesRead);
            }
            cipherOut.close();
            fileOut.close();
            socket.close();
            socket = serverSocket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            out.write(1);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String clientHash = reader.readLine();
            
            // Calcular Hash
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(path);
            while ((bytesRead = fis.read(buffer)) != -1) {
                sha256.update(buffer, 0, bytesRead);
            }    
            String serverHash = new BigInteger(1, sha256.digest()).toString(16);
            
            if (serverHash.equals(clientHash)) {
                System.out.println("Transferencia exitosa: el hash coincide.");
            } else {
                System.out.println("Error: el hash no coincide.");
            }  
            fis.close();
            reader.close();
        }
    }

    private static int getLastNameFile(){
        String path="response";
        File folder=new File(path);
        File[] files=folder.listFiles();
        ArrayList<String> arrayFilesPath=new ArrayList<>();
        for(File file:files){
            arrayFilesPath.add(file.getAbsolutePath());
        }
        if (!arrayFilesPath.isEmpty()) {
            String lastFile=arrayFilesPath.get(arrayFilesPath.size()-1);
            String lastCharacter=lastFile.charAt(lastFile.length()-5)+"";
            System.out.println(lastCharacter);
            if ("o".equals(lastCharacter)) {
                return 2;
            }else{
                return Integer.parseInt(lastCharacter)+1;
            }
        }else return 0;
    }
}
