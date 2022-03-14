package de.uol.swp.common.game.dto;

import de.uol.swp.common.game.board.PieceType;

import java.io.Serializable;
import java.util.Objects;

/**
 * Objects of this class are used to transfer piece data between the server and the clients
 */
public class PieceDTO implements Serializable {
    private final PieceType pieceType;
    private final PlayerDTO owner;

    /**
     * Constructor
     *
     * @param pieceType the type of the piece
     * @param owner     the player who owns the piece
     */
    public PieceDTO(PieceType pieceType, PlayerDTO owner) {
        this.pieceType = pieceType;
        this.owner = owner;
    }

    /**
     * Returns the type of the piece
     *
     * @return the type of the piece
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Returns the player who owns the piece
     *
     * @return the player who owns the piece
     */
    public PlayerDTO getOwner() {
        return owner;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PieceDTO pieceDTO = (PieceDTO) obj;
        return pieceType == pieceDTO.pieceType && Objects.equals(owner, pieceDTO.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, owner);
    }
}
