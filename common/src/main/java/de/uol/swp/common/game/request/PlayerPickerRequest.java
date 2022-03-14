package de.uol.swp.common.game.request;

import de.uol.swp.common.game.dto.PlayerDTO;

import java.util.Objects;

/**
 * PlayerPicker Requeste sends the selected Player to the server
 */
public class PlayerPickerRequest extends AbstractGameRequest {
    private PlayerDTO selectedPlayer;

    /**
     * Construcotr
     *
     * @param gameSessionName gameSessionName
     * @param selectedPlayer  selectedPlayer
     */
    public PlayerPickerRequest(String gameSessionName, PlayerDTO selectedPlayer) {
        super(gameSessionName);
        this.selectedPlayer = selectedPlayer;
    }


    /**
     * Getter for the selected Player
     *
     * @return selected Player
     */
    public PlayerDTO getSelectedPlayer() {
        return selectedPlayer;
    }

    /**
     * Setter for the selected Player
     *
     * @param selectedPlayer the selected Player
     */
    public void setSelectedPlayer(PlayerDTO selectedPlayer) {
        this.selectedPlayer = selectedPlayer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        PlayerPickerRequest that = (PlayerPickerRequest) obj;
        return Objects.equals(selectedPlayer, that.selectedPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), selectedPlayer);
    }
}
