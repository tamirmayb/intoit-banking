package intobank.account;

import akka.actor.ActorRef;
import intobank.kafka.serde.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static intobank.ApplicationProps.*;

public class ResponseStream {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ReadOnlyKeyValueStore<String, Double> accountBalanceStore;
    private final ActorRef router;

    public ResponseStream(ActorRef router, ReadOnlyKeyValueStore<String, Double> accountBalanceStore) {
        this.router = router;
        this.accountBalanceStore = accountBalanceStore;
        startReadStream();
        startAccountAcceptedStream();
        startAccountRefusedStream();
    }

    private void startReadStream() {
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(
                accountReadTopic,
                Consumed.with(Serdes.String(), new JsonSerde())
        ).process(() -> ResponseNotifierProcessor.readProcessor(router, accountBalanceStore));

        KafkaStreams streams = new KafkaStreams(builder.build(), getAccountReadStreamProps());

        log.info("Now streaming from topic {}", accountReadTopic);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private void startAccountAcceptedStream() {
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(
                accountAcceptedTopic,
                Consumed.with(Serdes.String(), new JsonSerde())
        ).process(() -> ResponseNotifierProcessor.changeAcceptedProcessor(router));

        KafkaStreams streams = new KafkaStreams(builder.build(), getAccountAcceptedStreamProps());

        log.info("Now streaming from topic {}", accountAcceptedTopic);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private void startAccountRefusedStream() {
        final StreamsBuilder builder = new StreamsBuilder();

        builder.stream(
                accountRefusedTopic,
                Consumed.with(Serdes.String(), new JsonSerde())
        ).process(() -> ResponseNotifierProcessor.changeRejectedProcessor(router));

        KafkaStreams streams = new KafkaStreams(builder.build(), getAccountRefusedStreamProps());

        log.info("Now streaming from topic {}", accountRefusedTopic);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
