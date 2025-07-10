import { Form } from "./form";

export default function Index({ params: {wfRunId} }: { params: { wfRunId: string } }) {
  return (
    <div className="flex font-sans bg-white shadow-md rounded">
      <div className="flex-auto w-96 p-6">
        <h1 className="text-lg font-bold mb-4">Upload Passport</h1>
        <Form wfRunId={wfRunId} />
      </div>
    </div>
  );
}
