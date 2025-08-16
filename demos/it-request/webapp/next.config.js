/** @type {import('next').NextConfig} */
const nextConfig = {
  experimental: {
    serverActions: {
      allowedOrigins: ['localhost:3000', 'localhost:4000', 'localhost:5173']
    }
  },
  env: {
    LHC_API_HOST: process.env.LHC_API_HOST || 'localhost',
    LHC_API_PORT: process.env.LHC_API_PORT || '2023',
    LHC_API_PROTOCOL: process.env.LHC_API_PROTOCOL || 'PLAINTEXT',
  }
}

module.exports = nextConfig
