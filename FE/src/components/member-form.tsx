import type { ReactNode } from "react";

export const inputClass = "min-w-0 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-base disabled:bg-gray-100 disabled:text-gray-500";
export const buttonClass = "rounded-md bg-blue-600 px-4 py-2 text-sm font-semibold text-white disabled:cursor-not-allowed disabled:opacity-40";
export const linkClass = "text-sm font-semibold text-blue-600 underline underline-offset-4";

export function MemberForm({ title, children }: { title: string; children: ReactNode }) {
  return <main className="mx-auto max-w-lg px-6 py-10">
    <section className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm">
      <h1 className="mb-6 text-2xl font-bold">{title}</h1>
      {children}
    </section>
  </main>;
}
