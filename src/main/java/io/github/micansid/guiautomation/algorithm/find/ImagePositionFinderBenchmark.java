package io.github.micansid.guiautomation.algorithm.find;

import io.github.micansid.guiautomation.util.Position;
import io.github.micansid.guiautomation.util.function.TriFunction;
import io.github.micansid.guiautomation.util.helper.Ensure;
import io.github.micansid.guiautomation.util.helper.StopWatch;
import io.github.micansid.guiautomation.util.image.Image;
import io.github.micansid.guiautomation.util.image.ImageExporter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter(AccessLevel.PRIVATE)
public class ImagePositionFinderBenchmark implements ImagePositionFinder {
  private final String imageExportPath;
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Map<ImagePositionFinder, StopWatch> benchmarkUnit = new HashMap<>();

  public ImagePositionFinderBenchmark(final ImagePositionFinder...  finders) {
    this(".", finders);
  }
  public ImagePositionFinderBenchmark(final String imageExportPath, final ImagePositionFinder...  finders) {
    Ensure.notNull(imageExportPath);
    Ensure.notEmpty(finders);
    this.imageExportPath = imageExportPath;
    for (ImagePositionFinder finder : finders) {
      getBenchmarkUnit().put(finder, new StopWatch(System::nanoTime));
    }
  }

  @Override
  public Optional<Position> find(final Image image, final Image pattern) {
    return runOnEachFinder(ImagePositionFinder::find, image, pattern,
        "The results of the find() method from the ImagePositionFinders differ");
  }

  @Override
  public List<Position> findAll(final Image image, final Image pattern) {
    return runOnEachFinder(ImagePositionFinder::findAll, image, pattern,
        "The results of the findAll() method from the ImagePositionFinders differ");
  }

  @Override
  public Map<Image, List<Position>> findAll(final Image image, final Set<Image> patterns) {
    return runOnEachFinder(ImagePositionFinder::findAll, image, patterns,
        "The results of the findAll() method from the ImagePositionFinders differ");
  }

  @Override
  public boolean at(final Image image, final Image pattern, final Position position) {
    return runOnEachFinder((finder, i, p) -> finder.at(i, p, position), image, pattern,
        "The results of the at() method from the ImagePositionFinders differ");
  }

  @Override
  public boolean at(final Image image, final Image pattern, final int positionX,
                    final int positionY) {
    return runOnEachFinder((finder, i, p) -> finder.at(i, p, positionX, positionY), image, pattern,
        "The results of the at() method from the ImagePositionFinders differ");
  }


  public String result() {
    return benchmarkResultMilliSeconds().entrySet()
        .stream().map(unit -> unit.getKey() + " - " + unit.getValue())
        .collect(Collectors.joining("\n"));
  }

  @Override
  public String toString() {
    return result();
  }

  public Map<Class<? extends ImagePositionFinder>, Long> benchmarkResultNanoSeconds() {
    return getBenchmarkUnit().entrySet().stream()
        .collect(Collectors.toMap(unit -> unit.getKey().getClass(),
            unit -> unit.getValue().duration()));
  }

  public Map<Class<? extends ImagePositionFinder>, Double> benchmarkResultMilliSeconds() {
    return getBenchmarkUnit().entrySet().stream()
        .collect(Collectors.toMap(unit -> unit.getKey().getClass(),
            unit -> ((double)unit.getValue().duration()) / 1_000_000));
  }

  private <T, U> U runOnEachFinder(final TriFunction<ImagePositionFinder,Image, T, U> method,
                                   final Image image,
                                   final T parameter, final String message) {
    List<U> results = new ArrayList<>();

    for (Map.Entry<ImagePositionFinder, StopWatch> unit : getBenchmarkUnit().entrySet()) {
      unit.getValue().start();
      results.add(method.apply(unit.getKey(), image, parameter));
      unit.getValue().pause();
    }

    for (U result : results) {
      if (!result.equals(results.get(0))) {
        ImageExporter exporter = new ImageExporter();
        exporter.export(image,
            getImageExportPath() + "/"
                + "ImagePositionFinderBenchmark_error_" + System.currentTimeMillis() + ".png");
        RuntimeException exception = new RuntimeException(message);
        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        getLogger().error(writer.toString());
        throw exception;
      }
    }
    return results.get(0);
  }
}
