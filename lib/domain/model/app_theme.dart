/// A selectable overall appearance for the app.
///
/// [classic] is the original Material 3 look that adapts to light/dark and the
/// wallpaper palette. [midnight] is a fixed dark, colourful theme with a
/// gradient net-worth hero and per-category accent colours.
enum AppTheme {
  classic('CLASSIC', 'Classic', 'Calm teal, adapts to light and dark'),
  midnight('MIDNIGHT', 'Midnight', 'Dark and colourful, always dark');

  const AppTheme(this.storageName, this.displayName, this.description);

  final String storageName;
  final String displayName;
  final String description;

  static AppTheme fromName(String? name) {
    for (final AppTheme theme in values) {
      if (theme.storageName == name) return theme;
    }
    return AppTheme.classic;
  }
}
