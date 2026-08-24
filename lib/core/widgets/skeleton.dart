import 'package:flutter/material.dart';

/// Lightweight shimmer effect — no external package needed.
class Shimmer extends StatefulWidget {
  const Shimmer({super.key, required this.child, this.enabled = true});

  final Widget child;
  final bool enabled;

  @override
  State<Shimmer> createState() => _ShimmerState();
}

class _ShimmerState extends State<Shimmer>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 1400),
  )..repeat();

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!widget.enabled) return widget.child;
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final base = isDark ? Colors.white12 : Colors.black.withValues(alpha: .06);
    final highlight =
        isDark ? Colors.white24 : Colors.black.withValues(alpha: .12);
    return AnimatedBuilder(
      animation: _ctrl,
      builder: (context, child) {
        return ShaderMask(
          blendMode: BlendMode.srcATop,
          shaderCallback: (bounds) {
            final dx = (bounds.width * 2) * (_ctrl.value * 2 - .5);
            return LinearGradient(
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
              colors: [base, highlight, base],
              stops: const [.35, .5, .65],
            ).createShader(bounds.translate(dx, 0));
          },
          child: child,
        );
      },
      child: widget.child,
    );
  }
}

/// A single rounded placeholder block.
class SkeletonBox extends StatelessWidget {
  const SkeletonBox({
    super.key,
    this.width,
    required this.height,
    this.radius = 12,
  });

  final double? width;
  final double height;
  final double radius;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(radius),
        color: isDark ? Colors.white.withValues(alpha: .07) : Colors.black
            .withValues(alpha: .05),
      ),
    );
  }
}

class SkeletonCircle extends StatelessWidget {
  const SkeletonCircle({super.key, this.size = 44});

  final double size;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: isDark ? Colors.white.withValues(alpha: .07) : Colors.black
            .withValues(alpha: .05),
      ),
    );
  }
}

/// Wrap any group of [SkeletonBox]es to give them the shimmer animation.
class SkeletonGroup extends StatelessWidget {
  const SkeletonGroup({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) => Shimmer(child: child);
}

// ---------------------------------------------------------------------------
// Ready-made page skeletons
// ---------------------------------------------------------------------------

class EpisodeGridSkeleton extends StatelessWidget {
  const EpisodeGridSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return SkeletonGroup(
      child: GridView.builder(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: .72,
        ),
        itemCount: 6,
        itemBuilder: (_, _) => Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(child: SkeletonBox(height: double.infinity, radius: 16)),
            const SizedBox(height: 8),
            SkeletonBox(width: double.infinity, height: 14, radius: 6),
            const SizedBox(height: 6),
            SkeletonBox(width: 90, height: 11, radius: 6),
          ],
        ),
      ),
    );
  }
}

class ArticleListSkeleton extends StatelessWidget {
  const ArticleListSkeleton({super.key, this.itemCount = 5});

  final int itemCount;

  @override
  Widget build(BuildContext context) {
    return SkeletonGroup(
      child: ListView.separated(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        physics: const NeverScrollableScrollPhysics(),
        itemCount: itemCount,
        separatorBuilder: (_, _) => const SizedBox(height: 14),
        itemBuilder: (_, _) => Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  SkeletonBox(width: 70, height: 10, radius: 6),
                  const SizedBox(height: 8),
                  SkeletonBox(width: double.infinity, height: 15, radius: 6),
                  const SizedBox(height: 6),
                  SkeletonBox(width: double.infinity, height: 15, radius: 6),
                  const SizedBox(height: 6),
                  SkeletonBox(width: 130, height: 15, radius: 6),
                ],
              ),
            ),
            const SizedBox(width: 12),
            SkeletonBox(height: 84, width: 110, radius: 14),
          ],
        ),
      ),
    );
  }
}

class SeasonGridSkeleton extends StatelessWidget {
  const SeasonGridSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return SkeletonGroup(
      child: GridView.builder(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: .78,
        ),
        itemCount: 4,
        itemBuilder: (_, _) => SkeletonBox(height: double.infinity, radius: 18),
      ),
    );
  }
}

class HomeSkeleton extends StatelessWidget {
  const HomeSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return SkeletonGroup(
      child: ListView(
        physics: const NeverScrollableScrollPhysics(),
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 32),
        children: [
          SkeletonBox(height: 160, radius: 20),
          const SizedBox(height: 22),
          Row(
            children: [
              SkeletonBox(width: 120, height: 18, radius: 8),
              const Spacer(),
              SkeletonBox(width: 60, height: 13, radius: 6),
            ],
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 190,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: 3,
              separatorBuilder: (_, _) => const SizedBox(width: 12),
              itemBuilder: (_, _) =>
                  SkeletonBox(height: 188, width: 250, radius: 18),
            ),
          ),
          const SizedBox(height: 22),
          Row(
            children: [
              SkeletonBox(width: 140, height: 18, radius: 8),
              const Spacer(),
              SkeletonBox(width: 60, height: 13, radius: 6),
            ],
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 120,
            child: ListView.separated(
              scrollDirection: Axis.horizontal,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: 3,
              separatorBuilder: (_, _) => const SizedBox(width: 12),
              itemBuilder: (_, _) => SkeletonBox(height: 118, width: 220, radius: 16),
            ),
          ),
          const SizedBox(height: 22),
          Row(
            children: [
              SkeletonBox(width: 130, height: 18, radius: 8),
              const Spacer(),
              SkeletonBox(width: 60, height: 13, radius: 6),
            ],
          ),
          const SizedBox(height: 12),
          ...List.generate(
            3,
            (_) => Padding(
              padding: const EdgeInsets.only(bottom: 14),
              child: Row(
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        SkeletonBox(width: double.infinity, height: 14, radius: 6),
                        const SizedBox(height: 7),
                        SkeletonBox(width: 180, height: 14, radius: 6),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  SkeletonBox(height: 64, width: 92, radius: 12),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class SearchSkeleton extends StatelessWidget {
  const SearchSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    return SkeletonGroup(
      child: ListView(
        physics: const NeverScrollableScrollPhysics(),
        padding: const EdgeInsets.all(16),
        children: List.generate(
          6,
          (_) => Padding(
            padding: const EdgeInsets.only(bottom: 16),
            child: Row(
              children: [
                SkeletonBox(height: 56, width: 56, radius: 12),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SkeletonBox(width: double.infinity, height: 14, radius: 6),
                      const SizedBox(height: 8),
                      SkeletonBox(width: 140, height: 11, radius: 6),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
