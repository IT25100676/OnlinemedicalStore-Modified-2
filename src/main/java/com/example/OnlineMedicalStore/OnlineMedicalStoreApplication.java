package com.example.OnlineMedicalStore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class OnlineMedicalStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineMedicalStoreApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openBrowser(ApplicationReadyEvent event) {
		if (!(event.getApplicationContext() instanceof WebServerApplicationContext context)) {
			return;
		}

		String url = "http://localhost:" + context.getWebServer().getPort();

		System.out.println("Opening browser at " + url);
		openSystemBrowser(url);
	}

	private static void openSystemBrowser(String url) {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.contains("mac")) {
				new ProcessBuilder("open", url).start();
			} else if (os.contains("win")) {
				new ProcessBuilder("cmd", "/c", "start", "", url).start();
			} else {
				new ProcessBuilder("xdg-open", url).start();
			}
		} catch (Exception exception) {
			System.out.println("Could not open browser automatically. Open this URL manually: " + url);
		}
	}

}
