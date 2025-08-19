import { Github } from "lucide-react";

export default function Page() {
	return (
		<div className="w-full h-full bg-gray-700 flex items-center justify-center">
			<div className="flex flex-col justify-center max-w-xs p-6 shadow-md rounded-xl sm:px-12 bg-gray-900 text-gray-100">
				<div className="space-y-4 text-center divide-y divide-gray-700">
					<div className="my-2 space-y-1">
						<h1 className="text-xl font-semibold sm:text-2xl">
							NextJS Boilerplate
						</h1>
						<p className="px-5 text-xs sm:text-base text-gray-400">
							Edit `page.tsx` to begin
						</p>
					</div>
					<div className="flex justify-center pt-2 space-x-4 align-center">
						<a
							href="https://github.com/littlehorse-enterprises/frontend-boilerplate"
							aria-label="GitHub"
							className="p-2 rounded-md text-gray-100 hover:text-violet-400"
						>
							<Github className="size-8" />
						</a>
					</div>
				</div>
			</div>
		</div>
	);
}
