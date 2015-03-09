package heap;

import chainexception.ChainException;

public class InvalidUpdateException extends ChainException {
    
    public InvalidUpdateException(Exception e, String n) {
        super(e, n);
    }
}

