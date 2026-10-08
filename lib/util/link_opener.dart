import 'package:flutter/services.dart';

/// Opens web links in the system browser through a small platform channel
/// (handled in Android's MainActivity), so no extra plugin is needed.
class LinkOpener {
  const LinkOpener._();

  static const MethodChannel _channel = MethodChannel(
    'com.cairnlabworks.networthy/links',
  );

  static const String privacyPolicyUrl =
      'https://cairn-labworks.github.io/networthy/privacy/';

  /// Returns false when the link couldn't be opened, for example when no
  /// browser is installed or the platform has no handler (iOS).
  static Future<bool> open(String url) async {
    try {
      return await _channel.invokeMethod<bool>('openUrl', url) ?? false;
    } on MissingPluginException {
      return false;
    } on PlatformException {
      return false;
    }
  }
}
