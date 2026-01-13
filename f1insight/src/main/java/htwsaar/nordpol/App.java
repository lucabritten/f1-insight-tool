package htwsaar.nordpol;

import htwsaar.nordpol.API.DriverClient;
// Test
public class App {
    public static void main(String[] args) {
        DriverClient client = new DriverClient();
        client.getDriverByName("Lando", "NORRIS");
    }
}
