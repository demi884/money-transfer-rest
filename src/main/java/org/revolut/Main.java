package org.revolut;

import com.networknt.server.Server;
import org.revolut.utils.AccountUtils;

/**
 * Created by Leo on 5/27/2018.
 */
public class Main {

    public static void main(String[] args) {
        AccountUtils.createAccounts(10);
        Server.main(args);
    }
}
