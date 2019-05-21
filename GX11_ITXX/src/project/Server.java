package project;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;

public class Server {
	
	DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;

	Commons common = new Commons("SERVER");
	private int port_address =  69;

	public Server() {
		try {
			receiveSocket = new DatagramSocket(port_address);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	private void handleReadRequest() {

	}
	
	private void handleWriteRequest() {

	}
	
	private void handleRequest() {
		// if read
		this.handleReadRequest();
		// else if write
		this.handleWriteRequest();
		// else
		// handle error
	}
	
	public void startListening() {
		// listen to the port needed for requests 
		// create threads to handle these req. received
		// One thread will wait on port 69 for UDP datagrams (that should contain RRQ or WRQ packets)
		// create another thread (call it the client connection thread), and pass it the TFTP packet to deal with
		this.handleRequest(); // send data along
		// go back to waiting on port 69 for another request. 
	}

	public void main() {
		// get ready to accept 
		//  supporting multiple concurrent read and write connections with different clients
		// For each RRQ, the server should respond with DATA block 1 and 0 bytes of data (no file I/O). For each WRQ the server should respond with ACK block 0. 
		Scanner scanner = new Scanner(System.in);
		String userInput = "";
		Server server = new Server();
		Thread requestListenerThread = new Thread(new RequestListener(server.getReceiveSocket()));
		requestListenerThread.start();
		
		try {
			while (!userInput.equals("shutdown")) {
				userInput = scanner.nextLine().toLowerCase();
			}
		} catch (Exception e) {
					
		}
		
		// elegant shutdown
		server.closeSocket();
		// finish all file transfers,  but refuse to create new connections with clients
		
	}

	private void closeSocket() {
		receiveSocket.close();
		System.out.println("Terminating Server");
	}

	private DatagramSocket getReceiveSocket() {
		return receiveSocket;
	}
}
