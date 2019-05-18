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
		}catch(SocketException se) {
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
			receivePacket = new DatagramPacket(data,data.length);
			System.out.println("Server: Waiting for message...");
			
			try {//receive packet
				receiveSocket.receive(receivePacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			//packet received --create client connection thread--
			System.out.println("Server: Packet received, creating client connection thread");
			Thread CCT = new Thread(new Client_Connection_Thread(receivePacket), "client connection thread");
			CCT.start();
		}		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server listener = new Server();
		listener.listen();
	}
}

/**
 * Class for the client connection thread
*/
class Client_Connection_Thread implements Runnable
{
	DatagramPacket receivePacket, sendPacket;
	DatagramSocket sendReceiveSocket; 
	
	/**
	 * Constructor for client connection thread class
	 * @param p - packet received from listener server
	 */
	public Client_Connection_Thread(DatagramPacket p) {
		try {
			sendReceiveSocket = new DatagramSocket();
		}catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		receivePacket = p; 	
		//send ACK data block code should be below
	}
	
	public void run() {
		byte[] data = new byte[512];//not sure
		receivePacket = new DatagramPacket(data,data.length);
		// receive first block of data
		try {
			sendReceiveSocket.receive(receivePacket);
		}catch(IOException e) {
			//check out what errors should do
			e.printStackTrace();
			System.exit(1);
		}
		/**
		 * Code to extract and write data to file should be here. 
		 */
		
		/**
		 * code to form ack data block should be here
		 */
		try {
			sendReceiveSocket.send(sendPacket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}	
}











