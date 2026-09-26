import 'package:flutter/material.dart';

/// Extra visual tokens that vary by [AppTheme] but are not part of the standard
/// Material [ColorScheme]: the net-worth hero treatment, the FAB fill and
/// whether category cards use per-type accent colours.
@immutable
class AppStyle extends ThemeExtension<AppStyle> {
  const AppStyle({
    required this.heroGradient,
    required this.heroForeground,
    required this.fabGradient,
    required this.colorfulCategories,
  });

  /// Gradient painted behind the net-worth card. When null the card falls back
  /// to a flat `primaryContainer` fill.
  final List<Color>? heroGradient;

  /// Text/icon colour used on top of [heroGradient]. Null when there is no
  /// gradient (the card then uses `onPrimaryContainer`).
  final Color? heroForeground;

  /// Gradient painted behind the "Add" FAB. Null keeps the default fill.
  final List<Color>? fabGradient;

  /// Whether asset/liability category cards are tinted with a per-type accent.
  final bool colorfulCategories;

  /// The understated Material You look (default).
  static const AppStyle classic = AppStyle(
    heroGradient: null,
    heroForeground: null,
    fabGradient: null,
    colorfulCategories: false,
  );

  /// The dark, colourful look: gradient hero, gradient FAB, tinted categories.
  static const AppStyle midnight = AppStyle(
    heroGradient: <Color>[
      Color(0xFF12B5A6),
      Color(0xFF4F46E5),
      Color(0xFF7C3AED),
    ],
    heroForeground: Colors.white,
    fabGradient: <Color>[Color(0xFF7C3AED), Color(0xFFEC4899)],
    colorfulCategories: true,
  );

  static AppStyle of(BuildContext context) =>
      Theme.of(context).extension<AppStyle>() ?? AppStyle.classic;

  @override
  AppStyle copyWith({
    List<Color>? heroGradient,
    Color? heroForeground,
    List<Color>? fabGradient,
    bool? colorfulCategories,
  }) => AppStyle(
    heroGradient: heroGradient ?? this.heroGradient,
    heroForeground: heroForeground ?? this.heroForeground,
    fabGradient: fabGradient ?? this.fabGradient,
    colorfulCategories: colorfulCategories ?? this.colorfulCategories,
  );

  // The two styles are swapped wholesale, so there is nothing meaningful to
  // interpolate; snap at the midpoint.
  @override
  AppStyle lerp(ThemeExtension<AppStyle>? other, double t) {
    if (other is! AppStyle) return this;
    return t < 0.5 ? this : other;
  }
}
