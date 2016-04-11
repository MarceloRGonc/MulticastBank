package Communication;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public enum Type { MOVE, BALANCE, LEAVE, STATE }

    public abstract Type getType();

    public abstract float getAmount();

    public abstract boolean getResponse();

}
