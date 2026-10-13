export const PUBLIC_PROFILE_SEO_EVENT = 'kodbtw:public-profile-seo';

export interface PublicProfileSeoDetail {
  username: string;
  displayName: string | null;
  indexable: boolean;
}

export function publishPublicProfileSeo(detail: PublicProfileSeoDetail): void {
  if (typeof window === 'undefined') return;
  window.dispatchEvent(new CustomEvent<PublicProfileSeoDetail>(PUBLIC_PROFILE_SEO_EVENT, { detail }));
}
