package Communication;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public enum Type {
        /** Operations */
        MOVE,
        MOVEMENTS,
        TRANSFER,
        BALANCE,

        /** State transfer */
        ASKSTATE,
        RECEIVESTATE,

        /** User options */
        REGISTER,
        LOGIN,
        LEAVE
    }

    public abstract Type getType();

    public abstract String getVMID();

}
