/// User preference for light / dark appearance.
enum AppThemeMode {
  system('SYSTEM', 'System default'),
  light('LIGHT', 'Light'),
  dark('DARK', 'Dark');

  const AppThemeMode(this.storageName, this.displayName);

  final String storageName;
  final String displayName;

  static AppThemeMode fromName(String? name) {
    for (final mode in values) {
      if (mode.storageName == name) return mode;
    }
    return AppThemeMode.system;
  }
}
