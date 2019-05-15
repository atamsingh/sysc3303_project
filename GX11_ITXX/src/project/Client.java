package project;

public class Client {
	
	Commons common = new Commons("CLIENT");
	
	private void readRequest(String file_name, byte[] data) {
		// establish connect
		// .....
		// ack
		// generate the request
		// send
		// receive
		// ack
		// .....
		// return on success
	}
	
	private void writeRequest(String file_name, byte[] data) {
		// similar to above but write
	}
	
	public void main() {
		Client c = new Client();
		// get input 
		// ask all the questions that needs to be asked 
		// call the appropriate functions to kick WRD or RRQ correctly
		
		// if exit asked for... gracefully close all the requests and exit. < this means we need to track when write and read requesta are going 
		// luckily this is not threaded just 1 main thread per client.
		c.writeRequest("", null);
		c.readRequest("", null);
	}
}
