=========================================================================
          Epson ePOS SDK for Android Version 2.14.0

          Copyright (C) Seiko Epson Corporation 2015 - 2020. All rights reserved.
=========================================================================

1.本ソフトウェアについて

Epson ePOS SDK for Android は、EPSON TMプリンターおよびEPSON TMインテリジェ
ントプリンターに印刷するためのAndroidアプリケーションを開発する開発者向け
SDKです。
Epson ePOS SDK で提供するAPIを使用してアプリケーションを開発します。
Epson ePOS SDK には、iOSデバイス向けの Epson ePOS SDK for iOS も用意されて
います。
詳細は Epson ePOS SDK for Android ユーザーズマニュアル を参照ください。

対応Androidバージョン
  Android 4.0.3 - 4.4.4
  Android 5.0 - 5.1.1
  Android 6.0 - 6.0.1
  Android 7.0 - 7.1.2
  Android 8.0 - 8.1
  Android 9.0
  Android 10.0

対応Android端末
  ARMv5TE 対応端末
  AArch64 対応端末
  armeabi-v7a 対応端末
  x86 対応端末
  x86-64 対応端末

サポートTMプリンター
  EPSON TM-T100（海外モデルのみ）
  EPSON TM-T20（海外モデルのみ）
  EPSON TM-T20II
  EPSON TM-T20III
  EPSON TM-T20IIIL（海外モデルのみ）
  EPSON TM-T20X
  EPSON TM-T60（海外モデルのみ）
  EPSON TM-T70
  EPSON TM-T70II
  EPSON TM-T81II（海外モデルのみ）
  EPSON TM-T81III（海外モデルのみ）
  EPSON TM-T82（海外モデルのみ）
  EPSON TM-T82X（海外モデルのみ）
  EPSON TM-T82II（海外モデルのみ）
  EPSON TM-T82III（海外モデルのみ）
  EPSON TM-T82IIIL（海外モデルのみ）
  EPSON TM-T83II（海外モデルのみ）
  EPSON TM-T83III（海外モデルのみ）
  EPSON TM-T88V
  EPSON TM-T88VI
  EPSON TM-T90II
  EPSON TM-P20
  EPSON TM-P60（海外モデルのみ）
  EPSON TM-P60II
  EPSON TM-P80
  EPSON TM-U220 シリーズ（海外モデルのみ）
  EPSON TM-U330 シリーズ（海外モデルのみ）
  EPSON TM-m10
  EPSON TM-m30
  EPSON TM-m30II
  EPSON TM-m30II-H
  EPSON TM-H6000V（海外モデルのみ）

サポートTMインテリジェントプリンター
  EPSON TM-T20II-i
  EPSON TM-T70-i
  EPSON TM-T82II-i（海外モデルのみ）
  EPSON TM-T83II-i（海外モデルのみ）
  EPSON TM-T88V-i
  EPSON TM-T88VI-iHUB（海外モデルのみ）
  EPSON TM-U220-i（海外モデルのみ）
  EPSON TM-T70II-DT
  EPSON TM-T88V-DT
  EPSON TM-H6000IV-DT（海外モデルのみ）
  EPSON TM-T70II-DT2
  EPSON TM-T88VI-DT2

サポートネットワークプリンター
  EPSON TM-L90
  EPSON TM-T88IV
  EPSON TM-T90
  EPSON TM-T90KP

サポートインターフェイス
  TMプリンター
    有線LAN
    無線LAN
    Bluetooth
    USB
  TMインテリジェントプリンター
    有線LAN
  ネットワークプリンター
    有線LAN
    無線LAN

2.提供ファイル

・ePOS2.jar
  APIをJavaプログラムから利用するためのjar形式ファイルにアーカイブされた
  コンパイル済みJavaのクラスファイルです。

・ePOSEasySelect.jar
  簡単にプリンターを選択するための Java のクラスファイルです。

・libepos2.so
  機能実行用ライブラリーです。（ARMv5TE, AArch64, x86-64 に対応）

・libeposeasyselect.so
  ePOSEasySelect機能実行用ライブラリーです。（ARMv5TE, AArch64, x86-64 に対応）

・ePOS_SDK_Sample_Android.zip
  サンプルプログラムファイルです。

・DeviceControlProgram_Sample.zip
  デバイス制御プログラム用のサンプルプログラムファイルです。

・EULA.ja.txt
  SOFTWARE LICENSE AGREEMENT が記載されています。

・EULA.en.txt
  SOFTWARE LICENSE AGREEMENT（英語版）が記載されています。

・ePOS_SDK_Android_um_ja_revx.pdf
  ユーザーズマニュアルです。

・ePOS_SDK_Android_um_en_revx.pdf
  ユーザーズマニュアル（英語版）です。

