package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.parser.PageParserFactory;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final PageParserFactory parserFactory;
  private final List<Pattern> ignoredUrls;
  private final int maxDepth;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      PageParserFactory pageParserFactory,
      @IgnoredUrls List<Pattern> ignoredUrls,
      @MaxDepth int maxDepth) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.parserFactory = pageParserFactory;
    this.ignoredUrls = ignoredUrls;
    this.maxDepth = maxDepth;
  }

  /**
   * Similar to SequentialWebCrawler#crawl - Except this
   * does crawling in parallel using threads of ForkJoinPool
   * @param startingUrls the starting points of the crawl.
   * @return CrawlResult
   */
  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Instant deadline = clock.instant().plus(timeout);
    ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
    ConcurrentSkipListSet<String> visitedUrls = new ConcurrentSkipListSet<>();

    // Start Crawling
    for (String url : startingUrls) {
      pool.invoke(new CrawlTaskParallel(
              url, deadline, maxDepth, counts,
              visitedUrls, clock, ignoredUrls, parserFactory)
      );
    }

    // Nothing found -> Return in default order
    if (counts.isEmpty()) {
      return new CrawlResult.Builder()
        .setWordCounts(counts)
        .setUrlsVisited(visitedUrls.size())
        .build();
    }

    // Sort count words & return
    return new CrawlResult.Builder()
      .setWordCounts(WordCounts.sort(counts, popularWordCount))
      .setUrlsVisited(visitedUrls.size())
      .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
