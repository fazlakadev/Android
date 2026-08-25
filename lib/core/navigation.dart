import 'package:flutter/material.dart';

/// Root navigator used by background handlers (FCM taps) to push screens
/// above whatever shell is currently mounted.
final GlobalKey<NavigatorState> appNavigatorKey =
    GlobalKey<NavigatorState>();
