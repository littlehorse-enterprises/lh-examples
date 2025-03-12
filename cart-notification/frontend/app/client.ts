import { LHConfig } from "littlehorse-client";

const client = LHConfig.from({
  apiHost: process.env.LHC_API_HOST || "localhost",
  apiPort: process.env.LHC_API_PORT || "2023",
}).getClient();

export default client
