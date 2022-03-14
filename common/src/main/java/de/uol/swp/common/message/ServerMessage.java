package de.uol.swp.common.message;

import de.uol.swp.common.user.Session;

import java.util.List;

/**
 * A message from server to a number of clients that
 * is not necessary a response to a request (aka server push)
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.message.Message
 * @since 2019-08-07
 */
public interface ServerMessage extends Message {

    /**
     * Getter for the receiver list
     *
     * @return a {@code List} of Session objects defining the receivers
     * @since 2019-10-08
     */
    List<Session> getReceiver();

    /**
     * Sets the receivers of this message
     *
     * @param receiver a {@code List} of Session objects defining the receivers
     * @since 2019-10-08
     */
    void setReceiver(List<Session> receiver);

    /**
     * Returns whether the receiver of this message need to be logged in to receive it
     *
     * @return whether the receiver of this message need to be logged in to receive it
     */
    boolean authorizationNeeded();
}
