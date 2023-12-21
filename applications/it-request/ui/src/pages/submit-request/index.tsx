import LoadingButton from "@mui/lab/LoadingButton";
import { Alert, AlertTitle, Card, CardActions, CardContent, TextField, Typography } from "@mui/material";
import { useState } from "react";

export default function SubmitRequest() {
  const [requesterEmail, setRequesterEmail] = useState<string>();
  const [description, setDescription] = useState<string>();
  const [error, setError] = useState<string | undefined>();
  const [loading, setLoading] = useState<boolean>(false);

  async function submitRequest(
    description: string | undefined,
    requesterEmail: string | undefined,
  ) {
    setError(undefined);
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8080/it-requests", {
        headers: {
          "Content-type": "application/json",
        },
        method: "POST",
        body: JSON.stringify({
          requesterEmail: requesterEmail,
          description: description,
        }),
      });

      if (!response.ok) {
        setError((await response.json()).message);
      }
    } catch (exception: any) {
      setError("Unexpected error happened");
    }

    setLoading(false);
  }

  return ( <Card sx={{ minWidth: 275 }}>
      <CardContent>
        <Typography variant="h6" color="text.primary" gutterBottom>
          Requester Email
        </Typography>
        <TextField
          required
          placeholder="someone@email.com"
          value={requesterEmail}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
            setRequesterEmail(event.target.value);
          }}
        />
        <Typography variant="h6" color="text.primary" gutterBottom>
          Request Description
        </Typography>
        <TextField
          required
          id="outlined-required"
          placeholder="e.g. access to jira"
          value={description}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
            setDescription(event.target.value);
          }}
        />
        {error && (
        <Alert severity="error">
          <AlertTitle>Error</AlertTitle>
          {error}
        </Alert>
      )}
      </CardContent>
      <CardActions>
        <LoadingButton
          loading={loading}
          onClick={() => submitRequest(description, requesterEmail)}
        >
          Submit
        </LoadingButton>
      </CardActions>
    </Card>
  );
}
