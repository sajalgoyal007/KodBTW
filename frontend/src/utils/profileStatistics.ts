/** Keeps an empty account set from presenting backend aggregate defaults as observed stats. */
export function getProfileMetricValue<T extends number | string>(
  value: T | null | undefined,
  connectedPlatformsCount: number | null | undefined,
): T | null {
  if (!connectedPlatformsCount || connectedPlatformsCount < 1) return null;
  return value ?? null;
}
