import { useRouter } from "next/navigation";

/**
 * 
 * @returns {Function} redirect function that makes it so you don't have to import the router in every component
 */
export default function useRedirect(): Function {
	const router = useRouter();
	const redirect = (path: string) => router.push(path);
	return redirect;
}