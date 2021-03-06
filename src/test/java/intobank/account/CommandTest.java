package intobank.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import intobank.gateway.Command;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CommandTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testJsonConversion() throws JsonProcessingException {
       CreditDebitCommand cmd = mapper.treeToValue(createCommand(), CreditDebitCommand.class);
       log.info("{}", cmd);
    }

    private JsonNode createCommand() {
        ObjectNode jsonCmd = JsonNodeFactory.instance.objectNode();

        jsonCmd.put("id", UUID.randomUUID().toString());
        jsonCmd.put("type", Command.CommandType.CREDIT.toString());

        ObjectNode command = JsonNodeFactory.instance.objectNode();
        command.put("account", "ACC-TEST-001");
        command.put("value", 10D);
        jsonCmd.set("command", command);
        return jsonCmd;
    }
}