・ePOS_SDK_Android_Migration_Guide_ja_revx.pdf
  マイグレーションガイドです。

・ePOS_SDK_Android_Migration_Guide_en_revx.pdf
  マイグレーションガイドガイド（英語版）です。

・TM-DT_Peripherals_ja_revx.pdf
  TM-DT シリーズ周辺機器制御ガイドです。

・TM-DT_Peripherals_en_revx.pdf
  TM-DT シリーズ周辺機器制御ガイド（英語版）です。

・README.ja.txt
  本書です。

・README.en.txt
  本書（英語版）です。

・OPOS_CCOs_1.14.001.msi
  OPOS CCO インストーラーパッケージです。

3.その他留意点

・使用方法、使用上の注意、等の詳細は、ユーザーズマニュアルを参照し、
  ご使用ください。
・インターフェイスがUSBの場合、事前にアプリケーションでUSBデバイスへのアク
  セス許可を取得することを推奨します。アプリケーションでアクセス許可を取得
  する方法を、以下に記します。
   1. AndroidManifest.xml に以下のコードを追記します。
      <manifest ...>
          <application>
              <activity ...>
                  <intent-filter>
                      <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
                  </intent-filter>
                  <meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
                    android:resource="@xml/device_filter" />
              </activity>
          </application>
      </manifest>

   2. リソースファイルに res/xml/device_filter.xml を追加し、device_filter.xml ファイルに以下のコードを記述します。
      <?xml version="1.0" encoding="utf-8"?>  
      <resources>  
          <usb-device vendor-id="1208" />
      </resources>

  アクセス許可を取得する際には、ダイアログが表示されるのでOKボタンを選択し
  てください。

  また事前にUSBデバイスへのアクセス許可を取得せず、connectメソッドによりポ
  ートオープンする際には以下の注意点があります。
    ・アクセス許可取得のダイアログで、OKボタンを選択するとポートオープンに
      10秒前後の時間が掛かります。
    ・アクセス許可取得のダイアログで、キャンセルボタンを選択すると30秒のタ
      イムアウト待ちになります。

