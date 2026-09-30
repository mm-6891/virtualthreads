package com.pildora.apps.virtualthreads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.apache.coyote.AbstractProtocol;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SpringBootApplication
public class VirtualthreadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualthreadsApplication.class, args);
	}

	@Bean
	public TomcatProtocolHandlerCustomizer<?> protocolHandlerThreads(
			@Value("${app.threads.type:platform}") String threadType) {
		Executor executor = switch (threadType) {
			case "virtual" -> Executors.newVirtualThreadPerTaskExecutor();
			case "platform" -> Executors.newFixedThreadPool(8);
			default -> throw new IllegalArgumentException(
					"Unsupported app.threads.type: " + threadType + ". Use 'platform' or 'virtual'.");
		};

		return protocolHandler -> {
			if (protocolHandler instanceof AbstractProtocol<?> proto) {
				proto.setExecutor(executor);
			}
		};
	}

}
