package rsps.account;

import backend.database.document.DocumentDatabase;

public class AccountDatabase extends DocumentDatabase<Account> {

    public AccountDatabase() {
        super("accounts", "json", 1);
    }

    @Override
    public Account deconstruct(byte[] data) {
        return null;
    }

    @Override
    public byte[] construct(Account document) {
        return new byte[0];
    }
}
