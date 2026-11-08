import ky, { HTTPError } from "ky";
import { API_BASE_URL } from "../config/env";

const memberClient = ky.create({ baseUrl: API_BASE_URL, timeout: 15000, retry: 0 });

export async function checkAvailability(field: "email" | "nickname", value: string) {
  const result = await memberClient.get(`members/${field}/${encodeURIComponent(value.trim())}`).json<{ available: boolean }>();
  if (typeof result.available !== "boolean") throw new Error("응답 형식이 올바르지 않습니다.");
  return result.available;
}

export async function signUp(member: { email: string; nickname: string; password: string }) {
  await memberClient.post("members", { json: member });
}

export function memberRequestError(error: unknown): string {
  if (error instanceof HTTPError) {
    if (error.response.status === 409) return "이미 사용 중인 이메일 또는 닉네임입니다. 다시 중복 확인해 주세요.";
    if (error.response.status === 400) return "입력값을 서버에서 확인하지 못했어요. 입력 조건을 확인해 주세요.";
    return `요청에 실패했어요. 잠시 후 다시 시도해 주세요. (HTTP ${error.response.status})`;
  }
  return "서버에 연결하지 못했거나 응답을 확인할 수 없어요. 서버 실행 상태와 API 주소를 확인해 주세요.";
}
