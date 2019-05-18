package project;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientInputLoader {
	boolean notShutDown = false;
	String requestType = "read"; // or write
	String read_file_name = "/Users/atamjeetsingh/Desktop/testing.txt"; // ask not for full path instead for directory to work in + file to read
	String write_file_name = "/Users/atamjeetsingh/Desktop/testing.txt"; // similar to above. make sure this string add concatenated when captured
	String mode = "verbose"; // or can be quiet
	InetAddress server_address = null;
	int server_port = 69;
	boolean write_to_file = true;
	
	public ClientInputLoader() throws UnknownHostException {
		server_address = InetAddress.getByName("");  // server address
	}
	
	public void askClientInput() {
		// ask questions and update the class variables;
		
	}
}
