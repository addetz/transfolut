package com.transfolut.bank;

import com.transfolut.TransfolutMessages;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Service to create bank account, keep track of them and perform operations on them.
 * This is the public endpoint of the bank service and it must exist as a singleton as it holds bank accounts.
 *
 * @author addetz
 */
public class BankService {

    private static final long MAXIMUM_ACCT_NUMBER = 99999999L;
    private static final long MINIMUM_ACCOUNT_NUMBER = 10000000L;
    private final BankTransferService transferService= new BankTransferService();

//  Need to protect against bombarding create account calls, while staying fast on read calls
    private final Map<Long, BankAccount> bankAccounts= new ConcurrentHashMap<>();
    private static BankService instance = null;

    private BankService() {
        // Exists only to defeat instantiation.
    }

    public static BankService getInstance() {
        if(instance == null) {
            instance = new BankService();
        }
        return instance;
    }

    /**
     * @return the full list of bank accounts
     */
    public List<Long> getBankAccounts() {
        return bankAccounts.keySet().stream().collect(Collectors.toList());
    }

    /**
     *
     * @param bankAcctNumber account number to fetch on
     * @return corresponding optional {@link BankAccount}
     */
    public Optional<BankAccount> getAccount(long bankAcctNumber) {
        return bankAccounts.containsKey(bankAcctNumber) ? Optional.of(bankAccounts.get(bankAcctNumber)) : Optional.empty();
    }

    /**
     * Generates a bank account number and creates a bank account.
     * @return the newly created {@link BankAccount}
     * @throws org.joda.money.IllegalCurrencyException if no corresponding currency is found
     */
    public long createAccount(String currencyCode) {
        BankAccount newBankAccount = new BankAccount(generateBankAcct(), CurrencyUnit.of(currencyCode));
        bankAccounts.putIfAbsent(newBankAccount.getBankAccountNumber(), newBankAccount);
        return newBankAccount.getBankAccountNumber();
    }

    /**
     * @return a randomly generated 8 digit bank account
     */
    private long generateBankAcct() {
        return ThreadLocalRandom.current().nextLong(MINIMUM_ACCOUNT_NUMBER, MAXIMUM_ACCT_NUMBER);
    }

    /**
     * Moves money from one account to another
     * Synchronization needed since we need several operations
     * @param fromAcctNumber - account to transfer from
     * @param toAcctNumber - account to transfer to
     * @param amount - amount to transfer
     */
    public void transfer(long fromAcctNumber, long toAcctNumber, Money amount) {
        Optional<BankAccount> fromAccount = getAccount(fromAcctNumber);
        Optional<BankAccount> toAccount = getAccount(toAcctNumber);

        if(!fromAccount.isPresent() || !toAccount.isPresent()) {
            throw new IllegalArgumentException(TransfolutMessages.TRANSFER_ACCOUNTS_NONEXISTENT);
        }

        transferService.transfer(fromAccount.get(), toAccount.get(), amount);
    }

    /**
     * Deposits money into a bank account given an account number.
     * @param acctNumber - account to deposit money into
     * @param amount - amount to deposit
     * @return new balance
     */
    public void deposit(long acctNumber, Money amount) {
        Optional<BankAccount> bankAccount = getAccount(acctNumber);

        if(!bankAccount.isPresent()) {
            throw new IllegalArgumentException(
                    String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, String.valueOf(acctNumber)));
        }

        bankAccount.get().deposit(amount);
    }

    /**
     * Withdrawn money from a bank account given an account number.
     * @param acctNumber - account to withdraw money from
     * @param amount - amount to withdraw
     * @return new balance
     */
    public void withdraw(long acctNumber, Money amount) {
        Optional<BankAccount> bankAccount = getAccount(acctNumber);

        if(!bankAccount.isPresent()) {
            throw new IllegalArgumentException(
                    String.format(TransfolutMessages.ACCOUNT_NOT_FOUND, String.valueOf(acctNumber)));
        }

        bankAccount.get().withdraw(amount);
    }

    /**
     * Clear all bank accounts from the list.
     */
    public void clearAccounts() {
        bankAccounts.clear();
    }
}
