package com.github.exadmin.ostm.cyberferret;

public record CyberFerretScanResult(ScanStatus status, int emailFindings, int otherFindings) {
    public enum ScanStatus { COMPLETED, FAILED }

    public static final CyberFerretScanResult FAILED = new CyberFerretScanResult(ScanStatus.FAILED, 0, 0);
}
