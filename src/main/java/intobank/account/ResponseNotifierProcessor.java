package intobank.account;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import intobank.response.CommandResponse;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static intobank.response.CommandResponse.buildForChange;
import static intobank.response.CommandResponse.buildForRead;

public class ResponseNotifierProcessor implements Processor<String, JsonNode> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ActorRef router;

    private final CommandResponse.Status status;

    private ReadOnlyKeyValueStore<String, Double> accountBalanceStore;


    public static ResponseNotifierProcessor readProcessor(final ActorRef router, ReadOnlyKeyValueStore<String, Double> accountBalanceStore) {
        return new ResponseNotifierProcessor(router, accountBalanceStore);
    }

    public static ResponseNotifierProcessor changeAcceptedProcessor(final ActorRef router) {
        return new ResponseNotifierProcessor(router, CommandResponse.Status.CHANGE_ACCEPTED);
    }

    public static ResponseNotifierProcessor changeRejectedProcessor(final ActorRef router) {
        return new ResponseNotifierProcessor(router, CommandResponse.Status.CHANGE_REFUSED);
    }

    private ResponseNotifierProcessor(final ActorRef router, final CommandResponse.Status status) {
        this.router = router;
        this.status = status;
    }

    private ResponseNotifierProcessor(final ActorRef router, ReadOnlyKeyValueStore<String, Double> accountBalanceStore) {
        this.router = router;
        this.accountBalanceStore = accountBalanceStore;
        this.status = CommandResponse.Status.READ_ACCEPTED;
    }

    @Override
    public void init(final ProcessorContext processorContext) {
    }

    @Override
    public void process(final String account, final JsonNode jsonNode) {
        log.info("Processing {} command {}", status, jsonNode.get("id").asText());
        CommandResponse response = null;

        switch (status) {
            case CHANGE_REFUSED:
                response = buildForChange(jsonNode, CommandResponse.Status.CHANGE_REFUSED);
                break;
            case CHANGE_ACCEPTED:
                response = buildForChange(jsonNode, CommandResponse.Status.CHANGE_ACCEPTED);
                break;
            case READ_ACCEPTED:
                response = buildForRead(jsonNode, accountBalanceStore.get(account));
                break;
        }
        router.tell(response, ActorRef.noSender());
    }

    @Override
    public void close() {
    }

}
