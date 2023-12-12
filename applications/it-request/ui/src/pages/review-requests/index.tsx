import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import TablePagination from '@mui/material/TablePagination';
import TableFooter from '@mui/material/TableFooter';
import useSWRInfinite from 'swr/infinite';
import { useState } from 'react';
import { useRouter } from 'next/router';

export default function BasicTable() {

    const [page, setPage] = useState(0);
    const [rowsPerPage, setRowsPerPage] = useState(1);
    const router = useRouter();

    const getKey = (pageIndex: number, previousPageData: any) => {
        // reached the end
        if (previousPageData && !previousPageData.data) return null
        
        // first page, we don't have `previousPageData`
        if (pageIndex === 0) return `http://localhost:8080/it-requests?pageSize=${rowsPerPage}`
        
        // add the cursor to the API endpoint
        return `http://localhost:8080/it-requests?pageSize=${rowsPerPage}&bookmark=${previousPageData.bookmark}`
    }

    const { data, size, setSize } = useSWRInfinite(getKey, (url) => fetch(url).then(r => r.json()))

  return  (
    <>
    <TableContainer component={Paper}>
      <Table sx={{ minWidth: 650 }} aria-label="simple table">
        <TableHead>
          <TableRow>
            <TableCell>Requester</TableCell>
            <TableCell align="right">Description</TableCell>
            <TableCell align="right">Status</TableCell>
          </TableRow>
        </TableHead>
        {data && data[page] &&
        <TableBody>
          {data[page].data.map((row: any) => (
            <TableRow
              hover
              onClick={() => {router.push(`/review-requests/${row.id}`)}}
              key={row.id}
              sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
            >
              <TableCell component="th" scope="row">
                {row.requesterEmail}
              </TableCell>
              <TableCell align="right">{row.description}</TableCell>
              <TableCell align="right">{row.status}</TableCell>
            </TableRow>
          ))}
        </TableBody>}
        <TableFooter>
          <TableRow>
          <TablePagination rowsPerPageOptions={[1, 2, 3]} onRowsPerPageChange={(event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  }} component="div" count={-1} onPageChange={(event, newPageNumber) => {
            setPage(newPageNumber)
            setSize(newPageNumber + 1)
            }} page={page} rowsPerPage={rowsPerPage}/>
          </TableRow>
        </TableFooter>
      </Table>
    </TableContainer>
    </>
    );
}