import LoadingButton from "@mui/lab/LoadingButton";
import { Alert, AlertTitle, Box, Stack, TextField } from "@mui/material";
import { useState } from "react";

export default function ButtonUsage() {

  const [requesterEmail, setRequesterEmail] = useState<string>();
  const [description, setDescription] = useState<string>();
  const [error, setError] = useState<string|undefined>();
  const [loading, setLoading] = useState<boolean>(false);

  async function submitRequest(description: string | undefined, requesterEmail: string | undefined) {
    setError(undefined)
    setLoading(true)

    try {
      const response = await fetch('http://localhost:8080/it-requests', {
        headers: {
          'Content-type': 'application/json',
        },
        method: 'POST',
        body: JSON.stringify({
          "requesterEmail": requesterEmail,
          "description": description
        }),
      })

      if (!response.ok) {
        setError((await response.json()).message);
      } else {

      }
    } catch (exception: any) {
      setError("Unexpected error happened");
    }

    setLoading(false)
  }

  return <Stack height={"100vh"} spacing={2}>
  <TextField
    required
    id="outlined-required"
    label="Requester Email"
    value={requesterEmail}
    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
      setRequesterEmail(event.target.value);
    }}
  />
  <TextField
    required
    id="outlined-required"
    label="Request Description"
    value={description}
    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
      setDescription(event.target.value);
    }}
  />
<LoadingButton loading={loading} onClick={() => submitRequest(description, requesterEmail)} variant="contained">
Submit
</LoadingButton>
{error && <Alert severity="error">
    <AlertTitle>Error</AlertTitle>
    {error}
  </Alert>}
</Stack>
}