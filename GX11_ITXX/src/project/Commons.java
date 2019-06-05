package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.Duration;
import java.util.Arrays;

public class Commons {

	private String classtype = "";
	
	public Commons(String name) {
		//	Class name for the class creating the commons object
		classtype = name;
	}
	public String getClassName() {
		return this.classtype;
	}
	
	public byte[] stringToByte(String example_text) {
		//	Converts String provided to Byte Array
		return example_text.getBytes();
	}
	
	public static byte intToByte(int example_int) {
		//	Converts INT provided to Byte
		return (byte)example_int;
	}
	
	public static byte[] concatenateByteArrays(byte[] a, byte[] b, boolean padded_zero) {
		//	merges 2 byte arrays. A is appended to B. Padded_zero boolean is an idicator for needing a zero byte b/w the arrays or not
		// think [..a..] + [0Byte] + [..b..]
		int len_a = a.length;
		int len_b = b.length;
		int final_len = len_a + len_b;
		byte[] new_a = null;
		
		if(padded_zero) {
			final_len += 1;
			new_a = new byte[len_a+1];
			for(int i = 0; i < len_a; i++) {
				new_a[i] = a[i];
			}
			new_a[len_a] = intToByte(0);
		}else {
			new_a = a;
		}
		
		byte[] final_array = new byte[final_len];	
		
		for(int i = 0; i < new_a.length; i++) {
			final_array[i] = new_a[i];
		}
		for(int i = 0; i < b.length; i++) {
			final_array[new_a.length + i] = b[i];
		}
		return final_array;
	}
	
	public void print(byte[] a, String message) {
		//	Prints the byte array provided in string and byte format along with the message 
		//	also prints the class name to provide easy stdout recognition. 
		System.out.print("["+classtype+"] ");
		System.out.print(message + " (string)");
		for(int i = 0; i < a.length; i++) {
			System.out.print((char)a[i]);
		}
		System.out.print("\n");
		System.out.print("["+classtype+"] ");
		System.out.print(message + " (bytes)");
		for(int i = 0; i < a.length; i++) {
			System.out.print(a[i]);
		}
		System.out.print("\n");
	}
	
	public void print(String message) {
		//	Prints a message with the class name for easy stdout recognition
		System.out.print("["+classtype+"] ");
		System.out.println(message + " ");
	}
	
