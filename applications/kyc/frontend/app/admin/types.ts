export const VERIFICATIONS_URL =
  process.env.VERIFICATIONS_URL || "http://localhost:8080";

export type Verification = {
  id: string;
  firstname: string;
  lastname: string;
  email: string;
  wfRunId: string;
};
