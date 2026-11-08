import { useState, type SubmitEvent } from "react";
import { Link } from "@tanstack/react-router";
import { checkAvailability, memberRequestError, signUp } from "../api/member-client";
import { buttonClass, inputClass, linkClass, MemberForm } from "../components/member-form";
import { emailError, nicknameError, passwordError } from "../utils/member-validation";

type Check = "idle" | "available" | "duplicate" | "error";
type Field = "email" | "nickname" | "password" | "confirmation";

export function SignupPage() {
  const [values, setValues] = useState({ email: "", nickname: "", password: "", confirmation: "" });
  const [touched, setTouched] = useState<Partial<Record<Field, boolean>>>({});
  const [checks, setChecks] = useState<Record<"email" | "nickname", Check>>({ email: "idle", nickname: "idle" });
  const [pending, setPending] = useState<"email" | "nickname" | "signup" | null>(null);
  const [message, setMessage] = useState("");
  const [success, setSuccess] = useState(false);
  const errors = {
    email: emailError(values.email), nickname: nicknameError(values.nickname), password: passwordError(values.password),
    confirmation: !values.confirmation ? "비밀번호 확인을 입력해 주세요." : values.password !== values.confirmation ? "비밀번호가 일치하지 않아요." : "",
  };
  const canSubmit = !Object.values(errors).some(Boolean) && checks.email === "available" && checks.nickname === "available";

  function change(field: Field, value: string) {
    setValues(previous => ({ ...previous, [field]: value }));
    setTouched(previous => ({ ...previous, [field]: true }));
    if (field === "email" || field === "nickname") setChecks(previous => ({ ...previous, [field]: "idle" }));
    setMessage("");
    setSuccess(false);
  }

  async function check(field: "email" | "nickname") {
    if (pending || errors[field]) return;
    setPending(field);
    setMessage("");
    try {
      const available = await checkAvailability(field, values[field]);
      setChecks(previous => ({ ...previous, [field]: available ? "available" : "duplicate" }));
    } catch (error) {
      setChecks(previous => ({ ...previous, [field]: "error" }));
      setMessage(memberRequestError(error));
    } finally { setPending(null); }
  }

  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    if (pending || !canSubmit) return;
    setPending("signup");
    setMessage("");
    try {
      await signUp({ email: values.email.trim(), nickname: values.nickname.trim(), password: values.password });
      setSuccess(true);
      setMessage("회원가입이 완료되었어요. 로그인 화면으로 이동할 수 있습니다.");
      setValues(previous => ({ ...previous, password: "", confirmation: "" }));
      setTouched({});
    } catch (error) {
      setMessage(memberRequestError(error));
      setChecks({ email: "idle", nickname: "idle" });
    } finally { setPending(null); }
  }

  return <MemberForm title="회원가입">
    <form noValidate onSubmit={submit} aria-busy={pending !== null}>
      <fieldset disabled={pending !== null} className="space-y-5">
        {(["email", "nickname", "password", "confirmation"] as const).map(field => {
          const label = { email: "이메일", nickname: "닉네임", password: "비밀번호", confirmation: "비밀번호 확인" }[field];
          const checkField = field === "email" || field === "nickname" ? field : null;
          const error = touched[field] ? errors[field] : "";
          return <div key={field}>
            <label htmlFor={`signup-${field}`} className="mb-2 block text-sm font-semibold">{label}</label>
            <div className="flex gap-2">
              <input id={`signup-${field}`} name={field} className={inputClass} value={values[field]} required
                type={field === "password" || field === "confirmation" ? "password" : field === "email" ? "email" : "text"}
                autoComplete={field === "password" || field === "confirmation" ? "new-password" : field === "email" ? "email" : "nickname"}
                aria-invalid={Boolean(error)} aria-describedby={`signup-${field}-help`}
                onBlur={() => setTouched(previous => ({ ...previous, [field]: true }))}
                onChange={event => change(field, event.target.value)} />
              {checkField && <button type="button" className={`${buttonClass} shrink-0`} disabled={Boolean(errors[checkField]) || pending !== null}
                onClick={() => void check(checkField)} aria-label={`${label} 중복 확인`}>
                {pending === checkField ? "확인 중..." : "중복 확인"}
              </button>}
            </div>
            <div id={`signup-${field}-help`} aria-live="polite" className="mt-1 text-sm">
              {error && <p className="text-red-600">{error}</p>}
              {field === "password" && !error && <p className="text-gray-500">영문 대·소문자, 숫자, 특수문자(!@#$%^&*) 포함 8~72자</p>}
              {checkField && !error && <p className={checks[checkField] === "available" ? "text-green-700" : "text-gray-600"}>
                {checks[checkField] === "available" ? `사용 가능한 ${label}입니다.` : checks[checkField] === "duplicate" ? `이미 사용 중인 ${label}입니다.` : checks[checkField] === "error" ? "중복 확인에 실패했어요. 다시 확인해 주세요." : `${label} 중복 확인이 필요합니다.`}
              </p>}
            </div>
          </div>;
        })}
        <button className={`${buttonClass} w-full`} disabled={!canSubmit || pending !== null}>
          {pending === "signup" ? "가입 요청 중..." : "회원가입"}
        </button>
      </fieldset>
    </form>
    {message && <p role={success ? "status" : "alert"} className={`mt-4 text-sm ${success ? "text-green-700" : "text-red-600"}`}>{message}</p>}
    <p className="mt-6 text-sm text-gray-600">이미 계정이 있나요? <Link to="/login" className={linkClass}>로그인</Link></p>
  </MemberForm>;
}
