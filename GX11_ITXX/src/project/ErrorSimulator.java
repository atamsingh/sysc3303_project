package project;

import java.net.*;

public class ErrorSimulator {
	
	Commons common = new Commons("ERROR SIMULATOR");
	String mode;
	
	public ErrorSimulator(String curr_mode) {
		mode = curr_mode;
	}
	
	public DatagramPacket vetPackage(DatagramPacket r) {
		// Error Simulator just returns packet for Iteration 1
		return r;
	}
	
}
