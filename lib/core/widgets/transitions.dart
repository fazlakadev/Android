import 'package:flutter/material.dart';

/// Smooth fade + slide page route used across the app.
class FadeSlideRoute<T> extends PageRouteBuilder<T> {
  FadeSlideRoute(Widget screen)
      : super(
          transitionDuration: const Duration(milliseconds: 280),
          reverseTransitionDuration: const Duration(milliseconds: 220),
          pageBuilder: (_, _, _) => screen,
          transitionsBuilder: (_, animation, _, child) {
            final curved =
                CurvedAnimation(parent: animation, curve: Curves.easeOutCubic);
            return FadeTransition(
              opacity: curved,
              child: SlideTransition(
                position: Tween<Offset>(
                  begin: const Offset(0, .035),
                  end: Offset.zero,
                ).animate(curved),
                child: child,
              ),
            );
          },
        );
}
