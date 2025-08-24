package com.ibradecode.gemini;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.BulletSpan;
import android.text.style.URLSpan;
import android.text.style.ForegroundColorSpan;
import android.graphics.Typeface;
import android.graphics.Color;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.HorizontalScrollView;
import android.view.Gravity;
import android.graphics.drawable.GradientDrawable;
import android.widget.TableLayout;
import android.widget.TableRow;
import java.util.regex.*;
import java.util.*;

public class MarkdownRenderer {

    public static List<MarkdownSegment> segmentMarkdown(String markdown) {
        List<MarkdownSegment> segments = new ArrayList<>();
        Pattern pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        Matcher matcher = Pattern.compile("```(?:\\w+)?\\s*([\\s\\S]*?)```").matcher(markdown);

        int lastIndex = 0;
        while (matcher.find()) {
            int codeStart = matcher.start();
            int codeEnd = matcher.end();

            String beforeCode = markdown.substring(lastIndex, codeStart).trim();
            if (!beforeCode.isEmpty()) {
                segments.add(new MarkdownSegment(false, beforeCode));
            }

            String code = matcher.group(1).trim();
            segments.add(new MarkdownSegment(true, code));

            lastIndex = codeEnd;
        }

        if (lastIndex < markdown.length()) {
            String afterCode = markdown.substring(lastIndex).trim();
            if (!afterCode.isEmpty()) {
                segments.add(new MarkdownSegment(false, afterCode));
            }
        }

        return segments;
    }

    public static SpannableStringBuilder render(String markdown) {
    SpannableStringBuilder spannable = new SpannableStringBuilder();
    String[] lines = markdown.split("\n");

    for (int i = 0; i < lines.length; i++) {
        String line = lines[i];

        // Detect markdown tables
        if (line.startsWith("|") && i + 1 < lines.length && lines[i + 1].contains("---")) {
            spannable.append("\n");
            i += 2; // Skip divider line
            continue;
        }

        if (line.startsWith("# ")) {
            spannable.append(applyHeader(line, 1));
        } else if (line.startsWith("## ")) {
            spannable.append(applyHeader(line, 2));
        } else if (line.startsWith("### ")) {
            spannable.append(applyHeader(line, 3));
        } else if (line.startsWith("#### ")) {
            spannable.append(applyHeader(line, 4));
        } else if (line.matches("^\\d+\\.\\s+.*")) {
            spannable.append(applyNumberedItem(line));
        } else if (line.startsWith("* ")) {
            spannable.append(applyBulletItem(line));
        } else {
            spannable.append(applyInlineStyles(line));
        }
        spannable.append("\n");
    }

    return spannable;
}
    private static SpannableStringBuilder applyHeader(String line, int level) {
    String prefix = new String(new char[level]).replace("\0", "#") + " ";
    if (line.startsWith(prefix)) {
        String text = line.substring(prefix.length()).trim(); // remove leading hashes
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Optional: Increase font size for header levels
        // spannable.setSpan(new RelativeSizeSpan(1.2f), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannable;
    }
    return new SpannableStringBuilder(line); // fallback
}

    private static SpannableStringBuilder applyBulletItem(String line) {
        String content = line.substring(2).trim();
        SpannableStringBuilder spannable = applyInlineStyles(content);
        spannable.setSpan(new BulletSpan(12), 0, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private static SpannableStringBuilder applyNumberedItem(String line) {
        String content = line.replaceFirst("^\\d+\\.\\s+", "").trim();
        return applyInlineStyles(content);
    }

    private static SpannableStringBuilder applyInlineStyles(String line) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(line);
        applyBold(spannable);
        applyItalic(spannable);
        applyCode(spannable);
        applyLinks(spannable);
        return spannable;
    }

    private static void applyBold(SpannableStringBuilder spannable) {
        Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher matcher = pattern.matcher(spannable.toString());
        while (matcher.find()) {
            spannable.setSpan(new StyleSpan(Typeface.BOLD), matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        removeMarkers(spannable, "**");
    }

    private static void applyItalic(SpannableStringBuilder spannable) {
        Pattern pattern = Pattern.compile("\\*(.*?)\\*");
        Matcher matcher = pattern.matcher(spannable.toString());
        while (matcher.find()) {
            spannable.setSpan(new StyleSpan(Typeface.ITALIC), matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        removeMarkers(spannable, "*");
    }

    private static void applyCode(SpannableStringBuilder spannable) {
        Pattern pattern = Pattern.compile("`(.*?)`");
        Matcher matcher = pattern.matcher(spannable.toString());
        while (matcher.find()) {
            spannable.setSpan(new TypefaceSpan("monospace"), matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), matcher.start(1), matcher.end(1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        removeMarkers(spannable, "`");
    }

  private static void applyLinks(SpannableStringBuilder spannable) {
    Pattern pattern = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");
    Matcher matcher = pattern.matcher(spannable.toString());

    while (matcher.find()) {
        String label = matcher.group(1);
        String url = matcher.group(2);

        int start = matcher.start();
        int end = matcher.end();

        spannable.replace(start, end, label);

        String updatedText = spannable.toString();
        int labelStart = updatedText.indexOf(label, start);
        int labelEnd = labelStart + label.length();

        if (labelStart >= 0 && labelEnd <= spannable.length()) {
            spannable.setSpan(new URLSpan(url), labelStart, labelEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        matcher = pattern.matcher(spannable.toString());
    }
}

public static void renderTableWithLayout(String markdown, TableLayout tableLayout, Context context) {
    String[] lines = markdown.split("\n");
    List<String[]> rows = new ArrayList<>();

    for (String line : lines) {
        if (line.contains("---")) continue;
        if (line.contains("|")) {
            String[] raw = line.split("\\|");
            List<String> cleaned = new ArrayList<>();
            for (String cell : raw) {
                String trimmed = cell.trim();
                if (!trimmed.isEmpty()) cleaned.add(trimmed);
            }
            if (!cleaned.isEmpty()) rows.add(cleaned.toArray(new String[0]));
        }
    }

    for (int i = 0; i < rows.size(); i++) {
        TableRow tableRow = new TableRow(context);
        tableRow.setPadding(4, 6, 4, 6);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(
            TableLayout.LayoutParams.MATCH_PARENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        ));

        for (String cellText : rows.get(i)) {
            TextView cell = new TextView(context);
            cell.setText(renderInline(cellText)); // ✅ bold, link, code
            cell.setPadding(8, 6, 8, 6);
            cell.setTextSize(13f);
            cell.setTextColor(Color.WHITE);
            cell.setGravity(Gravity.START);
           cell.setTypeface(Typeface.createFromAsset(context.getAssets(),"fonts/googlesans.ttf"), 0);
            

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(12f);
            bg.setColor(i == 0 ? Color.parseColor("#37474F") : Color.parseColor("#212121"));
            cell.setBackground(bg);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 2, 4, 2);
            cell.setLayoutParams(params);

            tableRow.addView(cell);
        }

        tableLayout.addView(tableRow);
    }
}
public static SpannableStringBuilder renderInline(String markdown) {
    SpannableStringBuilder spannable = new SpannableStringBuilder(markdown);
    applyBold(spannable);
    applyItalic(spannable);
    applyCode(spannable);
    applyLinks(spannable);
    return spannable;
}

    private static void removeMarkers(SpannableStringBuilder spannable, String marker) {
        int index = spannable.toString().indexOf(marker);
        while (index != -1) {
            spannable.delete(index, index + marker.length());
            index = spannable.toString().indexOf(marker);
        }
    }
}