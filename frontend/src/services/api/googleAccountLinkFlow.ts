export interface GoogleAccountLinkFlowOptions {
  credential: string;
  challengeId: string;
  inProgress: { current: boolean };
  linkIdentity: (credential: string, challengeId: string) => Promise<unknown>;
  setIsLinking: (value: boolean) => void;
  setError: (message: string | null) => void;
  setConnected: (value: boolean) => void;
  clearSensitiveFormState: () => void;
  formatError: (error: unknown) => string;
}

export async function runGoogleAccountLinkFlow(options: GoogleAccountLinkFlowOptions): Promise<void> {
  if (options.inProgress.current) return;
  options.inProgress.current = true;
  options.setIsLinking(true);
  options.setError(null);

  try {
    await options.linkIdentity(options.credential, options.challengeId);
    options.setConnected(true);
    options.clearSensitiveFormState();
  } catch (error) {
    options.setError(options.formatError(error));
  } finally {
    options.inProgress.current = false;
    options.setIsLinking(false);
  }
}
