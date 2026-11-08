import { Link } from "@tanstack/react-router";

export function Header() {
  return (
    <header className="border-b border-gray-200 bg-white">
      <div className="mx-auto flex min-h-16 max-w-6xl flex-wrap items-center justify-between gap-3 px-6 py-3">
        <div className="flex items-center gap-4 sm:gap-8">
          <Link
            to="/"
            className="text-xl font-bold text-black no-underline"
          >
            UMCine
          </Link>

          <nav className="flex items-center gap-4 text-sm sm:gap-6">
            <Link
              to="/"
              className="text-black no-underline"
            >
              영화
            </Link>

            <Link
              to="/search"
              className="text-black no-underline"
            >
              검색
            </Link>
          </nav>
        </div>

        <div className="flex items-center gap-3">
          <Link
            to="/search"
            className="flex h-9 w-9 items-center justify-center rounded-md border border-gray-200 text-black no-underline"
          >
            🔍
          </Link>

          <Link to="/my" className="text-sm text-gray-700 no-underline">내 정보</Link>
          <Link
            to="/login"
            className="rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white no-underline"
          >
            로그인
          </Link>
        </div>
      </div>
    </header>
  );
}
