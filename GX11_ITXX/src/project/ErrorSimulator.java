package project; 
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ErrorSimulator {
	DatagramPacket sendPacket, receivePacket,sendbackPacket;
	DatagramSocket sendReceiveSocket, receiveSocket;
	int verbose;
	boolean delay, dup, lose; 
	
	public ErrorSimulator(int verbose,int error) {
		try {
			
			sendReceiveSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(23);
		}catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		this.verbose = verbose; 
		if(error==0) {
			delay = false; dup = false; lose = false;
		}else if(error == 1) {
			delay = false; dup = false; lose = true;
		}else if(error == 2) {
			delay = true; dup = false; lose = false;
		}else if(error == 3) {
			delay = false; dup = true; lose = false;
		}
	}
	
	public void receiveandSend() {
		while(true) {
			byte data[] = new byte[516]; 
			sendbackPacket = new DatagramPacket(data,data.length);//receive packet not overwritten to hold information necessary to send back to client
			System.out.println("Error Simulator: Waiting for Packet.\n");
			try {//receive packet from client
				receiveSocket.receive(sendbackPacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			System.out.println("Error Simulator: Packet received");
			//////print information //////////////
			if(verbose==1) {
				System.out.println("From Client: " + sendbackPacket.getAddress());
		        System.out.println("Host port: " + sendbackPacket.getPort());
		        if(sendbackPacket.getData()[1]== (byte) 1) {
		        	System.out.println("Packet type: Read Request");
		        }else if(sendbackPacket.getData()[1] == (byte)2){
		        	System.out.println("Packet type: Write Request");
		        }else if(sendbackPacket.getData()[1] == (byte)3){
		        	System.out.println("Packet type: DATA BLOCK");
		        }else if(sendbackPacket.getData()[1] == (byte)4){
		        	System.out.println("Packet type: ACK BLOCK");
		        }
		        if(sendbackPacket.getData()[1] != (byte)1 || sendbackPacket.getData()[1] != (byte)2) {
		        	System.out.println("Block number is: "+ sendbackPacket.getData()[2]+ " "+ sendbackPacket.getData()[3]);
		        }				
				System.out.println("Number of bytes: "+ sendbackPacket.getLength());
			}
			
			try {//create packet to send to server
				if(receivePacket == null) {//client connection thread not yet created
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),69);
				}else {
					//get the port of the client connection thread
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),receivePacket.getPort());
				}
				
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			try {//send packet to server
				System.out.println("Error Simulator: packet formed");
				sendReceiveSocket.send(sendPacket);
				System.out.println("Error Simulator: packet sent");
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			
			////RECEIVE PACKET FROM SERVER//////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Error Simulator: Waiting for Packet...\n");
			byte[]msg = new byte [100];
			receivePacket = new DatagramPacket(msg,msg.length);
			try {//receive packet from server
				sendReceiveSocket.receive(receivePacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			//////print information it received////////////////////////////////////////////////
			System.out.println("Error Simulator: Packet received");
			if(verbose==1) {
				System.out.println("From Server: " + receivePacket.getAddress());
		        System.out.println("Host port: " + receivePacket.getPort());
		        if(receivePacket.getData()[1]== (byte) 1) {
		        	System.out.println("Packet type: Read Request");
		        }else if(receivePacket.getData()[1] == (byte)2){
		        	System.out.println("Packet type: Write Request");
		        }else if(receivePacket.getData()[1] == (byte)3){
		        	System.out.println("Packet type: DATA BLOCK");
		        }else if(receivePacket.getData()[1] == (byte)4){
		        	System.out.println("Packet type: ACK BLOCK");
		        }
		        if(receivePacket.getData()[1] != (byte)1 || receivePacket.getData()[1] != (byte)2) {
		        	System.out.println("Block number is: "+ receivePacket.getData()[2]+ " "+ receivePacket.getData()[3]);
		        }				
				System.out.println("Number of bytes: "+ receivePacket.getLength());
			}
			
			try {//form packet to send back to client
				sendPacket = new DatagramPacket(msg,receivePacket.getLength(),InetAddress.getLocalHost(),sendbackPacket.getPort());	
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			try {//send packet back to client
				//////print information before sending
				System.out.println("Error Simulator: packet formed");
				sendReceiveSocket.send(sendPacket);
				System.out.println("Error Simulator: packet sent");
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			

		}

	}

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner scanner = new Scanner(System.in);
		System.out.println("[ERROR SIM]Enter 1 for verbose mode or 0 for quiet mode!: ");
		int verbose = scanner.nextInt();//verbose mode will print out packet details
		System.out.println("[ERROR SIM]Enter 0: normal operation; 1 : lose a packet; 2 : delay a packet, 3 : duplicate a packet.");
		int error = scanner.nextInt();
		ErrorSimulator h = new ErrorSimulator(verbose,error);
		h.receiveandSend();

	}

}
