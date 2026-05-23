package gr.aueb.lecturelens.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        System.setProperty("java.naming.provider.url", "dns://8.8.8.8");
        SpringApplication.run(BackendApplication.class, args);
    }
}
