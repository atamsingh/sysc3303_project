package test;

public class TestCoordinator {
	
	public static void main(String[] args) {
		System.out.println("Running tests....");
		
		try {
			TestClient client_tester = new TestClient();
			client_tester.run();
<<<<<<< HEAD
			TestCommon common_tester = new TestCommon();
			common_tester.run();
=======

			TestServer server_tester = new TestServer();
>>>>>>> requests
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Tests Failed.");
			System.exit(1);
		}

		System.out.println("All tests successfully parsed....");
	}
}

