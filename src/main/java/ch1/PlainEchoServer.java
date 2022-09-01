package ch1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PlainEchoServer {
    public void serve(int port) throws Exception {
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                            while (true) {
                                writer.println(reader.readLine());
                                writer.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                clientSocket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
