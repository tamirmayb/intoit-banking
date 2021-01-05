package intobank.response;

public class ResponseWanted {
    private final String commandId;

    public ResponseWanted(final String commandId) {
        this.commandId = commandId;
    }

    public String getCommandId() {
        return commandId;
    }

}
