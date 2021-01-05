package intobank.account;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import intobank.gateway.Command;
import intobank.kafka.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static intobank.ApplicationProps.*;
import static intobank.account.CreditDebitCommand.fromJson;

public class AccountWriteStream {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();

    private AccountBalance accountBalance;
    private final ActorRef router;

    public AccountWriteStream(ActorRef router) {
        this.router = router;
        startStreams();
    }

    private ReadOnlyKeyValueStore<String, Double> accountBalanceStore = new ReadOnlyKeyValueStore<String, Double>() {
        @Override
        public Double get(final String s) {
            return null;
        }

        @Override
        public KeyValueIterator<String, Double> range(final String s, final String k1) {
            return null;
        }

        @Override
        public KeyValueIterator<String, Double> all() {
            return null;
        }

        @Override
        public long approximateNumEntries() {
            return 0;
        }
    };

    private void startStreams() {
        final StreamsBuilder builder = new StreamsBuilder();

        final KStream<String, JsonNode> accountStreams[] = builder.stream(
                accountTopic,
                Consumed.with(Serdes.String(), new JsonSerde()))
                .branch(
                        (account, jsonCommand) -> accountBalance.checkHasBalance(account, fromJson(jsonCommand).getValue()),
                        (account, jsonCommand) -> accountBalance.checkNoBalance(account, fromJson(jsonCommand).getValue())
                );

        KGroupedStream<String, Double> groupedAccount = groupValueByAccount(accountStreams[0]);
        materializeAccountAggregation(groupedAccount);

        accountStreams[1]
                .through(accountRefusedTopic)
                .process(() -> ResponseNotifierProcessor.changeRejectedProcessor(router));

        KafkaStreams streams = new KafkaStreams(builder.build(), getAccountStreamProps());

        streams.setStateListener((newState, oldState) -> {
                    // Check if store has been loaded
                    if (newState.equals(KafkaStreams.State.RUNNING) &&
                            oldState.equals(KafkaStreams.State.REBALANCING)) {
                        accountBalanceStore = streams.store(accountStore, QueryableStoreTypes.keyValueStore());
                        new ResponseStream(router, accountBalanceStore);
                        accountBalance = new AccountBalance(accountBalanceStore);
                    }
                }
        );

        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private KTable<String, Double> materializeAccountAggregation(final KGroupedStream<String, Double> groupedAccount) {
        return groupedAccount.aggregate(
                () -> 0D,
                (accountId, balance, oldBalance) -> oldBalance + balance,
                Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>
                        as(accountStore)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Double())
        );
    }

    private KGroupedStream<String, Double> groupValueByAccount(final KStream<String, JsonNode> accountStream) {
        return accountStream.through(accountAcceptedTopic)
                .mapValues((jsonNode) -> {
                    Command command = mapper.convertValue(jsonNode, Command.class);
                    double value = command.getCommand().get("value").asDouble();
                    return command.getType().equals(Command.CommandType.DEBIT) ? value * -1 : value;
                }).groupByKey();
    }

}
