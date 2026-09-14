/*
 * This file is part of CodeOps Studio.
 * CodeOps Studio - Code anywhere anytime
 * https://github.com/euptron/CodeOps-Studio
 * Copyright (C) 2024-2026 Etido Peter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/
 *
 * If you have more questions, feel free to message Etido Peter if you have any
 * questions or need additional information. Email: euptron@gmail.com
 */

package ir.hanzodev1375.ghostide.codeeditors.ui;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem;

/**
 * CustomCompletionItem represents a replace action for auto-completion. {@code prefixLength} is the
 * length of prefix (text length you want to replace before the auto-completion position). {@code
 * commitText} is the text you want to replace the original text.
 *
 * <p>Note that you must make sure the start position of replacement is on the same line as
 * auto-completion's required position.
 *
 * @author Etido Peter
 * @see SimpleCompletionItem
 */
public class CustomCompletionItem extends SimpleCompletionItem {

  /** Text to display as compressive description in mAdapter */
  @Nullable public CharSequence compDescription;

  public CustomCompletionItem(int prefixLength, String commitText) {
    this(commitText, prefixLength, commitText);
  }

  public CustomCompletionItem(CharSequence label, int prefixLength, String commitText) {
    this(label, null, prefixLength, commitText);
  }

  public CustomCompletionItem(
      CharSequence label, CharSequence desc, int prefixLength, String commitText) {
    this(label, desc, null, prefixLength, commitText);
  }

  public CustomCompletionItem(
      CharSequence label,
      CharSequence desc,
      CharSequence compDes,
      int prefixLength,
      String commitText) {
    this(label, desc, null, compDes, prefixLength, commitText);
  }

  public CustomCompletionItem(
      CharSequence label,
      CharSequence desc,
      Drawable icon,
      @Nullable CharSequence compDes,
      int prefixLength,
      String commitText) {
    super(label, desc, icon, prefixLength, commitText);
    this.compDescription = compDes;
  }

  public CustomCompletionItem compDes(CharSequence desc) {
    this.compDescription = desc;
    return this;
  }
}
