package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = delegate;
    this.state = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // Output
    Object result = null;
    Instant startTime = null;
    boolean isProfileAnnotation = method.getAnnotation(Profiled.class) != null;

    // Start Counting
    if(isProfileAnnotation){
      startTime = clock.instant();
    }

    try {
      // Invoke Method
      result = method.invoke(this.delegate, args);
    }
    // Propagate InvocationTargetException
    catch (InvocationTargetException ex) {
      System.err.println("Method Profiling InvocationTargetException: " + ex.getLocalizedMessage());
      throw ex.getTargetException();
    }
    // Propagate IllegalAccessException
    catch (IllegalAccessException ex) {
      System.err.println("Method Profiling IllegalAccessException: " + ex.getLocalizedMessage());
      throw new RuntimeException(ex);
    }
    // Compute Performance
    finally {
      // Compute Time Diff
      if(isProfileAnnotation) {
        Duration duration = Duration.between(startTime, clock.instant());
        state.record(this.delegate.getClass(), method, duration);
      }
    }
    return result;
  }
}
