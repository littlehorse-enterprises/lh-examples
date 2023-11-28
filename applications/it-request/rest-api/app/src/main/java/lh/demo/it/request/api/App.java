package lh.demo.it.request.api;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LHPublicApiGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public LHPublicApiGrpc.LHPublicApiBlockingStub client() throws IOException {
        final LHConfig config = new LHConfig();
        return config.getBlockingStub();
    }

}
