import 'package:intl/intl.dart';
import 'package:networthy/util/currency_util.dart';
import 'package:test/test.dart';

void main() {
  setUpAll(() => Intl.defaultLocale = 'en_US');

  group('format', () {
    test('groups thousands and drops decimals', () {
      expect(CurrencyUtil.format(1234.0, 'USD'), '\$1,234');
      expect(CurrencyUtil.format(0.0, 'USD'), '\$0');
    });

    test('rounds like java.lang.Math.round (half up, towards +inf)', () {
      expect(CurrencyUtil.format(1234.5, 'USD'), '\$1,235');
      expect(CurrencyUtil.format(1234.49, 'USD'), '\$1,234');
      // -0.5 rounds to 0 on the JVM, not to -1 as Dart's round() would.
      expect(CurrencyUtil.format(-0.5, 'USD'), '\$0');
      expect(CurrencyUtil.format(-1.5, 'USD'), '-\$1');
      expect(CurrencyUtil.format(-2.5, 'USD'), '-\$2');
    });

    test('keeps the sign outside the symbol', () {
      expect(CurrencyUtil.format(-4200.0, 'USD'), '-\$4,200');
    });

    test('falls back to the code for unknown currencies', () {
      expect(CurrencyUtil.format(10.0, 'ZZZ'), 'ZZZ10');
    });

    test('separates a letter symbol from the amount', () {
      // "R$" style symbols get a space so they do not run into the digits.
      expect(CurrencyUtil.symbol('BRL'), 'R \$');
      expect(CurrencyUtil.symbol('EUR'), '€');
    });
  });

  group('formatCompact', () {
    test('uses K above ten thousand', () {
      expect(CurrencyUtil.formatCompact(12345.0, 'USD'), '\$12.3K');
      expect(CurrencyUtil.formatCompact(10000.0, 'USD'), '\$10K');
    });

    test('keeps the plain format below ten thousand', () {
      expect(CurrencyUtil.formatCompact(9999.0, 'USD'), '\$9,999');
    });

    test('uses M and B for larger magnitudes', () {
      expect(CurrencyUtil.formatCompact(1250000.0, 'USD'), '\$1.2M');
      expect(CurrencyUtil.formatCompact(-2500000000.0, 'USD'), '-\$2.5B');
    });

    test('truncates rather than rounds the single decimal', () {
      expect(CurrencyUtil.formatCompact(1290000.0, 'USD'), '\$1.2M');
    });
  });

  group('metadata', () {
    test('masked balance uses the symbol and bullet placeholder', () {
      expect(CurrencyUtil.masked('USD'), '\$ ••••');
      expect(CurrencyUtil.maskShort, '••••');
    });

    test('display names come from the bundled ISO-4217 table', () {
      expect(CurrencyUtil.displayName('USD'), 'US Dollar');
      expect(CurrencyUtil.displayName('ZZZ'), 'ZZZ');
    });

    test('fraction digits follow the currency', () {
      expect(CurrencyUtil.fractionDigits('USD'), 2);
      expect(CurrencyUtil.fractionDigits('JPY'), 0);
      expect(CurrencyUtil.fractionDigits('ZZZ'), 2);
    });

    test('the currency list is alphabetical, complete and unique', () {
      final List<String> all = CurrencyUtil.allCurrencies;
      expect(all.length, 233);
      expect(all.toSet().length, all.length);
      expect(all, orderedEquals(List<String>.of(all)..sort()));
      expect(all, containsAll(CurrencyUtil.commonCurrencies));
    });
  });
}
