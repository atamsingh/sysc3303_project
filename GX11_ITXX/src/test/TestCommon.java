package test;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.function.Function;

public class TestCommon {
	private boolean server_status;
	private boolean client_status;

	project.Commons common_object = new project.Commons("TestCommon");
	
	Function<Void, Boolean> test_class_name = (Void v) -> {
		return common_object.getClassName() == "TestCommon";
	};
	
	Function<Void, Boolean> test_string_to_byte = (Void v) -> {
		String s = "abc";
		return Arrays.equals(common_object.stringToByte(s), s.getBytes());
	};
	
//	Function<Void, Boolean> test  = (Void v) -> {
//		return false;
//	};

	Function<Void, Boolean> test_generate_data_packet  = (Void v) -> {
		byte[] data = new byte[3];
		data[0] = (byte)675;
		data[1] = (byte)987;
		data[2] = (byte)5;
		
		byte[] expected_packet = new byte[7];
		expected_packet[0] = (byte)0;
		expected_packet[1] = (byte)3;
		expected_packet[2] = (byte)0;
		expected_packet[3] = (byte)56;
		expected_packet[4] = (byte)675;
		expected_packet[5] = (byte)987;
		expected_packet[6] = (byte)5;
		
		return Arrays.equals(common_object.generateDataPacket(data, 56), expected_packet);
	};

	Function<Void, Boolean> test_generate_ack = (Void v) -> {
		byte[] sample_ack = new byte[4];
		sample_ack[0] = (byte)0;
		sample_ack[1] = (byte)4;
		sample_ack[2] = (byte)0;
		sample_ack[3] = (byte)78;
		
		return Arrays.equals(sample_ack, common_object.generateAcknowledgement(78));
	};
	
	Function<Void, Boolean> test_filter_package = (Void v) -> {
		byte[] data = new byte[4];
		data[0] = (byte)0;
		data[1] = (byte)1;
		data[2] = (byte)2;
		data[3] = (byte)3;
		
		byte[] expected_data = new byte[2];
		expected_data[0] = (byte)0;
		expected_data[1] = (byte)1;
		
		try {
			DatagramPacket r = new DatagramPacket(data, 2, InetAddress.getByName("0.0.0.0"), 69);
			return Arrays.equals(expected_data, common_object.filterPackage(r));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	};
	
	Function<Void, Boolean> test_int_to_byte = (Void v) -> {
		return common_object.intToByte(1) == (byte) 1;
	};
	
	Function<Void, Boolean> test_concatenate_byte_arrays_no_padding  = (Void v) -> {
		byte[] final_expected = new byte[3];
		
		byte[] a = new byte[1];
		a[0] = (byte)1;
		final_expected[0] = (byte)1;
		
		byte[] b = new byte[2];
		b[0] = (byte)'a';
		final_expected[1] = (byte)'a';
		b[1] = (byte)'b';
		final_expected[2] = (byte)'b';
		
		return Arrays.equals(common_object.concatenateByteArrays(a, b, false), final_expected);
	};
	
	Function<Void, Boolean> test_concatenate_byte_arrays_with_padding  = (Void v) -> {
		byte[] final_expected = new byte[4];
		
		byte[] a = new byte[1];
		a[0] = (byte)1;
		final_expected[0] = (byte)1;
		
		final_expected[1] = (byte)0;
		
		byte[] b = new byte[2];
		b[0] = (byte)'a';
		final_expected[2] = (byte)'a';
		b[1] = (byte)'b';
		final_expected[3] = (byte)'b';
		
		return Arrays.equals(common_object.concatenateByteArrays(a, b, true), final_expected);
	};
	
	Function<Void, Boolean> test_send_request_and_wait_on_response_send_correctly = (Void v) -> {		
		Runnable server = new Runnable() {
			@SuppressWarnings("resource")
			public void run(){
				try {
					byte[] empty_data = new byte[100];
					DatagramPacket emptypacket = new DatagramPacket(empty_data, empty_data.length);
					DatagramSocket s2;
					s2 = new DatagramSocket(11);
					s2.receive(emptypacket); // receive the package
					s2.send(emptypacket); // send it right back
					server_status = true;
					s2.close();
				} catch (IOException e) {
					server_status = false;
					e.printStackTrace();
				}
			}
		};
		Thread s = new Thread(server);
		s.start();

		Runnable client = new Runnable() {
			public void run(){
				try {
					byte[] data = "abc".getBytes();
					DatagramSocket s1;
					DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 11);
					s1 = new DatagramSocket();
					DatagramPacket response = common_object.sendRequestAndWaitOnResponse(s1, packet);
					if(!Arrays.equals(common_object.filterPackage(response), data)) {
						client_status = false;
					}
					client_status = true;
					s1.close();
				} catch (Exception e) {
					client_status = false;
					e.printStackTrace();
				}
			}
		};
		Thread c = new Thread(client);
		c.start();
		
		try {
			s.join();
			c.join();
			if(client_status & server_status) {
				return true;
			}else {
				return false;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	};
	
	Function<Void, Boolean> test_send_request = (Void v) -> {
		Runnable server = new Runnable() {
			public void run(){
				try {
					byte[] empty_data = new byte[100];
					DatagramPacket emptypacket = new DatagramPacket(empty_data, empty_data.length);
					DatagramSocket s2;
					s2 = new DatagramSocket(11);
					s2.receive(emptypacket); // receive the package
					if(!Arrays.equals(common_object.filterPackage(emptypacket), "abc".getBytes())) {
						server_status = false;
					}else {
						server_status = true;
					}
					s2.close();
				} catch (IOException e) {
					server_status = false;
					e.printStackTrace();
				}
			}
		};
		Thread s = new Thread(server);
		s.start();

		Runnable client = new Runnable() {
			public void run(){
				try {
					byte[] data = "abc".getBytes();
					DatagramSocket s1;
					DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), 11);
					s1 = new DatagramSocket();
					boolean status = common_object.sendRequest(s1, packet);
					client_status = status;
					s1.close();
				} catch (Exception e) {
					client_status = false;
					e.printStackTrace();
				}
			}
		};
		Thread c = new Thread(client);
		c.start();

		try {
			s.join();
			c.join();
			if(client_status & server_status) {
				return true;
			}else {
				return false;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	};
	
	
	public void handleTest(String func_name, Function<Void, Boolean> func) throws Exception {
		System.out.println("running test `TestCommon`.`" + func_name + "`...");
		Boolean test_return = func.apply(null);
		if(!test_return) {
			throw new Exception();
		}
	}

	public void run() throws Exception {
		// call tests
		handleTest("test_class_name", test_class_name);
		handleTest("test_string_to_byte", test_string_to_byte);
		handleTest("test_int_to_byte", test_int_to_byte);
		handleTest("test_concatenate_byte_arrays_no_padding", test_concatenate_byte_arrays_no_padding);
		handleTest("test_concatenate_byte_arrays_with_padding", test_concatenate_byte_arrays_with_padding);
		handleTest("test_filter_package", test_filter_package);
		handleTest("test_send_request_and_wait_on_response_send_correctly", test_send_request_and_wait_on_response_send_correctly);
		handleTest("test_send_request", test_send_request);
		handleTest("test_generate_ack", test_generate_ack);
		handleTest("test_generate_data_packet", test_generate_data_packet);
	}
}
