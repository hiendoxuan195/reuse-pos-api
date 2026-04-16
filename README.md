# Reuse POS API

リユース業界向け買取品管理REST API。Spring Boot 3 + MyBatis + H2 (Oracle互換モード) で実装。

## Tech Stack

- **Java 21** (OpenJDK)
- **Spring Boot 3.4** (Web, Validation)
- **MyBatis 3** (XML Mapper, JOINクエリ)
- **H2 Database** (Oracle互換モード / 本番はOracle XEに切替可能)
- **Maven 3.9+**

## Domain Model

リユース店舗の買取品管理を想定したドメイン設計:

- **BuybackItem** — 買取品（品番, 品名, 買取価格, 販売価格, 状態ランク, ステータス）
- **ItemCategory** — カテゴリ（ブランド品, トレカ, ゲーム, 家電, 貴金属）
- **状態ランク**: S(新品同様), A(美品), B(良品), C(可), D(難あり)
- **ステータス**: IN_STOCK(在庫), ON_DISPLAY(陳列中), SOLD(売却済), RETURNED(返品)

## Quick Start

```bash
# Build & Run
mvn spring-boot:run

# Health check
curl http://localhost:8080/api/items/health
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/items` | 全件取得 |
| GET | `/api/items?status=IN_STOCK` | ステータス別取得 |
| GET | `/api/items?categoryId=1` | カテゴリ別取得 |
| GET | `/api/items/{id}` | 1件取得 |
| POST | `/api/items` | 買取品登録 |
| PUT | `/api/items/{id}` | 買取品更新 |
| PATCH | `/api/items/{id}/status` | ステータス変更 |
| GET | `/api/items/health` | ヘルスチェック |

## Usage Examples

```bash
# 全件取得
curl http://localhost:8080/api/items

# 買取品登録
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{
    "itemCode": "GAM-20260416-001",
    "name": "PS5 デジタルエディション",
    "categoryId": 3,
    "purchasePrice": 28000,
    "sellingPrice": 42000,
    "condition": "A",
    "customerName": "山田次郎"
  }'

# ステータス変更（在庫→陳列）
curl -X PATCH http://localhost:8080/api/items/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "ON_DISPLAY"}'

# カテゴリ別取得（トレカ）
curl http://localhost:8080/api/items?categoryId=2
```

## Design Decisions

1. **H2 Oracle互換モード**: 本番Oracle環境を想定しつつ、ローカル開発はH2で軽量に
2. **MyBatis XML Mapper**: JOINクエリ・動的SQLに強く、既存Oracle PL/SQLとの親和性が高い
3. **ドメイン設計**: リユース業界の買取→査定→陳列→販売フローを反映
4. **状態ランク+ステータス**: 買取品の品質管理とライフサイクル管理を分離

## Learning Notes

PHP/Python経験からJava/Spring Bootへのキャッチアップとして作成。
重点を置いた学習ポイント:
- Spring DI/IoC コンテナの設計思想（Laravel Service Containerとの比較）
- MyBatis の XML Mapper による型安全なDB操作
- Spring Boot の Convention over Configuration
