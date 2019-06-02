package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Server {
		
	public DatagramSocket receiveSocket;
	public DatagramPacket receivePacket;
	
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
	
	private DatagramPacket receive() {
		byte[] msg = new byte [100];
		receivePacket = new DatagramPacket(msg, msg.length);
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return receivePacket;
	}
	

	private void closeSocket() {
		receiveSocket.close();
		System.out.println("Server terminating.");
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("[SERVER]Enter 1 for verbose mode or 0 for quiet mode!: ");
		int verbose = scanner.nextInt();//verbose mode will print out packet details
		
		String userInput = "";
		Server server = new Server();
		System.out.println("Server: Waiting for message...");
		Thread requestListenerThread = new Thread(new RequestListener(server.getReceiveSocket(),server.receive(), verbose));
		requestListenerThread.start();

		try {
			while (!userInput.equals("shutdown")) {
				userInput = scanner.nextLine().toLowerCase();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		server.closeSocket();
		scanner.close();
	}

	private DatagramSocket getReceiveSocket() {
		return receiveSocket;
	}
}