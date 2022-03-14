package de.uol.swp.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;

import java.util.concurrent.TimeUnit;

/**
 * Cache Class used for storing javafx ImagePattern.
 * The get method uses the unchecked variant for brevity.
 * Example:
 * ImageCache.get("objects/coin_01.png");
 */
@SuppressWarnings({"PMD.ClassNamingConventions"})
public final class ImageCache {

    private static final LoadingCache<String, Image> IMAGES = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(new CacheLoader<>() {
                @Override
                public Image load(String key) {
                    return new Image(key);
                }
            });

    /**
     * Empty private Constructor
     */
    private ImageCache() {
    }

    /**
     * unchecked wrapper for get, can not fail with correct url
     *
     * @param key The image_url, root is graphics/
     * @return an ImageView with the same dimensions as the Imagefile pointed to by the key
     */
    public static ImageView getView(String key) {
        String imagePath = "graphics/" + key;
        return new ImageView(ImageCache.IMAGES.getUnchecked(imagePath));
    }

    /**
     * unchecked wrapper for get, can not fail with correct url
     *
     * @param key The image_url, root is graphics/
     * @return the Image of the Imagefile pointed to by the key
     */
    public static Image getImage(String key) {
        String imagePath = "graphics/" + key;
        return ImageCache.IMAGES.getUnchecked(imagePath);
    }

    /**
     * unchecked wrapper for get, can not fail with correct url
     *
     * @param key The image_url, root is graphics/
     * @return an ImagePattern with the same dimensions as the Imagefile pointed to by the key
     */
    public static ImagePattern getPattern(String key) {
        String imagePath = "graphics/" + key;
        return new ImagePattern(ImageCache.IMAGES.getUnchecked(imagePath));
    }

    /**
     * Unchecked image cache wrapper for urls provided by fxml
     *
     * @param key the full image url
     * @return an ImagePattern with the same dimensions as the Imagefile pointed to by the key
     */
    public static ImagePattern getPatternFXML(String key) {
        return new ImagePattern(ImageCache.IMAGES.getUnchecked(key));
    }

}
