package org.revolut.dao;

import com.google.common.annotations.VisibleForTesting;
import org.revolut.dto.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Leo on 5/27/2018.
 */
public class AccountDao {
    private static Map<String, Account> map = new ConcurrentHashMap<>();

    public static void initDatabase(List<Account> list) {
        map.clear();
        list.forEach(u -> map.put(u.getAccountId(), u));
    }

    /**
     * Method created for testing purposes, allows validate sum of money are not changing during transfer operations.
     */
    @VisibleForTesting
    public static BigDecimal sum() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Account account : map.values()) {
            sum = sum.add(account.getBalance());
        }
        return sum;
    }

    /**
     * @param accountId id of account
     * @return copy of user, it's required to emulate database behavior.
     */
    public Optional<Account> byId(String accountId) {
        return Optional.ofNullable(map.get(accountId)).map(a -> a.toBuilder().build());
    }

    public List<Account> fetchAll() {
        return map.values().stream().map(a -> a.toBuilder().build()).collect(Collectors.toList());
    }

    public void updateAccount(Account account) {
        map.replace(account.getAccountId(), account);
    }
}
