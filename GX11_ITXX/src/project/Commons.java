package project;

import java.net.DatagramPacket;

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

}
