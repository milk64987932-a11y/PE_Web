import { Link, useParams } from "@tanstack/react-router";
import { getMovieDetail } from "../../api/movies/get-movie-detail";
import { toMovie } from "../../api/movies/models";
import { useQuery } from "@tanstack/react-query";
import { getMovieDetailErrorMessage } from "../../utils/movies/get-movie-detail-error-message";
import { MovieImage } from "../../components/movies/movie-image";
import { BookmarkButton } from "../../components/bookmark-button";

export function MovieDetailPage() {
  const { movieId: movieIdParam } = useParams({ from: "/movies/$movieId" });
  const movieId = Number(movieIdParam);
  const isValidMovieId = /^[1-9]\d*$/.test(movieIdParam) && Number.isSafeInteger(movieId);
  const movieDetailQuery = useQuery({
    queryKey: ["movies", "detail", movieId],
    queryFn: ({ signal }) => getMovieDetail(String(movieId), signal),
    enabled: isValidMovieId,
    staleTime: 30 * 1000,
  });
  const { data, isPending: loading } = movieDetailQuery;
  if (!isValidMovieId) return <main className="mx-auto max-w-6xl px-6 py-10"><p role="alert">올바르지 않은 영화 번호예요. 0보다 큰 정수 ID를 입력해 주세요.</p><Link to="/">영화 목록으로</Link></main>;
  if (loading) return <main className="mx-auto max-w-6xl px-6 py-10" role="status">영화 정보를 불러오는 중이에요.</main>;
  if (movieDetailQuery.isError) return <main className="mx-auto max-w-6xl px-6 py-10"><p role="alert">{getMovieDetailErrorMessage(movieDetailQuery.error)}</p><Link to="/">영화 목록으로</Link></main>;
  const movie = data ? toMovie(data) : undefined;

  if (!movie) {
    return <main>영화를 찾을 수 없어요.</main>;
  }

 return (
  <main className="bg-[#f7f8fa]">
    <section className="relative h-[360px] overflow-hidden">
      <MovieImage
        src={movie.backdropPath}
        alt=""
        aria-hidden="true"
        className="h-full w-full object-cover"
      />
      <div className="pointer-events-none absolute inset-0 bg-black/20" />
    </section>

    <section className="mx-auto max-w-6xl px-6 py-10">
      <Link
        to="/"
        className="mb-6 inline-block text-sm font-semibold text-blue-600"
      >
        ← 영화 목록
      </Link>

      <div className="flex flex-col gap-8 sm:flex-row">
        <div className="relative w-[220px] shrink-0 self-start">
          <MovieImage
            src={movie.posterPath}
            alt={`${movie.title} 포스터`}
            className="w-[220px] rounded-xl object-cover"
          />
          <BookmarkButton movieId={movie.id} />
        </div>

        <div className="flex-1">
          <h1 className="mb-2 text-4xl font-bold">{movie.title}</h1>

          <p className="mb-1 text-gray-500">{movie.originalTitle}</p>
          <p className="mb-1 text-gray-500">{movie.releaseDate}</p>
          <p className="mb-1 text-gray-500">
            {movie.genres.join(" · ")}
          </p>
          <p className="mb-6 text-gray-500">{movie.runtime}</p>

          <h2 className="mb-3 text-2xl font-bold">{movie.tagline}</h2>

          <p className="max-w-3xl leading-7 text-gray-700">
            {movie.overview}
          </p>
        </div>
      </div>
    </section>
  </main>
);
}
