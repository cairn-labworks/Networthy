import 'package:flutter/material.dart';

// Brand seed: a calm financial green/teal. These hand-tuned tonal values are used
// when the platform does not provide a dynamic (Material You) color scheme.

const ColorScheme lightColors = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF006A60),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFF9EF2E4),
  onPrimaryContainer: Color(0xFF00201C),
  secondary: Color(0xFF4A635F),
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFCCE8E2),
  onSecondaryContainer: Color(0xFF05201C),
  tertiary: Color(0xFF456179),
  onTertiary: Color(0xFFFFFFFF),
  tertiaryContainer: Color(0xFFCBE6FF),
  onTertiaryContainer: Color(0xFF001E30),
  error: Color(0xFFBA1A1A),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFFFFDAD6),
  onErrorContainer: Color(0xFF410002),
  surface: Color(0xFFFBFAF8),
  onSurface: Color(0xFF191C1B),
  surfaceContainerHighest: Color(0xFFDAE5E1),
  onSurfaceVariant: Color(0xFF3F4947),
  outline: Color(0xFF6F7976),
  outlineVariant: Color(0xFFBEC9C5),
  surfaceTint: Color(0xFF006A60),
  inverseSurface: Color(0xFF2D3130),
  onInverseSurface: Color(0xFFEFF1EF),
  inversePrimary: Color(0xFF82D5C8),
);

const ColorScheme darkColors = ColorScheme(
  brightness: Brightness.dark,
  primary: Color(0xFF82D5C8),
  onPrimary: Color(0xFF003731),
  primaryContainer: Color(0xFF005048),
  onPrimaryContainer: Color(0xFF9EF2E4),
  secondary: Color(0xFFB1CCC6),
  onSecondary: Color(0xFF1C3531),
  secondaryContainer: Color(0xFF334B47),
  onSecondaryContainer: Color(0xFFCCE8E2),
  tertiary: Color(0xFFACCAE5),
  onTertiary: Color(0xFF133348),
  tertiaryContainer: Color(0xFF2C4A60),
  onTertiaryContainer: Color(0xFFCBE6FF),
  error: Color(0xFFFFB4AB),
  onError: Color(0xFF690005),
  errorContainer: Color(0xFF93000A),
  onErrorContainer: Color(0xFFFFDAD6),
  surface: Color(0xFF191C1B),
  onSurface: Color(0xFFE0E3E1),
  surfaceContainerHighest: Color(0xFF3F4947),
  onSurfaceVariant: Color(0xFFBEC9C5),
  outline: Color(0xFF899390),
  outlineVariant: Color(0xFF3F4947),
  surfaceTint: Color(0xFF82D5C8),
  inverseSurface: Color(0xFFE0E3E1),
  onInverseSurface: Color(0xFF191C1B),
  inversePrimary: Color(0xFF006A60),
);

// Semantic colors for positive (assets/gains) and negative (liabilities/losses).
const Color positiveGreen = Color(0xFF1B8A5A);
const Color positiveGreenDark = Color(0xFF6DD69C);
const Color negativeRed = Color(0xFFBA1A1A);
const Color negativeRedDark = Color(0xFFFFB4AB);

/// Palette used for chart slices, cycled by index.
const List<Color> chartColors = <Color>[
  Color(0xFF00897B),
  Color(0xFF3949AB),
  Color(0xFFF9A825),
  Color(0xFF8E24AA),
  Color(0xFF43A047),
  Color(0xFFE53935),
  Color(0xFF1E88E5),
  Color(0xFFF4511E),
  Color(0xFF6D4C41),
  Color(0xFF00ACC1),
  Color(0xFF7CB342),
  Color(0xFFD81B60),
];

Color chartColorAt(int index) => chartColors[index % chartColors.length];
