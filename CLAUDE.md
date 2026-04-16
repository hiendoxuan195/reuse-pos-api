# CLAUDE.md — Reuse POS API

## Overview

リユース業界向け買取品管理REST API。Spring Boot 3.4 + MyBatis + H2 (Oracle互換モード)。

## Tech Stack

- Java 21 (OpenJDK)
- Spring Boot 3.4 (Web, Validation)
- MyBatis 3.0.4 (XML Mapper)
- H2 Database (Oracle互換モード `MODE=Oracle`)
- Maven 3.9+

## Architecture

```
Controller (REST) → Service (Business Logic) → Mapper (MyBatis) → H2/Oracle DB
```

4層アーキテクチャ:
- **Controller**: HTTP リクエスト/レスポンス処理。薄く保つ
- **Service**: ビジネスロジック。デフォルト値設定、バリデーション
- **Mapper**: MyBatis インターフェース + XML SQL定義。JOINクエリ対応
- **Model**: POJO エンティティ。DB列とのマッピング

## Build & Run

```bash
# Java 21 必須
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# ビルド
mvn clean package

# 起動
mvn spring-boot:run

# テスト
mvn test
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/items` | 全件取得 |
| GET | `/api/items?status={status}` | ステータス別 |
| GET | `/api/items?categoryId={id}` | カテゴリ別 |
| GET | `/api/items/{id}` | 1件取得 |
| POST | `/api/items` | 新規登録 |
| PUT | `/api/items/{id}` | 更新 |
| PATCH | `/api/items/{id}/status` | ステータス変更 |
| GET | `/api/items/health` | ヘルスチェック |

## Domain Model

- **BuybackItem**: 買取品（品番, 品名, 買取価格, 販売価格, 状態ランク, ステータス）
- **ItemCategory**: カテゴリ（ブランド品, トレカ, ゲーム, 家電, 貴金属）
- 状態ランク: S/A/B/C/D
- ステータス: IN_STOCK → ON_DISPLAY → SOLD / RETURNED

## File Structure

```
src/main/java/com/example/reusepos/
├── ReusePosApplication.java          # エントリーポイント
├── controller/
│   └── BuybackItemController.java    # REST API (7エンドポイント)
├── service/
│   └── BuybackItemService.java       # ビジネスロジック
├── mapper/
│   └── BuybackItemMapper.java        # MyBatis Mapper Interface
└── model/
    ├── BuybackItem.java              # 買取品エンティティ
    └── ItemCategory.java             # カテゴリエンティティ

src/main/resources/
├── application.yml                   # DB接続・MyBatis設定
├── db/
│   ├── schema.sql                    # テーブル定義
│   └── data.sql                      # 初期データ
└── mapper/
    └── BuybackItemMapper.xml         # SQL定義 (JOINクエリ)
```

## Conventions

- Java: PascalCase (クラス), camelCase (変数/メソッド)
- DB列: UPPER_SNAKE_CASE → MyBatis `map-underscore-to-camel-case: true` で自動変換
- `ITEM_CONDITION` (H2で `CONDITION` は予約語のため)
- コミットメッセージ: conventional commit (feat/fix/refactor/test)

## Testing

```bash
# 全テスト
mvn test

# API手動テスト
curl http://localhost:8080/api/items/health
curl http://localhost:8080/api/items
curl -X POST http://localhost:8080/api/items \
  -H "Content-Type: application/json" \
  -d '{"itemCode":"TEST-001","name":"テスト品","categoryId":1,"purchasePrice":1000}'
```

## H2 Console

起動中に `http://localhost:8080/h2-console` でDB直接操作可能。
- JDBC URL: `jdbc:h2:mem:reusepos`
- User: `sa` / Password: (空)
