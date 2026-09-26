import 'package:networthy/domain/model/app_theme.dart';
import 'package:networthy/ui/theme/app_style.dart';
import 'package:networthy/ui/theme/colors.dart';
import 'package:test/test.dart';

void main() {
  group('AppTheme', () {
    test('round-trips through its storage name', () {
      for (final AppTheme theme in AppTheme.values) {
        expect(AppTheme.fromName(theme.storageName), theme);
      }
    });

    test('unknown or missing names fall back to Classic', () {
      expect(AppTheme.fromName('NOPE'), AppTheme.classic);
      expect(AppTheme.fromName(null), AppTheme.classic);
    });
  });

  group('AppStyle', () {
    test('classic is a flat, neutral look', () {
      expect(AppStyle.classic.heroGradient, isNull);
      expect(AppStyle.classic.fabGradient, isNull);
      expect(AppStyle.classic.colorfulCategories, isFalse);
    });

    test('midnight adds gradients and colourful categories', () {
      expect(AppStyle.midnight.heroGradient, isNotNull);
      expect(AppStyle.midnight.heroGradient!.length, greaterThanOrEqualTo(2));
      expect(AppStyle.midnight.fabGradient, isNotNull);
      expect(AppStyle.midnight.colorfulCategories, isTrue);
    });

    test('lerp snaps between the two styles at the midpoint', () {
      expect(AppStyle.classic.lerp(AppStyle.midnight, 0.2), AppStyle.classic);
      expect(AppStyle.classic.lerp(AppStyle.midnight, 0.8), AppStyle.midnight);
    });
  });

  group('categoryColorAt', () {
    test('cycles through the palette', () {
      expect(categoryColorAt(0), categoryPalette.first);
      expect(categoryColorAt(categoryPalette.length), categoryPalette.first);
      expect(categoryColorAt(1), categoryPalette[1]);
    });
  });
}
