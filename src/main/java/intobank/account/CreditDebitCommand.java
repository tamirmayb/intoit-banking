package intobank.account;

import akka.actor.ActorRef;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import intobank.gateway.Command;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.StringJoiner;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreditDebitCommand {
    private String id;
    private Command.CommandType type;
    private ActorRef router;
    private Instruction command;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static CreditDebitCommand fromJson(final JsonNode jsonNode) {
        try {
            return mapper.treeToValue(jsonNode, CreditDebitCommand.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Setter
    @Getter
    public static class Instruction {
        protected String account;
        protected double value;

        @Override
        public String toString() {
            return new StringJoiner(", ", Instruction.class.getSimpleName() + "[", "]")
                    .add("account='" + account + "'")
                    .add("value=" + value)
                    .toString();
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CreditDebitCommand.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("type=" + type)
                .add("router=" + router)
                .add("command=" + command)
                .toString();
    }

    public double getValue() {
        return Command.CommandType.DEBIT.equals(type) ? command.value * -1 : command.value;
    }
}
