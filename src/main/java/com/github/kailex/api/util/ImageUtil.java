package com.github.kailex.api.util;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * This class offers help methods to work with JavaFx Images.
 *
 * @author Alexander Ley
 * @version 1.1
 */
public class ImageUtil {

    /**
     * Loads an image from path name beginning in resource folder. Only BMP, GIF, JPEG, PNG files are allowed.
     * @param name path to texture (e.g. textures/picture.png)
     * @throws IOException if texture cannot found or cannot load.
     */
    public static Image loadImage(String name) throws IOException {
        final URL url = FxUtils.class.getResource("/" + name);

        if (url == null) throw new IllegalArgumentException(name + " cannot found.");

        return new Image(url.openStream());
    }

    /**
     * Loads an image from abstract file.
     * @param file file which points to BMP, GIF, JPEG, PNG file.
     * @throws IOException if texture cannot found or cannot load.
     */
    public static Image loadImage(File file) throws IOException {
        final URL url = file.toURI().toURL();
        return new Image(url.openStream());
    }

    /**
     * Loads an image from abstract path.
     * @param path path which points to BMP, GIF, JPEG, PNG file.
     * @throws IOException if texture cannot found or cannot load.
     */
    public static Image loadImage(Path path) throws IOException {
        return loadImage(path.toFile());
    }

    /**
     * Put all images in images together in one new image.
     * @param width width of the new image.
     * @param height height of the new image.
     * @return Returns a merged image.
     */
    public static Image mergeImages(List<Image> images, int width, int height){
        if (images.contains(null)) throw new IllegalArgumentException("Image cannot be null.");

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        images.forEach(image -> gc.drawImage(image, 0, 0, width, height));

        SnapshotParameters snap = new SnapshotParameters();
        snap.setFill(Color.color(1, 1, 1, 0));

        return canvas.snapshot(snap, new WritableImage(width, height));
    }

    /**
     * Put all images in images together in one new image.
     * @param scale width and height of the image.
     * @return Returns a merged image.
     */
    public static Image mergeImages(List<Image> images, int scale){
        return mergeImages(images, scale, scale);
    }

    /**
     * Put all images in images together in one new image.
     * @param scale width and height of the image.
     * @return Returns a merged image.
     */
    public static Image mergeImages(int scale, Image... images){
        return mergeImages(Arrays.asList(images), scale);
    }

    /**
     * Put all images in images together in one new Image. This method uses the width and height of the largest image.
     * @return Returns a merged image.
     */
    public static Image mergeImages(List<Image> images){
        return mergeImages(images,
                images.stream().map(Image::getWidth).max(Comparator.naturalOrder()).orElseThrow().intValue(),
                images.stream().map(Image::getHeight).max(Comparator.naturalOrder()).orElseThrow().intValue()
        );
    }

    /**
     * Put all images in images together in one new Image. This method uses the width and height of the largest image.
     * @return Returns a merged image.
     */
    public static Image mergeImages(Image... images){
        return mergeImages(Arrays.asList(images));
    }

    /**
     * @param imgWidth image width
     * @param imgHeight image height
     * @param newWidth width of new image
     * @param newHeight height of new image
     * @return Returns a rotated image.
     */
    public static Image rotateImage(@NotNull Image img, double degrees, int imgWidth, int imgHeight, int newWidth, int newHeight){
        Canvas canvas = new Canvas(newWidth, newHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Rotate r = new Rotate(degrees, img.getWidth() / 2, img.getHeight() / 2);
        gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
        gc.drawImage(img, 0, 0,  imgWidth, imgHeight);

        SnapshotParameters snap = new SnapshotParameters();
        snap.setFill(Color.color(1, 1, 1, 0));

        return canvas.snapshot(snap, new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight()));
    }

    /**
     * Width and height of new image are the same as old image.
     * @return Returns a rotated image.
     */
    public static Image rotateImage(Image img, double degrees){
        return rotateImage(img, degrees, (int) img.getWidth(), (int) img.getHeight(), (int) img.getWidth(), (int) img.getHeight());
    }

    public static Image getImageSnippet(Image img, int uvx, int uvy, int width, int height){
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.drawImage(img, uvx, uvy, width, height, 0, 0, width, height);

        SnapshotParameters snap = new SnapshotParameters();
        snap.setFill(Color.color(1, 1, 1, 0));

        return canvas.snapshot(snap, new WritableImage(width, height));
    }
}
