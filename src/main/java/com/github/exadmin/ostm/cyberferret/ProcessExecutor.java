package com.github.exadmin.ostm.cyberferret;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessExecutor {
    private static final int DEFAULT_BUFFER_BYTES = 64 * 1024;
    private static final Duration DEFAULT_CLEANUP_PHASE = Duration.ofSeconds(5);
    private static final Duration DEFAULT_DRAINER_GRACE = Duration.ofSeconds(5);

    private final int bufferBytes;
    private final Duration terminationGrace;
    private final Duration forceGrace;
    private final Duration drainerGrace;

    public ProcessExecutor() {
        this(DEFAULT_BUFFER_BYTES, DEFAULT_CLEANUP_PHASE, DEFAULT_CLEANUP_PHASE, DEFAULT_DRAINER_GRACE);
    }

    ProcessExecutor(int bufferBytes, Duration terminationGrace, Duration forceGrace, Duration drainerGrace) {
        this.bufferBytes = bufferBytes;
        this.terminationGrace = terminationGrace;
        this.forceGrace = forceGrace;
        this.drainerGrace = drainerGrace;
    }

    public ProcessResult execute(
            List<String> command,
            Path workingDirectory,
            Map<String, String> environment,
            Duration timeout,
            boolean mergeErrorStream) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(mergeErrorStream);
        builder.environment().putAll(environment);

        Process process = builder.start();
        BoundedByteBuffer stdout = new BoundedByteBuffer(bufferBytes);
        BoundedByteBuffer stderr = new BoundedByteBuffer(bufferBytes);
        ExecutorService drainers = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<?>> futures = new ArrayList<>();
        futures.add(drainers.submit(() -> drain(process.getInputStream(), stdout)));
        if (!mergeErrorStream) futures.add(drainers.submit(() -> drain(process.getErrorStream(), stderr)));

        boolean timedOut = !process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        Set<ProcessHandle> observed = new HashSet<>();
        boolean processesStopped = true;
        if (timedOut) {
            process.toHandle().descendants().forEach(observed::add);
            process.destroy();
            cleanup(process.toHandle(), observed, terminationGrace, false);
            cleanup(process.toHandle(), observed, forceGrace, true);
            processesStopped = !process.isAlive() && observed.stream().noneMatch(ProcessHandle::isAlive);
            closeProcessStreams(process);
        }

        boolean drainersStopped = awaitDrainers(futures, drainerGrace);
        drainers.shutdownNow();
        int exitCode = process.isAlive() ? -1 : process.exitValue();
        return new ProcessResult(
                exitCode,
                timedOut,
                stdout.toByteArray(),
                mergeErrorStream ? new byte[0] : stderr.toByteArray(),
                processesStopped && drainersStopped);
    }

    private static void drain(InputStream input, BoundedByteBuffer output) {
        byte[] chunk = new byte[8192];
        try (input) {
            int read;
            while ((read = input.read(chunk)) != -1) output.write(chunk, 0, read);
        } catch (IOException ignored) {
            // Closing process streams is part of bounded timeout cleanup.
        }
    }

    private static void cleanup(ProcessHandle root, Set<ProcessHandle> observed, Duration duration, boolean force)
            throws InterruptedException {
        long deadline = System.nanoTime() + duration.toNanos();
        do {
            root.descendants().forEach(observed::add);
            observed.add(root);
            for (ProcessHandle handle : observed) {
                if (!handle.isAlive()) continue;
                if (force) handle.destroyForcibly();
                else handle.destroy();
            }
            if (observed.stream().noneMatch(ProcessHandle::isAlive)) return;
            Thread.sleep(20);
        } while (System.nanoTime() < deadline);
    }

    private static boolean awaitDrainers(List<Future<?>> futures, Duration grace) throws InterruptedException {
        long deadline = System.nanoTime() + grace.toNanos();
        for (Future<?> future : futures) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) return false;
            try {
                future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (ExecutionException | TimeoutException exception) {
                return false;
            }
        }
        return true;
    }

    private static void closeProcessStreams(Process process) {
        try {
            process.getOutputStream().close();
        } catch (IOException ignored) {
        }
        try {
            process.getInputStream().close();
        } catch (IOException ignored) {
        }
        try {
            process.getErrorStream().close();
        } catch (IOException ignored) {
        }
    }
}
