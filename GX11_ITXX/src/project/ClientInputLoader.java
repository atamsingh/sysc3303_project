package project;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientInputLoader {
	private Scanner scanner = new Scanner(System.in);
	boolean notShutDown = true;
	String requestType = "read"; // or write
	private String server_file_path = "/Users/atamjeetsingh/Desktop";
	private String client_file_path = "/Users/atamjeetsingh/Desktop";
	private String server_file_subname = "testing.txt";
	private String client_file_subname = "testing_copy.txt";
	String read_file_name = client_file_path + "/" + server_file_subname; // ask not for full path instead for directory to work in + file to read
	String write_file_name = client_file_path + "/" + client_file_subname; // similar to above. make sure this string add concatenated when captured
	String mode = "verbose"; // or can be quiet
	InetAddress server_address = null;
	int server_port = 69;
	boolean write_to_file = true;
	//first run
	private boolean firstItteration = true;
	
	public ClientInputLoader() throws UnknownHostException {
		server_address = InetAddress.getByName("");  // server address
	}
	
	public void printall() {
		System.out.println("mode: " + mode);
		System.out.println("notShutDown: " + notShutDown);
		System.out.println("requestType: " + requestType);
		System.out.println("read_file_name: " + read_file_name);
		System.out.println("write_file_name: " + write_file_name);
		System.out.println("server_port: " + server_port);
		System.out.println("write_to_file: " + write_to_file);
		System.out.println("firstItteration: " + firstItteration);
	}
	
	public void askClientInput() throws UnknownHostException {

		if(firstItteration) {
			System.out.println("Welcome to use the SYSC 3303's team 11's project");
			System.out.println("Project iteration : 1");
			notShutDown = true;
			this.ask_server_address();
			this.ask_port();
			this.ask_mode();
			this.ask_requestType();
			this.ask_server_filepath();
			this.ask_server_filename();
			this.ask_client_filepath();
			this.ask_client_filename();
			if(requestType.equals("read"))
			{
				read_file_name = server_file_path + "/" + server_file_subname;
				write_file_name = client_file_path + "/" + client_file_subname;
			}
			else
			{
				read_file_name = client_file_path + "/" + client_file_subname;
				write_file_name = server_file_path + "/" + server_file_subname;
			}
			firstItteration = false;
		} else {
			System.out.print("Do you want to continue using this client (Y/N):");
			if(!ask_continue())
			{
				scanner.close();
				notShutDown = false;
			} else {
				System.out.print("Do you want to change the server address " + server_address.toString() + " (Y/N):");
				if(ask_continue())
				{this.ask_server_address();}
			
				System.out.print("Do you want to change the port number " + server_port + " (Y/N):");
				if(ask_continue())
				{this.ask_port();}
				
				System.out.print("Do you want to change the mode " + mode + " (Y/N):");
				if(ask_continue())
				{this.ask_mode();}
				
				System.out.print("Do you want to change the request type " + requestType + " (Y/N):");
				if(ask_continue())
				{this.ask_requestType();}
				
				System.out.print("Do you want to change the file path on server " + server_file_path + " (Y/N):");
				if(ask_continue())
				{this.ask_server_filepath();}
				
				System.out.print("Do you want to change the file name on server " + server_file_subname + " (Y/N):");
				if(ask_continue())
				{this.ask_server_filename();}
				
				System.out.print("Do you want to change the file path on client " + client_file_path + " (Y/N):");
				if(ask_continue())
				{this.ask_client_filepath();}
				
				System.out.print("Do you want to change the file name on client " + client_file_subname + " (Y/N):");
				if(ask_continue())
				{this.ask_client_filename();}
				
				if(requestType.equals("read"))
				{
					read_file_name = server_file_path + "/" + server_file_subname;
					write_file_name = client_file_path + "/" + client_file_subname;
				}
				else
				{
					read_file_name = client_file_path + "/" + client_file_subname;
					write_file_name = server_file_path + "/" + server_file_subname;
				}
				
				scanner.close();
			}
		}
		return;
	}
	
	//asking the server address
	private void ask_server_address() throws UnknownHostException
	{
		System.out.print("Please type in the server address  or local for localhost:");
		String input = scanner.nextLine();
		if(input.equals("local")) {
			server_address= InetAddress.getLocalHost();
		}else {
			server_address = InetAddress.getByName(input);
		}
		
	}
	
	//ask for the port
	private void ask_port()
	{
		System.out.print("Please type in the port number :");
		server_port = scanner.nextInt();
		scanner.nextLine();
	}
	
	//ask for file path in server
	private void ask_server_filepath()
	{
		System.out.print("Please type in file path on server :");
		server_file_path = scanner.nextLine();
	}
	
	//ask for the file path in client
	private void ask_client_filepath()
	{
		System.out.print("Please type in file path on local :");
		client_file_path = scanner.nextLine();
	}
	
	//ask for the file name in server
	private void ask_server_filename()
	{
		System.out.print("Please type in file name on server :");
		server_file_subname = scanner.nextLine();
	}
	
	//ask the file name in client
	private void ask_client_filename()
	{
		System.out.print("Please type in file name on server :");
		client_file_subname = scanner.nextLine();
	}
	
	//ask the request type
	private void ask_requestType()
	{
		System.out.print("Please type in request type :");
		requestType = scanner.nextLine().toLowerCase();
		while (!requestType.equals("read") && !requestType.equals("write"))
		{
			System.out.println("Please type in request type as read or write :");
			requestType = scanner.nextLine().toLowerCase();
		}
	}
	
	//ask the mode
	private void ask_mode()
	{
		System.out.print("Please type in the mode :");
		mode = scanner.nextLine().toLowerCase();
		while (!mode.equals("verbose") && !mode.equals("quiet"))
		{
			System.out.print("Please type in mode as verbose or quiet :");
			mode = scanner.nextLine().toLowerCase();
		}
	}
	
	private boolean ask_continue()
	{
		String continue_input = scanner.nextLine().toUpperCase();
		return continue_input.equals("Y");
	}

}
