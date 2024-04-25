import { cookies } from "next/headers";
import { Login } from "./components/Login";
import { Shop } from "./components/Shop";
import { Notification } from "./components/Notification";

export default function Index() {
  const email = cookies().get("email")?.value;
  if (!email) return <Login />;
  return <>
    <Shop />
    <Notification />
  </>
}
