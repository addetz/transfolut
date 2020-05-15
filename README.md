Transfolut
==========
This a standalone REST API providing simple functionality for transferring money between bank accounts.

It has functionality to:
- create a new account
- retrieve the list of all accounts
- fetch the balances and transaction history of a specified account
- deposit and withdraw money from a specified account
- transfer money between two accounts

Technologies
============
This is a Java8 project which uses Jersey and deploys an embedded Grizzly server to run.
It is built with Gradle.


Endpoints
=========
The application will run at http://localhost:8080/transfolut.
It exposes the following endpoints:

/accounts 
---------
- Return : a JSON object of all account numbers available in the app
- Method : GET 
- Responses: 200

/accounts 
---------
- Create a new bank account with a nominated primary currency code
- Return : bank account number of the new bank account
- Method : POST 
- Data parameters: {"currency":[String]}
- Responses: 200 OK or 400 for invalid currency

/accounts/{:accountNumber}
-------------------------
- Return @ JSON object of account balances for the given account number 
- Method : GET
- Responses : 200 OK or 400 for invalid account number

/accounts/{:accountNumber}/history
----------------------------------
- Return : JSON object of account history for the given account number
- Method : GET
- Responses : 200 OK or 400 for invalid account number 

/transfer
---------
- Transfer money between accounts
- Return: Success message
- Method : POST 
- Data parameters: {"fromAcct": [String], "toAcct":[String], "currency":[String], "amount": [String]} 
- Responses : 200 or 400 for invalid parameters

/deposit
--------
- Deposit money into a nominated account 
- Return: Success message
- Method : POST 
- Data parameters: {"acct":[String], "currency":[String], "amount": [String]} 
- Responses : 200 or 400 for invalid parameters

/withdraw
---------
- Withdraw money from a nominated account 
- Return: Success message
- Method : POST 
- Data parameters: {"acct":[String], "currency":[String], "amount": [String]} 
- Responses : 200 or 400 for invalid parameters

Implementation details
======================
- The bank accounts do not allow overdrafts.
- Balances in all the Joda Money currency list are allowed.
- Bank operations cannot be performed across currencies.
- Each account has separate balances for each non zero currency that is available in the account
- Transfers to or from an account which does not already have a balance in that currency will create 
a corresponding currency balance.
- The only zero balance allowed is in the bank account primary currency set at account creation. 
- Exchange rate functionality is out of scope, but can be performed retroactively given the exchange rate history 
and account balance history.


How to build the project
======================

1. No containers or servers are required for running the project.
2. Run ```./gradlew build``` from the project directory. 
3. Run the Transfolut app ```./gradlew run```