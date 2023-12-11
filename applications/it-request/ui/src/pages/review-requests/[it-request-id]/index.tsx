import { Button, Stack, TextField } from "@mui/material";
import { useRouter } from "next/router";
import { useState } from "react";
import useSWR from "swr";
import useSWRMutation from "swr/mutation";

export default function ButtonUsage() {

  const [comments, setComments] = useState<string|null>(null);

  const router = useRouter()

  const { data, error, isLoading, mutate } = useSWR(
    `http://localhost:8080/it-requests/${router.query['it-request-id']}`,
    (url) => fetch(url).then(r => r.json()).then(json => {setComments(json.comments); return json}),
  )

  const { trigger, isMutating, error: errorPepe } = useSWRMutation(
    `http://localhost:8080/it-requests/${router.query['it-request-id']}`,
    (url, { arg }: { arg: any }) => fetch(url + '/complete', {
      headers: {
        'Content-type': 'application/json',
      },
      method: 'POST',
      body: JSON.stringify({
        "comments": comments,
        "userId": arg.userId,
        "isApproved": arg.isApproved
      }),
    })
  )

  return <Stack height={"100vh"} spacing={2}>
    {isLoading || isMutating ? <h1>Loading</h1> : <>
  <TextField
    id="outlined-required"
    label="Request Status"
    value={data.status}
    disabled
  />
  <TextField
    id="outlined-required"
    label="Requester Email"
    value={data.requesterEmail}
    disabled
  />
  <TextField
    id="outlined-required"
    label="Description"
    value={data.description}
    disabled
    multiline
  />
  <TextField
    required
    id="outlined-required"
    label="Comments"
    value={comments}
    multiline
    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
      console.log(`value: ${event.target.value}`);
      setComments(event.target.value);
    }}
    disabled={data.status !== 'PENDING'}
  />
  <Stack direction="row" spacing={2}>
  <Button onClick={() => trigger({userId: 'someone', isApproved: false})} variant="contained">
Reject
</Button>
  <Button onClick={() => trigger({userId: 'someone', isApproved: true})} variant="contained">
Accept
</Button>
</Stack>
  </>
  }
</Stack>
}