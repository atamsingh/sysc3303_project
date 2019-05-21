package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RequestListener implements Runnable {

    DatagramSocket receiveSocket;
    DatagramPacket sendPacket, receivePacket;

    public RequestListener(DatagramSocket receiveSocket) {
        this.receiveSocket = receiveSocket;
    }

    public void receiveRequest() throws Exception {
        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        
        try {
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.exit(1);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                receiveRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}