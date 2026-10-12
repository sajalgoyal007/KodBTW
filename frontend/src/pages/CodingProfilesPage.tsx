import React, { useState } from 'react';
import { AppNavbar } from '../components/layout/AppNavbar';
import { usePlatformAccounts } from '../hooks/usePlatformAccounts';
import { PlatformAccountModal } from '../components/platform/PlatformAccountModal';
import { PlatformStatsModal } from '../components/platform/PlatformStatsModal';
import { ConfirmModal } from '../components/common/ConfirmModal';
import { PlatformAccountResponse, PlatformAccountRequest } from '../types/platform';
import { PlatformType } from '../types/platform';
import { getDashboardRefreshFeedback, getPlatformAvailabilityLabel } from '../utils/platformStatsSource';
import {
  PlusCircle,
  ExternalLink,
  Clock,
  BarChart2,
  Edit2,
  Trash2,
  Layers,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

const SUPPORTED_PLATFORMS = [
  { platform: 'LEETCODE', label: 'LeetCode' },
  { platform: 'CODEFORCES', label: 'Codeforces' },
  { platform: 'CODECHEF', label: 'CodeChef' },
  { platform: 'GEEKSFORGEEKS', label: 'GeeksforGeeks' },
  { platform: 'HACKERRANK', label: 'HackerRank' },
] as const;

const getPlatformDisplayName = (platform: string) =>
  SUPPORTED_PLATFORMS.find((item) => item.platform === platform)?.label ?? 'Coding platform';

const getSyncStatusLabel = (status: PlatformAccountResponse['syncStatus']) => {
  switch (status) {
    case 'NEVER_SYNCED': return 'Not synced yet';
    case 'RUNNING': return 'Sync in progress';
    case 'SUCCEEDED': return 'Sync completed';
    case 'FAILED': return 'Last attempt failed';
  }
};

export const CodingProfilesPage: React.FC = () => {
  const {
    accounts,
    loading,
    actionLoading,
    error,
    fetchAccounts,
    createAccount,
    updateAccount,
    deleteAccount,
    syncAccount,
  } = usePlatformAccounts();

  // Modal states
  const [isAccountModalOpen, setIsAccountModalOpen] = useState<boolean>(false);
  const [editingAccount, setEditingAccount] = useState<PlatformAccountResponse | null>(null);
  const [preferredPlatform, setPreferredPlatform] = useState<PlatformType | null>(null);

  const [statsModalAccount, setStatsModalAccount] = useState<PlatformAccountResponse | null>(null);

  const [deletingAccount, setDeletingAccount] = useState<PlatformAccountResponse | null>(null);
  const [initialSyncing, setInitialSyncing] = useState(false);
  const [refreshingAccountId, setRefreshingAccountId] = useState<number | null>(null);
  const [connectFeedback, setConnectFeedback] = useState<{ type: 'success' | 'warning' | 'error'; message: string } | null>(null);

  const existingPlatforms = accounts.map((a) => a.platform);

  const getPlatformBrandColor = (pName: string) => {
    switch (pName.toUpperCase()) {
      case 'LEETCODE':
        return '#f89f1b';
      case 'CODEFORCES':
        return '#1f8acb';
      case 'CODECHEF':
        return '#8b4513';
      case 'GEEKSFORGEEKS':
        return '#2f8d46';
      case 'HACKERRANK':
        return '#2ec866';
      default:
        return 'var(--color-primary)';
    }
  };

  const formatDate = (isoString: string) => {
    if (!isoString) return '—';
    try {
      const d = new Date(isoString);
      return isNaN(d.getTime()) ? isoString : d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    } catch {
      return isoString;
    }
  };

  const handleOpenCreate = (platform: PlatformType | null = null) => {
    setConnectFeedback(null);
    setEditingAccount(null);
    setPreferredPlatform(platform);
    setIsAccountModalOpen(true);
  };

  const handleOpenEdit = (acc: PlatformAccountResponse) => {
    setConnectFeedback(null);
    setEditingAccount(acc);
    setIsAccountModalOpen(true);
  };

  const handleSubmitAccount = async (data: PlatformAccountRequest) => {
    if (editingAccount) {
      await updateAccount(editingAccount.id, data);
      setConnectFeedback({ type: 'success', message: `${getPlatformDisplayName(editingAccount.platform)} account updated.` });
    } else {
      setConnectFeedback(null);
      const account = await createAccount(data);
      if (account.sourceStatus === 'SOURCE_PENDING') {
        setConnectFeedback({
          type: 'warning',
          message: `${getPlatformDisplayName(account.platform)} connected. Live statistics are currently unavailable.`,
        });
        return;
      }
      setInitialSyncing(true);
      try {
        await handleRefreshStats(account, true);
      } finally {
        setInitialSyncing(false);
      }
    }
  };

  const handleRefreshStats = async (account: PlatformAccountResponse, justConnected = false) => {
    if (account.sourceStatus === 'SOURCE_PENDING') return;
    setConnectFeedback(null);
    setRefreshingAccountId(account.id);
    try {
      const result = await syncAccount(account.id);
      const feedback = getDashboardRefreshFeedback(result);
      setConnectFeedback({
        type: feedback.success ? 'success' : result.syncStatus.status === 'FAILED' ? 'error' : 'warning',
        message: `${justConnected ? `${getPlatformDisplayName(account.platform)} connected. ` : ''}${feedback.message}`,
      });
    } catch {
      // A failed refresh must not make an already-created account look unsaved.
      setConnectFeedback({
        type: 'error',
        message: `${justConnected ? `${getPlatformDisplayName(account.platform)} connected, but ` : ''}stats could not be refreshed. Try again shortly.`,
      });
    } finally {
      setRefreshingAccountId(null);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deletingAccount) return;
    try {
      await deleteAccount(deletingAccount.id);
      setConnectFeedback({ type: 'success', message: `${getPlatformDisplayName(deletingAccount.platform)} account disconnected.` });
      setDeletingAccount(null);
    } catch {
      setConnectFeedback({ type: 'error', message: 'We could not disconnect this account. Please try again.' });
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <AppNavbar />

      <main
        style={{
          flex: 1,
          maxWidth: '1280px',
          width: '100%',
          margin: '0 auto',
          padding: 'clamp(1rem, 3vw, 2rem) clamp(1rem, 3vw, 1.5rem)',
          display: 'flex',
          flexDirection: 'column',
          gap: '2rem',
        }}
      >
        {/* Top Header / Action Bar */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, letterSpacing: '-0.02em' }}>
              Coding Platforms
            </h1>
            <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', marginTop: '0.25rem' }}>
              Add your handles and review which platforms provide live statistics. Handles are self-reported and unverified.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div className="badge badge-muted">
              <span>{accounts.length} / {SUPPORTED_PLATFORMS.length} Platforms Connected</span>
            </div>

            <button
              onClick={() => handleOpenCreate()}
              className="btn btn-primary"
              disabled={loading || accounts.length >= 5}
              title={accounts.length >= 5 ? 'All 5 supported platforms are connected' : 'Connect a new platform'}
            >
              <PlusCircle size={16} />
              <span>Connect Platform</span>
            </button>
          </div>
        </div>

        {/* Loading Skeletons */}
        {loading && accounts.length === 0 && (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
              gap: '1.5rem',
            }}
          >
            {[1, 2, 3].map((i) => (
              <div key={i} className="card skeleton" style={{ height: '220px' }} />
            ))}
          </div>
        )}

        {/* Global Error */}
        {error && (
          <div className="alert alert-error" role="alert">
            <AlertCircle size={18} />
            <span style={{ flex: 1 }}>{error}{accounts.length > 0 ? ' Your saved account list is still shown below.' : ''}</span>
            <button onClick={() => fetchAccounts()} className="btn btn-ghost" style={{ padding: '0.25rem 0.5rem' }}>
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        )}

        {connectFeedback && (
          <div className={`alert alert-${connectFeedback.type}`} role={connectFeedback.type === 'error' ? 'alert' : 'status'} aria-live={connectFeedback.type === 'error' ? 'assertive' : 'polite'}>
            {connectFeedback.message}
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && accounts.length === 0 && (
          <div
            className="card"
            style={{
              padding: '4rem 2rem',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '1.25rem',
              maxWidth: '560px',
              margin: '2rem auto',
            }}
          >
            <div
              style={{
                width: '56px',
                height: '56px',
                borderRadius: 'var(--radius-lg)',
                backgroundColor: 'var(--color-primary-soft)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--color-primary)',
              }}
            >
              <Layers size={28} />
            </div>
            <div>
              <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>
                No coding platforms connected yet
              </h2>
              <p style={{ color: 'var(--color-text-secondary)', fontSize: '0.9375rem', lineHeight: 1.5 }}>
                Link a supported coding profile. Live statistics depend on each platform's available data source and may not be available for every platform.
              </p>
            </div>
            <button
              onClick={() => handleOpenCreate()}
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
            >
              <PlusCircle size={18} /> Connect Platform
            </button>
          </div>
        )}

        {(!loading || accounts.length > 0) && !error && (
          <section aria-label="Supported coding platforms">
            <h2 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: '0.75rem' }}>Supported Platforms</h2>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 190px), 1fr))', gap: '0.75rem' }}>
              {SUPPORTED_PLATFORMS.map(({ platform, label }) => {
                const linked = accounts.find((account) => account.platform === platform);
                const availabilityLabel = getPlatformAvailabilityLabel(platform, linked);
                return (
                  <div key={platform} className="card platform-summary-card" style={{ padding: '1rem', borderTop: `3px solid ${getPlatformBrandColor(platform)}` }}>
                    <div style={{ fontWeight: 700 }}>{label}</div>
                    <div style={{ marginTop: '0.35rem', color: 'var(--color-text-secondary)', fontSize: '0.8125rem' }}>
                      {linked ? <span className="platform-account-identity">Connected · @{linked.username}</span> : 'Not connected'}
                    </div>
                    <div style={{ marginTop: '0.25rem', color: 'var(--color-text-muted)', fontSize: '0.75rem' }}>
                      {availabilityLabel}
                    </div>
                    {linked?.lastSuccessAt && <div style={{ marginTop: '0.2rem', color: 'var(--color-text-muted)', fontSize: '0.6875rem' }}>Last successful sync · {formatDate(linked.lastSuccessAt)}</div>}
                    <button
                      className="btn btn-secondary"
                      style={{ marginTop: '0.75rem', width: '100%', padding: '0.4rem 0.6rem', fontSize: '0.75rem' }}
                      onClick={() => linked ? setStatsModalAccount(linked) : handleOpenCreate(platform)}
                      aria-label={linked ? `View ${label} account stats` : `Connect ${label}`}
                    >
                      {linked ? 'View account stats' : 'Connect'}
                    </button>
                  </div>
                );
              })}
            </div>
          </section>
        )}

        {/* Account Cards Grid */}
        {accounts.length > 0 && (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 320px), 1fr))',
              gap: '1.5rem',
            }}
          >
            {accounts.map((acc) => {
              const brandColor = getPlatformBrandColor(acc.platform);
              return (
                <div
                  key={acc.id}
                  className="card platform-account-card"
                  style={{
                    position: 'relative',
                    overflow: 'hidden',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                  }}
                >
                  <div style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '3px', backgroundColor: brandColor }} />

                  <div>
                    {/* Header: Platform & Verification */}
                    <div className="platform-account-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                      <div className="platform-account-heading">
                        <h3 style={{ fontSize: '1.25rem', fontWeight: 700, overflowWrap: 'anywhere' }}>{getPlatformDisplayName(acc.platform)}</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginTop: '0.25rem' }}>
                          <span className="mono" style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
                            <span className="platform-account-identity">@{acc.username}</span>
                          </span>
                          {acc.profileUrl && (
                            <a
                              href={acc.profileUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              style={{ color: 'var(--color-text-muted)', display: 'inline-flex', alignItems: 'center' }}
                              title="External Profile"
                              aria-label={`Open ${acc.platform} profile in a new tab`}
                            >
                              <ExternalLink size={12} />
                            </a>
                          )}
                        </div>
                      </div>

                      <div className="platform-account-verification">
                        <span className="badge badge-muted" title="Platform handle ownership has not been verified">
                          <Clock size={12} /> Self-reported · Unverified
                        </span>
                      </div>
                    </div>

                    {/* Metadata */}
                    <div
                      className="platform-account-metadata"
                      style={{
                        backgroundColor: 'var(--color-bg-base)',
                        padding: '0.75rem 1rem',
                        borderRadius: 'var(--radius-md)',
                        border: '1px solid var(--color-border-subtle)',
                        fontSize: '0.8125rem',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '0.375rem',
                        marginBottom: '1.25rem',
                      }}
                    >
                      <div className="platform-account-meta-row">
                        <span style={{ color: 'var(--color-text-muted)' }}>Connected Since</span>
                        <span className="mono" style={{ color: 'var(--color-text-primary)' }}>{formatDate(acc.connectedAt)}</span>
                      </div>
                      {acc.profileUrl && (
                        <div className="platform-account-meta-row">
                          <span style={{ color: 'var(--color-text-muted)' }}>Profile URL</span>
                          <span
                            className="mono"
                            style={{
                              color: 'var(--color-text-secondary)',
                              maxWidth: '180px',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {acc.profileUrl}
                          </span>
                        </div>
                      )}
                      <div className="platform-account-meta-row">
                        <span style={{ color: 'var(--color-text-muted)' }}>Statistics</span>
                        <span className="platform-account-status" style={{ color: 'var(--color-text-primary)' }}>
                          {getPlatformAvailabilityLabel(acc.platform, acc)}
                        </span>
                      </div>
                      {acc.sourceStatus !== 'SOURCE_PENDING' && (
                        <div className="platform-account-meta-row">
                          <span style={{ color: 'var(--color-text-muted)' }}>Sync Status</span>
                          <span className="mono" style={{ color: acc.syncStatus === 'FAILED' ? 'var(--color-error)' : 'var(--color-text-primary)' }}>
                            {getSyncStatusLabel(acc.syncStatus)}{acc.lastSuccessAt ? ` · Last successful sync ${formatDate(acc.lastSuccessAt)}` : ''}
                          </span>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Actions Bar */}
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      paddingTop: '0.875rem',
                      borderTop: '1px solid var(--color-border-subtle)',
                      gap: '0.5rem',
                    }}
                    className="platform-account-actions"
                  >
                    <button
                      onClick={() => setStatsModalAccount(acc)}
                      className="btn btn-secondary"
                      style={{ flex: 1, padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                    >
                      <BarChart2 size={13} /> Inspect Stats
                    </button>

                    {acc.sourceStatus !== 'SOURCE_PENDING' && <button
                      onClick={() => handleRefreshStats(acc)}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                      title="Refresh Stats"
                      aria-label={`Refresh ${acc.platform} stats`}
                      disabled={refreshingAccountId !== null}
                    >
                      <RefreshCw size={13} className={refreshingAccountId === acc.id ? 'spin' : ''} />
                    </button>}

                    <button
                      onClick={() => handleOpenEdit(acc)}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                      title="Edit Account"
                      aria-label={`Edit ${acc.platform} account`}
                    >
                      <Edit2 size={13} />
                    </button>

                    <button
                      onClick={() => { setConnectFeedback(null); setDeletingAccount(acc); }}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem', color: 'var(--color-error)' }}
                      title="Disconnect Account"
                      aria-label={`Disconnect ${acc.platform} account`}
                    >
                      <Trash2 size={13} />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </main>

      {/* Account Create/Edit Modal */}
      <PlatformAccountModal
        isOpen={isAccountModalOpen}
        initialAccount={editingAccount}
        existingPlatforms={existingPlatforms}
        preferredPlatform={preferredPlatform}
        isLoading={actionLoading || initialSyncing}
        onClose={() => { setIsAccountModalOpen(false); setPreferredPlatform(null); }}
        onSubmit={handleSubmitAccount}
      />

      {/* Platform Stats Modal */}
      <PlatformStatsModal
        isOpen={!!statsModalAccount}
        accountId={statsModalAccount?.id ?? null}
        platformName={statsModalAccount?.platform || ''}
        username={statsModalAccount?.username || ''}
        onClose={() => setStatsModalAccount(null)}
      />

      {/* Disconnect Confirmation Modal */}
      <ConfirmModal
        isOpen={!!deletingAccount}
        title="Disconnect Platform"
        message={`Are you sure you want to disconnect ${deletingAccount?.platform} (@${deletingAccount?.username})? Its statistics will no longer be aggregated in your dashboard.`}
        confirmLabel="Disconnect"
        cancelLabel="Cancel"
        isDestructive={true}
        isLoading={actionLoading}
        onConfirm={handleConfirmDelete}
        onClose={() => { if (!actionLoading) setDeletingAccount(null); }}
      />
    </div>
  );
};
