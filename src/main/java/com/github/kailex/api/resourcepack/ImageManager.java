package com.github.kailex.api.resourcepack;

import com.github.kailex.api.util.ImageUtil;
import com.github.kailex.api.util.Rotation;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class can manage an amount (Menge) of images and their rotations.
 * All images were merged together (with individual rotation) to create one image.
 *
 * @author Alexander Ley
 * @version 1.2
 */
public class ImageManager implements Serializable {

    /**
     * A Hashmap containing all Images from textures folder. The Hashmap maps filename (with .png) and the corresponding Image.
     */
    public static final HashMap<String, Image> IMAGE_MAP = new HashMap<>();

    //Render Pipeline
    /**
     * List of all imageKeys which were later merged together.
     */
    private List<String> imageKeys = new ArrayList<>();
    /**
     * List of corresponding rotations.
     */
    private List<Rotation> imageRot = new ArrayList<>();

    /**
     * Rendered Image
     */
    @Nullable
    private transient Image image;

    /**
     * Creates an empty ImageManager (imageKeys and imageRot are empty).
     */
    public ImageManager(){ }

    /**
     * @param imageKey ImageKey (Note: The key have to exists in IMAGE_MAP)
     */
    public ImageManager(String... imageKey){
        this(Arrays.asList(imageKey));
    }

    /**
     * @param imageKeys ImageKeys (Note: All list elements have to exists in IMAGE_MAP)
     */
    public ImageManager(List<String> imageKeys) {
        imageKeys.forEach(this::addKey);
    }

    /**
     * Adds an image (via key) to render pipeline.
     * @param key ImageKey (Note: The key have to exists in IMAGE_MAP)
     * @param rot Individual Rotation.
     */
    public void addKey(@NotNull String key, Rotation rot){
        if (!IMAGE_MAP.containsKey(key)) throw new IllegalArgumentException("Key is not in Image Map.");

        imageKeys.add(key);
        imageRot.add(rot);
    }

    /**
     * Adds an image (via key) to render pipeline (Individual Rotation is 0).
     * @param key ImageKey (Note: The key have to exists in IMAGE_MAP)
     */
    public void addKey(@NotNull String key){
        addKey(key, Rotation.NORTH);
    }

    /**
     * Insert key in image pipeline at specific position.
     */
    public void insertKey(@NotNull String key, Rotation rot, int pos){
        imageKeys = insert(imageKeys, key, pos);
        imageRot = insert(imageRot, rot, pos);
    }

    /**
     * Insert key in image pipeline at specific position.
     */
    public void insertKey(@NotNull String key, int pos){
        insertKey(key, Rotation.NORTH, pos);
    }

    /**
     * Help method to insert element at specific position into list. The origin list won't be modified.
     * @return Returns new list with inserted element.
     */
    private <T> List<T> insert(List<T> list, @NotNull T element, int pos){
        if (pos < 0 || pos >= list.size()) throw new IllegalArgumentException(pos + " is not in list.");

        List<T> retList = new ArrayList<>();


        for (int i = 0; i < list.size(); i++){
            if (i == pos){
                retList.add(element);
            }

            retList.add(list.get(i));
        }

        return retList;
    }

    public int size(){
        return imageKeys.size();
    }

    /**
     *  Removes image key and corresponding rotation data.
     */
    public void removeKey(String key){
        int pos = imageKeys.indexOf(key);
        imageKeys.remove(key);
        imageRot.remove(pos);
    }

    /**
     * Clears ImageManager. Image is after clear() null.
     */
    public void clear(){
        imageKeys.clear();
        imageRot.clear();
    }

    /**
     * @return Returns rendered image.
     */
    public Image getImage() {
        renderImage();
        return image;
    }

    /**
     * Render all images in pipeline with their individual rotations.
     */
    private void renderImage(){
        if (imageKeys.isEmpty()) return;

        List<Image> list = new ArrayList<>();

        for (int i = 0; i < imageKeys.size(); i++){
            final Rotation rot = imageRot.get(i);
            Image image = IMAGE_MAP.get(imageKeys.get(i));

            if (rot != Rotation.NORTH){
                image = ImageUtil.rotateImage(image, rot.getDegrees());
            }

            list.add(image);
        }

        this.image = ImageUtil.mergeImages(list);
    }
}