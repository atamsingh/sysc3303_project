package project; 
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ErrorSimulator {
	DatagramPacket sendPacket, receivePacket,sendbackPacket, dupPacket;
	DatagramSocket sendReceiveSocket, receiveSocket;
	int verbose,errorblock,timeout;//error block is the block number to sim error on
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
			System.out.println("[ERROR SIM]Enter the amount of time in seconds to delay for");
			Scanner scanner = new Scanner(System.in);
			 timeout = scanner.nextInt();
			 timeout = timeout*1000;//change to milliseconds
			scanner.close();
		}else if(error == 3) {
			delay = false; dup = true; lose = false;
		}
		if(error!=0) {
			System.out.println("[ERROR SIM]Enter the block number you want the error to occur on");
			Scanner scanner = new Scanner(System.in);
			 errorblock = scanner.nextInt();
			scanner.close();
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
				//get the port of the client connection thread. Now transmitting data
				sendPacket = new DatagramPacket(data,sendbackPacket.getLength(),InetAddress.getLocalHost(),receivePacket.getPort());
			}					
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			if(lose) {
				if(Commons.getBlockNumber(sendPacket)!=errorblock) {
					//not errorblock so send as usual
					try {//send packet to server
						System.out.println("Error Simulator: packet formed");
						sendReceiveSocket.send(sendPacket);
						System.out.println("Error Simulator: packet sent");
					}catch(IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				}
				System.out.println("Error simulator: Losing packet");
			}else {
				try {//send packet to server
					System.out.println("Error Simulator: packet formed");
					if(delay && Commons.getBlockNumber(sendPacket)==errorblock) {//if delay is true then make error sim sleep before sending
						try {
							System.out.println("Error simulator: delaying packet");
							Thread.sleep(timeout);
						}catch(InterruptedException t) {
							t.printStackTrace();
							System.exit(1);
						}						
					}
					if(dup && sendPacket.getData()[1] == (byte)3 && Commons.getBlockNumber(dupPacket)==errorblock) {
						sendReceiveSocket.send(dupPacket);
						dup = false; //turn of dup to avoid getting stuck here
					}else {
						sendReceiveSocket.send(sendPacket);
						dupPacket = sendPacket;
					}
					System.out.println("Error Simulator: packet sent");
				}catch(IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
				
				
			
			
			
			
			////RECEIVE PACKET FROM SERVER//////////////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Error Simulator: Waiting for Packet...\n");
			byte[]msg = new byte [516];
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
			if(lose) {
				if(Commons.getBlockNumber(receivePacket)!=errorblock) {
					//not errorblock then send as usual
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
			}else {
				try {//send packet back to client
					//////print information before sending
					System.out.println("Error Simulator: packet formed");
					if(delay && Commons.getBlockNumber(sendPacket)==errorblock) {//if delay is true then make error sim sleep before sending
						try {
							System.out.println("Error simulator: delaying packet");
							Thread.sleep(timeout);
						}catch(InterruptedException t) {
							t.printStackTrace();
							System.exit(1);
						}						
					}
					if(dup && sendPacket.getData()[1] == (byte)3 && Commons.getBlockNumber(dupPacket)==errorblock) {
						//if its a data block and dup is true 
						sendReceiveSocket.send(dupPacket);
						dup = false; //turn of dup to avoid getting stuck here
					}else {
						sendReceiveSocket.send(sendPacket);
						dupPacket = sendPacket;
					}
					System.out.println("Error Simulator: packet sent");
				}catch(IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
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
