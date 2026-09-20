/// A lightweight success/failure wrapper mirroring Kotlin's `Result`, used so
/// network calls can surface friendly messages instead of throwing.
class Result<T> {
  const Result._(this._value, this.errorMessage);

  factory Result.success(T value) => Result<T>._(value, null);

  factory Result.failure(String message) => Result<T>._(null, message);

  final T? _value;
  final String? errorMessage;

  bool get isSuccess => errorMessage == null;

  bool get isFailure => errorMessage != null;

  /// The value of a successful result. Only call after checking [isSuccess].
  T get value => _value as T;

  T? get valueOrNull => _value;
}
