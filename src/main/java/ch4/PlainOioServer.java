package ch4;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

public class PlainOioServer {
    public void serve(int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                final Socket client = serverSocket.accept();
                System.out.println("Accepted connection from" + client.toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OutputStream out = client.getOutputStream();
                            out.write("Hi\r\n".getBytes(Charset.forName("UTF-8")));
                            out.flush();
                            client.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                client.close();
                            } catch (IOException ioException) {
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
