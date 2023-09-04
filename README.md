## Udacity Java Programmer - Project 2 - Parallel Web Crawler

The Parallel Web Crawler is a Java program that allows you to crawl and analyze web pages in parallel. It is designed to efficiently fetch and process web pages from multiple threads concurrently, making use of parallelism and synchronization techniques.

![](./media/udacisearch.png)

Image source: Audacity

### Main Components of the App

- **Parallelism**: The crawler fetches and processes web pages using multiple threads, allowing for faster and more efficient crawling.
- **Synchronization**: The crawler ensures that multiple threads run in parallel without conflicts or race conditions by using appropriate synchronization techniques.
- **Avoid Duplicate URLs**: The crawler keeps track of visited pages to avoid revisiting the same web page multiple times.
- **Configuration**: The crawler can be configured using a JSON file, allowing you to customize various parameters such as the starting pages, ignored URLs, maximum depth, timeout, and more.
- **Profiling**: The crawler includes a profiling feature that records method invocation times for annotated methods, providing insights into the performance of the crawler.
  File I/O: The crawler can read the configuration from a JSON file and write the crawl results and profiling data to files.

### Project Requirement & Setup
This project was built on java v20.0.2 You need to have JDK & [maven](https://maven.apache.org/install.html) v3.9.4 or higher installed to run this.
> For Vscode, [Java Language Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) extensions are required.
1. Clone the repository
2. Open `webcrawler` directory in the terminal.
3. `mvn test` to run all unit tests.
4. Running the project

   ```
   mvn package
   java -classpath target/udacity-webcrawler-1.0.jar \
       com.udacity.webcrawler.main.WebCrawlerMain \
       src/main/config/sample_config.json
   ```
5. You can edit config like url, maxDepth, timeout, etc. at `webcrawler/src/main/config/sample_config.json`
