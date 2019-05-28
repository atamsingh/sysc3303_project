package project;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client {
	
	Commons common = null;
	ClientInputLoader input_grabber = null;
	private DatagramSocket sendReceiveSocket;
	final Duration timeout = Duration.ofSeconds(1);
	final int max_retry = 10;
	
	public Client(ClientInputLoader input) {
		common = new Commons("CLIENT");
		this.input_grabber = input;
		try {
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private DatagramPacket tryReceivingUntilSuccessful(DatagramSocket s, DatagramPacket p, int block_to_be_received, int size) {
		// send request and wait on response
		// we keep waiting until we hear something. Server should retransmit if something not ack'ed
		DatagramPacket response = common.sendRequestAndWaitOnResponse(s, p, size);
		// we received a response, lets make sure it's not null and is the block we except (the one after the last one)
		while(response == null || Commons.getBlockNumber(response) != block_to_be_received) {
			// while this condition is not met, keep waiting.
			response = common.receiveRequest(s, size);
		}
		// once we have a response for the next block we expected, return
		return response;
	}
	
	private DatagramPacket trySendingUntilSuccessful(DatagramSocket s, DatagramPacket p, int block_num) {
		return trySendingUntilSuccessful(s, p, block_num, 1);
	}
	
	private DatagramPacket trySendingUntilSuccessful(DatagramSocket s, DatagramPacket p, int block_num, int counter) {
		// if request counter higher than max try return null
		if(counter > max_retry) {
			return null;
		}
		// else create executor
		ExecutorService executor = Executors.newSingleThreadExecutor();

		// create future promise for request
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Future<DatagramPacket> handler = executor.submit(new Callable() {
		    public DatagramPacket call() throws Exception {
		        return common.sendRequestAndWaitOnResponse(s, p);
		    }
		});
		
		// try to call this
		try {
			this.logVerbose("making request to server and waiting (iteration: " + counter + ")...");
			// wait for timeout to get response. 
			DatagramPacket response = handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			// if no exception, confirm the acknowledgement is correct.
			if(common.confirmAcknowledgement(response, block_num)) {
				// if the ack received is correct, send response back/
				this.logVerbose("Response received and acknowledged.");
				executor.shutdownNow();
				return response;
			}else {
				// if ack received is not for the block we anticipated, try sending again and only return when successful or max retry limit hit
				this.logVerbose("Response received but is not for the block that needed to be acknowledged... sending again...");
				executor.shutdownNow();
				return trySendingUntilSuccessful(s, p, block_num, counter++);
			}
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			// if timeout happens, or some other error occurs. clean everything and try sending again.
			handler.cancel(true);
			executor.shutdownNow();
		    return trySendingUntilSuccessful(s, p, block_num, counter++);
		}
	}

	public void logVerbose(String message) {
		if(this.input_grabber.mode.contentEquals("verbose")) {
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
		DatagramPacket responseFromServer  = null;
		int blocks_received = 1;
		while(responseFromServer == null) {
			responseFromServer = this.tryReceivingUntilSuccessful(this.sendReceiveSocket, readRequestPacket, blocks_received, 516);
		}
		this.logVerbose("received packet " + blocks_received + "... ");
		byte[] curr_response = common.filterPackage(responseFromServer);
		byte[] all_data = curr_response;
		while(curr_response.length >= 512) {
			byte[] ack_data = common.generateAcknowledgement(blocks_received);
			DatagramPacket curr_ack = new DatagramPacket(ack_data, ack_data.length, responseFromServer.getAddress(), responseFromServer.getPort());
			responseFromServer = null;
			while(responseFromServer == null) {
				responseFromServer = this.tryReceivingUntilSuccessful(this.sendReceiveSocket, curr_ack, blocks_received, 1024);
			}
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
			DatagramPacket responseFromServer = null; 
			while(responseFromServer == null) {
				responseFromServer = this.trySendingUntilSuccessful(this.sendReceiveSocket, dataPacket, curr_block_number); // also confirms correct block ack or keeps trying
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
		DatagramPacket responseFromServer = null;
		while(responseFromServer == null) {
			this.logVerbose("Tring to send ack...");
			responseFromServer = this.trySendingUntilSuccessful(this.sendReceiveSocket, writeRequestPacket, 0); // block 0 for ack
		}
		this.logVerbose("Received Acknowledgement from the server.");
		this.logVerbose("starting to send file to server.");
		// start sending file
		this.sendFileToServer(file_name_to_read, responseFromServer.getAddress(), responseFromServer.getPort());
	}

	private void handleRequest() throws IOException {
		if(input_grabber.notShutDown) {
			this.logVerbose("New request started to be handled.");
			String filename_to_read = input_grabber.read_file_name;
			this.logVerbose("File requested to be read (locally to write to server/read from server): " + filename_to_read);
			String filename_to_write_to = input_grabber.write_file_name;
			this.logVerbose("File requested to write (locally from server/to server): " + filename_to_write_to);
			if(input_grabber.requestType.contentEquals("read")) {
				this.logQuiet("Reading " + filename_to_read + " from server...");
				this.readRequest(filename_to_read, filename_to_write_to);
			}else if(input_grabber.requestType.equals("write")) {
				this.logQuiet("Writing file " + filename_to_read + " to " + filename_to_write_to + " on server...");
				this.writeRequest(filename_to_read, filename_to_write_to);
			}
		}
	}
	
	public void kickOff() throws IOException {
		while(input_grabber.notShutDown) {
			this.logVerbose("Asking user for input");
			input_grabber.askClientInput();
			this.logQuiet("handling request");
			this.handleRequest();
		}
		this.logVerbose("Client asked for shutdown. Made sure all transfers are complete");
		this.logQuiet("Goodbye!");
	}

	
	public static void main(String[] args) {
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
