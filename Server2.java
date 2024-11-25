import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server2 {
    public static void main(String[] args) throws Exception {
        int port = 5000;
        System.out.println("Servidor escuchando en el puerto " + port);
        getLastNameFile();
        int addToPath=getLastNameFile();
        String path="response2\\archivo_recibido"+(addToPath== 0 ? "":addToPath)+".txt";
        try (ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            //DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            FileOutputStream fileOut = new FileOutputStream(path);
        ) {
            
            System.out.println("Cliente conectado.");
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
            }
            System.out.println("Archivo recibido y guardado en: " + path);
          
        }

        
    }

    private static int getLastNameFile(){
        String path="response2";
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
