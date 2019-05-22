package project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientConnection implements Runnable {

    DatagramPacket requestPacket;
    DatagramSocket sendReceiveSocket;
    int blockNumber;
    private String filename, mode;
    private int port;
    private boolean writeRequest = false;
    private boolean readRequest = false;
    OutputStream os;
    int verbose; 

    public ClientConnection(DatagramPacket requestPacket) {
        this.requestPacket = requestPacket;
        blockNumber = 0;
        filename = Commons.extractFilename(requestPacket.getData());
        port = requestPacket.getPort();
	System.out.println("Enter 1 for verbose mode or 0 for quiet mode!: ");
	Scanner scanner = new Scanner(System.in);
	verbose = scanner.nextInt();//verbose mode will print out packet details

        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }

        parseRequestPacket();
    }
    
    private void parseRequestPacket() {
        if (requestPacket.getData()[1] == (byte)1) {//read request
        	System.out.println("client is reading");
            readRequest = true;
        } else {//write request
        	System.out.println("client is writing");
            writeRequest = true;
        }
    }

    public void readFromServer() {
        boolean connection = true;
        byte[]  fileBytes = null; 
        byte[]  dataBytes = null;
        byte[] receiveBytes = new byte[100];
        DatagramPacket dataPacket;
        DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);

        // Read file into byte array, check if file exists, or if access is denied
       	//  Forgot errors are not a part of this iteration, currently WORK-IN-PROGRESS
        try {
            fileBytes = Files.readAllBytes(Paths.get(filename));
        }  catch (IOException e) {
            e.printStackTrace();
        }

        while (connection) {
            blockNumber++; 

            // Get next DATA packet
            try {
//            	Commons c = new Commons("Client Connection");
//            	c.print(fileBytes, "file");
//            	System.out.println(blockNumber);
                dataBytes = Commons.getNextBlock(fileBytes, blockNumber);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Construct and send DATA packet
            try {
                dataPacket = new DatagramPacket(dataBytes, dataBytes.length, InetAddress.getLocalHost(), port);
                sendReceiveSocket.send(dataPacket);
                //sent DATA packet---print data
				if(verbose==1) {
					System.out.println("Client Connection Thread: sending packet");
				        System.out.println("Packet type: DATA BLOCK");
					System.out.println("Block number is: "+ dataPacket.getData()[2]+ " "+ dataPacket.getData()[3]);
					System.out.println("Number of bytes: "+ dataPacket.getLength());
				}
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Could not send DATA");
            }

            // Wait for ACK packet
            try {
                sendReceiveSocket.receive(receivePacket);
				if(verbose==1) {
					System.out.println("Client Connection Thread: received packet");
					System.out.println("From host: " + receivePacket.getAddress());
				        System.out.println("Host port: " + receivePacket.getPort());
					System.out.println("Packet type: ACK BLOCK");
					System.out.println("Block number is: "+ receivePacket.getData()[2]+ " "+ receivePacket.getData()[3]);
				}
            } catch (Exception e) {
                System.out.println("Could not receive ACK");
            }

            if (dataBytes.length < 512) {
                connection = false;
            }
        }
    }

    public void writeToServer() {
        boolean connection = true;
        byte[] data = new byte[512];
        DatagramPacket sendPacket, receivePacket;
        String FILEPATH;

		//get the filepath from read request
		int p1 = 0;//index position of the first 0 byte in the message 
		int len = requestPacket.getLength();//length of meaningful data
		for(int i=2;i<len;i++) {//find first 0 
			if(requestPacket.getData()[i] == (byte) 0) {//found parse point when 0 is found 
				p1 = i;
				break;
			}				
		}
		byte[] filename = new byte[p1-2];
		for(int i = 2; i<filename.length+2;i++) {
			filename[i-2] = requestPacket.getData()[i]; 
        }
        
		FILEPATH = new String(filename,0,filename.length); 
		File f = new File(FILEPATH);
		os = this.openwrite(f);
		
		//send ACK data block code 
		byte[] ack = new byte[4];
		ack[0] = (byte) 0;
		ack[1] = (byte) 4;
		ack[2] = (byte) 0;
		ack[3] = (byte) 0;
		
		sendPacket = new DatagramPacket(ack,ack.length,requestPacket.getAddress(),requestPacket.getPort());
		try {//send ack 
			sendReceiveSocket.send(sendPacket);
			if(verbose==1) {
				System.out.println("Client Connection Thread: sending ACK block");
		       		System.out.println("Packet type: ACK Block");
				System.out.println("Block number is: "+ sendPacket.getData()[2]+ " "+ sendPacket.getData()[3]);
			}
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
        }
        
        while (connection) {
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
			len = receivePacket.getLength();
			byte[] filedata = new byte[len-4];//first 4 bytes in receivePacket are not data
			for(int i=4;i<len;i++) {
				filedata[i-4] = receivePacket.getData()[i];
				}
			
			this.writeByte(filedata, os);		
			//print details
			if(verbose==1) {
				System.out.println("From host: " + receivePacket.getAddress());
		        	System.out.println("Host port: " + receivePacket.getPort());
				System.out.println("Packet type: DATA Block");
				System.out.println("Block number is: "+ receivePacket.getData()[2]+ " "+ receivePacket.getData()[3]);
				System.out.println("Number of bytes: "+ len);
			}
			/**
			 * code to form ack data block
			 */
			ack = new byte[4];
			ack[0] = (byte) 0;
			ack[1] = (byte) 4;
			ack[2] = receivePacket.getData()[2];
			ack[3] = receivePacket.getData()[3];
			
			sendPacket = new DatagramPacket(ack,ack.length,receivePacket.getAddress(),receivePacket.getPort());
			
			try {//send ACK block
				sendReceiveSocket.send(sendPacket);
				if(verbose==1) {
					System.out.println("Client Connection Thread: sending ACK block");
		       			System.out.println("Packet type: ACK Block");
					System.out.println("Block number is: "+ receivePacket.getData()[2]+ " "+ receivePacket.getData()[3]);
				}
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
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

    private void closeSocket() {
        sendReceiveSocket.close();
        System.out.println("Client Connection terminating\n");
    }

    @Override
    public void run() {
        try {
            if (writeRequest) {
                writeToServer();
            } else if (readRequest) {
                readFromServer();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        closeSocket();
    }
}