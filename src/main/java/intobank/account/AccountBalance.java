package intobank.account;

import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountBalance {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ReadOnlyKeyValueStore<String, Double> accountBalanceStore;

    public AccountBalance(final ReadOnlyKeyValueStore<String, Double> accountBalanceStore) {
        this.accountBalanceStore = accountBalanceStore;
    }

    public boolean checkNoBalance(String accountId, double value) {
        double balance = getCurrentBalance(accountId);
        log.info("Checking if account {} has NO balance ? {}", accountId, balance + value < 0);
        return balance + value < 0;
    }

    public boolean checkHasBalance(String accountId, double value) {
        double balance = getCurrentBalance(accountId);
        log.info("Checking if account {} has balance ? {}", accountId, balance + value >= 0);
        return balance + value >= 0;
    }

    public Double getCurrentBalance(String accountId) {
        Double balance = accountBalanceStore.get(accountId);
        balance = balance == null ? 0 : balance;
        return balance;
    }

}
