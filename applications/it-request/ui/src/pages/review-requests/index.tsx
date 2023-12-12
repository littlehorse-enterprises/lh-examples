import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import useSWRInfinite from 'swr/infinite';
import { useState } from 'react';
import { useRouter } from 'next/router';
import IconButton from '@mui/material/IconButton';
import { ChevronLeft, ChevronRight } from '@mui/icons-material';
import { Stack } from '@mui/material';

export default function BasicTable() {

    const [rowsPerPage, setRowsPerPage] = useState(5);
    const router = useRouter();

    const getKey = (pageIndex: number, previousPageData: any) => {
        // reached the end
        if (previousPageData && !previousPageData.data) return null
        
        // first page, we don't have `previousPageData`
        if (pageIndex === 0) return `http://localhost:8080/it-requests?pageSize=${rowsPerPage}`
        
        // add the cursor to the API endpoint
        return `http://localhost:8080/it-requests?pageSize=${rowsPerPage}&bookmark=${previousPageData.bookmark}`
    }

    const { data, size: page, setSize: setPage } = useSWRInfinite(getKey, (url) => fetch(url).then(r => r.json()))

  return  (
    <Paper>
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Requester</TableCell>
            <TableCell align="right">Description</TableCell>
            <TableCell align="right">Status</TableCell>
          </TableRow>
        </TableHead>
        {data && data[page - 1] &&
        <TableBody>
          {data[page - 1].data.map((row: any) => (
            <TableRow
              hover
              onClick={() => {router.push(`/review-requests/${row.id}`)}}
              key={row.id}
            >
              <TableCell>{row.requesterEmail}</TableCell>
              <TableCell align="right">{row.description}</TableCell>
              <TableCell align="right">{row.status}</TableCell>
            </TableRow>
          ))}
        </TableBody>}
      </Table>
    </TableContainer>
    <Stack direction="row">
    <IconButton disabled={page <= 1} onClick={() => {
      setPage(page - 1)
    }}>
            <ChevronLeft />
          </IconButton>
    <IconButton disabled={data && data[page - 1] && data[page - 1].bookmark === null}   onClick={() => {
      setPage(page + 1)
    }}>
            <ChevronRight />
          </IconButton>
    </Stack>
    </Paper>
    );
}