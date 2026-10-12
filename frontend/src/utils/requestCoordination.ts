export function createSingleFlightGate() {
  let active = false;

  return {
    tryEnter(): boolean {
      if (active) return false;
      active = true;
      return true;
    },
    leave(): void {
      active = false;
    },
  };
}

export function createLatestRequestTracker() {
  let version = 0;

  return {
    begin(): number {
      version += 1;
      return version;
    },
    isCurrent(requestVersion: number): boolean {
      return requestVersion === version;
    },
    invalidate(): void {
      version += 1;
    },
  };
}
