package de.uol.swp.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javafx.scene.media.AudioClip;

import java.util.concurrent.TimeUnit;

/**
 * Cache Class used for storing javafx AudioClip.
 * The get method uses the unchecked variant for brevity.
 * Example:
 * AudioCache.get("diceSound.wav");
 */
public final class AudioCache {
    private static final LoadingCache<String, AudioClip> SOUNDS = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<>() {
                @Override
                public AudioClip load(String key) {
                    return new AudioClip(key);
                }
            });

    /**
     * Empty private Constructor
     */
    private AudioCache() {
    }

    /**
     * unchecked wrapper for get, can not fail with correct url
     *
     * @param key The audio_url, root is sounds/
     * @return an AudioClip
     */
    public static AudioClip getAudio(String key) {
        String audioPath = "/sounds/" + key;
        return AudioCache.SOUNDS.getUnchecked(ImageCache.class.getResource(audioPath).toString());
    }


}
