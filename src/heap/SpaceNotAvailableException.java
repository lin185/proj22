package heap;

import chainexception.ChainException;

public class SpaceNotAvailableException extends ChainException {
    
    public SpaceNotAvailableException(Exception e, String n) {
        super(e, n);
    }
}

