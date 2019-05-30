package project; 
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ErrorSimulator {
	DatagramPacket sendPacket, receivePacket,sendbackPacket, dupPacket;
	DatagramSocket sendReceiveSocket, receiveSocket;
	int verbose,errorblock,timeout;//error block is the block number to sim error on
	boolean delay, dup, lose; 
	TalkToServer serverlink;
	TalkToClient clientlink;
	byte eblocktype;
	
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
			System.out.println("[ERROR SIM]Enter the amount of time in seconds to delay for");
			Scanner scanner = new Scanner(System.in);
			 timeout = scanner.nextInt();
			 timeout = timeout*1000;//change to milliseconds
		}else if(error == 3) {
			delay = false; dup = true; lose = false;
		}
		if(error!=0) {
			System.out.println("[ERROR SIM]Enter the block number you want the error to occur on");
			Scanner scan = new Scanner(System.in);
			
			errorblock = scan.nextInt();
			
			System.out.println("[ERROR SIM]Enter the block type you want the error to occur on\nEnter 1: RRQ block; 2: WRQ block; 3: DATA block; 4: ACK block");
			int temp = scan.nextInt();
			eblocktype = (byte) temp;
			//scanner.close();
		}
		serverlink = new TalkToServer();
		clientlink = new TalkToClient();
	}
	
	public void printInfo(String s, DatagramPacket packet) {
		System.out.println("From " + s + ": " + packet.getAddress());
        System.out.println("Host port: " + packet.getPort());
        if(packet.getData()[1]== (byte) 1) {
        	System.out.println("Packet type: Read Request");
        }else if(packet.getData()[1] == (byte)2){
        	System.out.println("Packet type: Write Request");
        }else if(packet.getData()[1] == (byte)3){
        	System.out.println("Packet type: DATA BLOCK");
        }else if(packet.getData()[1] == (byte)4){
        	System.out.println("Packet type: ACK BLOCK");
        }
        if(packet.getData()[1] != (byte)1 || packet.getData()[1] != (byte)2) {
        	System.out.println("Block number is: "+ packet.getData()[2]+ " "+ packet.getData()[3]);
        }				
		System.out.println("Number of bytes: "+ packet.getLength());
	}
	
	public void receiveandSend() {
		while(true) {
			//////////////////////////////RECEIVING FROM CLIENT//////////////////////////////////////////////////
			byte data[] = new byte[516]; 
			sendbackPacket = new DatagramPacket(data,data.length);//receive packet not overwritten to hold information necessary to send back to client
			System.out.println("Error Simulator: Waiting for Packet from client...\n");
			
			try {//receive packet from client
				receiveSocket.receive(sendbackPacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Error Simulator: Packet received");
			//////print information //////////////
			if(verbose==1) {
				printInfo("Client", sendbackPacket);
			}/////end of verbose if
			
			//////////////SEND PACKET FORMATION////////////////////////////////////////
			try {//create packet to send to server
				if(receivePacket == null) {//client connection thread not yet created so send to server to create threads
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),69);
				}else {
					//get the port of the client connection thread. Now transmitting data
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),receivePacket.getPort());
				}					
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			/////////////SENDING TO THE SERVER -------ERROR OP CHECKS----------
			if(lose) {
				losePacket("server");							
			}else if(delay){
				delayPacket("server");
			}else if(dup) {
				dupPacket("server");
			}
			
			////RECEIVE PACKET FROM SERVER//////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Error Simulator: Waiting for Packet from server...\n");
			byte[]msg = new byte [516];
			receivePacket = new DatagramPacket(msg,msg.length);
			try {//receive packet from server
				receiveSocket.receive(receivePacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			//////print information it received////////////////////////////////////////////////
			System.out.println("Error Simulator: Packet received");
			if(verbose==1) {
				printInfo("Server",receivePacket);
			}
			
			//////////////SEND PACKET FORMATION///////////////////////////////////////////////////////////////////
			try {//form packet to send back to client
				sendPacket = new DatagramPacket(msg,receivePacket.getLength(),InetAddress.getLocalHost(),sendbackPacket.getPort());	
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			////////////////////SENDING BACK TO CLIENT ------ ERROR OP CHECKS/////////////////////////////////////
			
			if(lose) {
				losePacket("client");							
			}else if(delay){
				delayPacket("client");
			}else if(dup) {
				dupPacket("client");
			}
		}
	}

	

	
	public void losePacket(String destination) {
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[3]==eblocktype) {//check block number and block type
			//dont send anything if current block number are type match the user specified ones
			System.out.println("Error Simulator: Losing packet!");
		}else {
			//send as usual
			if(destination == "server") {//sending to server
				serverlink.sendServer(sendPacket);
			}else {//sending to client
				clientlink.sendClient(sendPacket);
			}
			
		}
	}
	
	public void delayPacket(String destination) {
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[3]==eblocktype) {//check block number and block type
			//sleep to simulate delay
			System.out.println("Error Simulator: Delaying packet!");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(destination == "server") {//sending to server
				serverlink.sendServer(sendPacket);
			}else {//sending to client
				clientlink.sendClient(sendPacket);
			}
			
		}else {
			//send as usual
			if(destination == "server") {//sending to server
				serverlink.sendServer(sendPacket);
			}else {//sending to client
				clientlink.sendClient(sendPacket);
			}
		}
	}
	
	public void dupPacket(String destination) {
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[3]==eblocktype) {//check block number and block type
			//send dupPacket
			System.out.println("Error Simulator: Sending dup packet!");
			
			if(destination == "server") {//sending to server
				serverlink.sendServer(dupPacket);
			}else {//sending to client
				clientlink.sendClient(dupPacket);
			}
			
		}else {
			//send as usual
			
			if(destination == "server") {//sending to server
				serverlink.sendServer(sendPacket);
			}else {//sending to client
				clientlink.sendClient(sendPacket);
			}
			
			dupPacket = sendPacket;
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
