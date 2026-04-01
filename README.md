# Project Metrova (TWELITE Monitor for Android)

本プロジェクトは、USBシリアルを介して TWELITE CUE/PAL センサーのデータをリアルタイムに監視・解析する Android アプリケーションです。

## 📸 実機デモ (Hardware Setup)

[在这里插入你的 oneDriver.jpg 或 twoDriver.jpg]

## 📱 動作確認済み環境 (Verified Environment)

現場での長時間利用を想定し、以下の環境で安定動作を確認しています。

- **Device:** Huawei P10 Pro
- **OS:** Android 10 / EMUI 11.0.0
- **Battery Performance:** - 省電力モード設定により、12時間以上の連続待機・監視が可能。
    - 外部電源のない屋外環境でも、一日中の信号検知作業に使用できます。

## 🛠 必要機器 (Required Components)

1.  **Android 端末** (USBホスト/OTG機能対応)
2.  **USB Type-C 変換アダプタ** (OTGアダプター)
3.  **TWELITE MONOSTICK** または **TWELITE CUE**

## 💡 主なユースケース (Use Cases)

- **屋外設備信号検知**: 電源確保が困難な場所でのセンサー到達範囲確認。
- **IoTデバイス設置前テスト**: 現場での通信環境（LQI）の事前調査。
- **アナログメーターのデジタル化PoC**: 現場で手軽にセンサー値を確認。

## ⚙️ 機能 (Features)

- **リアルタイム・モニタリング**: 受信したシリアルデータの解析とリスト表示。
- **通信ログ表示**: 受信した Raw Data (RX信号) のスクロール表示。
- **自動再接続**: USBの抜き差しを検知し、自動的に通信を復旧。

Note: This project is part of a series of low-cost IoT initiatives for SMEs. (本プロジェクトは、中小企業向け低コストIoT施策の一環として開発されました。)
