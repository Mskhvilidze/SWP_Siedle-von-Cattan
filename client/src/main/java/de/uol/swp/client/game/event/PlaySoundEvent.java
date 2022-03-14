package de.uol.swp.client.game.event;

/**
 * Event used to play a given audio file if the tab of the given game session is selected
 * <p>
 * In order to play an audio file using this event, post an instance of it
 * onto the EventBus.
 */
public class PlaySoundEvent {
    private final String gameSessionName;
    private final String audioKey;

    /**
     * Constructor
     *
     * @param gameSessionName the name of the game session the sound is coming from
     * @param audioKey        the name.wav of the audio file located in resources/sounds/
     */
    public PlaySoundEvent(String gameSessionName, String audioKey) {
        this.gameSessionName = gameSessionName;
        this.audioKey = audioKey;
    }

    /**
     * Returns name of the game session the sound is coming from
     *
     * @return name of the game session the sound is coming from
     */
    public String getGameSessionName() {
        return gameSessionName;
    }

    /**
     * Returns the name.wav of the audio file located in resources/sounds/
     *
     * @return the name.wav of the audio file located in resources/sounds/
     */
    public String getAudioKey() {
        return audioKey;
    }
}
