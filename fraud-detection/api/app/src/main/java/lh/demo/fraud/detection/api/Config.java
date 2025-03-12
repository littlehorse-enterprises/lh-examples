package lh.demo.fraud.detection.api;

import io.littlehorse.sdk.common.config.LHConfig;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import lh.demo.fraud.detection.api.worker.ApproveTransactionWorker;
import lh.demo.fraud.detection.api.worker.DetectFraudWorker;
import lh.demo.fraud.detection.api.worker.RejectTransactionWorker;
import lh.demo.fraud.detection.api.worker.SaveTransactionWorker;
import lh.demo.fraud.detection.api.workflow.FraudDetectionWorkflow;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Class responsible for loading configuration of the server from environment
 * variables.
 */
@SpringBootConfiguration
public class Config {

    @Bean
    public LHConfig lhConfig() {
        return new LHConfig();
    }

    @Bean
    public LittleHorseBlockingStub lhClient(LHConfig config) throws Exception {
        return config.getBlockingStub();
    }

    @Bean
    public SaveTransactionWorker saveTransactionWorker(LHConfig config, TransactionRepository repository) {
        return new SaveTransactionWorker(config, repository);
    }

    @Bean
    public ApproveTransactionWorker approveTransactionWorker(LHConfig config, TransactionRepository repository) {
        return new ApproveTransactionWorker(config, repository);
    }

    @Bean
    public DetectFraudWorker detectFraudWorker(LHConfig config, TransactionRepository repository) {
        return new DetectFraudWorker(config, repository);
    }

    @Bean
    public RejectTransactionWorker rejectTransactionWorker(LHConfig config, TransactionRepository repository) {
        return new RejectTransactionWorker(config, repository);
    }

    @Bean
    public FraudDetectionWorkflow registerWorkflow(LittleHorseBlockingStub client) {
        return new FraudDetectionWorkflow(client);
    }
}
