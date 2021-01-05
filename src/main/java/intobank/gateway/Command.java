package intobank.gateway;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;

import java.util.StringJoiner;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Command {
    protected String id;
    protected CommandType type;
    protected JsonNode command;
    protected ActorRef router;

    public enum CommandType {
        TRANSFER, READ, CREDIT, DEBIT
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Command.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("command=" + command)
                .add("router=" + router)
                .toString();
    }

    public static Command ofRead(String accountId) {
        ObjectNode cmdNode = JsonNodeFactory.instance.objectNode();
        cmdNode.put("account", accountId);

        return Command.builder()
                .id(UUID.randomUUID().toString())
                .type(CommandType.READ)
                .command(cmdNode)
                .build();

    }
}
