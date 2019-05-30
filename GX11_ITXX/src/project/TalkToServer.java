package project;
import java.io.*;
import java.net.*;

public class TalkToServer {
	private DatagramSocket sendSocket;
	private DatagramPacket sendPacket;
	
	public TalkToServer() {
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void sendServer(DatagramPacket sendPacket) {
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
