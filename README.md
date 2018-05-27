# money-transfer-rest
Rest service for money transfer(demo project)

## Implemented methods

| Path                                  | Type | Params                                                  | Description                                |
|---------------------------------------|------|---------------------------------------------------------|--------------------------------------------|
| /v1/transactions                      | GET  |                                                         | fetch all transactions                     |
| /v1/pending_transactions              | GET  |                                                         | fetch pending transactions                 |
| /v1/transactions/{transactionId}      | GET  |                                                         | fetch transaction by uuid                  |
| /v1/accounts/{accountId}/transactions | GET  |                                                         | fetch transactions for provided accountId  |
| /v1/accounts                          | GET  |                                                         | fetch all accounts                         |
| /v1/accounts/{accountId}              | GET  |                                                         | fetch account by id                        |
| /v1/transfer                          | POST | String fromAccountId, String toAccountId, Double amount | transfer money from one account to another |
