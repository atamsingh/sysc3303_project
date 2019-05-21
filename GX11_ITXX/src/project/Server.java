package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
		
	DatagramSocket receiveSocket;
	DatagramPacket receivePacket;
	
	/**
	 * Constructor for the server listener class
	 * Initializes socket on port 69
	 */
	public Server() {
		try {
			receiveSocket = new DatagramSocket(69);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
	}
	
	/**
	 * receives datagram packet and creates client connection thread to deal with transfer of information
	 */
	public void listen() {
		while(true) {
			byte[] data = new byte[100];
			receivePacket = new DatagramPacket(data, data.length);
			System.out.println("Server: Waiting for message...");
			
			//receive packet
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			//packet received --create client connection thread--
			System.out.println("Server: Packet received, creating client connection thread");
			Thread clientConnectionThread = new Thread(new ClientConnection(receivePacket));
			clientConnectionThread.start();
		}		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub 
		Server server = new Server();
		server.listen();
	}
}









