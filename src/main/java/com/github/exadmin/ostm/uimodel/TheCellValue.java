package com.github.exadmin.ostm.uimodel;

public class TheCellValue {
    private String visualValue;
    private String sortByValue;
    private String toolTipText;

    /**
     * Create cell-value to be rendered in the report
     * @param visualValue visual value how it will look like
     * @param sortByValue hidden value how it will be interpreted by html rendering framework (for instance: sorting will be done by this values, not visual)
     */
    public TheCellValue(String visualValue, String sortByValue) {
        this.visualValue = visualValue;
        this.sortByValue = sortByValue;
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
}
