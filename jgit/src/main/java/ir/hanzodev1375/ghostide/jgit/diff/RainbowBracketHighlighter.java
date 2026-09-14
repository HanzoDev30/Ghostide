package ir.hanzodev1375.ghostide.jgit.diff;

import android.graphics.Color;
import java.util.*;

public class RainbowBracketHighlighter {
    
    
    private static final int[] RAINBOW_COLORS = {
        Color.rgb(255, 180, 180), 
        Color.rgb(255, 215, 140), 
        Color.rgb(255, 255, 150), 
        Color.rgb(180, 255, 180), 
        Color.rgb(150, 220, 255), 
        Color.rgb(210, 180, 255), 
        Color.rgb(255, 160, 210)  
    };
    
    private static final char[] OPEN_BRACKETS = {'(', '{', '['};
    private static final char[] CLOSE_BRACKETS = {')', '}', ']'};
    
    /**
     * اسکن یک خط و اضافه کردن HighlightSpan برای براکت‌ها با رنگ‌های رنگین‌کمانی
     * @param text       متن خط
     * @param ignoreRanges محدوده‌هایی که نباید هایلایت شوند (مثلاً داخل کامنت یا استرینگ)
     * @return لیست اسپن‌های براکت
     */
    public static List<SyntaxHighlighter.HighlightSpan> findBrackets(String text, List<SyntaxHighlighter.HighlightSpan> ignoreRanges) {
        List<SyntaxHighlighter.HighlightSpan> bracketSpans = new ArrayList<>();
        
        if (text == null || text.isEmpty()) return bracketSpans;
        
        
        Stack<Integer> stack = new Stack<>();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            if (isInIgnoreRange(i, ignoreRanges)) {
                continue;
            }
            
            int bracketType = getBracketType(c);
            if (bracketType != -1 && isOpenBracket(c)) {
                
                int level = stack.size();
                int color = RAINBOW_COLORS[level % RAINBOW_COLORS.length];
                bracketSpans.add(new SyntaxHighlighter.HighlightSpan(i, i+1, color));
                stack.push(i);
            } 
            else if (bracketType != -1 && isCloseBracket(c)) {
                
                if (!stack.isEmpty()) {
                    int level = stack.size() - 1;
                    int color = RAINBOW_COLORS[level % RAINBOW_COLORS.length];
                    bracketSpans.add(new SyntaxHighlighter.HighlightSpan(i, i+1, color));
                    stack.pop();
                } else {
                    
                    bracketSpans.add(new SyntaxHighlighter.HighlightSpan(i, i+1, Color.rgb(255, 100, 100)));
                }
            }
        }
        
        return bracketSpans;
    }
    
    private static int getBracketType(char c) {
        for (int i = 0; i < OPEN_BRACKETS.length; i++) {
            if (c == OPEN_BRACKETS[i] || c == CLOSE_BRACKETS[i]) return i;
        }
        return -1;
    }
    
    private static boolean isOpenBracket(char c) {
        for (char open : OPEN_BRACKETS) if (c == open) return true;
        return false;
    }
    
    private static boolean isCloseBracket(char c) {
        for (char close : CLOSE_BRACKETS) if (c == close) return true;
        return false;
    }
    
    private static boolean isInIgnoreRange(int pos, List<SyntaxHighlighter.HighlightSpan> ignoreRanges) {
        if (ignoreRanges == null) return false;
        for (SyntaxHighlighter.HighlightSpan span : ignoreRanges) {
            if (pos >= span.start && pos < span.end) return true;
        }
        return false;
    }
}