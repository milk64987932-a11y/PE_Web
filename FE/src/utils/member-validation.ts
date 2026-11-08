export const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[A-Za-z0-9!@#$%^&*]{8,}$/;

export function emailError(value: string): string {
  if (!value.trim()) return "이메일을 입력해 주세요.";
  return EMAIL_REGEX.test(value.trim()) && value.trim().length <= 254
    ? "" : "올바른 이메일 주소를 입력해 주세요. (최대 254자)";
}

export function nicknameError(value: string): string {
  const length = value.trim().length;
  return length >= 2 && length <= 12 ? "" : "닉네임은 앞뒤 공백을 제외하고 2~12자로 입력해 주세요.";
}

export function passwordError(value: string): string {
  return PASSWORD_REGEX.test(value) && value.length <= 72
    ? "" : "비밀번호는 영문 대·소문자, 숫자, 특수문자(!@#$%^&*)를 모두 포함한 8~72자로 입력해 주세요.";
}
