# Reuse POS API

リユース業界向け買取品管理REST API。Spring Boot 3 + MyBatis + H2 (Oracle互換モード) で実装。

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Client (curl / Browser)               │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────────────┐
│  Controller Layer                                        │
│  BuybackItemController.java                              │
│  ┌────────────────────────────────────────────────────┐  │
│  │ GET /api/items          → list(status?, categoryId?)│  │
│  │ GET /api/items/{id}     → get(id)                   │  │
│  │ POST /api/items         → create(item)              │  │
│  │ PUT /api/items/{id}     → update(id, item)          │  │
│  │ PATCH /api/items/{id}/status → updateStatus(id)     │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────┘
                      │ @Autowired (DI)
┌─────────────────────▼───────────────────────────────────┐
│  Service Layer                                           │
│  BuybackItemService.java                                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │ - デフォルト値設定（condition=B, status=IN_STOCK）  │  │
│  │ - CRUD操作の委譲                                    │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────┘
                      │ @Mapper (MyBatis)
┌─────────────────────▼───────────────────────────────────┐
│  Mapper Layer                                            │
│  BuybackItemMapper.java (Interface)                      │
│  BuybackItemMapper.xml  (SQL定義)                        │
│  ┌────────────────────────────────────────────────────┐  │
│  │ findAll()       → SELECT + LEFT JOIN                │  │
│  │ findById(id)    → SELECT WHERE ID = ?               │  │
│  │ findByStatus()  → SELECT WHERE STATUS = ?           │  │
│  │ insert(item)    → INSERT + useGeneratedKeys         │  │
│  │ update(item)    → UPDATE SET ... WHERE ID = ?       │  │
│  │ updateStatus()  → UPDATE STATUS WHERE ID = ?        │  │
│  └────────────────────────────────────────────────────┘  │
└─────────────────────┬───────────────────────────────────┘
                      │ JDBC
┌─────────────────────▼───────────────────────────────────┐
│  Database (H2 in-memory, Oracle互換モード)               │
│  ┌──────────────┐    ┌──────────────────────────────┐   │
│  │ITEM_CATEGORY │    │ BUYBACK_ITEM                  │   │
│  │──────────────│    │──────────────────────────────│   │
│  │ID            │◄───│CATEGORY_ID (FK)               │   │
│  │NAME          │    │ID, ITEM_CODE, NAME            │   │
│  │DESCRIPTION   │    │PURCHASE_PRICE, SELLING_PRICE  │   │
│  └──────────────┘    │ITEM_CONDITION, STATUS         │   │
│                      │CUSTOMER_NAME, PURCHASED_AT    │   │
│                      └──────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Tech Stack

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Java | 21 (OpenJDK) | 言語 |
| Spring Boot | 3.4 | Webフレームワーク |
| MyBatis | 3.0.4 | O/Rマッパー (XML Mapper) |
| H2 Database | — | インメモリDB (Oracle互換モード) |
| Maven | 3.9+ | ビルドツール |

## Domain Model

リユース店舗の買取→査定→陳列→販売フローを反映:

```
[買取] → [在庫 IN_STOCK] → [陳列 ON_DISPLAY] → [売却 SOLD]
                                              ↘ [返品 RETURNED]
```

| モデル | 説明 |
|--------|------|
| **BuybackItem** | 買取品（品番, 品名, 買取価格, 販売価格, 状態ランク, ステータス） |
| **ItemCategory** | カテゴリ（ブランド品, トレカ, ゲーム, 家電, 貴金属） |

**状態ランク**: `S`(新品同様) > `A`(美品) > `B`(良品) > `C`(可) > `D`(難あり)

## Quick Start（初心者向け）

### 1. 前提条件の確認

```bash
# Java 21 が必要
java --version
# → openjdk 21.x.x が表示されればOK

# Maven が必要
mvn --version
# → Apache Maven 3.9.x が表示されればOK
```

**インストールされていない場合（macOS）:**
```bash
brew install openjdk@21 maven
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

### 2. ビルド & 起動

```bash
# リポジトリをクローン
git clone https://github.com/hiendoxuan195/reuse-pos-api.git
cd reuse-pos-api

# ビルド（初回は依存関係のダウンロードで1-2分）
mvn clean package

