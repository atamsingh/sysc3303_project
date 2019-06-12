package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Server {
		
	public DatagramSocket receiveSocket;
	public DatagramPacket receivePacket;
	public static int v = 0;
	
	/**
	 * Constructor for the server listener class
	 * Initializes socket on port 69
	 */
	public Server() {
		try {
			receiveSocket = new DatagramSocket(69);
			receive();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		
	}
	
	private void receive() {
		while(true) {
			byte[] msg = new byte [100];
			receivePacket = new DatagramPacket(msg, msg.length);
			try {
				receiveSocket.receive(receivePacket);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Thread requestListenerThread = new Thread(new RequestListener(receiveSocket,receivePacket, v));
			requestListenerThread.start();
		}
		
	}
	

	private void closeSocket() {
		receiveSocket.close();
		System.out.println("Server terminating.");
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("[SERVER]Enter 1 for verbose mode or 0 for quiet mode!: ");
		int verbose = scanner.nextInt();//verbose mode will print out packet details
		v = verbose;
		String userInput = "";
		Server server = new Server();
		System.out.println("Server: Waiting for message...");
		

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