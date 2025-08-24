package com.ibradecode.gemini;

public class MarkdownSegment {
    public boolean isCode;
    public String content;

    public MarkdownSegment(boolean isCode, String content) {
        this.isCode = isCode;
        this.content = content;
    }
    public boolean isTableLike() {
    return content.contains("|") && content.contains("---");
}
}