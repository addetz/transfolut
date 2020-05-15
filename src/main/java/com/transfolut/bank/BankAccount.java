package com.transfolut.bank;

import com.transfolut.TransfolutMessages;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.joda.time.LocalDateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Model class to represent a bank account and its {@link Money} balances.
 *
 * @author addetz
 */
public class BankAccount {

    private final long bankAccountNumber;
    private final CurrencyUnit primaryCurrency;
    private final Map<CurrencyUnit, BigDecimal> balances = new ConcurrentHashMap<>();
    private final Queue<String> history = new ConcurrentLinkedQueue<>();
    private final ReentrantLock balanceReadLock = new ReentrantLock();

    BankAccount(long bankAcctNumber, CurrencyUnit primaryCurrency) {
        this.bankAccountNumber = bankAcctNumber;
        this.primaryCurrency = primaryCurrency;
        balances.putIfAbsent(primaryCurrency, BigDecimal.ZERO);
        history.add(String.format(TransfolutMessages.HISTORY_ACCOUNT_CREATED, new LocalDateTime(System.currentTimeMillis())));
    }

    /**
     * @return the account number of the bank account
     */
    public long getBankAccountNumber() {
        return bankAccountNumber;
    }

    /**
     * @return the balances of the {@link BankAccount}
     */
    public List<String> getBalances() {
        return balances.entrySet().stream()
                .map(balance -> Money.of(balance.getKey(), balance.getValue()).toString())
                .collect(Collectors.toList());
    }

    /**
     * @return the full bank statement of the {@link BankAccount}
     */
    public List<String> getHistory() {
        return history.stream().collect(Collectors.toList());
    }


    /**
     * Depositing a new currency will create the corresponding entry in the balances
     * otherwise it will be added to the existing currency balance.
     * Zero balances are not allowed.
     * @param deposit to add
     */
    public void deposit(Money deposit) {
        balances.compute(deposit.getCurrencyUnit(), (currency, money) ->
                (money == null) ? deposit.getAmount() : money.add(deposit.getAmount()));
        String hist = String.format(TransfolutMessages.HISTORY_TRANSACTION, new LocalDateTime(System.currentTimeMillis()), deposit);
        history.add(hist);
        cleanUpZeroBalances();
    }


    /**
     * Helper method to clean up any zero balances from the account list.
     * Only the primary account currency is allowed to stay with a zero balance.
     */
    private void cleanUpZeroBalances() {
        balances.entrySet()
                .removeIf(balance -> balance.getValue().compareTo(BigDecimal.ZERO) == 0
                        && !balance.getKey().equals(primaryCurrency));
    }


    /**
     * Withdraw an amount from the account if enough funds are available.
     * @param withdrawal to remove
     * @return new balance
     */
    public void withdraw(Money withdrawal) {
        // In this case, a read lock is required such that only one withdrawal at a time is validated
        balanceReadLock.lock();
        try {
            if(!balances.containsKey(withdrawal.getCurrencyUnit()) ||
                    balances.get(withdrawal.getCurrencyUnit()).compareTo(withdrawal.getAmount()) < 0) {
                throw new IllegalArgumentException(String.format(TransfolutMessages.INSUFFICIENT_BALANCE, withdrawal.toString()));
            }
            deposit(withdrawal.negated());
        } finally {
            balanceReadLock.unlock();
        }
    }

    @Override
    public String toString() {
        return String.format("%s", bankAccountNumber);
    }
}