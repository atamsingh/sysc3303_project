import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream; 
import java.io.OutputStream;

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
			if(receivePacket.getData()[1]== (byte)1) {//read request
				Thread CRT = new Thread(new Client_Read_Thread(receivePacket),"client read thread");
				CRT.start();
			}else{//write request
				Thread CWT = new Thread(new Client_Write_Thread(receivePacket), "client write thread");
				CWT.start();
			}
			
		}		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub    
		Server listener = new Server();
		listener.listen();
		
	}
	
}

/*
 * Thread to handle client read requests.
 * NOT DONE YET JUST BARE BONES!
 */
class Client_Read_Thread implements Runnable
{
	DatagramPacket receivePacket, sendPacket;
	DatagramSocket sendReceiveSocket; 
	String FILEPATH;
	OutputStream os;
	
	/**
	 * Constructor for client read thread class. 
	 * @param p - packet received from listener server
	 */
	public Client_Read_Thread(DatagramPacket p) {
		try {
			sendReceiveSocket = new DatagramSocket();
		}catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		receivePacket = p;
		//get the filepath from read request
		int p1 = 0;//index position of the first 0 byte in the message 
		int len = receivePacket.getLength();//length of meaningful data
		for(int i=2;i<len;i++) {//find first 0 
			if(receivePacket.getData()[i] == (byte) 0) {//found parse point when 0 is found 
				p1 = i;
				break;
			}				
		}
		byte[] filename = new byte[p1-2];
		for(int i = 2; i<filename.length+2;i++) {
			filename[i-2] = receivePacket.getData()[i]; 
		}
		FILEPATH = new String(filename,0,filename.length); 
		File f = new File(FILEPATH);
		os = this.openwrite(f);
		
		
		
		//send ACK data block code should be below
	}
	/**
	 * Set up the output stream for writing the data.
	 * @param file file to write to.
	 * @return outputstream to write with
	 */
	public OutputStream openwrite(File file) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os;
	}
	
	/**
	 * Close the outputstream when done writing to file
	 * @param os output stream to close
	 */
	public void closewrite(OutputStream os) {
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Write bytes to file.
	 * @param bytes data to be written to file
	 * @param os output stream to use
	 */
	public void writeByte(byte[] bytes,OutputStream os) 
	    { 
	        try { 
	  
	            // Starts writing the bytes in it 
	            os.write(bytes);
	        }catch (Exception e) { 
	            System.out.println("Exception: " + e); 
	        } 
	    }
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(true) {
			
		}
	}		
	
	
}



//////////////////CLIENT WRITE THREAD ////////////////////////////////////////////

/**
 * Class for the client write thread to handle write requests from client. 
*/
class Client_Write_Thread implements Runnable
{
	DatagramPacket receivePacket, sendPacket;
	DatagramSocket sendReceiveSocket; 
	String FILEPATH;
	OutputStream os;
	
	/**
	 * Constructor for client write thread class. 
	 * @param p - packet received from listener server
	 */
	public Client_Write_Thread(DatagramPacket p) {
		try {
			sendReceiveSocket = new DatagramSocket();
		}catch(SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
		receivePacket = p;
		//get the filepath from read request
		int p1 = 0;//index position of the first 0 byte in the message 
		int len = receivePacket.getLength();//length of meaningful data
		for(int i=2;i<len;i++) {//find first 0 
			if(receivePacket.getData()[i] == (byte) 0) {//found parse point when 0 is found 
				p1 = i;
				break;
			}				
		}
		byte[] filename = new byte[p1-2];
		for(int i = 2; i<filename.length+2;i++) {
			filename[i-2] = receivePacket.getData()[i]; 
		}
		FILEPATH = new String(filename,0,filename.length); 
		File f = new File(FILEPATH);
		os = this.openwrite(f);
		
		
		
		//send ACK data block code should be below
		byte[] ack = new byte[4];
		ack[0] = (byte) 0;
		ack[1] = (byte) 4;
		ack[2] = (byte) 0;
		ack[3] = (byte) 0;
		
		sendPacket = new DatagramPacket(ack,ack.length,receivePacket.getAddress(),receivePacket.getPort());
		try {//send ack 
			sendReceiveSocket.send(sendPacket);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * Set up the output stream for writing the data.
	 * @param file file to write to.
	 * @return outputstream to write with
	 */
	public OutputStream openwrite(File file) {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return os;
	}
	
	/**
	 * Close the outputstream when done writing to file
	 * @param os output stream to close
	 */
	public void closewrite(OutputStream os) {
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Write bytes to file.
	 * @param bytes data to be written to file
	 * @param os output stream to use
	 */
	public void writeByte(byte[] bytes,OutputStream os) 
	    { 
	        try { 
	  
	            // Starts writing the bytes in it 
	            os.write(bytes);
	        }catch (Exception e) { 
	            System.out.println("Exception: " + e); 
	        } 
	    }
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(true) {
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
			
			System.out.println("Server: Packet received!");
			/**
			 * Code to extract and write data to file.
			 */
			int len = receivePacket.getLength();
			byte[] filedata = new byte[len-4];//first 4 bytes in receivePacket are not data
			for(int i=4;i<len;i++) {
				filedata[i-4] = receivePacket.getData()[i];
				}
			
			this.writeByte(filedata, os);
			//need to close the outputstream somewhere.
			
		
			
			/**
			 * code to form ack data block
			 */
			byte[] ack = new byte[4];
			ack[0] = (byte) 0;
			ack[1] = (byte) 4;
			ack[2] = receivePacket.getData()[0];
			ack[3] = receivePacket.getData()[1];
			
			sendPacket = new DatagramPacket(ack,ack.length,receivePacket.getAddress(),receivePacket.getPort());
			
			try {//send ACK block
				sendReceiveSocket.send(sendPacket);
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
	}		
}










