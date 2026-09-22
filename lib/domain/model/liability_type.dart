/// The category of a liability (something owed).
enum LiabilityType {
  loan('LOAN', 'Loan'),
  mortgage('MORTGAGE', 'Mortgage'),
  creditCard('CREDIT_CARD', 'Credit card'),
  pendingPayment('PENDING_PAYMENT', 'Pending payment'),
  tax('TAX', 'Tax due'),
  other('OTHER', 'Other');

  const LiabilityType(this.storageName, this.displayName);

  final String storageName;
  final String displayName;

  static LiabilityType fromName(String? name) {
    for (final type in values) {
      if (type.storageName == name) return type;
    }
    return LiabilityType.other;
  }
}
