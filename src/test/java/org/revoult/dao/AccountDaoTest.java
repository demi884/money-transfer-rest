package org.revoult.dao;

import org.junit.Test;
import org.revoult.dto.Account;

import java.util.Collections;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Created by Leo on 5/27/2018.
 */
public class AccountDaoTest {
    private AccountDao sut = new AccountDao();

    @Test
    public void byIdShouldReturnCopy() {
        String testAccountId = "testAccountId";
        AccountDao.initDatabase(Collections.singletonList(Account.builder().accountId(testAccountId).build()));

        Optional<Account> firstCall = sut.byId(testAccountId);
        Optional<Account> secondCall = sut.byId(testAccountId);
        assertFalse(firstCall.get() == secondCall.get());
        assertEquals(firstCall.get().getAccountId(), secondCall.get().getAccountId());
    }
}
