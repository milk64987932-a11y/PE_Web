import { useState } from "react";
import MovieGrid from "../../components/movies/movie-grid";
import Pagination from "../../components/movies/pagination";
import { getMovies } from "../../api/movies/get-movies";
import { toMovie } from "../../api/movies/models";
import { useQuery } from "@tanstack/react-query";
import { getMovieDetailErrorMessage } from "../../utils/movies/get-movie-detail-error-message";
export function MovieListPage() {
  const [page, setPage] = useState(1);
  const moviesQuery = useQuery({
    queryKey: ["movies", "list", { page }],
    queryFn: ({ signal }) => getMovies(page, signal),
  });
  const { data, isPending: loading } = moviesQuery;
  const error = moviesQuery.isError ? getMovieDetailErrorMessage(moviesQuery.error) : undefined;
  return (
    <main className="mx-auto max-w-6xl px-6 py-10">
      <h1 className="mb-6 text-3xl font-bold">인기 영화 목록</h1>
      {loading ? <p role="status">영화를 불러오는 중이에요.</p> : error ? <p role="alert">{error}</p> :
        data?.results.length ? <MovieGrid movies={data.results.map(toMovie)} /> : <p>등록된 영화가 없어요.</p>}
      <Pagination page={page} totalPages={Math.min(data?.total_pages ?? page, 500)} loading={loading} onPageChange={setPage} />
    </main>
  );
}
