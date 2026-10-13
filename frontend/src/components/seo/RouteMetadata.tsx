import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { PUBLIC_PROFILE_SEO_EVENT, PublicProfileSeoDetail } from './publicProfileSeo';

const SITE_ORIGIN = 'https://kodbtw.vercel.app';
const HOME_TITLE = 'KodBTW | Coding Profiles and Progress Analytics';
const HOME_DESCRIPTION = 'Bring live LeetCode and Codeforces statistics into one coding profile, follow persisted progress and analytics, and share your public KodBTW profile.';

const privateRouteTitles: Record<string, string> = {
  '/dashboard': 'Dashboard',
  '/analytics': 'Analytics',
  '/contests': 'Contests',
  '/leaderboard': 'Leaderboard',
  '/profile': 'Account Profile',
  '/coding-profiles': 'Coding Profiles',
};

function setMeta(attribute: 'name' | 'property', key: string, content: string): void {
  let element = Array.from(document.head.querySelectorAll('meta'))
    .find((candidate) => candidate.getAttribute(attribute) === key);

  if (!element) {
    element = document.createElement('meta');
    element.setAttribute(attribute, key);
    document.head.appendChild(element);
  }

  element.setAttribute('content', content);
}

function setCanonical(href: string | null): void {
  let element = document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]');
  if (!href) {
    element?.remove();
    return;
  }

  if (!element) {
    element = document.createElement('link');
    element.rel = 'canonical';
    document.head.appendChild(element);
  }
  element.href = href;
}

export function RouteMetadata(): null {
  const { pathname } = useLocation();

  useEffect(() => {
    const applyProfileMetadata = (event: Event) => {
      const detail = (event as CustomEvent<PublicProfileSeoDetail>).detail;
      if (!detail?.username) return;

      const displayName = detail.displayName?.trim() || detail.username;
      const title = `${displayName} | Public Coding Profile | KodBTW`;
      const description = `View ${displayName}'s public coding profile on KodBTW, including available coding-platform statistics and progress.`;
      const canonical = `${SITE_ORIGIN}/u/${encodeURIComponent(detail.username)}`;

      document.title = title;
      setMeta('name', 'description', description);
      setMeta('name', 'robots', detail.indexable ? 'index,follow' : 'noindex,follow');
      setMeta('property', 'og:title', title);
      setMeta('property', 'og:description', description);
      setMeta('property', 'og:url', canonical);
      setMeta('property', 'og:type', 'profile');
      setMeta('name', 'twitter:title', title);
      setMeta('name', 'twitter:description', description);
      setCanonical(canonical);
    };

    window.addEventListener(PUBLIC_PROFILE_SEO_EVENT, applyProfileMetadata);
    return () => window.removeEventListener(PUBLIC_PROFILE_SEO_EVENT, applyProfileMetadata);
  }, []);

  useEffect(() => {
    const profileMatch = pathname.match(/^\/u\/([^/]+)\/?$/);
    const privateTitle = privateRouteTitles[pathname];
    const isPrivate = Boolean(privateTitle);
    const isAuth = pathname === '/login' || pathname === '/register';
    let title = HOME_TITLE;
    let description = HOME_DESCRIPTION;
    let canonical: string | null = `${SITE_ORIGIN}/`;

    if (pathname === '/login') {
      title = 'Sign In | KodBTW';
      description = 'Sign in to manage your KodBTW coding profile and progress.';
      canonical = null;
    } else if (pathname === '/register') {
      title = 'Create Your Profile | KodBTW';
      description = 'Create a KodBTW profile to organize supported coding-platform statistics and share your developer profile.';
      canonical = null;
    } else if (privateTitle) {
      title = `${privateTitle} | KodBTW`;
      description = 'Private account area on KodBTW.';
      canonical = null;
    } else if (profileMatch) {
      let username = profileMatch[1];
      try {
        username = decodeURIComponent(username);
      } catch {
        // Keep the encoded path segment readable if it contains malformed escapes.
      }
      title = `${username} | Public Coding Profile | KodBTW`;
      description = `View ${username}'s public coding profile on KodBTW, including available coding-platform statistics and progress.`;
      canonical = null;
    }

    document.title = title;
    setMeta('name', 'description', description);
    setMeta('name', 'robots', isPrivate || isAuth || profileMatch ? 'noindex,follow' : 'index,follow');
    setMeta('property', 'og:title', title);
    setMeta('property', 'og:description', description);
    setMeta('property', 'og:url', canonical ?? `${SITE_ORIGIN}${pathname}`);
    setMeta('name', 'twitter:title', title);
    setMeta('name', 'twitter:description', description);
    setMeta('property', 'og:type', profileMatch ? 'profile' : 'website');
    setCanonical(canonical);
  }, [pathname]);

  return null;
}
