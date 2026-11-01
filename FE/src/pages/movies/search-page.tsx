import { Link, useNavigate, useSearch } from "@tanstack/react-router";
import { useState, type SubmitEvent } from "react";
import { searchMovies } from "../../api/movies/search-movies";
import { toMovie } from "../../api/movies/models";
import { useQuery } from "@tanstack/react-query";
import { getMovieDetailErrorMessage } from "../../utils/movies/get-movie-detail-error-message";
import { MovieImage } from "../../components/movies/movie-image";
import { BookmarkButton } from "../../components/bookmark-button";

export function SearchPage() {
  const { query } = useSearch({ from: "/search" });
  const navigate = useNavigate({ from: "/search" });
  const [searchText, setSearchText] = useState(query ?? "");
  const [previousQuery, setPreviousQuery] = useState(query);

  if (query !== previousQuery) {
    setPreviousQuery(query);
    setSearchText(query ?? "");
  }

  const normalizedQuery = query?.trim() ?? "";
  const searchQuery = useQuery({
    queryKey: ["movies", "search", { query: normalizedQuery }],
    queryFn: ({ signal }) => searchMovies(normalizedQuery, signal),
    enabled: normalizedQuery.length > 0,
  });
  const { data, isLoading: loading } = searchQuery;
  const error = searchQuery.isError ? getMovieDetailErrorMessage(searchQuery.error) : undefined;
  const searchResults = data?.results.map(toMovie) ?? [];
  function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextQuery = searchText.trim();
    navigate({
      search: nextQuery ? { query: nextQuery } : {},
    });
  }

  return (
  <main className="mx-auto max-w-6xl px-6 py-10">
    <h1 className="mb-6 text-3xl font-bold">영화 검색</h1>

    <form onSubmit={handleSubmit} className="mb-8 flex gap-3">
      <input
        aria-label="검색어"
        value={searchText}
        onChange={(event) => setSearchText(event.target.value)}
        className="min-w-0 flex-1 rounded-md border border-gray-300 px-4 py-3 outline-none focus:border-blue-500"
      />

      <button
        type="submit"
        className="rounded-md bg-blue-600 px-5 py-3 font-semibold text-white"
      >
        검색
      </button>
    </form>

    {!normalizedQuery ? (
      <p className="text-gray-500">검색어를 입력해 주세요.</p>
    ) : (
      <>
        <h2 className="mb-2 text-2xl font-bold">
          ‘{query}’ 검색 결과
        </h2>

        <p className="mb-6 text-gray-500">
          영화 {data?.total_results ?? 0}편
        </p>

        {loading ? <p role="status">검색 중이에요.</p> : error ? <p role="alert">{error}</p> : searchResults.length === 0 ? (
          <p className="text-gray-500">검색 결과가 없어요.</p>
        ) : (
          <ul className="space-y-6">
            {searchResults.map((movie) => (
              <li
                key={movie.id}
                className="flex gap-5 rounded-xl bg-white p-4 shadow-sm"
              >
                <div className="relative w-32 shrink-0 self-start">
                  <MovieImage
                    src={movie.posterPath}
                    alt={`${movie.title} 포스터`}
                    className="w-32 rounded-lg object-cover"
                  />
                  <BookmarkButton movieId={movie.id} />
                </div>

                <div>
                  <h3 className="text-xl font-bold">{movie.title}</h3>
                  <p className="text-sm text-gray-500">
                    {movie.originalTitle}
                  </p>
                  <p className="text-sm text-gray-500">
                    {movie.releaseDate}
                  </p>
                  <p className="mt-2 text-sm leading-6 text-gray-700">
                    {movie.overview}
                  </p>

                  <Link
                    to="/movies/$movieId"
                    params={{ movieId: String(movie.id) }}
                    className="mt-3 inline-block text-sm font-semibold text-blue-600"
                  >
                    상세 보기
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        )}
      </>
    )}
  </main>
);
}
