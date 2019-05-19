package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
	
	public byte intToByte(int example_int) {
		//	Converts INT provided to Byte
		return (byte)example_int;
	}
	
	public byte[] concatenateByteArrays(byte[] a, byte[] b, boolean padded_zero) {
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
			new_a[len_a] = this.intToByte(0);
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
		return this.sendRequestAndWaitOnResponse(socket, request, 100);
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request, int size) {
		//	Sends the request packet provided to the socket and waits on a response.
		try {
			socket.send(request);
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
	
	public boolean confirmAcknowledgement(DatagramPacket r) {
		// Confirms is the TFTP Acknowledgement received is correct.
		// assuming no errors for iteration 1
		return true;
	}
	
	private byte[] blockNumToTwoBytes(int block_number) {
		// Given an int block number, returns 2 bytes of block number to be used in ack packets.
		byte[] block_num_data = new byte[2];
		block_num_data[0] = this.intToByte(0);
		block_num_data[1] = this.intToByte(block_number);
		return block_num_data;
	}
	
	public byte[] generateDataPacket(byte[] data, int block_number) {
		// Given data and block number, generates a data packet format byte for TFTP transfer.
		byte[] header = new byte[2];
		header[0] = this.intToByte(0);
		header[1] = this.intToByte(3);
		
		byte[] block_data = this.blockNumToTwoBytes(block_number);
		
		byte[] head = this.concatenateByteArrays(header,  block_data, false);
		
		return this.concatenateByteArrays(head, data, false);
	}
}
