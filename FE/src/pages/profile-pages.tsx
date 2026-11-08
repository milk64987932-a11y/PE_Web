import { useState, type SubmitEvent } from "react";
import { Link, Outlet, useNavigate } from "@tanstack/react-router";
import { ProfileContext, useProfile } from "../components/profile-context";
import { buttonClass, inputClass, linkClass, MemberForm } from "../components/member-form";
import { nicknameError } from "../utils/member-validation";

export function ProfileLayout() {
  const [profile, setProfile] = useState({ nickname: "UMC 영화팬", email: "movie@example.com" });
  return <ProfileContext.Provider value={{ profile, updateProfile: setProfile }}>
    <Outlet />
  </ProfileContext.Provider>;
}

export function ProfilePage() {
  const { profile } = useProfile();
  return <MemberForm title="내 정보">
    <p className="mb-6 text-sm text-gray-500">예시 회원 정보입니다. 로그인한 사용자의 정보가 아니며, 변경 내용은 이 화면을 나가면 초기화됩니다.</p>
    <dl className="space-y-5">
      <div><dt className="text-sm text-gray-500">닉네임</dt><dd className="mt-1 break-all font-semibold">{profile.nickname}</dd></div>
      <div><dt className="text-sm text-gray-500">이메일</dt><dd className="mt-1 break-all font-semibold">{profile.email}</dd></div>
    </dl>
    <Link to="/my/edit" className={`${buttonClass} mt-6 inline-block no-underline`}>내 정보 수정</Link>
  </MemberForm>;
}

export function ProfileEditPage() {
  const { profile, updateProfile } = useProfile();
  const [nickname, setNickname] = useState(profile.nickname);
  const navigate = useNavigate();
  const error = nicknameError(nickname);
  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    if (error) return;
    updateProfile({ ...profile, nickname: nickname.trim() });
    await navigate({ to: "/my" });
  }
  return <MemberForm title="내 정보 수정">
    <p className="mb-6 text-sm text-gray-500">예시 데이터의 닉네임을 수정합니다. 서버에 저장되지 않습니다.</p>
    <form onSubmit={submit} className="space-y-5" noValidate>
      <div>
        <label htmlFor="profile-nickname" className="mb-2 block text-sm font-semibold">닉네임</label>
        <input id="profile-nickname" className={inputClass} value={nickname} autoComplete="nickname" required
          aria-invalid={Boolean(error)} aria-describedby="profile-nickname-error" onChange={event => setNickname(event.target.value)} />
        <p id="profile-nickname-error" aria-live="polite" className="mt-1 text-sm text-red-600">{error}</p>
      </div>
      <p className="break-all text-sm text-gray-600">이메일: {profile.email}</p>
      <div className="flex items-center gap-4">
        <button className={buttonClass} disabled={Boolean(error)}>수정</button>
        <Link to="/my" className={linkClass}>취소</Link>
      </div>
    </form>
  </MemberForm>;
}
