# Money Transfers

Application provides the following REST services:
- create account
```
POST: /accounts
Requesy Body:
{
    "number": "123-456-78-3",
    "currency": "USD",
    "balance": 500,
    "userId": 2
}
```
- close account
```
DELETE: /accounts/:account_id
```
- get all non-closed accounts
```
GET: /accounts?showClosed=0
```
- get all accounts
```
GET: /accounts?showClosed=1
```
- make transfer from one account to another
```
POST: /transfers
Request Body:
{
    "accountIdFrom": 1,
    "accountIdTo": 2,
    "sum": 45.5
}

```
- get account with list of incoming and outgoing transactions
```
GET: /accounts/:account_id
```

Supported currencies:
- USD
- EUR
- GBP

### How to run locally

Use Main Class: ```io.vertx.core.Launcher```

Program arguments: ```run com.revolut.moneytransfers.HttpServerVerticle```

HTTP Server will start at ```localhost:8089```

Health Check can be done via request ```localhost:8089/health```
___
To run rests use the following command: ```mvn test```

### Build

Application is served as fat jar and located under target folder: ```moneytransfers-1.0-SNAPSHOT-fat.jar```