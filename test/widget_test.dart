import 'package:flutter_test/flutter_test.dart';
import 'package:green_cycle/models/models.dart';

void main() {
  test('User model parses safely with missing fields', () {
    final u = User.fromJson({'id': 'u1', 'nickname': '小绿', 'points': 100});
    expect(u.id, 'u1');
    expect(u.points, 100);
    expect(u.totalCarbon, 0);
    expect(u.levelName, '环保新人');
  });

  test('RecycleOrder status text mapping', () {
    final o = RecycleOrder.fromJson({'id': 'o1', 'status': 'in_progress', 'items': []});
    expect(o.statusText, '回收员正在赶来');
    expect(o.isActive, true);
    expect(o.canCancel, false);
  });
}
