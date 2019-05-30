package project;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TalkToClient {
	private DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	
	public TalkToClient() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void sendClient(DatagramPacket sendPacket) {
		this.sendPacket = sendPacket;
		try {//send packet to server
			System.out.println("Error Simulator: packet formed");
			sendSocket.send(sendPacket);
			System.out.println("Error Simulator: packet sent");
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
