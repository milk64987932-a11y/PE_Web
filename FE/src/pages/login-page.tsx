import { useState, type SubmitEvent } from "react";
import { Link } from "@tanstack/react-router";
import { buttonClass, inputClass, linkClass, MemberForm } from "../components/member-form";
import { emailError } from "../utils/member-validation";

export function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const invalidEmail = email ? emailError(email) : "";
  function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    if (emailError(email) || !password) return;
    setMessage("현재는 로그인 화면 실습 단계입니다. 실제 로그인은 처리되지 않았습니다. 로그인 API는 8주차에 연결합니다.");
    setPassword("");
  }
  return <MemberForm title="로그인">
    <p className="mb-6 text-sm text-gray-500">7주차 화면 실습입니다. 실제 로그인 기능은 아직 연결되지 않았습니다.</p>
    <form onSubmit={submit} className="space-y-5" noValidate>
      <div>
        <label htmlFor="login-email" className="mb-2 block text-sm font-semibold">이메일</label>
        <input id="login-email" className={inputClass} type="email" autoComplete="email" value={email} required
          aria-invalid={Boolean(invalidEmail)} aria-describedby="login-email-error"
          onChange={event => { setEmail(event.target.value); setMessage(""); }} />
        <p id="login-email-error" className="mt-1 text-sm text-red-600" aria-live="polite">{invalidEmail}</p>
      </div>
      <div>
        <label htmlFor="login-password" className="mb-2 block text-sm font-semibold">비밀번호</label>
        <input id="login-password" className={inputClass} type="password" autoComplete="current-password" value={password} required
          onChange={event => { setPassword(event.target.value); setMessage(""); }} />
      </div>
      <button className={`${buttonClass} w-full`} disabled={Boolean(emailError(email)) || !password}>로그인</button>
    </form>
    {message && <p role="status" className="mt-4 text-sm text-gray-600">{message}</p>}
    <p className="mt-6 text-sm text-gray-600">계정이 없나요? <Link to="/signup" className={linkClass}>회원가입</Link></p>
  </MemberForm>;
}
