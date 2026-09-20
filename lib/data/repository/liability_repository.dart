import '../local/dao/liability_dao.dart';
import '../local/entity/liability_entity.dart';

class LiabilityRepository {
  const LiabilityRepository(this._dao);

  final LiabilityDao _dao;

  Stream<List<LiabilityEntity>> observeForPortfolio(int portfolioId) =>
      _dao.observeForPortfolio(portfolioId);

  Future<List<LiabilityEntity>> getForPortfolio(int portfolioId) =>
      _dao.getForPortfolio(portfolioId);

  Stream<LiabilityEntity?> observeById(int id) => _dao.observeById(id);

  Future<LiabilityEntity?> getById(int id) => _dao.getById(id);

  Future<int> save(LiabilityEntity liability) async {
    final LiabilityEntity stamped = liability.copyWith(
      updatedAt: DateTime.now().millisecondsSinceEpoch,
    );
    if (liability.id == 0) {
      final int position =
          await _dao.maxPosition(liability.portfolioId, liability.type) + 1;
      return _dao.insert(stamped.copyWith(position: position));
    }
    await _dao.update(stamped);
    return liability.id;
  }

  Future<void> delete(int id) => _dao.deleteById(id);

  /// Persists a new custom order for a set of liabilities (typically one type).
  Future<void> updateOrder(List<int> orderedIds) =>
      _dao.updatePositions(orderedIds);
}
