import { Button } from "./ui/button";

const keys: (number | "🔄" | "✔️")[] = [
	1,
	2,
	3,
	4,
	5,
	6,
	7,
	8,
	9,
	"🔄",
	0,
	"✔️",
];

export default function Keypad({
	loading,
	onClick,
}: {
	loading: boolean;
	onClick: (key: number | "🔄" | "✔️") => void;
}) {
	return (
		<div className="grid grid-cols-3 gap-2 place-items-center">
			{keys.map((key) => (
				<Button
					key={key}
					size="icon"
					className="text-2xl"
					onClick={() => onClick(key)}
					disabled={loading}
				>
					{key}
				</Button>
			))}
		</div>
	);
}
