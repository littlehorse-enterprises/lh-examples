import { Actions } from "./actions";
import { getUserTaskRuns } from "./getUserTaskRuns";

export const dynamic = "force-dynamic";

export default async function Index() {
  const userTaskRuns = await getUserTaskRuns();

  return (
    <div className="flex font-sans bg-white shadow-md rounded">
      <div className="flex-auto w-96 p-6">
        <h1 className="text-lg font-bold mb-4">Pending Verifications</h1>
        {userTaskRuns.length === 0 && <>No pending verfications</>}
        {userTaskRuns.map((userTaskRun) => (
          <div
            key={userTaskRun.id?.userTaskGuid}
            className="flex text-md mb-2 text-cyan-600 border font-medium rounded p-2 capitalize"
          >
            <div className="flex-grow">{userTaskRun.notes}</div>
            <Actions userTaskRun={userTaskRun} />
          </div>
        ))}
      </div>
    </div>
  );
}
