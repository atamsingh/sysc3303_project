package test;

public class TestCoordinator {
	
	public static void main(String[] args) {
		try {
			TestClient client_tester = new TestClient();
			client_tester.run();

			TestServer server_tester = new TestServer();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Tests Failed.");
			System.exit(1);
		}
	}
}