・Android StudioでminifyEnabledをtrueに設定する場合は、proguardファイルに
  以下を追加してください。

  -keep class com.epson.** { *; }
  -dontwarn com.epson.**

  proguardファイル(proguard-rules.pro)はbuild.gradleファイルに以下のように設定します。
     buildTypes {
        release {
           proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
     }

・印刷処理を繰り返し行う場合、Printerクラスのインスタンスの生成と破棄は繰り返し処理の外で行い、
  短い間隔で繰り返さないようにしてください。

4.制限事項

・以下のTMインテリジェントプリンターでは検索機能(Discoveryクラス)
  をサポートしていません。

    TM-DT シリーズ (TM-DT ソフトウェア Ver. 3.01 以前)
    TM-i  シリーズ（TM-i ファームウェア Ver. 4.30 以前）

  検索開始してからTMインテリジェントプリンターの電源を入れた場合、TMインテリジェントプリンターが
  検出されないことがあります。その場合は、TMインテリジェントプリンターが印刷可能になるまで時間を空けてから、
  再度検索を開始してください。

5.バージョン履歴

  Version 2.14.0
    ・サポートTMプリンターを追加
      ・TM-m30II
      ・TM-m30II-H
    ・新機能追加
      ・まとめ反転印刷に対応
      ・UTF-8の印刷に対応

  Version 2.13.0
    ・対応Androidバージョンを追加
      ・Android 10.0
    ・対応Android端末を追加
      ・armeabi-v7a 対応端末
      ・x86 対応端末

  Version 2.12.2
    ・不具合修正
      ・endTransaction APIを実行すると、極めて低い確率でフリーズする現象を修正

  Version 2.12.1
    ・不具合修正
      ・連続してAPIを実行すると稀にフリーズする現象を修正

  Version 2.12.0
    ・サポートＴＭプリンターを追加
      ・TM-T20III
      ・TM-T82III
    ・TM-T88VIにバーコードスキャナが接続できるようになりました。
    ・DiscoveryクラスのfilterOptionに以下を追加
      ・bondedDevices：以前に接続したことのあるデバイスを含めて検索する
    ・不具合修正
        ・TM-U220でconnect/disconnect APIを連続して実行すると、不正な印字を行う現象を修正
        ・同時にプリンターから複数のレスポンスを受信すると、稀にクラッシュする現象を修正
        ・自動釣銭機をDisconnect中に、getStatus APIを実行するとクラッシュする現象を修正
        ・connect API実行後、端末をsleepして復帰するとCPUが100%になる現象を修正
        ・環境によって、稀にconnect APIに時間がかかる現象を修正

  Version 2.11.0
    ・対応Androidバージョンを追加
      ・Android 8.1
      ・Android 9.0
    ・サポートＴＭプリンターを追加
        ・TM-T20IIIL（海外モデルのみ）
        ・TM-T20X（海外モデルのみ）
        ・TM-T81III（海外モデルのみ）
        ・TM-T82X（海外モデルのみ）
        ・TM-T82IIIL（海外モデルのみ）
        ・TM-T83III（海外モデルのみ）
        ・TM-T100（海外モデルのみ）
    ・TM-U330で、addTextSizeおよびaddLogo APIが使用できるようになりました
    ・TM-DTシリーズ、TM-iシリーズ以外でもgetAdmin、getLocation APIが使用できるようになりました
    ・不具合修正
        ・Printerがオフライン状態の時に印刷する（あるいは印刷中にPrinterがオフライン状態になる）と、コールバックが３０秒以上返ってこない現象を修正
        ・PrinterクラスでSendData APIのコールバックを待たずにsendDataを複数回送ると、印刷の順番が入れ替わる現象を修正
        ・TM-P80でaddLayout APIを呼ぶと、パラメータエラーを返す現象を修正
        ・Connect APIで稀にフリーズする現象を修正
        ・Discoveryクラスの検索実行中にクラッシュする現象を修正

  Version 2.10.0
    ・CATクラスに以下を追加
      ・API追加
        ・getOposErrorCode：OPOS拡張エラーコード取得
        ・sendDirectIOCommand：任意DirectIOコマンド送信
      ・イベント追加
        ・setDirectIOCommandReplyEventListener：DirectIOコマンドの実行結果受信
        ・setStatusUpdateEventListener：OPOSの StatusUpdateEventを通知
      ・コールバックコード追加
        ・CODE_ERR_OPOSCODE：OPOS拡張エラーが発生した
    ・AArch64 端末、x86-64 端末に対応

  Version 2.9.2a
    ・ライブラリはVer.2.9.2と同一です
    ・サポートTMプリンターを追加
      ・TM-T70II-DT2
      ・TM-T88VI-DT2

  Version 2.9.2
    ・不具合修正
      ・TM-U330（海外モデルのみ）のaddTextStyle()とaddImage()で、2色印字が行えない現象を修正

  Version 2.9.0
    ・TM-m30にバーコードスキャナが接続できるようになりました。
    ・Printerクラスのconnect()のtargetパラメータにデバイスIDを付加しない場合に限り、
      Printer.EVENT_POWER_OFF以外のステータス更新イベントが、intervalの値によらず、
      すぐに通知されるようになりました。
    ・不具合修正
      ・LineDisplay接続をサポートしていないTMプリンターで、プリンターがオフライン状態の時に
        connectを実行し、その後にsendDataを実行するとオフライン要因のエラーが返らず、
        TIMEOUTエラーが返る現象を修正
      ・Printerの印刷またはLineDisplayの表示がステータスモニターの監視間隔だけ遅延することがある現象を修正
      ・TCP接続でconnect()実行中に、物理的な通信切断（LANケーブルを抜くなど）が発生すると
        アプリケーションが強制終了することがある現象を修正
      ・UB-E03が存在するネットワーク環境で、parseNFC()が失敗することがある現象を修正

  Version 2.8.1
    ・サポート周辺機器を追加
      ・CashChangerクラス
      ・POSKeyboardクラス
      ・CATクラス
      ・OtherPeripheralクラス
      ・MSRクラス
    ・パッケージに以下を追加
      ・デバイス制御プログラム用のサンプルプログラム
      ・TM-DT シリーズ周辺機器制御ガイド
      ・OPOS CCO インストーラーパッケージ
    ・ライブラリに組み込んでいるOpenSSLをバージョン1.0.2kからバージョン1.0.2oに更新

  Version 2.7.0
    ・サポートTMプリンターを追加
      ・TM-H6000V（海外モデルのみ）
    ・対応Androidバージョンを追加
      ・Android 7.1.2
      ・Android 8.0
    ・TCP通信において一定時間送受信が無くても切断されないようにしました
    ・不具合修正
      ・Discoveryクラスのstop APIの実行に時間がかかることがある現象を修正
      ・ePOS-Print 互換APIで、USB接続で使用していると、アプリケーションが強制終了
        することがある現象を修正
      ・ネットワーク接続されたプリンターを検索するとアプリケーションが強制終了する
        ことがある現象を修正

  Version 2.6.0
    ・サポートTMプリンターを追加
      ・TM-T88VI
    ・不具合修正
      ・Printerの印刷またはLineDisplayの表示がステータスモニターの監視間隔だけ遅延することがある現象を修正

  Version 2.5.2
    ・不具合修正
      ・onPtrReceive()が呼ばれた直後に別スレッドでdisconnect（）を実行すると、
        アプリケーションがハングアップすることがある現象を修正
        
  Version 2.5.1a
    ・ユーザーズマニュアルの誤記を修正しました

  Version 2.5.1
    ・対応Androidバージョンを追加
      ・Android 7.1.1
    ・TMプリンターにカスタマーディスプレイを接続して使用していて、プリンターの電源OFF/ONなどによって
      通信が切断された場合、プリンターかカスタマーディスプレイのどちらかで接続し直すことで、
      通信が復帰するようにしました
    ・PrinterStatusInfoのautoRecoverErrorに“Printer.COVER_OPEN”を追加しました
    ・プリンターファームウェアの許容量を超えるデータサイズの印刷ジョブが送信された場合に
      返すコールバックコード“CODE_ERR_REQUEST_ENTITY_TOO_LARGE”を追加しました
    ・ライブラリに組み込んでいるOpenSSLをバージョン1.0.2hからバージョン1.0.2kに更新
    ・不具合修正
      ・LineDisplayのdisconnect APIを実行すると、“ERR_NOT_FOUND”が返ることがある現象を修正
      ・ステータスモニターの更新間隔の初期値が1秒になることがある現象を修正
      ・SSLを有効にしたTMインテリジェントプリンターに対して、ステータスモニターを有効にすると
        アプリケーションが強制終了することがある現象を修正
      ・TCP/IP接続時、検索の開始と終了を繰り返すとアプリケーションが強制終了することがある現象を修正

  Version 2.5.0
    ・サポートTMプリンターを追加
      ・TM-P80
    ・不具合修正
      ・disconnect API実行時にプリンターの電源をOFFすると
        ハングアップすることがある現象を修正
      ・ポータブル(TM-P)系プリンターで印刷中にオフラインが発生すると
        sendData APIのコールバックが通知されないことがある現象を修正

  Version 2.4.2
    ・不具合修正
      ・42桁モードをサポートしているプリンターに対してaddImage APIを実行した場合、
        通常桁数モードで最大幅まで画像が印刷ができない現象を修正

  Version 2.4.1
    ・ライブラリに組み込んでいるOpenSSLを、新しいバージョンに更新

  Version 2.4.0
    ・サポートTMプリンターを追加
      ・TM-T88VI-iHUB（海外モデルのみ）
    ・対応Androidバージョンを追加
      ・Android 6.0.1
      ・Android 7.0
    ・不具合修正
      ・副接続I/Fからの印刷が、失敗する場合がある現象を修正
      ・副接続I/Fからの印刷結果通知に時間がかかる現象を修正
      ・getPrefix APIで取得される情報が正しくない場合がある現象を修正
      ・インテリジェントプリンターの周辺機器検索結果（DeviceInfo構造体の内容）が正しくない現象を修正
      ・プリンターがオフラインの状態でsendData APIを実行した後、オフライン要因を取り除いても、
        次回以降のsendData APIがエラーになる場合がある現象を修正
      ・TCP/IP接続で、印刷結果通知がエラーとなった時に、disconnect APIを実行すると
        ハングアップすることがある現象を修正
    ・印刷データもしくはディスプレイ表示データがプリンターファームウェアの許容量を超えて送信された場合に
      返すコールバックコード“CODE_ERR_TOO_MANY_REQUESTS”を追加しました。

  Version 2.3.0
    ・サポートTMプリンターを追加
      ・TM-T60（海外モデルのみ）
    ・不具合修正
      ・TM-インテリジェントプリンターに対してdisconnect APIを実行した場合、関数が返答しないことがある現象を修正
      
  Version 2.1.0
    ・サポートTMプリンターを追加
      ・TM-T88VI（海外モデルのみ）
    ・対応Androidバージョンを追加
      ・Android 6.0
    ・LogクラスにSDKのバージョンを取得するためのgetSdkVersion APIを追加
    ・不具合修正
      ・印刷中にOFFLINEが発生した場合、OFFLINE要因を取り除くと残りのデータが印刷される現象を修正
      ・TM-P20/TM-P60II/TM-m10/TM-m30/TM-T90IIで印刷中に電源OFFした場合、プリンターステータスの接続状態が"接続中"になる現象を修正
      ・disconnect API実行時に、切断イベントが発生しない現象を修正
      ・parseNFC APIに複数のタグで構成されたNFCタグのデータを指定しても、先頭のタグしか解析されない現象を修正
      ・addFeedPosition APIで、正しく紙送りが行われない現象を修正

  Version 2.0.0
    ・新規リリース

