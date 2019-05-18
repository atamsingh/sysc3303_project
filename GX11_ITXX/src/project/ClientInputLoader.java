package project;

public class ClientInputLoader {
	boolean notShutDown = false;
	String requestType = "read"; // or write
	String read_file_name = "/Users/atamjeetsingh/Desktop/testing.txt"; // ask not for full path instead for directory to work in + file to read
	String write_file_name = "/Users/atamjeetsingh/Desktop/testing.txt"; // similar to above. make sure this string add concatenated when captured
	String mode = "verbose"; // or can be quiet
	int server_port = 69;
	boolean write_to_file = true;
	
	public void askClientInput() {
		// ask questions and update the class variables;
	}
}
