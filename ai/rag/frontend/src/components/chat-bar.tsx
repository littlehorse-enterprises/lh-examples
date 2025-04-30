import { PlusIcon } from "lucide-react";
import { Button } from "./ui/button";

export default function ChatBar() {
    return (
        <div className="border border-white/20 w-full h-16 rounded-full bg-white/20 flex flex-col px-5 py-10 justify-center">
            <input>
            </input>
            <div className="flex items-center justify-end">
                <Button size="icon" className="rounded-full">
                    <PlusIcon />
                </Button>
            </div>
        </div>
    );
}