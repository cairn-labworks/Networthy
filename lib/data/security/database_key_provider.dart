import 'dart:convert';
import 'dart:typed_data';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'backup_crypto.dart';

/// Provides the 256-bit passphrase used to encrypt the SQLCipher database.
///
/// The passphrase is generated once with a CSPRNG and stored in the platform
/// keystore (Android Keystore / iOS Keychain) through `flutter_secure_storage`,
/// so the raw passphrase never touches plaintext storage and never leaves the
/// device.
class DatabaseKeyProvider {
  const DatabaseKeyProvider([this._storage = _defaultStorage]);

  static const String _keyDbPassphrase = 'db_passphrase';
  static const int _keySizeBytes = 32;

  static const FlutterSecureStorage _defaultStorage = FlutterSecureStorage(
    aOptions: AndroidOptions(),
    iOptions: IOSOptions(
      accessibility: KeychainAccessibility.first_unlock_this_device,
    ),
  );

  final FlutterSecureStorage _storage;

  /// Returns the database passphrase, generating and persisting one on first use.
  /// The value is base64 text because SQLCipher's Flutter binding takes a string.
  Future<String> getOrCreatePassphrase() async {
    final String? existing = await _storage.read(key: _keyDbPassphrase);
    if (existing != null && existing.isNotEmpty) return existing;
    final Uint8List generated = BackupCrypto.randomBytes(_keySizeBytes);
    final String encoded = base64.encode(generated);
    await _storage.write(key: _keyDbPassphrase, value: encoded);
    return encoded;
  }
}
