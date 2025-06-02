package com.github.exadmin.ostm.uimodel;

public class TheCellValue {
    private String visualValue;
    private String sortByValue;
    private String toolTipText;
    private SeverityLevel severityLevel;
    private String httpReference;

    /**
     * Create cell-value to be rendered in the report
     * @param visualValue visual value how it will look like
     * @param sortByValue hidden value how it will be interpreted by html rendering framework (for instance: sorting will be done by this values, not visual)
     */
    public TheCellValue(Object visualValue, Object sortByValue, SeverityLevel severityLevel) {
        this.visualValue = visualValue.toString();
        this.sortByValue = sortByValue.toString();
        this.severityLevel = severityLevel;
    }

    public String getVisualValue() {
        return visualValue;
    }

    public void setVisualValue(String visualValue) {
        this.visualValue = visualValue;
    }

    public String getSortByValue() {
        return sortByValue;
    }

    public void setSortByValue(String sortByValue) {
        this.sortByValue = sortByValue;
    }

    @Override
    public String toString() {
        return visualValue;
    }

    public String getToolTipText() {
        return toolTipText;
    }

    public void setToolTipText(String toolTipText) {
        this.toolTipText = toolTipText;
    }

    public SeverityLevel getSeverityLevel() {
        return severityLevel;
    }

    public String getHttpReference() {
        return httpReference;
    }

    public void setHttpReference(String httpReference) {
        this.httpReference = httpReference;
    }

    public TheCellValue withHttpReference(String httpReference) {
        setHttpReference(httpReference);
        return this;
    }

    public TheCellValue withTooltip(String htmlBody) {
        setToolTipText(htmlBody);
        return this;
    }

    public void setSeverityLevel(SeverityLevel severityLevel) {
        this.severityLevel = severityLevel;
    }
}
