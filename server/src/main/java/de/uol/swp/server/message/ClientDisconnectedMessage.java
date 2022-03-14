package de.uol.swp.server.message;

/**
 * This message is posted onto the EventBus if a client disconnects
 * <p>
 * The LobbyService uses this message to log the user out of every Lobby they are currently part of.
 * The UserManagement uses this message to log out the user.
 * <p>
 *
 * @author Marco Grawunder
 * @see de.uol.swp.server.message.AbstractServerInternalMessage
 * @see de.uol.swp.server.communication.ServerHandler#clientDisconnected
 * @see de.uol.swp.server.usermanagement.AuthenticationService
 * @since 2019-08-07
 */
public class ClientDisconnectedMessage extends AbstractServerInternalMessage {

}
