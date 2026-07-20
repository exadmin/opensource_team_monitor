package com.github.exadmin.ostm.cyberferret;

public record ProcessResult(int exitCode, boolean timedOut, byte[] stdout, byte[] stderr, boolean cleanupComplete) {
    public ProcessResult {
        stdout = stdout.clone();
        stderr = stderr.clone();
    }

    @Override
    public byte[] stdout() {
        return stdout.clone();
    }

    @Override
    public byte[] stderr() {
        return stderr.clone();
    }
}
