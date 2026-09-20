import 'package:dynamic_color/dynamic_color.dart' show DynamicColorPlugin;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
// ignore_for_file: deprecated_member_use
// `DynamicColorPlugin` still returns the (deprecated) CorePalette type.
import 'package:material_color_utilities/material_color_utilities.dart';

typedef DynamicColorWidgetBuilder = Widget Function(
  ColorScheme? lightDynamic,
  ColorScheme? darkDynamic,
);

/// Provides the wallpaper-derived (Material You) color schemes when the
/// platform exposes them, mirroring Compose's `dynamicLightColorScheme` /
/// `dynamicDarkColorScheme`. The schemes are null while loading and on
/// platforms without dynamic color, in which case the brand palette is used.
class DynamicColorBuilder extends StatefulWidget {
  const DynamicColorBuilder({required this.builder, super.key});

  final DynamicColorWidgetBuilder builder;

  @override
  State<DynamicColorBuilder> createState() => _DynamicColorBuilderState();
}

class _DynamicColorBuilderState extends State<DynamicColorBuilder> {
  ColorScheme? _light;
  ColorScheme? _dark;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    ColorScheme? light;
    ColorScheme? dark;
    try {
      final CorePalette? palette = await DynamicColorPlugin.getCorePalette();
      if (palette != null) {
        light = schemeFromCorePalette(palette, Brightness.light);
        dark = schemeFromCorePalette(palette, Brightness.dark);
      } else {
        final Color? accent = await DynamicColorPlugin.getAccentColor();
        if (accent != null) {
          light = ColorScheme.fromSeed(seedColor: accent);
          dark = ColorScheme.fromSeed(
            seedColor: accent,
            brightness: Brightness.dark,
          );
        }
      }
    } on PlatformException {
      // Dynamic color is unsupported here; fall back to the brand palette.
    }
    if (!mounted) return;
    setState(() {
      _light = light;
      _dark = dark;
    });
  }

  @override
  Widget build(BuildContext context) => widget.builder(_light, _dark);
}

/// Maps a platform [CorePalette] onto a Material 3 [ColorScheme] using the
/// standard tone assignments.
ColorScheme schemeFromCorePalette(CorePalette palette, Brightness brightness) {
  Color tone(TonalPalette source, int value) => Color(source.get(value));
  final bool dark = brightness == Brightness.dark;
  return ColorScheme(
    brightness: brightness,
    primary: tone(palette.primary, dark ? 80 : 40),
    onPrimary: tone(palette.primary, dark ? 20 : 100),
    primaryContainer: tone(palette.primary, dark ? 30 : 90),
    onPrimaryContainer: tone(palette.primary, dark ? 90 : 10),
    secondary: tone(palette.secondary, dark ? 80 : 40),
    onSecondary: tone(palette.secondary, dark ? 20 : 100),
    secondaryContainer: tone(palette.secondary, dark ? 30 : 90),
    onSecondaryContainer: tone(palette.secondary, dark ? 90 : 10),
    tertiary: tone(palette.tertiary, dark ? 80 : 40),
    onTertiary: tone(palette.tertiary, dark ? 20 : 100),
    tertiaryContainer: tone(palette.tertiary, dark ? 30 : 90),
    onTertiaryContainer: tone(palette.tertiary, dark ? 90 : 10),
    error: tone(palette.error, dark ? 80 : 40),
    onError: tone(palette.error, dark ? 20 : 100),
    errorContainer: tone(palette.error, dark ? 30 : 90),
    onErrorContainer: tone(palette.error, dark ? 90 : 10),
    surface: tone(palette.neutral, dark ? 6 : 98),
    onSurface: tone(palette.neutral, dark ? 90 : 10),
    surfaceContainerLowest: tone(palette.neutral, dark ? 4 : 100),
    surfaceContainerLow: tone(palette.neutral, dark ? 10 : 96),
    surfaceContainer: tone(palette.neutral, dark ? 12 : 94),
    surfaceContainerHigh: tone(palette.neutral, dark ? 17 : 92),
    surfaceContainerHighest: tone(palette.neutral, dark ? 22 : 90),
    onSurfaceVariant: tone(palette.neutralVariant, dark ? 80 : 30),
    outline: tone(palette.neutralVariant, dark ? 60 : 50),
    outlineVariant: tone(palette.neutralVariant, dark ? 30 : 80),
    shadow: tone(palette.neutral, 0),
    scrim: tone(palette.neutral, 0),
    inverseSurface: tone(palette.neutral, dark ? 90 : 20),
    onInverseSurface: tone(palette.neutral, dark ? 20 : 95),
    inversePrimary: tone(palette.primary, dark ? 40 : 80),
    surfaceTint: tone(palette.primary, dark ? 80 : 40),
  );
}
