package lh.demo.fraud.detection.api;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import lh.demo.fraud.detection.api.workflow.FraudDetectionWorkflow;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("transactions")
@CrossOrigin("http://localhost:3000")
public class TransactionController {
    private final LittleHorseBlockingStub client;
    private final TransactionRepository repository;

    public TransactionController(LittleHorseBlockingStub client, TransactionRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createTransaction(@RequestBody @Valid CreateTransactionRequest transaction) {
        String wfRunId = UUID.randomUUID().toString().replace("-", "");

        RunWfRequest runWf = RunWfRequest.newBuilder()
                .setId(wfRunId)
                .setWfSpecName(FraudDetectionWorkflow.WORKFLOW_NAME)
                .putVariables("source-account", LHLibUtil.objToVarVal(transaction.sourceAccount()))
                .putVariables("destination-account", LHLibUtil.objToVarVal(transaction.destinationAccount()))
                .putVariables("amount", LHLibUtil.objToVarVal(transaction.amount()))
                .build();

        client.runWf(runWf);
    }

    @GetMapping
    public List<Transaction> getTransactions() {
        return StreamSupport.stream(repository.findAll().spliterator(), false).toList();
    }
}
