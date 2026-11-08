import { createFileRoute } from "@tanstack/react-router";
import { ProfileLayout } from "../pages/profile-pages";
export const Route = createFileRoute("/my")({ component: ProfileLayout });
