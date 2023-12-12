import * as React from "react";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import useSWRInfinite from "swr/infinite";
import { useState } from "react";
import { useRouter } from "next/router";
import IconButton from "@mui/material/IconButton";
import { ChevronLeft, ChevronRight, Search } from "@mui/icons-material";
import {
  MenuItem,
  Select,
  SelectChangeEvent,
  Stack,
  TextField,
} from "@mui/material";

export default function ReviewRequests() {
  const [rowsPerPage, setRowsPerPage] = useState(1);
  const [status, setStatus] = useState("ALL");
  const [statusInput, setStatusInput] = useState("ALL");
  const [requester, setRequester] = useState("");
  const [requesterInput, setRequesterInput] = useState("");
  const router = useRouter();

  const getKey = (_pageIndex: number, previousPageData: any) => {
    // reached the end
    if (previousPageData && !previousPageData.data) return null;

    // add the cursor to the API endpoint
    return `http://localhost:8080/it-requests?pageSize=${rowsPerPage}${
      previousPageData ? "&bookmark=" + previousPageData.bookmark : ""
    }&status=${status === "ALL" ? "" : status}${
      requester !== "" ? "&requesterEmail=" + requester : ""
    }`;
  };

  const {
    data,
    size: page,
    setSize: setPage,
  } = useSWRInfinite(getKey, (url) => fetch(url).then((r) => r.json()));

  return (
    <Paper>
      <Stack direction="row">
        <div>Requester:</div>
        <TextField
          required
          value={requesterInput}
          onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
            setRequesterInput(event.target.value);
          }}
        />
        <div>Status:</div>
        <Select
          value={statusInput}
          onChange={(event: SelectChangeEvent) => {
            setStatusInput(event.target.value);
          }}
        >
          <MenuItem value="ALL">ALL</MenuItem>
          <MenuItem value="PENDING">PENDING</MenuItem>
          <MenuItem value="APPROVED">APPROVED</MenuItem>
          <MenuItem value="REJECTED">REJECTED</MenuItem>
        </Select>
        <IconButton
          onClick={() => {
            setStatus(statusInput);
            setRequester(requesterInput);
          }}
        >
          <Search />
        </IconButton>
      </Stack>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Requester</TableCell>
              <TableCell align="right">Description</TableCell>
              <TableCell align="right">Status</TableCell>
            </TableRow>
          </TableHead>
          {data && data[page - 1] && (
            <TableBody>
              {data[page - 1].data.map((row: any) => (
                <TableRow
                  hover
                  onClick={() => {
                    router.push(`/review-requests/${row.id}`);
                  }}
                  key={row.id}
                >
                  <TableCell>{row.requesterEmail}</TableCell>
                  <TableCell align="right">{row.description}</TableCell>
                  <TableCell align="right">{row.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          )}
        </Table>
      </TableContainer>
      <Stack direction="row">
        <IconButton
          disabled={page <= 1}
          onClick={() => {
            setPage(page - 1);
          }}
        >
          <ChevronLeft />
        </IconButton>
        <IconButton
          disabled={data && data[page - 1] && data[page - 1].bookmark === null}
          onClick={() => {
            setPage(page + 1);
          }}
        >
          <ChevronRight />
        </IconButton>
      </Stack>
    </Paper>
  );
}
