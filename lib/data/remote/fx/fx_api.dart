import 'dart:convert';

import 'package:http/http.dart' as http;

/// Response from open.er-api.com/v6/latest/{base}.
class FxResponse {
  const FxResponse({
    this.result,
    this.baseCode,
    this.rates,
    this.timeLastUpdateUnix,
  });

  factory FxResponse.fromJson(Map<String, Object?> json) => FxResponse(
    result: json['result'] as String?,
    baseCode: json['base_code'] as String?,
    rates: (json['rates'] as Map<String, Object?>?)?.map(
      (String key, Object? value) =>
          MapEntry<String, double>(key, (value as num).toDouble()),
    ),
    timeLastUpdateUnix: (json['time_last_update_unix'] as num?)?.toInt(),
  );

  final String? result;
  final String? baseCode;
  final Map<String, double>? rates;
  final int? timeLastUpdateUnix;
}

/// Public, key-less exchange-rate endpoint. Returns units of each currency per
/// 1 unit of the requested base. Only the base currency code is transmitted.
class FxApi {
  FxApi({http.Client? client}) : _client = client ?? http.Client();

  static const String baseUrl = 'https://open.er-api.com/';

  final http.Client _client;

  Future<FxResponse> getLatest([String base = 'USD']) async {
    final Uri uri = Uri.parse('${baseUrl}v6/latest/$base');
    final http.Response response = await _client.get(uri);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw http.ClientException('HTTP ${response.statusCode}', uri);
    }
    return FxResponse.fromJson(
      jsonDecode(utf8.decode(response.bodyBytes)) as Map<String, Object?>,
    );
  }
}
