import {
  Button,
  Card,
  CardActions,
  CardContent,
  TextField,
  Typography,
} from "@mui/material";
import { useRouter } from "next/router";
import { useState } from "react";
import useSWR from "swr";
import useSWRMutation from "swr/mutation";

export default function ButtonUsage() {
  const [comments, setComments] = useState<string | null>(null);

  const router = useRouter();

  const { data, error, isLoading } = useSWR(
    `http://localhost:8080/it-requests/${router.query["it-request-id"]}`,
    (url) =>
      fetch(url)
        .then((r) => r.json())
        .then((json) => {
          setComments(json.comments);
          return json;
        }),
  );

  const {
    trigger,
    isMutating,
    error: errorMutation,
  } = useSWRMutation(
    `http://localhost:8080/it-requests/${router.query["it-request-id"]}`,
    (url, { arg }: { arg: any }) =>
      fetch(url + "/complete", {
        headers: {
          "Content-type": "application/json",
        },
        method: "POST",
        body: JSON.stringify({
          comments: comments,
          userId: arg.userId,
          isApproved: arg.isApproved,
        }),
      }),
  );

  if (isLoading || isMutating) {
    return <h1>Loading</h1>;
  }

  return (
    <Card sx={{ minWidth: 275 }}>
      <CardContent>
        <Typography variant="h6" color="text.primary" gutterBottom>
          Request Status
        </Typography>
        <Typography variant="body1" color="text.secondary" gutterBottom>
          {data.status}
        </Typography>
        <Typography variant="h6" color="text.primary" gutterBottom>
          Requester Email
        </Typography>
        <Typography variant="body1" color="text.secondary" gutterBottom>
          {data.requesterEmail}
        </Typography>
        <Typography variant="h6" color="text.primary" gutterBottom>
          Description
        </Typography>
        <Typography variant="body1" color="text.secondary" gutterBottom>
          {data.description}
        </Typography>
        <Typography variant="h6" color="text.primary" gutterBottom>
          Comments
        </Typography>
        {data.status === "PENDING" ? (
          <TextField
            required
            value={comments}
            multiline
            onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
              console.log(`value: ${event.target.value}`);
              setComments(event.target.value);
            }}
            disabled={data.status !== "PENDING"}
          />
        ) : (
          <Typography variant="body1" color="text.secondary" gutterBottom>
            {data.comments}
          </Typography>
        )}
      </CardContent>
      {data.status === "PENDING" && (
        <CardActions>
          <Button
            onClick={() => trigger({ userId: "someone", isApproved: false })}
          >
            Reject
          </Button>
          <Button
            onClick={() => trigger({ userId: "someone", isApproved: true })}
          >
            Accept
          </Button>
        </CardActions>
      )}
    </Card>
  );
}
