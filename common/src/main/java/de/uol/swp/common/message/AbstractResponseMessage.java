package de.uol.swp.common.message;

/**
 * Base class of all response messages. Basic handling of answers from the server
 * to the client
 *
 * @author Marco Grawunder
 * @see de.uol.swp.common.message.AbstractMessage
 * @see de.uol.swp.common.message.ResponseMessage
 * @since 2019-08-07
 */

@SuppressWarnings({"PMD.AbstractClassWithoutAnyMethod"})
public abstract class AbstractResponseMessage extends AbstractMessage implements ResponseMessage {
}
