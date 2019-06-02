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
	

	/**
	 * receives datagram packet and creates client connection thread to deal with transfer of information
	 */
	public void listen() {
		while(keepRunning) {
			byte[] data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Server: Waiting for message...");
			
			//receive packet
			/*try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				// wait on all threads to be closed.
				for(int i = 0; i <= all_threads.length; i++) {
					// join all the threads to the main one and wait for them to finish
					try {
						if(all_threads[i] != null) {
							all_threads[i].join();
						}
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					System.exit(1);
				}
			}*/
			
			//packet received --create client connection thread--
			System.out.println("Server: Packet received, creating client connection thread");
			Thread clientConnectionThread = new Thread(new ClientConnection(forwardPacket, this.verbose_mode));
			if(all_threads.length <= this.max_connections) {
				while(all_threads.length <= this.max_connections) {
					// wait till a connection is open 
				}
				all_threads[all_threads.length - 1] = clientConnectionThread;
				clientConnectionThread.start();
			}
		}
	}
	
	public void close() {
		this.keepRunning = false;
	}

    @Override
    public void run() {
        while (true) {
            try {
                listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
