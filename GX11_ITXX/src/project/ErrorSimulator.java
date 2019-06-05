package project; 
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ErrorSimulator {
	DatagramPacket sendPacket, receivePacket,sendbackPacket, dupPacket;
	DatagramSocket receiveSocket;
	int verbose,errorblock,timeout,erroroperation;//error block is the block number to sim error on
	boolean delay, dup, lose; 
	byte eblocktype;
	
	@SuppressWarnings("resource")
	public ErrorSimulator(int verbose,int error) {
		try {
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
			
			System.out.println("[ERROR SIM]Enter the block type you want the error to occur on\nEnter 3: DATA block; 4: ACK block");
			int temp = scan.nextInt();
			eblocktype = (byte) temp;
			//scanner.close();
		}
		System.out.println("ERROR TESTING\n");
		System.out.println("Enter 0: normal operation, 1: invalid TFTP opcode on RRQ or WRQ");
		Scanner scan = new Scanner(System.in);
		erroroperation = scan.nextInt();
		
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
		System.out.println(new String(packet.getData().toString()));
	}
	
	
	/**
	 * 
	 */
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
					System.out.println("sending to port 69");
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),69);
					//if error operation also change opcode
					if(erroroperation == 1) {
						sendPacket.getData()[1] = (byte) 8;
					}
				}else {
					//get the port of the client connection thread. Now transmitting data
					System.out.println("sending to port "+receivePacket.getPort());
					sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),receivePacket.getPort());
				}					
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			/////////////SENDING TO THE SERVER -------ERROR OP CHECKS----------
			if(lose) {
				boolean lost = losePacket("server");
				if(lost) {
					continue;
				}
			}else if(delay){
				delayPacket("server");
			}else if(dup) {
				dupPacket("server");
			}else {
				sendPacket("server");
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
				System.out.println("sending to port 69");
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
			}else {
				sendPacket("client");
			}
			
			System.out.println("---------");
		}
	}
	
	public boolean losePacket(String destination) {
		System.out.println("in lose option");
		System.out.println("BLOCKS received: " + Commons.getBlockNumber(sendPacket) + ", error block: "+ errorblock);
		System.out.println("TYPES received: " + sendPacket.getData()[1] + ", error type: "+ eblocktype);
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[1]==eblocktype) {//check block number and block type
			//dont send anything if current block number are type match the user specified ones
			lose = false; // reset still already lost once.
			System.out.println("Error Simulator: Losing packet!");
			return true;
		}else {
			//send as usual
			if(destination == "server") {//sending to server
				sendServer(sendPacket);
			}else {//sending to client
				sendClient(sendPacket);
			}
			return false;
		}
	}
	
	public void delayPacket(String destination) {
		System.out.println("in delay option");
		System.out.println("BLOCKS received: " + Commons.getBlockNumber(sendPacket) + ", error block: "+ errorblock);
		System.out.println("TYPES received: " + sendPacket.getData()[1] + ", error type: "+ eblocktype);
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[1]==eblocktype) {//check block number and block type
			delay = false; // reset still already lost once.
			//sleep to simulate delay
			System.out.println("Error Simulator: Delaying packet!");
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(destination == "server") {//sending to server
				sendServer(sendPacket);
			}else {//sending to client
				sendClient(sendPacket);
			}
			
		}else {
			//send as usual
			if(destination == "server") {//sending to server
				sendServer(sendPacket);
			}else {//sending to client
				sendClient(sendPacket);
			}
		}
	}
	
	public void dupPacket(String destination) {
		System.out.println("in dupe option");
		System.out.println("BLOCKS received: " + Commons.getBlockNumber(sendPacket) + ", error block: "+ errorblock);
		System.out.println("TYPES received: " + sendPacket.getData()[1] + ", error type: "+ eblocktype);
		if(Commons.getBlockNumber(sendPacket)==errorblock && sendPacket.getData()[3]==eblocktype) {//check block number and block type
			dup = false; // reset still already lost once.
			//send dupPacket
			System.out.println("Error Simulator: Sending dup packet!");
			
			if(destination == "server") {//sending to server
				sendServer(dupPacket);
			}else {//sending to client
				sendClient(dupPacket);
			}
			
		}else {
			//send as usual
			
			if(destination == "server") {//sending to server
				sendServer(sendPacket);
			}else {//sending to client
				sendClient(sendPacket);
			}
			
			dupPacket = sendPacket;
		}
	}
	
	public void sendPacket(String destination) {
		if(destination == "server") {//sending to server
			sendServer(sendPacket);
		}else {//sending to client
			sendClient(sendPacket);
		}
	}
	
	public void sendServer(DatagramPacket sendPacket) {
		try {//send packet to server
			System.out.println("Error Simulator: packet formed");
			receiveSocket.send(sendPacket);
			System.out.println("Error Simulator: packet sent");
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void sendClient(DatagramPacket sendPacket) {
		this.sendPacket = sendPacket;
		try {//send packet to server
			System.out.println("Error Simulator: packet formed");
			receiveSocket.send(this.sendPacket);
			System.out.println("Error Simulator: packet sent");
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
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
