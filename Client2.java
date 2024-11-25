import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client2 {
    public static void main(String[] args) throws Exception {
        try(Scanner scanner = new Scanner(System.in)){
            //System.out.print("Host:");
            String host = "localhost";//scanner.nextLine();
            System.out.print("\nPath:");
            String file=scanner.nextLine();
            //System.out.print("\nPort:");
            int port = 5000;//scanner.nextInt();

      

            // Conectar al servidor
            try(Socket socket = new Socket(host, port);
            //DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            FileInputStream fileIn = new FileInputStream(file)){

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileIn.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                System.out.println("Archivo enviado exitosamente.");
            }
        }

    }
}
