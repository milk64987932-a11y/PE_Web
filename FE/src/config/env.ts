export const TMDB_BASE_URL = (import.meta.env.VITE_TMDB_API_BASE_URL?.trim() || "https://api.themoviedb.org/3").replace(/\/+$/, "") + "/";
export function getTmdbToken(): string {
  const token = import.meta.env.VITE_TMDB_API_READ_ACCESS_TOKEN?.trim();
  if (!token) throw new Error("TMDB 토큰이 없어요. FE/.env.local에 VITE_TMDB_API_READ_ACCESS_TOKEN을 설정해 주세요.");
  return token;
}
export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL?.trim() || "http://localhost:8080").replace(/\/+$/, "") + "/";
