package io.github.micansid.guiautomation.algorithm.find;

import io.github.micansid.guiautomation.util.helper.Ensure;
import io.github.micansid.guiautomation.util.image.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

public class BadCharacterPattern {
  @Delegate
  private final Image image;
  @Getter(AccessLevel.PRIVATE)
  private final PatternLine line;

  public BadCharacterPattern(final Image image, final int checkLine) {
    Ensure.notNull(image);
    Ensure.notNegative(checkLine);
    Ensure.smaller(checkLine, image.getHeight());

    this.image = image;
    line = new PatternLine(image, checkLine);
  }

  public BadCharacterPattern(final Image image) {
    Ensure.notNull(image);

    this.image = image;
    line = bestDeltaLine(image);
  }

  public int getLineIndex() {
    return getLine().getLineIndex();
  }

  public int getTransparentOffset() {
    return getLine().getTransparentOffset();
  }

  public boolean containsTransparent() {
    return getLine().getTransparentOffset() != 0;
  }

  public Map<Integer, Integer> getColorDelta() {
    return getLine().getColorDelta();
  }

  private PatternLine bestDeltaLine(final Image pattern) {
    return IntStream.range(0, pattern.getHeight())
        .mapToObj(y -> new PatternLine(pattern, y))
        .sorted().findFirst().get();
  }

  @Getter(AccessLevel.PRIVATE)
  private class PatternLine implements Comparable<PatternLine> {
    private final int lineIndex;
    private final int transparentOffset;
    private final Set<Integer> colors;
    private final Map<Integer, Integer> colorDelta;

    private PatternLine(final Image image, final int lineIndex) {
      this.lineIndex = lineIndex;
      transparentOffset = transparentOffset(image, lineIndex);
      colors = colors(image, lineIndex);
      colorDelta = colorDelta(image, lineIndex);
    }

    @Override
    public int compareTo(PatternLine patternLine) {
      Ensure.notNull(patternLine);

      int result;
      if (getTransparentOffset() == 0 && patternLine.getTransparentOffset() == 0) {
        return 0;
      } else if (getTransparentOffset() == 0 && patternLine.getTransparentOffset() != 0) {
        result = -1;
      } else if (getTransparentOffset() != 0 && patternLine.getTransparentOffset() == 0) {
        result = 1;
      } else {
        result = patternLine.getTransparentOffset() - getTransparentOffset();
      }
      return result;
    }

    private int transparentOffset(final Image image, final int patternLineIndex) {
      int result = 0;
      for (int index = 0; index < image.getWidth() && result == 0; index++) {
        if (image.isTransparent(image.getWidth() - index - 1, patternLineIndex)) {
          result = index + 1;
        }
      }
      return result;
    }

    private Set<Integer> colors(final Image image, final int lineIndex) {
      Set<Integer> colors = new HashSet<>();
      for (int x = 0; x < image.getWidth(); x++) {
        if (!image.isTransparent(x, lineIndex)) {
          colors.add(image.getRgb(x, lineIndex));
        }
      }
      return colors;
    }

    private Map<Integer, Integer> colorDelta(final Image pattern, final int patternLineIndex) {
      List<Integer> line = pattern.getRgbLine(patternLineIndex);
      HashMap<Integer, Integer> colorDelta = new HashMap<>();
      for (int x = 0; x < line.size(); x++) {
        colorDelta.put(line.get(x), line.size() - x - 1);
      }
      colorDelta.remove(line.get(line.size() - 1));

      return Collections.unmodifiableMap(colorDelta);
    }

    private int getDifferentColorCount() {
      return getColors().size();
    }

    public Map<Integer, Integer> getColorDelta() {
      return Collections.unmodifiableMap(colorDelta);
    }
  }
}
