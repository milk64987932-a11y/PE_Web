import { createFileRoute } from "@tanstack/react-router";
import { ProfilePage } from "../pages/profile-pages";
export const Route = createFileRoute("/my/")({ component: ProfilePage });
