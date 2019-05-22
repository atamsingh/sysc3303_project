package project;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Arrays;

public class Client {
	
	Commons common = null;
	ClientInputLoader input_grabber = null;
	private DatagramSocket sendReceiveSocket;
	
	public Client(ClientInputLoader input) {
		common = new Commons("CLIENT");
		this.input_grabber = input;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void logVerbose(String message) {
		if(this.input_grabber.mode == "verbose") {
			common.print(message);
		}
	}
	
	public void logQuiet(String message) {
		common.print(message);
	}
	
	private byte[] getFileData(String fileName) { 
	    try {
	    	String data = "";
	    	this.logVerbose("loading file from disk");
			data = new String(Files.readAllBytes(Paths.get(fileName)));
			return this.common.stringToByte(data);
		} catch (IOException e) {
	    	this.logVerbose("File " + fileName + " does not exist or the program does not have access to the folder.");
	    	System.exit(1);
			e.printStackTrace();
		}
	    return null;
	}
	
	private byte[] generatePacket(String file_name, int opcode_int) {
		this.logVerbose("creating request packet with opcode of " + opcode_int + ".");
		byte[] opcode = new byte[2];
		opcode[0] = common.intToByte(0);
		opcode[1] = common.intToByte(opcode_int);
		byte[] filename_bytes = common.stringToByte(file_name);
		byte[] zero_byte = new byte[1];
		zero_byte[0] = common.intToByte(0);
		this.logVerbose("assuming file mode of null");
		byte[] mode_text = common.stringToByte("");
		
		byte[] a = common.concatenateByteArrays(opcode, filename_bytes, false);
		byte[] b = common.concatenateByteArrays(a, mode_text, true);
		
		return common.concatenateByteArrays(b, zero_byte, false);
	}
	
	private void writeFileData(String fileName, byte[] data_array) throws IOException { 
		this.logVerbose("creating file " +fileName+ "if not exists.");
		File file = new File(fileName);
		file.createNewFile();

		OutputStream os = new FileOutputStream(file);
		this.logVerbose("starting writing data receievd to disk.");
		os.write(data_array); 
		os.close();
		this.logVerbose("writing to file completed through outputstream.");
	}

	private void readRequest(String file_name_to_read, String file_name_to_write_to) throws IOException {
		// TODO - Clean this up. Yuck!!
		// generate read request header
		byte[] data = this.generatePacket(file_name_to_read, 1);
		DatagramPacket readRequestPacket = new DatagramPacket(data, data.length, input_grabber.server_address, input_grabber.server_port);
		this.logVerbose("Sending RRQ packet to server on " + input_grabber.server_address + ":" + input_grabber.server_port + ".");
		DatagramPacket responseFromServer = common.sendRequestAndWaitOnResponse(this.sendReceiveSocket,readRequestPacket, 516);
		int blocks_received = 1;
		this.logVerbose("received packet " + blocks_received + "... ");
		byte[] curr_response = common.filterPackage(responseFromServer);
		byte[] all_data = curr_response;
		while(curr_response.length >= 512) {
			byte[] ack_data = common.generateAcknowledgement(blocks_received);
			DatagramPacket curr_ack = new DatagramPacket(ack_data, ack_data.length, responseFromServer.getAddress(), responseFromServer.getPort());
			responseFromServer = common.sendRequestAndWaitOnResponse(this.sendReceiveSocket,curr_ack, 1024);
			blocks_received++;
			this.logVerbose("received packet " + blocks_received + "... ");
			curr_response = common.filterPackage(responseFromServer);
			all_data = common.concatenateByteArrays(all_data, curr_response, false);
		}

		this.logVerbose("last file packet received...");
		//ack last received block
		byte[] ack_data = common.generateAcknowledgement(blocks_received);
		DatagramPacket curr_ack = new DatagramPacket(ack_data, ack_data.length, responseFromServer.getAddress(), responseFromServer.getPort());
		common.sendRequest(this.sendReceiveSocket, curr_ack);
		
		// received a data block with less than 512 bytes with it. Assuming data ends.
		if(this.input_grabber.write_to_file) {
			this.logVerbose("Starting writing file to disk...");
			this.writeFileData(file_name_to_write_to, all_data);
			this.logQuiet("Wrote file received from server to " + file_name_to_write_to + " path.");
		} else {
			common.print(all_data, "File received from server");
		}
	}
	
	private void sendFileToServer(String file_name_to_read, InetAddress address, int port) {
		this.logVerbose("Reading " + file_name_to_read + " from client disk.");
		byte[] file_data = this.getFileData(file_name_to_read);
		int number_of_blocks = (int)Math.ceil(file_data.length / 512.0);
		this.logVerbose("blocks " + number_of_blocks + " selected for byte array size of " + file_data.length + " for file to read.");
		for(int i = 0; i < number_of_blocks; i++) {
			int start_block_index = i  * 512;
			int end_block_index = ((i+1) * 512);
			byte[] curr_data = Arrays.copyOfRange(file_data, start_block_index, end_block_index);
			int curr_block_number = i + 1; // starts from 1
			byte[] data_to_send = common.generateDataPacket(curr_data, curr_block_number);
			this.logVerbose("sending block " + curr_block_number + " from byte index " + start_block_index + " to " + end_block_index + ".");
			DatagramPacket dataPacket = new DatagramPacket(data_to_send, data_to_send.length, address, port);
			DatagramPacket responseFromServer = common.sendRequestAndWaitOnResponse(this.sendReceiveSocket, dataPacket);
			if(!common.confirmAcknowledgement(responseFromServer)) {
				this.logQuiet("Error sending data to server. Confirm that the server is still available and listening.");
				break;
			}
			this.logVerbose("Successfully received acknowledgement for block " + curr_block_number + ".");
		}
		// all file sent over.
		this.logQuiet("File " + file_name_to_read + " sent over to server at " + address + ":" + port + ".");
	}
	
	private void writeRequest(String file_name_to_read, String file_name_to_write_to) throws UnknownHostException {
		// generate write request header
		this.logVerbose("Generating WRQ packet for write request");
		byte[] data = this.generatePacket(file_name_to_write_to, 2);
		DatagramPacket writeRequestPacket = new DatagramPacket(data, data.length, input_grabber.server_address, input_grabber.server_port);
		this.logVerbose("Sent request to server for wrq.");
		DatagramPacket responseFromServer = common.sendRequestAndWaitOnResponse(this.sendReceiveSocket, writeRequestPacket);
		// confirm it is acknowledged
		if(common.confirmAcknowledgement(responseFromServer)) {
			this.logVerbose("Received Acknowledgement from the server. confirming if actually acknowledged.");
			this.logVerbose("starting to send file to server.");
			// start sending file
			this.sendFileToServer(file_name_to_read, responseFromServer.getAddress(), responseFromServer.getPort());
		}else {
			this.logVerbose("Response from server received but not acknowledged.");
			this.logQuiet("Error sending data to server. Confirm that the server is still available and listening.");
		}
	}

	private void handleRequest() throws IOException {
		if(input_grabber.notShutDown) {
			this.logVerbose("New request started to be handled.");
			String filename_to_read = input_grabber.read_file_name;
			this.logVerbose("File requested to be read (locally to write to server/read from server): " + filename_to_read);
			String filename_to_write_to = input_grabber.write_file_name;
			this.logVerbose("File requested to write (locally from server/to server): " + filename_to_write_to);
			if(input_grabber.requestType == "read") {
				this.logQuiet("Reading " + filename_to_read + " from server...");
				this.readRequest(filename_to_read, filename_to_write_to);
			}else if(input_grabber.requestType == "write") {
				this.logQuiet("Writing file " + filename_to_read + " to " + filename_to_write_to + " on server...");
				this.writeRequest(filename_to_read, filename_to_write_to);
			}
		}
	}
	
	public void kickOff() throws IOException {
		while(input_grabber.notShutDown) {
			this.logVerbose("Asking user for input");
//			input_grabber.askClientInput();
			this.logVerbose("handling request");
			this.handleRequest();
			System.exit(1);
		}
		this.logVerbose("Client asked for shutdown. Made sure all transfers are complete");
		this.logQuiet("Goodbye!");
	}

	public static void main() {
		try {
			ClientInputLoader cig;
			cig = new ClientInputLoader();
			Client c = new Client(cig);
			c.kickOff();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


class TestServerAndClientLocally {
    public static void main(final String[] args) {

         Thread serverThread = new Thread() {
            public void run() {
                Server.main(args);
            }
        };

         Thread clientThread = new Thread() {
            public void run() {
                Client.main();
            }
        };

         serverThread.start();

         try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         clientThread.start();
    }
}
  
