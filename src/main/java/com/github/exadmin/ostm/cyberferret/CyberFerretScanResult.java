package com.github.exadmin.ostm.cyberferret;

public record CyberFerretScanResult(ScanStatus status, int emailFindings, int otherFindings, long timeoutSeconds) {
    public enum ScanStatus { COMPLETED, TIMED_OUT, FAILED }

    public static final CyberFerretScanResult FAILED = new CyberFerretScanResult(ScanStatus.FAILED, 0, 0);

    public CyberFerretScanResult(ScanStatus status, int emailFindings, int otherFindings) {
        this(status, emailFindings, otherFindings, 0);
    }
}
