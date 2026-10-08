import React, { useState } from 'react';
import { AppNavbar } from '../components/layout/AppNavbar';
import { usePlatformAccounts } from '../hooks/usePlatformAccounts';
import { PlatformAccountModal } from '../components/platform/PlatformAccountModal';
import { PlatformStatsModal } from '../components/platform/PlatformStatsModal';
import { ConfirmModal } from '../components/common/ConfirmModal';
import { PlatformAccountResponse, PlatformAccountRequest } from '../types/platform';
import {
  PlusCircle,
  ExternalLink,
  CheckCircle2,
  Clock,
  BarChart2,
  Edit2,
  Trash2,
  Layers,
  AlertCircle,
  RefreshCw,
} from 'lucide-react';

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

  const [statsModalAccount, setStatsModalAccount] = useState<PlatformAccountResponse | null>(null);

  const [deletingAccount, setDeletingAccount] = useState<PlatformAccountResponse | null>(null);
  const [initialSyncing, setInitialSyncing] = useState(false);
  const [refreshingAccountId, setRefreshingAccountId] = useState<number | null>(null);
  const [connectFeedback, setConnectFeedback] = useState<{ type: 'success' | 'error'; message: string } | null>(null);

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

  const handleOpenCreate = () => {
    setEditingAccount(null);
    setIsAccountModalOpen(true);
  };

  const handleOpenEdit = (acc: PlatformAccountResponse) => {
    setEditingAccount(acc);
    setIsAccountModalOpen(true);
  };

  const handleSubmitAccount = async (data: PlatformAccountRequest) => {
    if (editingAccount) {
      await updateAccount(editingAccount.id, data);
    } else {
      setConnectFeedback(null);
      const account = await createAccount(data);
      setInitialSyncing(true);
      await handleRefreshStats(account, true);
      setInitialSyncing(false);
    }
  };

  const handleRefreshStats = async (account: PlatformAccountResponse, justConnected = false) => {
    setConnectFeedback(null);
    setRefreshingAccountId(account.id);
    try {
      const result = await syncAccount(account.id);
      if (result.syncStatus.status === 'SUCCEEDED') {
        setConnectFeedback({
          type: 'success',
          message: justConnected
            ? `${account.platform} connected and stats refreshed.`
            : `${account.platform} stats refreshed.`,
        });
      } else {
        setConnectFeedback({
          type: 'error',
          message: `${justConnected ? `${account.platform} connected, but ` : ''}stats could not be refreshed. ${result.syncStatus.failureMessage || 'Try again shortly.'}`,
        });
      }
    } catch {
      // A failed refresh must not make an already-created account look unsaved.
      setConnectFeedback({
        type: 'error',
        message: `${justConnected ? `${account.platform} connected, but ` : ''}stats could not be refreshed. Try again shortly.`,
      });
    } finally {
      setRefreshingAccountId(null);
    }
  };

  const handleConfirmDelete = async () => {
    if (!deletingAccount) return;
    try {
      await deleteAccount(deletingAccount.id);
      setDeletingAccount(null);
    } catch (err) {
      // Handled in hook or alert
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
          padding: '2rem 1.5rem',
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
              Connect and manage your competitive programming profiles
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <div className="badge badge-muted">
              <span>{accounts.length} / 5 Platforms Connected</span>
            </div>

            <button
              onClick={handleOpenCreate}
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
        {loading && (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
              gap: '1.5rem',
            }}
          >
            {[1, 2, 3].map((i) => (
              <div key={i} className="card skeleton" style={{ height: '220px' }} />
            ))}
          </div>
        )}

        {/* Global Error */}
        {!loading && error && (
          <div className="alert alert-error">
            <AlertCircle size={18} />
            <span style={{ flex: 1 }}>{error}</span>
            <button onClick={() => fetchAccounts()} className="btn btn-ghost" style={{ padding: '0.25rem 0.5rem' }}>
              <RefreshCw size={14} /> Retry
            </button>
          </div>
        )}

        {connectFeedback && (
          <div className={`alert alert-${connectFeedback.type}`} role="status">
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
                backgroundColor: 'rgba(235, 115, 18, 0.1)',
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
                Link your LeetCode, Codeforces, CodeChef, GeeksforGeeks, or HackerRank accounts to sync problems, ratings, and streaks into your unified dashboard.
              </p>
            </div>
            <button
              onClick={handleOpenCreate}
              className="btn btn-primary"
              style={{ padding: '0.75rem 1.5rem', fontSize: '0.9375rem' }}
            >
              <PlusCircle size={18} /> Connect Platform
            </button>
          </div>
        )}

        {/* Account Cards Grid */}
        {!loading && !error && accounts.length > 0 && (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
              gap: '1.5rem',
            }}
          >
            {accounts.map((acc) => {
              const brandColor = getPlatformBrandColor(acc.platform);
              return (
                <div
                  key={acc.id}
                  className="card"
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
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                      <div>
                        <h3 style={{ fontSize: '1.25rem', fontWeight: 700 }}>{acc.platform}</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.375rem', marginTop: '0.25rem' }}>
                          <span className="mono" style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)' }}>
                            @{acc.username}
                          </span>
                          {acc.profileUrl && (
                            <a
                              href={acc.profileUrl}
                              target="_blank"
                              rel="noopener noreferrer"
                              style={{ color: 'var(--color-text-muted)', display: 'inline-flex', alignItems: 'center' }}
                              title="External Profile"
                            >
                              <ExternalLink size={12} />
                            </a>
                          )}
                        </div>
                      </div>

                      <div>
                        {acc.verified ? (
                          <span className="badge badge-success">
                            <CheckCircle2 size={12} /> Verified
                          </span>
                        ) : (
                          <span className="badge badge-muted">
                            <Clock size={12} /> Connected
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Metadata */}
                    <div
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
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--color-text-muted)' }}>Connected Since</span>
                        <span className="mono" style={{ color: 'var(--color-text-primary)' }}>{formatDate(acc.connectedAt)}</span>
                      </div>
                      {acc.profileUrl && (
                        <div style={{ display: 'flex', justifyContent: 'space-between', overflow: 'hidden' }}>
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
                      <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'var(--color-text-muted)' }}>Sync Status</span>
                        <span className="mono" style={{ color: acc.syncStatus === 'FAILED' ? 'var(--color-error)' : 'var(--color-text-primary)' }}>
                          {acc.syncStatus.replace('_', ' ')}{acc.lastSuccessAt ? ` · ${formatDate(acc.lastSuccessAt)}` : ''}
                        </span>
                      </div>
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
                  >
                    <button
                      onClick={() => setStatsModalAccount(acc)}
                      className="btn btn-secondary"
                      style={{ flex: 1, padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                    >
                      <BarChart2 size={13} /> Inspect Stats
                    </button>

                    <button
                      onClick={() => handleRefreshStats(acc)}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                      title="Refresh Stats"
                      aria-label={`Refresh ${acc.platform} stats`}
                      disabled={refreshingAccountId !== null}
                    >
                      <RefreshCw size={13} className={refreshingAccountId === acc.id ? 'spin' : ''} />
                    </button>

                    <button
                      onClick={() => handleOpenEdit(acc)}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem' }}
                      title="Edit Account"
                    >
                      <Edit2 size={13} />
                    </button>

                    <button
                      onClick={() => setDeletingAccount(acc)}
                      className="btn btn-ghost"
                      style={{ padding: '0.4375rem 0.625rem', fontSize: '0.75rem', color: 'var(--color-error)' }}
                      title="Disconnect Account"
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
        isLoading={actionLoading || initialSyncing}
        onClose={() => setIsAccountModalOpen(false)}
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
        onClose={() => setDeletingAccount(null)}
      />
    </div>
  );
};
