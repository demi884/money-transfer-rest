package org.revolut.utils;

import org.apache.commons.lang3.RandomUtils;
import org.revolut.dao.AccountDao;
import org.revolut.dto.Account;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 5/27/2018.
 */
public class AccountUtils {
    private static final String ACCOUNT_ID_PREFIX = "userId";
    private volatile static int userCount = 10;

    public static void createAccounts(int count) {
        userCount = count;
        List<Account> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(AccountUtils.createUser(i));
        }
        AccountDao.initDatabase(list);

    }

    private static Account createUser(int i) {
        return Account.builder().accountId(ACCOUNT_ID_PREFIX + i).balance(randomAmount()).build();
    }

    public static BigDecimal randomAmount() {
        return BigDecimal.valueOf(RandomUtils.nextDouble(0, 100_000_000)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static Account randomAccount() {
        return createUser(RandomUtils.nextInt(0, userCount));
    }
}
