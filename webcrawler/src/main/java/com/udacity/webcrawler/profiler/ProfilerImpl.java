package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);

    // Get All Class Methods
    Method[] methods = klass.getMethods();
    if(methods.length == 0) {
      throw new IllegalArgumentException("Class does not have any methods");
    }

    // Check If any method is annotated with @Profiled
    boolean isAnnotated = false;
    for (Method method : methods) {
      if(method.getAnnotation(Profiled.class) != null){
        isAnnotated = true;
        break;
      }
    }
    if(!isAnnotated) {
      throw new IllegalArgumentException("No class method has @Profiled Annotation");
    }

    // Create Interceptor
    ProfilingMethodInterceptor interceptor = new ProfilingMethodInterceptor(clock, delegate, state);

    // Return Proxy
    return klass.cast(Proxy.newProxyInstance(klass.getClassLoader(), new Class[]{klass}, interceptor));
  }

  @Override
  public void writeData(Path path) {
    try(BufferedWriter writer = Files.newBufferedWriter(path)) {
      writeData(writer);
    } catch(IOException e){
      System.err.println("Error Writing Profile At Path: " + e.getLocalizedMessage());
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