	public byte[] filterPackage(DatagramPacket r) {
		//	Given a datagram packet, filters the data to only the number of bytes received. This drop the buffer empty bytes
		int size = r.getLength();
		byte[] raw_data = r.getData();
		byte data[] = new byte[size];
		for(int i = 0; i < size; i++) {
			data[i] = raw_data[i];
		}
		return data;
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request) {
		//	Sends the request packet provided to the socket and waits on a response.
		// this func calls the same func with a different signature. this overwrite provides default 100 buffer size. 
		return this.sendRequestAndWaitOnResponse(socket, request, 100, Duration.ofSeconds(600)); // a very very long time
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request, int size) {
		return this.sendRequestAndWaitOnResponse(socket, request, 100, Duration.ofSeconds(600)); // a very very long time
	}

	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request, Duration timeout) {
		return this.sendRequestAndWaitOnResponse(socket, request, 100, timeout);
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request, int size, Duration timeout) {
		try {
			socket.setSoTimeout((int) timeout.toMillis());
			socket.send(request);
			DatagramPacket receivePacket = new DatagramPacket(new byte[size], size);
			socket.receive(receivePacket);
			return receivePacket;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	public DatagramPacket receiveRequest(DatagramSocket socket) {
		return receiveRequest(socket, 100);
	}
	
	public DatagramPacket receiveRequest(DatagramSocket socket, int size) {
		// Sends the request packet provided to the socket and DOES NOT wait on a response.
		try {
			DatagramPacket receivePacket = new DatagramPacket(new byte[size], size);
			socket.receive(receivePacket);
			return receivePacket;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean sendRequest(DatagramSocket socket, DatagramPacket request) {
		//	Sends the request packet provided to the socket and DOES NOT wait on a response.
		try {
			socket.send(request);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public byte[] generateAcknowledgement(int block_num) {
		// Creates TFTP Acknowledgement given a certain block number.
		byte[] ack = new byte[4];
		ack[0] = this.intToByte(0);
		ack[1] = this.intToByte(4);
		byte[] block_num_array = this.blockNumToTwoBytes(block_num);
		ack[2] = block_num_array[0];
		ack[3] = block_num_array[1];
		
		return ack;
	}

	public boolean confirmAcknowledgement(DatagramPacket r, int block_num) {
		if (r == null) {
			return false;
		}else {
			int received = extractTwoBytes(r.getData(), 2);
			System.out.println("block received is #" + received);
			return received == block_num;
		}
	}
	
	private static byte[] blockNumToTwoBytes(int block_number) {
		// Given an int block number, returns 2 bytes of block number to be used in ack packets.
		byte[] block_num_data = new byte[2];
		block_num_data[0] = intToByte(0);
		block_num_data[1] = intToByte(block_number);
		return block_num_data;
	}
	
	public static byte[] generateDataPacket(byte[] data, int block_number) {
		// Given data and block number, generates a data packet format byte for TFTP transfer.
		byte[] header = new byte[2];
		header[0] = intToByte(0);
		header[1] = intToByte(3);
		
		byte[] block_data = blockNumToTwoBytes(block_number);
		
		byte[] head = concatenateByteArrays(header,  block_data, false);
		
		return concatenateByteArrays(head, data, false);
	}

	// Extracts string from packets
	public static String extractString(byte[] receivedMessage, int init) {
		String extractedString = "";

		for (int i = init; receivedMessage[i] != (byte)0; i++) {
			extractedString += String.valueOf((char)receivedMessage[i]);
		}

		return extractedString;
	}

	// Extracts filename string from packet
	public static String extractFilename(byte[] receivedMessage) {
		return extractString(receivedMessage, 2);
	}

	public static byte[] getNextBlock(byte[] fileBytes, int blockNumber) {
		int blockNumberForHeader = blockNumber + 1;
		int start_index =  512 * blockNumber;
		int end_index = 512 * blockNumberForHeader;
		if(end_index > fileBytes.length){
			end_index = fileBytes.length;
		}
		byte[] dataPacketBytes = Arrays.copyOfRange(fileBytes, start_index, end_index);
		return generateDataPacket(dataPacketBytes, blockNumberForHeader);
	}

	public static int getBlockNumber(DatagramPacket packet) {
		if (packet == null)
			return -1;
		else
			return extractTwoBytes(packet.getData(), 2);
	}

	public static int extractTwoBytes(byte [] receivedMessage, int startPosition) {
		int code = 0;

		code += receivedMessage[0 + startPosition] * 16;
		code += receivedMessage[1 + startPosition];

		return code;
	}

/**
 * Construct TFTP ERROR packet
 * 
 * @param errorCode
 * @param errorMessage
 * @return
 */
	public static byte[] constructError(int errorCode, String errorMessage) {
		byte[] errorPacketHeaderBytes = new byte[4];
		byte[] errorMessageBytes = errorMessage.getBytes();
		byte[] errorPacket = new byte[4 + errorMessageBytes.length + 1];
		byte[] zeroByte = new byte[1];
		
		errorPacketHeaderBytes[0] = 0;
		errorPacketHeaderBytes[1] = 5;
		errorPacketHeaderBytes[2] = (byte) (errorCode >> 8);
		errorPacketHeaderBytes[3] = (byte) (errorCode);
		
		zeroByte[0] = 0;
		
		System.arraycopy(errorPacketHeaderBytes, 0, errorPacket, 0, errorPacketHeaderBytes.length);
		System.arraycopy(errorMessageBytes, 0, errorPacket, errorPacketHeaderBytes.length, errorMessageBytes.length);
		System.arraycopy(zeroByte, 0, errorPacket, errorPacket.length-1, 1);
		
		return errorPacket;
	}
}
