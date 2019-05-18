package project;
//NNAmdi Okwechime

public class Server {
	
	Commons common = new Commons("SERVER");
	int port_address =  69;
	
	private void handleReadRequest() {

	}
	
	private void handleWriteRequest() {

	}
	
	private void handleRequest() {
		// if read
		this.handleReadRequest();
		// else if write
		this.handleWriteRequest();
		// else
		// handle error
	}
	
	public void startListening() {
		// listen to the port needed for requests 
		// create threads to handle these req. received
		// One thread will wait on port 69 for UDP datagrams (that should contain RRQ or WRQ packets)
		// create another thread (call it the client connection thread), and pass it the TFTP packet to deal with
		this.handleRequest(); // send data along
		// go back to waiting on port 69 for another request. 
	}

	public void main() {
		// get ready to accept 
		//  supporting multiple concurrent read and write connections with different clients
		// For each RRQ, the server should respond with DATA block 1 and 0 bytes of data (no file I/O). For each WRQ the server should respond with ACK block 0. 
		Server s = new Server();
		s.startListening();
		
		// elegant shutdown
		// finish all file transfers,  but refuse to create new connections with clients
		
	}
}
