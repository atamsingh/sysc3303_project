package project;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RequestListener implements Runnable {

    DatagramSocket receiveSocket;
    DatagramPacket sendPacket, receivePacket;

    public RequestListener(DatagramSocket receiveSocket) {
        this.receiveSocket = receiveSocket;
    }

    @Override
    public void run() {
        
    }
}