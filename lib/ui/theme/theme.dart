import 'package:flutter/material.dart';

import 'colors.dart';

/// Semantic colors that are not part of the standard Material scheme.
@immutable
class FinanceColors extends ThemeExtension<FinanceColors> {
  const FinanceColors({required this.positive, required this.negative});

  static const FinanceColors light = FinanceColors(
    positive: positiveGreen,
    negative: negativeRed,
  );

  static const FinanceColors dark = FinanceColors(
    positive: positiveGreenDark,
    negative: negativeRedDark,
  );

  final Color positive;
  final Color negative;

  static FinanceColors of(BuildContext context) =>
      Theme.of(context).extension<FinanceColors>() ?? light;

  @override
  FinanceColors copyWith({Color? positive, Color? negative}) => FinanceColors(
    positive: positive ?? this.positive,
    negative: negative ?? this.negative,
  );

  @override
  FinanceColors lerp(ThemeExtension<FinanceColors>? other, double t) {
    if (other is! FinanceColors) return this;
    return FinanceColors(
      positive: Color.lerp(positive, other.positive, t)!,
      negative: Color.lerp(negative, other.negative, t)!,
    );
  }
}

// Material 3 type scale. Uses the platform default font family so the app reads
// as a native Material app without shipping custom fonts.
const TextTheme appTypography = TextTheme(
  displayLarge: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 57,
    height: 64 / 57,
    letterSpacing: -0.25,
  ),
  displayMedium: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 45,
    height: 52 / 45,
  ),
  displaySmall: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 36,
    height: 44 / 36,
  ),
  headlineLarge: TextStyle(
    fontWeight: FontWeight.w600,
    fontSize: 32,
    height: 40 / 32,
  ),
  headlineMedium: TextStyle(
    fontWeight: FontWeight.w600,
    fontSize: 28,
    height: 36 / 28,
  ),
  headlineSmall: TextStyle(
    fontWeight: FontWeight.w600,
    fontSize: 24,
    height: 32 / 24,
  ),
  titleLarge: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 22,
    height: 28 / 22,
  ),
  titleMedium: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 16,
    height: 24 / 16,
    letterSpacing: 0.15,
  ),
  titleSmall: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 14,
    height: 20 / 14,
    letterSpacing: 0.1,
  ),
  bodyLarge: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 16,
    height: 24 / 16,
    letterSpacing: 0.5,
  ),
  bodyMedium: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 14,
    height: 20 / 14,
    letterSpacing: 0.25,
  ),
  bodySmall: TextStyle(
    fontWeight: FontWeight.w400,
    fontSize: 12,
    height: 16 / 12,
    letterSpacing: 0.4,
  ),
  labelLarge: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 14,
    height: 20 / 14,
    letterSpacing: 0.1,
  ),
  labelMedium: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 12,
    height: 16 / 12,
    letterSpacing: 0.5,
  ),
  labelSmall: TextStyle(
    fontWeight: FontWeight.w500,
    fontSize: 11,
    height: 16 / 11,
    letterSpacing: 0.5,
  ),
);

// Slightly rounded, friendly shapes consistent with Material 3.
const double shapeExtraSmall = 4;
const double shapeSmall = 8;
const double shapeMedium = 16;
const double shapeLarge = 24;
const double shapeExtraLarge = 28;

/// Builds the app theme, optionally seeded from the platform's dynamic
/// (Material You) palette on Android 12+.
ThemeData buildTheme({
  required Brightness brightness,
  ColorScheme? dynamicScheme,
}) {
  final bool dark = brightness == Brightness.dark;
  final ColorScheme scheme = dynamicScheme ?? (dark ? darkColors : lightColors);
  return ThemeData(
    useMaterial3: true,
    brightness: brightness,
    colorScheme: scheme,
    textTheme: appTypography,
    extensions: <ThemeExtension<dynamic>>[
      dark ? FinanceColors.dark : FinanceColors.light,
    ],
    cardTheme: CardThemeData(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(shapeMedium),
      ),
    ),
    dialogTheme: DialogThemeData(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(shapeExtraLarge),
      ),
    ),
    bottomSheetTheme: const BottomSheetThemeData(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(
          top: Radius.circular(shapeExtraLarge),
        ),
      ),
    ),
    inputDecorationTheme: InputDecorationTheme(
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(shapeExtraSmall),
      ),
    ),
    snackBarTheme: const SnackBarThemeData(behavior: SnackBarBehavior.floating),
  );
}
