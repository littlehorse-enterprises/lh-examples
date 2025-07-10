import http from "http";
import { Server } from "socket.io";

const PORT = process.env.PORT || 3001;
const server = http.createServer();
const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"]
  },
});

io.on("connection", (socket) => {
  socket.on("item", (input: string) => {
    const {wfRunId, message}: Message = JSON.parse(input);
    io.emit(`item.${wfRunId}`, message);
  });

  socket.on("checkout", (input: string) => {
    const {wfRunId, message}: Message = JSON.parse(input);
    io.emit(`checkout.${wfRunId}`, message);
  });

  socket.on("stale", (input: string) => {
    const {wfRunId, message}: Message = JSON.parse(input);
    io.emit(`stale.${wfRunId}`, message);
  });

  socket.on("connection", () => {
    console.log("Connected!");
  });
});

server.listen(PORT, () => {
  console.log(`Notifications service listening on port ${PORT}`);
});

type Message = {
  wfRunId: string;
  message: string;
};
