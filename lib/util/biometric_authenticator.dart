import 'package:local_auth/local_auth.dart';
import 'package:local_auth_android/local_auth_android.dart';
import 'package:local_auth_darwin/local_auth_darwin.dart';

/// Result of querying whether the device can perform an app-lock challenge.
enum BiometricCapability {
  /// Fingerprint / face / device PIN is available and can be used.
  available,

  /// Hardware exists but no biometric or device credential is enrolled.
  notEnrolled,

  /// The device cannot authenticate the user at all.
  unavailable,
}

/// Thin wrapper over `local_auth` mirroring the Android BiometricPrompt flow:
/// biometrics *or* the device credential (PIN/pattern/passcode) unlock the app.
class BiometricAuthenticator {
  const BiometricAuthenticator._();

  static final LocalAuthentication _auth = LocalAuthentication();

  /// Mirrors `BiometricManager.canAuthenticate(BIOMETRIC_WEAK or
  /// DEVICE_CREDENTIAL)`: a device secured with a PIN/pattern/passcode or with
  /// enrolled biometrics can run the challenge.
  static Future<BiometricCapability> capability() async {
    try {
      if (await _auth.isDeviceSupported()) return BiometricCapability.available;
      if (await _auth.canCheckBiometrics) {
        return BiometricCapability.notEnrolled;
      }
      return BiometricCapability.unavailable;
    } on LocalAuthException catch (error) {
      return switch (error.code) {
        LocalAuthExceptionCode.noCredentialsSet ||
        LocalAuthExceptionCode.noBiometricsEnrolled =>
          BiometricCapability.notEnrolled,
        _ => BiometricCapability.unavailable,
      };
    }
  }

  /// Shows the platform prompt. Returns null on success, or a failure message.
  static Future<String?> authenticate({
    required String title,
    required String subtitle,
  }) async {
    try {
      final bool ok = await _auth.authenticate(
        localizedReason: subtitle,
        authMessages: <AuthMessages>[
          AndroidAuthMessages(signInTitle: title),
          const IOSAuthMessages(),
        ],
        persistAcrossBackgrounding: true,
      );
      return ok ? null : 'Authentication failed';
    } on LocalAuthException catch (error) {
      return error.description ?? error.code.name;
    }
  }
}
