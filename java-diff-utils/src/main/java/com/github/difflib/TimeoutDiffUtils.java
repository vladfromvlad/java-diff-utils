package com.github.difflib;

import com.github.difflib.algorithm.DiffAlgorithmI;
import com.github.difflib.algorithm.DiffAlgorithmListener;
import com.github.difflib.patch.Patch;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiPredicate;

public final class TimeoutDiffUtils {

  public static <T> Patch<T> diff(List<T> original, List<T> revised, DiffAlgorithmListener progress,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(original, revised, progress),
                              timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> original, List<T> revised,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(original, revised),
                              timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> original, List<T> revised, boolean includeEqualParts,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(original, revised, includeEqualParts),
                              timeout, unit);
  }

  /**
   * Computes the difference between the original and revised text.
   */
  public static Patch<String> diff(String sourceText, String targetText,
                                   DiffAlgorithmListener progress,
                                   long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(sourceText, targetText, progress),
                              timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> source, List<T> target,
                                  BiPredicate<T, T> equalizer,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(source, target, equalizer),
                              timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> original, List<T> revised,
                                  DiffAlgorithmI<T> algorithm, DiffAlgorithmListener progress,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return diff(original, revised, algorithm, progress, false, timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> original, List<T> revised,
                                  DiffAlgorithmI<T> algorithm, DiffAlgorithmListener progress,
                                  boolean includeEqualParts,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(
        () -> DiffUtils.diff(original, revised, algorithm, progress, includeEqualParts),
        timeout, unit);
  }

  public static <T> Patch<T> diff(List<T> original, List<T> revised, DiffAlgorithmI<T> algorithm,
                                  long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return executeWithTimeout(() -> DiffUtils.diff(original, revised, algorithm),
                              timeout, unit);
  }

  private static <T> Patch<T> executeWithTimeout(Callable<Patch<T>> diffOperation,
                                                 long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    ExecutorService executor = Executors.newSingleThreadExecutor();

    Future<Patch<T>> future = executor.submit(diffOperation);

    try {
      return future.get(timeout, unit);
    } finally {
      executor.shutdownNow();  // This will interrupt the task if it's still running
    }
  }
}
