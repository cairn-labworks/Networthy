import Flutter
import UIKit

/// iOS has no equivalent of Android's FLAG_SECURE, so the next best thing is to
/// cover the UI with a blur while the app is inactive or backgrounded. That
/// keeps balances out of the app-switcher snapshot.
class SceneDelegate: FlutterSceneDelegate {
  private var privacyView: UIVisualEffectView?

  override func sceneWillResignActive(_ scene: UIScene) {
    super.sceneWillResignActive(scene)
    guard privacyView == nil,
          let windowScene = scene as? UIWindowScene,
          let window = windowScene.windows.first(where: { $0.isKeyWindow }) ?? windowScene.windows.first
    else { return }
    let blur = UIVisualEffectView(effect: UIBlurEffect(style: .systemMaterial))
    blur.frame = window.bounds
    blur.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    window.addSubview(blur)
    privacyView = blur
  }

  override func sceneDidBecomeActive(_ scene: UIScene) {
    super.sceneDidBecomeActive(scene)
    privacyView?.removeFromSuperview()
    privacyView = nil
  }
}
