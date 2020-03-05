# Money Transfers

Application provides the following REST services:
- create account
- close account
- get all non-closed accounts
- get all accounts
- make transfer from one account to another
- get account with list of incoming and outgoing transactions

### How to run locally

Use Main Class: ```io.vertx.core.Launcher```

Program arguments: ```run com.revolut.moneytransfers.HttpServerVerticle```

___
To run rests use the following command: ```mvn test```

### Build

Application is served as fat jar and located under target folder: ```moneytransfers-1.0-SNAPSHOT-fat.jar```