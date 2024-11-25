import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    public static void main(String[] args) throws Exception {
        try(Scanner scanner = new Scanner(System.in)){
            //System.out.print("Host:");
            String host = "localhost";//scanner.nextLine();
            System.out.print("\nPath:");
            String file=scanner.nextLine();
            //System.out.print("\nPort:");
            int port = 5000;//scanner.nextInt();

            // Generación de la clave pública y acuerdo Diffie-Hellman
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
            keyPairGen.initialize(2048);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(keyPair.getPrivate());

            // Conectar al servidor
            Socket socket = new Socket(host, port);

            // Flujos de entrada y salida
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Enviar la clave pública del cliente
            byte[] clientPublicKeyBytes = keyPair.getPublic().getEncoded();
            out.writeInt(clientPublicKeyBytes.length);
            out.write(clientPublicKeyBytes);

            // Recibir la clave pública del servidor
            byte[] serverPublicKeyBytes = new byte[in.readInt()];
            in.readFully(serverPublicKeyBytes);
            PublicKey serverPublicKey = KeyFactory.getInstance("DH").generatePublic(new X509EncodedKeySpec(serverPublicKeyBytes));

            // Generar la clave compartida
            keyAgreement.doPhase(serverPublicKey, true);
            byte[] sharedSecret = keyAgreement.generateSecret();
            SecretKey aesKey = new SecretKeySpec(sharedSecret, 0, 32, "AES");

            // Preparar el cifrado
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            FileInputStream fileIn = new FileInputStream(file);
            CipherOutputStream cipherOut = new CipherOutputStream(out, cipher);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                cipherOut.write(buffer, 0, bytesRead);
            }
            cipherOut.close();
            fileIn.close();
            socket.close();
            socket = new Socket(host, port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            in.read();
            //Calcular Hash
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            fileIn = new FileInputStream(file);
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                sha256.update(buffer, 0, bytesRead);
            }
            fileIn.close();
            String fileHash = new BigInteger(1, sha256.digest()).toString(16);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(fileHash);
            writer.newLine();
            writer.flush();

            in.close();
            out.close();
            socket.close();
            scanner.close();
        }

    }
}
