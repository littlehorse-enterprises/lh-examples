import Image from "next/image";
import { ITRequestFlow } from "@/components/ITRequestFlow";

export default function Page() {
	return (
		<>
      {/* Header */}
      <header id="header" role="banner" className="h-16 border-b border-white/20">
        <div className="flex items-center justify-start mx-auto max-w-5xl py-4 px-6">
          <Image 
            src="/logo.png" 
            alt="LittleHorse" 
            width={200}
            height={60}
            className="block"
          />
          <h1 className="text-2xl font-normal m-0 ml-auto">User Tasks / IT Request Example</h1>
        </div>
      </header>
      
      {/* Main App */}
      <main id="main" role="main" className="min-h-[calc(100vh-4rem)] flex flex-col mx-auto max-w-5xl py-6 px-6">
        <p className="border-b border-white/20 leading-relaxed mb-6 opacity-90 pb-6">
          This UI guides you through the LittleHorse User Tasks IT Request example. A requester starts an
          IT Request workflow, completes a requesting task by providing the Requested Item and a Justification,
          then a Finance user reviews and either approves or declines. The app validates user input before calling
          the API and always shows the latest response for the current step.
        </p>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 flex-1 min-h-0 items-baseline">
          <ITRequestFlow />
        </div>
      </main>
		</>
	);
}
