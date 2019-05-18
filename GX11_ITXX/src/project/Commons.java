package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Commons {

	private String classtype = "";
	
	public Commons(String name) {
		classtype = name;
	}
	
	public byte[] stringToByte(String example_text) {
		return example_text.getBytes();
	}
	
	public byte intToByte(int example_int) {
		return (byte)example_int;
	}
	
	public byte[] concatenateByteArrays(byte[] a, byte[] b, boolean padded_zero) {
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
		System.out.print("["+classtype+"] ");
		System.out.println(message + " ");
	}
	
	public byte[] filterPackage(DatagramPacket r) {
		int size = r.getLength();
		byte[] raw_data = r.getData();
		byte data[] = new byte[size];
		for(int i = 0; i < size; i++) {
			data[i] = raw_data[i];
		}
		return data;
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request) {
		return this.sendRequestAndWaitOnResponse(socket, request, 100);
	}
	
	public DatagramPacket sendRequestAndWaitOnResponse(DatagramSocket socket, DatagramPacket request, int size) {
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
		try {
			socket.send(request);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public byte[] generateAcknowledgement(int block_num) {
		byte[] ack = new byte[4];
		ack[0] = this.intToByte(0);
		ack[1] = this.intToByte(4);
		byte[] block_num_array = this.blockNumToTwoBytes(block_num);
		ack[2] = block_num_array[0];
		ack[3] = block_num_array[1];
		
		return ack;
	}
	
	public boolean confirmAcknowledgement(DatagramPacket r) {
		// assuming no errors for iteration 1
		return true;
	}
	
	public byte[] handleDataPacket(DatagramPacket r) {
		return null;
	}
	
	public byte[] blockNumToTwoBytes(int block_number) {
		byte[] block_num_data = new byte[2];
		block_num_data[0] = this.intToByte(0);
		block_num_data[1] = this.intToByte(block_number);
		return block_num_data;
	}
	
	public byte[] generateDataPacket(byte[] data, int block_number) {
		byte[] header = new byte[2];
		header[0] = this.intToByte(0);
		header[1] = this.intToByte(3);
		
		byte[] block_data = this.blockNumToTwoBytes(block_number);
		
		byte[] head = this.concatenateByteArrays(header,  block_data, false);
		
		return this.concatenateByteArrays(head, data, false);
	}
}
