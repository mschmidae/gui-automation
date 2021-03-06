package io.github.micansid.guiautomation.util.image;

import io.github.micansid.guiautomation.util.helper.Ensure;
import io.github.micansid.guiautomation.util.Position;
import io.github.micansid.guiautomation.util.Section;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable interpretation of a image used in the context of screen. The class is inspired by the
 * class BufferedImage.
 * Unlike the BuffedImage the ScreenImage overrides the methods equal() and hashCode(). Two
 * ScreenImage representing a equal picture are equal.
 */
@EqualsAndHashCode
@Getter(AccessLevel.PUBLIC)
public class Image implements Supplier<Image> {

  /**
   * RGB information of the image.
   */
  @Getter(AccessLevel.PRIVATE)
  private final int[] imageData;

  /**
   * width of the image.
   */
  private final int width;

  /**
   * height of the image.
   */
  private final int height;


  /**
   * Construct a ScreenImage from a BufferedImage.
   * @param image to interpret
   */
  public Image(final BufferedImage image) {
    this(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0,
        image.getWidth()), image.getWidth(), image.getHeight());
  }


  /**
   * Construct a ScreenImage from an array with the RGB information.
   * @param imageData RGB information, the size of the array have to be width * height.
   * @param width     of the image
   * @param height    of the image
   */
  public Image(final int[] imageData, final int width, final int height) {
    Ensure.notNegative(width);
    Ensure.notNegative(height);
    Ensure.notNull(imageData);
    Ensure.equal(width * height, imageData.length);
    this.width = width;
    this.height = height;
    this.imageData = Arrays.copyOf(imageData, width * height);
  }

  public int getRgb(final int x, final int y) {
    ensureCoordinates(x, y);
    return getImageData()[y * getWidth() + x];
  }

  public int getRgb(final Position position) {
    Ensure.notNull(position);
    return getRgb(position.getX(), position.getY());
  }

  public List<Integer> getRgbLine(final int y) {
    return getLine(this::getRgb, y);
  }

  public List<Integer> getRgbColumn(final int x) {
    return getColumn(this::getRgb, x);
  }


  public int[] getRgbData() {
    return Arrays.copyOf(getImageData(), getWidth() * getHeight());
  }


  /**
   * Get sub ScreenImage with the given parameters.
   * @param x x-coordinate of the left upper bound
   * @param y y-coordinate of the left upper bound
   * @param w width of the new image
   * @param h height of the new image
   * @return ScreenImage of the defined section
   */
  public Image getSubImage(final int x, final int y, final int w, final int h) {
    return new Image(bufferedImage().getSubimage(x, y, w, h));
  }


  /**
   * Get sub ScreenImag of the given section.
   * @param section defining the sub ScreenImage position and size
   * @return ScreenImage of the defined section
   */
  public Image getSubImage(final Section section) {
    return getSubImage(section.getStartPosition().getX(), section.getStartPosition().getY(),
        section.getWidth(), section.getHeight());
  }

  public int getRed(final int x, final int y) {
    ensureCoordinates(x, y);
    return new Color(getRgb(x, y), true).getRed();
  }

  public int getRed(final Position position) {
    Ensure.notNull(position);
    return getRed(position.getX(), position.getY());
  }

  public int getGreen(final int x, final int y) {
    ensureCoordinates(x, y);
    return new Color(getRgb(x, y), true).getGreen();
  }

  public int getGreen(final Position position) {
    Ensure.notNull(position);
    return getGreen(position.getX(), position.getY());
  }

  public int getBlue(final int x, final int y) {
    ensureCoordinates(x, y);
    return new Color(getRgb(x, y), true).getBlue();
  }

  public int getBlue(final Position position) {
    Ensure.notNull(position);
    return getBlue(position.getX(), position.getY());
  }

  public int getAlpha(final int x, final int y) {
    ensureCoordinates(x, y);
    return new Color(getRgb(x, y), true).getAlpha();
  }

  public int getAlpha(final Position position) {
    Ensure.notNull(position);
    return getAlpha(position.getX(), position.getY());
  }

  /**
   * Check if coordinate is transparent, identified by alpha 0.
   * @param x coordinate
   * @param y coordinate
   * @return true if alpha of this coordinate is 0
   */
  public boolean isTransparent(final int x, final int y) {
    ensureCoordinates(x, y);
    return getAlpha(x, y) == 0;
  }

  /**
   * Check if position is transparent, identified by alpha 0.
   * @param position to check
   * @return true if alpha of this position is 0
   */
  public boolean isTransparent(final Position position) {
    Ensure.notNull(position);
    return isTransparent(position.getX(), position.getY());
  }


  /**
   * Convert image to BufferedImage.
   * @return BufferedImage of this image
   */
  public BufferedImage bufferedImage() {
    BufferedImage result = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
    result.setRGB(0, 0, getWidth(), getHeight(), getImageData(), 0, getWidth());
    return result;
  }

  public Position middle() {
    return new Position(getWidth() / 2, getHeight() / 2);
  }

  private void ensureCoordinates(final int x, final int y) {
    Ensure.notNegative(x);
    Ensure.notNegative(y);
    Ensure.smaller(x, getWidth());
    Ensure.smaller(y, getHeight());
  }

  private List<Integer> getLine(final BiFunction<Integer, Integer, Integer> getter, final int y) {
    Ensure.notNegative(y);
    Ensure.smaller(y, getHeight());

    List<Integer> result = new ArrayList<>();
    for (int index = 0; index < getWidth(); index++) {
      result.add(getter.apply(index, y));
    }

    return result;
  }

  private List<Integer> getColumn(final BiFunction<Integer, Integer, Integer> getter, final int x) {
    Ensure.notNegative(x);
    Ensure.smaller(x, getWidth());

    List<Integer> result = new ArrayList<>();
    for (int index = 0; index < getHeight(); index++) {
      result.add(getter.apply(x, index));
    }

    return result;
  }

  @Override
  public Image get() {
    return this;
  }
}

