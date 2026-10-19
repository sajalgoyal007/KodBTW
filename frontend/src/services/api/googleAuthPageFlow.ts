export interface GoogleAuthPageFlowOptions {
  credential: string;
  challengeId: string;
  destination: string;
  inProgress: { current: boolean };
  submitIfReady: (submit: () => Promise<void>) => Promise<boolean>;
  loginWithGoogle: (credential: string, challengeId: string) => Promise<void>;
  navigate: (path: string, options: { replace: boolean }) => void;
  setError: (message: string | null) => void;
  setIsLoading: (loading: boolean) => void;
  formatError: (error: unknown) => string;
}

/** Runs the shared Google credential flow used by both auth pages. */
export async function runGoogleAuthPageFlow(options: GoogleAuthPageFlowOptions): Promise<void> {
  if (options.inProgress.current) return;

  options.inProgress.current = true;
  options.setError(null);
  try {
    await options.submitIfReady(async () => {
      options.setIsLoading(true);
      await options.loginWithGoogle(options.credential, options.challengeId);
      options.navigate(options.destination, { replace: true });
    });
  } catch (error) {
    options.setError(options.formatError(error));
  } finally {
    options.setIsLoading(false);
    options.inProgress.current = false;
  }
}
