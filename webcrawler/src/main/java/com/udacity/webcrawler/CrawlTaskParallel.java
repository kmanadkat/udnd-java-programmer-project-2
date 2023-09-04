package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

/**
 * Similar to SequentialWebCrawler but for Parallel Thread Pool Execution
 */
public class CrawlTaskParallel extends RecursiveAction {
    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final ConcurrentHashMap<String, Integer> counts;
    private final ConcurrentSkipListSet<String> visitedUrls;

    // Dependents from ParallelWebCrawler
    private final Clock clock;
    private final List<Pattern> ignoredUrls;
    private final PageParserFactory parserFactory;

    public CrawlTaskParallel(
        String url,
        Instant deadline,
        int maxDepth,
        ConcurrentHashMap<String, Integer> counts,
        ConcurrentSkipListSet<String> visitedUrls,
        Clock clock,
        List<Pattern> ignoredUrls,
        PageParserFactory parserFactory
    ) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.clock = clock;
        this.ignoredUrls = ignoredUrls;
        this.parserFactory = parserFactory;
    }
    @Override
    protected void compute() {
        // Validate Depth & Time -> Break Recursion
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return;
        }
        // Validate Ignore Urls -> Break Recursion
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return;
            }
        }

        // Protect From Race Conditions
        synchronized(this) {
            // If Url already present -> Break Recursion
            if (visitedUrls.contains(url)) {
                return;
            }
            // Add Url
            visitedUrls.add(url);
        }

        // Parse Result & Store to HashMap
        PageParser.Result result = parserFactory.get(url).parse();
        for (ConcurrentHashMap.Entry<String, Integer> entry : result.getWordCounts().entrySet()) {
            counts.compute(entry.getKey(), (k, v) -> (v == null) ? entry.getValue() : entry.getValue()+v);
        }

        // Crawl Further Links from Result
        List<CrawlTaskParallel> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlTaskParallel(
                link,
                deadline,
                maxDepth - 1,
                counts,
                visitedUrls,
                clock,
                ignoredUrls,
                parserFactory
            ));
        }

        // Distribute Subtasks in Threads For Parallel Execution
        // Recursive Call
        invokeAll(subtasks);
    }
}
