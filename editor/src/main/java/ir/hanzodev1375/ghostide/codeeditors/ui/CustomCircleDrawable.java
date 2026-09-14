package ir.hanzodev1375.ghostide.codeeditors.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import ir.theme.M3Theme;

public class CustomCircleDrawable extends Drawable {

  private final Paint mPaint;
  private final Paint mTextPaint;
  private final CompletionItemKind mKind;
  private final boolean mCircle;

  public CustomCircleDrawable(CompletionItemKind kind, boolean circle, int color) {
    mKind = kind;
    mCircle = circle;

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setColor(color);

    mTextPaint = new Paint();
    mTextPaint.setColor(contrastText(color));
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextSize(Resources.getSystem().getDisplayMetrics().density * 14);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
  }

  public CustomCircleDrawable(CompletionItemKind kind, boolean circle) {
    this(kind, circle, getFallbackColor(kind));
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    float width = getBounds().right;
    float height = getBounds().bottom;
    float radius = width * 0.3f;

    if (mKind == CompletionItemKind.Variable || mKind == CompletionItemKind.Function) {
      canvas.drawRoundRect(4f, 4f, width - 4f, height - 4f, radius, radius, mPaint);
    } else if (mCircle) {
      canvas.drawCircle(width / 2f, height / 2f, width / 2f, mPaint);
    } else {
      canvas.drawRect(0f, 0f, width, height, mPaint);
    }

    canvas.save();
    canvas.translate(width / 2f, height / 2f);
    float textCenter = -(mTextPaint.descent() + mTextPaint.ascent()) / 2f;
    canvas.drawText(mKind.getDisplayChar(), 0f, textCenter, mTextPaint);
    canvas.restore();
  }

  @Override
  public void setAlpha(int alpha) {
    mPaint.setAlpha(alpha);
    mTextPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    mTextPaint.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  private static int getFallbackColor(CompletionItemKind kind) {
    if (kind == null) {
      return M3Theme.primary();
    }
    switch (kind) {
      case Identifier:
        return M3Theme.onSurfaceVariant();
      case Text:
        return M3Theme.outline();
      case Method:
        return M3Theme.secondary();
      case Function:
        return M3Theme.secondaryContainer();
      case Constructor:
        return M3Theme.tertiary();
      case Field:
        return M3Theme.primary();
      case Variable:
        return M3Theme.primaryContainer();
      case Class:
        return M3Theme.primary();
      case Interface:
        return M3Theme.tertiary();
      case Module:
        return M3Theme.outlineVariant();
      case Property:
        return M3Theme.secondary();
      case Unit:
        return M3Theme.onSurfaceVariant();
      case Value:
        return M3Theme.tertiaryContainer();
      case Enum:
        return M3Theme.secondary();
      case Keyword:
        return M3Theme.primary();
      case Snippet:
        return M3Theme.tertiary();
      case Color:
        return M3Theme.tertiaryContainer();
      case Reference:
        return M3Theme.secondaryContainer();
      case File:
        return M3Theme.primaryContainer();
      case Folder:
        return M3Theme.onSurfaceVariant();
      case EnumMember:
        return M3Theme.secondaryContainer();
      case Constant:
        return M3Theme.primaryContainer();
      case Struct:
        return M3Theme.tertiaryContainer();
      case Event:
        return M3Theme.error();
      case Operator:
        return M3Theme.secondaryContainer();
      case TypeParameter:
        return M3Theme.tertiaryContainer();
      case User:
        return M3Theme.secondary();
      case Issue:
        return M3Theme.error();
      default:
        return M3Theme.primary();
    }
  }

  private static int contrastText(int bg) {
    return ColorUtils.calculateLuminance(bg) > 0.5f ? Color.BLACK : Color.WHITE;
  }
}
