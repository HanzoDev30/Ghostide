package ir.hanzodev1375.ghostide.models;

import androidx.annotation.DrawableRes;

public record CommandItem(
    String id,
    String title,
    String desc,
    @DrawableRes int iconRes,
    boolean enabled) {}