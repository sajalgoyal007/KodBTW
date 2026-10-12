type RefreshTask = () => Promise<void>;

/** Coalesces ordinary fetches and runs at most one queued follow-up refresh. */
export function createQueuedRefreshRunner() {
  let inFlight: Promise<void> | null = null;
  let queuedTask: RefreshTask | null = null;

  return (task: RefreshTask, queueIfActive = false): Promise<void> => {
    if (inFlight) {
      if (queueIfActive) queuedTask = task;
      return inFlight;
    }

    const run = async () => {
      let failure: unknown;
      let failed = false;
      const execute = async (nextTask: RefreshTask) => {
        try {
          await nextTask();
        } catch (error) {
          if (!failed) failure = error;
          failed = true;
        }
      };

      await execute(task);
      while (queuedTask) {
        const nextTask = queuedTask;
        queuedTask = null;
        await execute(nextTask);
      }
      inFlight = null;
      if (failed) throw failure;
    };

    inFlight = run();
    return inFlight;
  };
}
