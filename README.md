# Olist Commerce Platform (`olist-commerce-platform`)
*(E-Commerce Operations, Analytics & Automated SLA Dispute Verification — React + Spring Boot + PostgreSQL + LLM REST API)*

## 1. Kiến trúc hệ thống
1. **Frontend (`frontend/` - React + TypeScript + Vite):** Giao diện Dashboard cho đội vận hành 3PL lọc đơn vi phạm, xem bằng chứng đối chiếu và xuất biên bản phạt.
2. **Backend (`src/main/java` - Java 21 + Spring Boot 3):**
   * **Tầng 1 (`tier1_metrics` + `Flyway SQL`):** Đo lường số liệu chuẩn xác trên PostgreSQL (tính độ trễ giao hàng thực tế so với cam kết SLA và độ trễ chuẩn bị hàng của Seller).
   * **Tầng 2 (`tier2_ai` + `RestClient`):** Gọi trực tiếp **API LLM bên ngoài** (Groq API / Gemini / OpenRouter / OpenAI) thông qua [`LlmClientConfig`](file:///home/naoto/sla-attribution-engine/src/main/java/com/ecommerce/sla/tier2_ai/config/LlmClientConfig.java) và [`ReviewAiExtractorService`](file:///home/naoto/sla-attribution-engine/src/main/java/com/ecommerce/sla/tier2_ai/service/ReviewAiExtractorService.java) để bóc tách bình luận $\le 2$ sao ra JSON Schema chuẩn.
   * **Tầng 3 (`tier3_arbitration`):** Trọng tài đối soát tự động ghép nối số liệu SQL (Tầng 1) và ngữ nghĩa AI (Tầng 2) để quy trách nhiệm (`CARRIER_3PL_FAULT`, `SELLER_MERCHANT_FAULT`, `CUSTOMER_SUBJECTIVE_BIAS`).
3. **Database (`PostgreSQL 16`):** Chạy qua Docker nhẹ nhàng ở máy cá nhân hoặc dùng Supabase trên Cloud.

## 2. Hướng dẫn chạy nhanh
```bash
# 1. Tạo file .env và điền LLM_API_KEY (ví dụ từ Groq Cloud miễn phí)
cp .env.example .env

# 2. Khởi chạy PostgreSQL 16 bằng Docker Compose (chỉ 1 container nhẹ)
make db-up

# 3. Chạy Unit Test Backend
make test

# 4. Chạy Backend Spring Boot (port 8080)
make run

# 5. Chạy Frontend React (port 5173)
make fe-install
make fe-dev
```
