package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RequestListener implements Runnable {

    DatagramSocket receiveSocket;
    DatagramPacket receivePacket,forwardPacket;
    
    int verbose_mode;
    boolean keepRunning = true;
    int max_connections = 60000;
    Thread[] all_threads = new Thread[max_connections];
    
	public RequestListener(DatagramSocket receiveSocket, DatagramPacket forwardPacket, int v) {
        this.receiveSocket = receiveSocket;
        this.verbose_mode = v;
        this.forwardPacket = forwardPacket;
	}
	
	public void receiveRequest() throws Exception {
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		System.out.println("Server: Waiting for request.\n");

		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			System.exit(1);
		}

		Thread clientConnectionThread = new Thread(new ClientConectionThread(receivePacket))
		clientConnectionThread.start();
	}
	
	public void close() {
		this.keepRunning = false;
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
