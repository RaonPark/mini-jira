import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // /api 로 시작하는 요청을 Spring Boot 로 넘긴다.
      // 브라우저 입장에서는 전부 같은 오리진(5173)이므로
      // 백엔드에 CORS 설정을 따로 하지 않아도 된다.
      // 대상 포트는 backend 의 application.yml 의 server.port 와 맞춰야 한다.
      '/api': 'http://localhost:8070',
    },
  },
})
