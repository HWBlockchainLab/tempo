package org.hyperledger.tempo.ts;

public interface TsValidatorPlugin {
    /**
     * The Validate method used by the committer to ensure the transaction data consistency in the ledger
     *
     * @param txData The argument is a set of:
     *               * Query transactions will send a list of unique keys and their committed version numbers (without values) that the transaction 	reads during simulation.
     *               * Mutational transactions will send a list of unique keys and their values that the transaction writes.
     */
    Boolean validate(Object txData);
}