# 起動（H2インメモリDBが自動起動。Docker不要！）
mvn spring-boot:run
```

**起動成功の確認:**
```
Started ReusePosApplication in 0.7 seconds
```

### 3. 動作確認

```bash
# ヘルスチェック
curl http://localhost:8080/api/items/health
# → {"status":"UP","app":"reuse-pos-api"}

# サンプルデータ取得（起動時に自動挿入される）
curl http://localhost:8080/api/items | python3 -m json.tool
```

### 4. APIを試す

```bash
# 買取品を登録する
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

# ステータスを変更（在庫 → 陳列中）
curl -X PATCH http://localhost:8080/api/items/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "ON_DISPLAY"}'

# カテゴリで絞り込み（トレカ = categoryId:2）
curl "http://localhost:8080/api/items?categoryId=2"

# ステータスで絞り込み
curl "http://localhost:8080/api/items?status=IN_STOCK"
```

### 5. H2 Database Console（ブラウザでDB操作）

起動中に http://localhost:8080/h2-console にアクセス:
- JDBC URL: `jdbc:h2:mem:reusepos`
- User: `sa`
- Password: (空欄のまま)

## API Reference

| Method | Path | Body | Description |
|--------|------|------|-------------|
| `GET` | `/api/items` | — | 全件取得（カテゴリ名をJOINで付与） |
| `GET` | `/api/items?status=IN_STOCK` | — | ステータスで絞り込み |
| `GET` | `/api/items?categoryId=1` | — | カテゴリで絞り込み |
| `GET` | `/api/items/{id}` | — | 1件取得 |
| `POST` | `/api/items` | JSON | 買取品登録（condition/statusはデフォルト値あり） |
| `PUT` | `/api/items/{id}` | JSON | 買取品の全項目更新 |
| `PATCH` | `/api/items/{id}/status` | `{"status":"SOLD"}` | ステータスのみ変更 |
| `GET` | `/api/items/health` | — | ヘルスチェック |

## Project Structure

```
reuse-pos-api/
├── CLAUDE.md                                 ← AIエージェント設定
├── README.md                                 ← このファイル
├── pom.xml                                   ← Maven依存関係
└── src/
    ├── main/
    │   ├── java/com/example/reusepos/
    │   │   ├── ReusePosApplication.java      ← エントリーポイント
    │   │   ├── controller/
    │   │   │   └── BuybackItemController.java ← REST API定義
    │   │   ├── service/
    │   │   │   └── BuybackItemService.java   ← ビジネスロジック
    │   │   ├── mapper/
    │   │   │   └── BuybackItemMapper.java    ← MyBatis Interface
    │   │   └── model/
    │   │       ├── BuybackItem.java          ← 買取品エンティティ
    │   │       └── ItemCategory.java         ← カテゴリエンティティ
    │   └── resources/
    │       ├── application.yml               ← DB接続・MyBatis設定
    │       ├── db/
    │       │   ├── schema.sql                ← テーブル定義
    │       │   └── data.sql                  ← サンプルデータ
    │       └── mapper/
    │           └── BuybackItemMapper.xml     ← SQL定義（JOINクエリ）
    └── test/
        └── java/com/example/reusepos/
            └── BuybackItemControllerTest.java ← API統合テスト
```

## Design Decisions

1. **H2 Oracle互換モード** — 本番Oracle環境を想定しつつ、Docker不要で即起動
2. **MyBatis XML Mapper** — JOINクエリ・動的SQLに強く、既存Oracle PL/SQLとの親和性が高い
3. **ドメイン設計** — リユース業界の買取→査定→陳列→販売フローを反映
4. **状態ランク+ステータス** — 品質管理とライフサイクル管理を分離設計
5. **ITEM_CONDITION列名** — H2で`CONDITION`は予約語のため回避（Oracle本番では`CONDITION`に変更可）

## Testing

```bash
# 全テスト実行
mvn test

# テスト結果
# Tests run: X, Failures: 0, Errors: 0
```

## Learning Notes

PHP/Python経験からJava/Spring Bootへのキャッチアップとして作成。

| PHP/Laravel | Java/Spring Boot | 気づき |
|-------------|-----------------|--------|
| Service Container | DI/IoC Container | 起動時に解決。コンパイル時チェックで早期発見 |
| Eloquent ORM | MyBatis XML | MyBatisはSQLを直接書く。複雑JOINに強い |
| artisan serve | mvn spring-boot:run | Convention over Configuration |
| Migration | schema.sql + data.sql | Spring Boot初期化で自動実行 |
