import { createContext, useContext } from "react";

export type Profile = { nickname: string; email: string };
export const ProfileContext = createContext<{ profile: Profile; updateProfile: (profile: Profile) => void } | null>(null);
export function useProfile() {
  const context = useContext(ProfileContext);
  if (!context) throw new Error("내 정보 화면 안에서 사용해 주세요.");
  return context;
}
