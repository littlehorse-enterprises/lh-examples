import http from 'http';
import { Server } from 'socket.io';

const PORT = process.env.PORT || 3001
const server = http.createServer();
const io = new Server(server);

io.on('connection', (socket) => {
  socket.on("warehouse", (message: string) => {
    const transaction: Transaction = JSON.parse(message);
    console.log("WAREHOUSE_TRANSACTION_RECEIVED:", transaction);
    io.emit(`warehouse.${transaction.account}`, transaction);
  });

  socket.on('payments', (message: string) => {
    const transaction: Transaction = JSON.parse(message);
    console.log("PAYMENT_TRANSACTION_RECEIVED:", transaction)
    io.emit(`payments.${transaction.account}`, transaction)
  })

  socket.on("payment.failures", (message: string) => {
    const transaction: Transaction = JSON.parse(message);
    console.log("FAILED_TRANSACTION_RECEIVED:", transaction);
    io.emit(`payments.failures.${transaction.account}`, transaction);
  });

  socket.on("connection", () => {
    console.log("Connected!");
  });
});

server.listen(PORT, () => {
  console.log(`Notifications service listening on port ${PORT}`);
});


type Transaction = {
  account: String
  amount: number
}
