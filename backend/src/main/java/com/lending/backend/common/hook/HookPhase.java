package com.lending.backend.common.hook;

/**
 * Represents the phase in which a hook is executed.
 */
public enum HookPhase {
    /**
     * Executed before validation.
     */
    PRE_VALIDATE,
    
    /**
     * Executed after validation but before the operation.
     */
    PRE_OPERATION,
    
    /**
     * Executed after the operation but before the transaction is committed.
     */
    POST_OPERATION,
    
    /**
     * Executed after the transaction is committed.
     */
    AFTER_COMMIT
}
