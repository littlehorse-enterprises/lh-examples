"use client";
import React, { useState, useEffect } from "react";
import axios from "axios";
import { VendingMachine } from "@/types";
import { toast } from "sonner";
import Keypad from "@/components/keypad";
import { Input } from "@/components/ui/input";

const InventoryPage = () => {
	const [inventory, setInventory] = useState<VendingMachine>({
		100: {
			name: "doritos - nacho",
			price: 1.5,
			quantity: 10,
		},
		101: {
			name: "lays - classic",
			price: 1.25,
			quantity: 15,
		},
		102: {
			name: "snickers",
			price: 1.75,
			quantity: 20,
		},
		103: {
			name: "m&m's - peanut",
			price: 1.5,
			quantity: 12,
		},
		104: {
			name: "oreos",
			price: 1.5,
			quantity: 8,
		},
		105: {
			name: "skittles",
			price: 1.25,
			quantity: 18,
		},
		106: {
			name: "kit kat",
			price: 1.5,
			quantity: 10,
		},
		107: {
			name: "coca-cola",
			price: 2.0,
			quantity: 25,
		},
		108: {
			name: "pepsi",
			price: 2.0,
			quantity: 20,
		},
		109: {
			name: "mountain dew",
			price: 2.0,
			quantity: 15,
		},
		110: {
			name: "red bull",
			price: 2.5,
			quantity: 10,
		},
		111: {
			name: "gatorade - lemon-lime",
			price: 2.0,
			quantity: 12,
		},
		112: {
			name: "bottled water",
			price: 1.0,
			quantity: 30,
		},
		113: {
			name: "cheetos",
			price: 1.5,
			quantity: 14,
		},
		114: {
			name: "twix",
			price: 1.75,
			quantity: 16,
		},
		115: {
			name: "reese's peanut butter cups",
			price: 1.75,
			quantity: 20,
		},
		116: {
			name: "pringles - original",
			price: 1.75,
			quantity: 10,
		},
		117: {
			name: "planters peanuts",
			price: 1.5,
			quantity: 15,
		},
		118: {
			name: "nature valley granola bar",
			price: 1.25,
			quantity: 12,
		},
		119: {
			name: "trail mix",
			price: 1.75,
			quantity: 8,
		},
	});
	const [loading, setLoading] = useState(false);
	const [selection, setSelection] = useState<string>("");

	useEffect(() => {
		// Fetch the inventory from the Inventory Service on component mount
		axios
			.get("http://localhost:5000/inventory")
			.then((response) => setInventory(response.data))
			.catch((error) =>
				console.error("Error fetching inventory:", error)
			);
	}, []);

	// Function to handle purchase
	const handlePurchase = (item: string) => {
		setLoading(true);
		axios
			.post("http://localhost:5001/purchase", {
				item,
			})
			.then((response) => {
				if ((response.data = "💀"))
					return toast.error(`Purchase failed`);

				// Update the inventory state
				// setInventory((prevInventory) => ({
				// 	...prevInventory,
				// 	[item]: prevInventory[item] - 1,
				// }));

				toast.success(`Purchased ${item} successfully!`);
			})
			.catch((error) => toast.error(`Purchase failed: ${error.response}`))
			.finally(() => {
				setLoading(false);
				setSelection("");
			});
	};

	// Render the inventory and purchase buttons
	return (
		<div className="flex justify-center items-center h-full">
			<div className="flex border-4 ">
				<div className="grid grid-cols-4 gap-2 border-3 rounded p-1 h-[600px]">
					{Object.keys(inventory).map((id) => (
						<div key={id}>
							<div className="font-bold">
								{inventory[id].name} ({inventory[id].quantity})
							</div>
							<div className="space-x-2">
								<div className="p-2 rounded-full bg-black text-white inline-block">
									<span>{id}</span>
								</div>
								<span>${inventory[id].price.toFixed(2)}</span>
							</div>
						</div>
					))}
				</div>
				<div className="space-y-4 px-8 border-l-4 flex flex-col justify-center">
					<Input value={selection} readOnly />
					<Keypad
						loading={loading}
						onClick={(key) => {
							if (typeof key === "number")
								setSelection(`${selection}${key}`);

							if (key === "🔄") setSelection("");

							if (key === "✔️") handlePurchase(selection);
						}}
					/>
				</div>
			</div>
		</div>
	);
};

export default InventoryPage;
