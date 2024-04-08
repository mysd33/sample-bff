# SpringBootのBackend For Frontend(BFF)アプリケーションサンプル

## 概要
* ユーザに画面を提供するSpringBootのサンプルアプリケーションである。
* ログイン後、TODOの取得、TODOの登録、TODOの完了、TODOの削除を行える画面を提供する。
* TODOの操作は別途作成されたTODOを管理するBackendアプリケーションのREST APIを利用している。
* 非同期処理/バッチアプリケーションを利用して、TodoリストファイルからTodoを一括登録を行える画面も提供する。
* ユーザはDBで管理されており、ユーザの登録、変更、削除の管理する画面も提供する。
![実装イメージ](img/sample-bff.png)

## プロジェクト構成
* sample-bff
    * 本プロジェクト。当該名称のリポジトリを参照のこと。Spring BootのWebブラウザアプリケーション（Backend for Frontend）で、ユーザがログイン後、TODOやユーザを管理する画面を提供する。また、画面やAPIからsample-batchへの非同期実行依頼も可能である。
        * デフォルトでは「spring.profiles.active」プロパティが「dev」になっている。プロファイルdevの場合は、RDB永続化にはH2DBによる組み込みDB、S3アクセスは無効化、セッション外部化は無効化、SQS接続はsample-batch側で組み込みで起動するElasticMQへ送信するようになっている。
        * プロファイルproductionの場合は、RDB永続化にはPostgreSQL(AWS上はAurora等）、セッション外部化はRedis(ローカル時はRedis on Docker、AWS上はElastiCache for Redis)、SQS接続はSQSへ送信するようになっている。
* sample-backend（またはsample-backend-dynamodb)
    * 別のプロジェクト。当該名称のリポジトリを参照のこと。Spring BootのREST APIアプリケーションで、sample-webやsample-batchが送信したREST APIのメッセージを受信し処理することが可能である。
        * sample-backendは永続化にRDBを使っているが、sample-backend-dynamodbは同じAPのDynamoDB版になっている。
        * デフォルトでは「spring.profiles.active」プロパティが「dev」になっている。プロファイルdevの場合は、RDB永続化にはH2DBによる組み込みDBになっている。また、sample-backend-dynamodbプロジェクトの場合は、AP起動時にDynamoDBの代わりに、DynamoDB Localを組み込みで起動し、接続するようになっている。
        * プロファイルproductionの場合は、RDB永続化にはPostgreSQL(AWS上はAurora等）になっている。また、sample-backend-dynamodbプロジェクトの場合は、DynamoDBに接続するようになっている。
* sample-batch
    * 別プロジェクト。当該名称のリポジトリを参照のこと。Spring JMSを使ったSpring Bootの非同期処理アプリケーションで、sample-webやsample-schedulelaunchが送信した非同期実行依頼のメッセージをSQSを介して受信し処理することが可能である。
        * デフォルトでは「spring.profiles.active」プロパティが「dev」になっている。プロファイルdevの場合は、AP起動時にSQSの代わりにElasticMQを組み込みで起動し、リッスンするようになっている。また、RDB永続化にはH2DBによる組み込みDBになっている。
        * プロファイルproductionの場合は、SQSをリッスンするようになっている。また、RDB永続化にはPostgreSQL(AWS上はAurora等）になっている。
* sample-schedulelaunch
    * 別プロジェクト。当該名称のリポジトリを参照のこと。SpringBootのCLIアプリケーションで、実行時に引数または環境変数で指定したスケジュール起動バッチ定義IDに対応するジョブの非同期実行依頼を実施し、SQSを介して、sample-batchアプリケーションのジョブを実行する。スケジュールによるバッチ起動を想定したアプリケーション。
        * デフォルトでは「spring.profiles.active」プロパティが「dev」になっている。プロファイルdevの場合は、SQS接続はsample-batch側で組み込みで起動するElasticMQへ送信するようになっている。
        * プロファイルproductionの場合は、SQS接続はSQSへ送信するようになっている。

## 画面一覧
* 作成している画面は以下の通り。

| 画面名 | 説明 | 画面イメージ |
| ---- | ---- | ---- |
| ログイン画面 | トップページの画面で、ユーザがログインするための画面。 | [画面](img/screen/screen1.png) |
| メニュー画面 | メニュー画面。ログインユーザのロールによって管理者メニューの表示有無が切り替わる。 |  [画面](img/screen/screen2.png) |
| Todo管理画面 | Todoリストの一覧表示、登録、完了、削除といった操作を実施できる画面。 | [画面](img/screen/screen3.png) |
| Todo一括登録画面 | Todoリストを記載したCSVファイルをアップロードし、非同期でTodoリストを登録できる画面。 | [画面](img/screen/screen4.png) |
| ユーザ一覧画面 | 登録されているユーザを一覧表示&CSVファイルダウンロードできる画面。 | [画面](img/screen/screen5.png) |
| ユーザ登録画面 | ユーザを新規登録するための画面。 | [画面](img/screen/screen6.png) |
| ユーザ詳細画面 | ユーザの詳細情報の表示と情報更新するための画面。 | [画面](img/screen/screen7.png) |

## 画面遷移図
![画面遷移図](img/screen-flow.png)

# 事前準備
* 以下のライブラリを用いているので、EclipseのようなIDEを利用する場合には、プラグインのインストールが必要
    * [Lombok](https://projectlombok.org/)
        * [Eclipseへのプラグインインストール](https://projectlombok.org/setup/eclipse)
        * [IntelliJへのプラグインインストール](https://projectlombok.org/setup/intellij)
    * [Mapstruct](https://mapstruct.org/)
        * [EclipseやIntelliJへのプラグインインストール](https://mapstruct.org/documentation/ide-support/)

## EclipseやIntelliJ等での動作確認
* MainクラスをSampleBffApplicationとして、Spring Bootアプリケーションを起動します。
* ブラウザまたは、REST APIクライアントツールを使って、ヘルスチェックポイントエンドポイントをAPIを呼び出す。ローカル実行の場合8080でポートで起動する。
    * GET http://localhost:8080/actuator/health
        * 正常に起動していれば、以下返却する。
        ```json
        {
            status: "UP"
        }    
        ``` 
* ローカル実行の場合、8080ポートで起動するので、ブラウザで、以下にアクセスするとログイン画面に遷移する。
    * http://localhost:8080/
* ログイン画面が表示されたら、例えば以下のユーザ情報を入力する。

    | ユーザID | パスワード | ロール |
    | ---- | ---- | ---- |
    | yamada@xxx.co.jp | password | 管理者 |
    | tamura@xxx.co.jp | password | 一般ユーザ |
* ログイン後、メニューが表示される。
    * 「Todo管理」ボタンを押下するとTodo管理、Todo一括登録の画面を表示する。
    * 「管理者」ロールでログインしている場合のみ「ユーザ管理」ボタンが表示され、ボタンを押下すると、ユーザ管理画面を表示する。

## 非同期処理実行の操作手順について
  * 本アプリから非同期処理/バッチアプリ（sample-batch）への非同期処理実行依頼が可能である。動作手順は、sample-batchのREADME.mdを参照すること。

## OpenAPI
* Springdoc-openapiにより、RestControllerの実装からAPIドキュメントをリバースエンジニアリングできる
    * アプリケーションを起動し、以下のURLへアクセスするとそれぞれjson、yaml、html形式のドキュメントを表示する。
    * http://localhost:8000/v3/api-docs
        * json形式のドキュメント
    * http://localhost:8000/v3/api-docs.yaml    
        * yaml形式のドキュメント
    * http://localhost:8000/swagger-ui.html
        * html形式（Swagger-UI）のドキュメント  

## Redisのローカル起動
* MavenのデフォルトのProfile設定では、Spring Session Data Redisのjarを読み込まないようにして無効化し、オンメモリでのセッション管理となっているので、何もしなくてよい。
* MavenのProfileを「production」に切り替えてビルドした実行可能jarでは、Spring Session Data Redisが有効化されセッションを外部管理するため、Redisサーバが必要となる。
    * Spring Boot3系より、AutoConfigureでSpring Session Data Redisがクラスパスに存在するかによって有効になるため、mavenのプロファイルも「production」を指定してビルドするとSpring Session Data Redisが有効になる。
    * ローカル実行時は、mvnコマンドで-P prroductionを指定してビルドする。Eclipse上では、プロジェクトのプロパティから「Maven」→「Active Maven Profiles」に「production」を追加してビルドする。    
    * AWS上でAPを起動する場合はElastiCache for Redisを起動しておくことを想定している。
* Redisの利用するようなケースは、通常、Spring Bootのプロファイルも「production」に切り替えることを前提としている。
* Profile「procution」でビルドしたAPをローカル実行する場合は、AP起動前にあらかじめ、redisをDockerで起動しローカル実行しておく必要がある。以下で、Redisのローカル実行手順を示す。
    * DockerによるRedisのローカル実行手順
        * 以下のコマンドで、Redisを起動し6379番ポートで公開する。
        ```sh
        docker run --name test-redis -p 6379:6379 -d redis
        ```
        * redisコンテナに入ってredis cliにより接続
        ```sh
        docker exec -i -t test-redis /bin/bash
        #コンテナ内のターミナルにログイン
        > redis-cli
        
        #「keys *」コマンド等で、redisにセッション情報が格納されたか確認できる
        > keys *
        ```
    * RedisのKeyspace-Notificationを有効化して、キーの有効期限切れ（セッションタイムアウト）の検知できるように設定する必要がある
        * https://docs.spring.io/spring-session/reference/api.html#api-redisindexedsessionrepository-sessiondestroyedevent        
        * https://redis.io/docs/manual/keyspace-notifications/
            * Spring Session Data Redisはデフォルトで、Keyspace-Notificationを有効化してくれるが、Elasti Cache for Redisではconfigコマンドの実行が禁止されているため、当該サンプルAPでは、application-production.ymlに「spring.session.redis.configure-action」を「none」で設定しているため、redis-cliでconfigコマンド実行する手順としている。
                * configコマンドでの「gxE」の意味                
                    * g: DEL, EXPIRE, RENAMEのような一般的なコマンド。
                    * x: 期限切れ（Expired）イベント。キーが有効期限切れになる毎に生成される
                    * E: キーイベント
        ```sh
        #コンテナ内のターミナルにログイン
        > redis-cli
        #configコマンドでKeyspace-Notificationを有効化
        > config set notify-keyspace-events gxE
        #設定の確認        
        > config get notify-keyspace-events
        ```

        * なお、ElastiCacheでKeyspace-Notificationを有効化するには、カスタムキャッシュパラメータグループで notify-keyspace-events パラメータを使用して、キースペース通知を有効にする。
            * https://aws.amazon.com/jp/premiumsupport/knowledge-center/elasticache-redis-keyspace-notifications/

## PostgreSQLのローカル起動
* Spring BootのProfileが「dev」（デフォルト）でSpringBootアプリケーションを実行する場合、H2DBが起動するので、何もしなくてよい。
* Profileを「production」に切り替えてSpringBootアプリケーションを実行する場合、DBがPostgreSQLで動作する設定になっているため、事前にPostgreSQLを起動する必要がある。
    * AWS上でAPを起動する場合はAurora for PostgreSQLや、RDS for PostgreSQLを起動しておくことを想定している。
* Profile「procution」でAPをローカル実行する場合は、AP起動前にあらかじめ、PostgreSQLをDockerで起動しローカル実行しておく必要がある。以下で、PostgreSQLのローカル実行手順を示す。
```sh
#Postgres SQLの起動
docker run --name test-postgres -p 5432:5432 -e POSTGRES_PASSWORD=password -d postgres
#Postgresのコンテナにシェルで入って、psqlコマンドで接続
docker exec -i -t test-postgres /bin/bash
> psql -U postgres

# psqlで、testdbデータベースを作成
postgres> CREATE DATABASE testdb;
```

## SQSの設定
* Spring BootのProfileが「dev」でSpringBootアプリケーションを実行する場合、「sample-batch」アプリケーション側で、ElasitqMQが起動し、「SampleQueue」という名前のキューを作成し、それを使ってメッセージ送信するので、何もしなくてよい。
* Profileが「production」に切り替えてSpringBootアプリケーションを実行する場合、事前にAWS上にSQSのバケットを作成する必要がある。
    * 「production」に切り替えるには、例えばJVM引数を「-Dspring.profiles.active=production」に変更するか、環境変数「SPRING_PROFILES_ACTIVE=production」を設定する等して、sample-bff、sample-batchの両方のプロジェクトのプロファイルを「production」に変えて実行する。
    * 「SampleQueue」という名前のキューを作成すればよいが、キュー名を変更したい場合はapplication-production.ymlの「delayed.batch.queue」プロパティを作成したキュー名に変更する。
        * 「sample-batch」アプリケーション側も変更が必要

## S3の設定
* Spring BootのProfileが「dev」でSpringBootアプリケーションを実行する場合、S3アクセスは無効化し、ローカルのファイルシステムアクセスする設定になっている。
    * application-dev.ymlの「aws.s3.localfake.type」が「file」であり、「aws.s3.localfake.base-dir」を一時保存するファイルシステムのディレクトリパスが現状、C:\tmpになっているので、フォルダの変更が必要な場合は、変更する。
        * 「sample-batch」アプリケーション側も変更が必要
* Profileが「dev」でも、S3のローカル起動用のFake（MinIOやs3rver）を起動したい場合には、以下の通り
    * MinIOの場合
        * [MinIOのサイト](https://min.io/download#/windows)の手順に従い、インストールし、MinIOを起動
        * 以下は、Windows版での起動例
            * C:\minioフォルダにminio.exeを格納して、起動した例（デフォルトポート9000番ポートで起動、コンソールは9001番ポートで起動）
        ```sh        
        C:\minio\minio.exe server C:\minio\data --console-address ":9001"
        ```
        *  application-dev.ymlの「aws.s3.localfake.type」を「minio」に変更し、以下の通り設定
        ```yaml
        aws:
          s3:
            localfake:
              type: minio
              port: 9000
              access-key-id: minioadmin
              secret-access-key: minioadmin
            bucket: mysd33bucket123
        ```
    * s3rverの場合
        * [s3rverのサイト](https://github.com/jamhall/s3rver)の手順に従い、npmでインストールし、s3rverを起動
        * 以下、起動例
        ```
        s3rver -d C:\s3rver
        ```
        *  application-dev.ymlの「aws.s3.localfake.type」を「s3rver」に変更し、以下の通り設定
        ```yaml
        aws:
          s3:
            localfake:
              type: s3rver
              port: 4568
            bucket: mysd33bucket123
        ```

* Profileが「production」に切り替えてSpringBootアプリケーションを実行する場合、S3を使用する設定になっているため、事前にAWS上に、S3のバケットを作成する必要がある。
    * application-production.ymlの「aws.s3.bucket」プロパティを作成したバケット名に変更する。
    * APがS3にアクセスする権限が必要なので、開発端末上でローカル実行する場合はS3のアクセス権限をもったIAMユーザのクレデンシャル情報が「%USERPROFILE%/.aws/credentials」や「~/.aws/credentials」に格納されている、もしくはEC2やECS等のAWS上のラインタイム環境で実行する場合は対象のAWSリソースにSQSのアクセス権限を持ったIAMロールが付与されている必要がある。

## X-Rayデーモンのローカル起動
* Spring BootのProfileに「xray」を追加してSpringBootアプリケーションを実行する場合、X-Rayにトレースデータを送信するため、X-Rayデーモンを起動しておく必要がある。
* ローカルでのX-Rayデーモンの起動方法は以下を参照すること。
    * デーモンのダウンロード    
        * https://docs.aws.amazon.com/ja_jp/xray/latest/devguide/xray-daemon.html
    * デーモンのローカル実行
        * https://docs.aws.amazon.com/ja_jp/xray/latest/devguide/xray-daemon-local.html
* Windows版の場合の起動例（C:\aws-xray-daemon-windows-process-3.xフォルダにxray_windows.exeを格納した例）
    ```    
    C:\aws-xray-daemon-windows-process-3.x\xray_windows.exe -o -n ap-northeast-1
    ```    

## Dockerでのアプリ起動
### ローカルでDocker実行（Profileを「dev」でSpringBoot実行）の場合
* Mavenビルド
```sh
#Windows
.\mvnw.cmd package
#Linux/Mac
./mvnw package
```

* ローカルでDockerビルド
```sh
docker build -t XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest .
```

* ローカルでDocker実行（Profileを「dev」でSpringBoot実行）
```sh
#デフォルトのプロファイルの場合
docker run -d -p 8080:8080 --name samplebff  --env API_BACKEND_URL=http://host.docker.internal:8000 XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest

#logをjson形式に変更する場合
docker run -d -p 8080:8080 --name samplebff --env SPRING_PROFILES_ACTIVE=dev,log_container --env API_BACKEND_URL=http://host.docker.internal:8000 XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest
```

### ローカルでDocker実行（Profileを「production」でSpringBoot実行）　の場合
* Mavenビルド（Profileを「production」でビルド）
```sh
#Windows
.\mvnw.cmd package -P production
#Linux/Mac
./mvnw package -P production
```

* ローカルでDockerビルド
```sh
docker build -t XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest .
```

* ローカルでDocker実行（Profileを「production」でSpringBoot実行）　
    * ※Redisのローカル起動、PostgreSQLのローカル起動、AWS上のSQS作成、S3作成も必要

```sh
#logをデフォルト（タブ区切り）形式のままの場合
docker run -d -p 8080:8080 -v %USERPROFILE%\.aws\:/home/app/.aws/ --name samplebff --env SPRING_PROFILES_ACTIVE=production,log_default --env API_BACKEND_URL=http://host.docker.internal:8000 --env SPRING_REDIS_HOST=(ローカルPCのプライベートIP) --env SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/testdb XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest

#logをjson形式に変更する場合
docker run -d -p 8080:8080 -v %USERPROFILE%\.aws\:/home/app/.aws/ --name samplebff --env SPRING_PROFILES_ACTIVE=production,log_container --env API_BACKEND_URL=http://host.docker.internal:8000 --env SPRING_REDIS_HOST=(ローカルPCのプライベートIP) --env SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/testdb XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest
```

## ECRプッシュ
```sh
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com
docker push XXXXXXXXXXXX.dkr.ecr.ap-northeast-1.amazonaws.com/sample-bff:latest
```

## 参考：Java Flight Recorder（JFR）の利用
* 以下のサイトから、JMC（Java Mission Controll）をダウンロードしインストール
    * https://jdk.java.net/jmc/8/

* Javaコマンド実行時、以下のJFRのオプションをつけて、SpringBootのJava APを起動
```
-XX:StartFlightRecording:filename=recording.jfr,duration=10s
```

* APを動作させて、JFRを記録する

* AP実行が終了したら、出力されたrecording.jfrファイルを、JMCで開く
    * 例えば、時間のかかっている処理を特定するには、JMC画面のアウトラインから「メソッドプロファイリング」を選択して確認できる。
    * また、JMC画面のアウトラインから「Javaアプリケーション」を選択すると、時系列で、CPU使用率やヒープ使用量、メソッドプロファイリングを確認することができる。
    * その他、GCの発生状況、スレッドダンプでのデッドロックの確認等、さまざまな情報が確認できる。
* 参考
    * https://www.alpha.co.jp/blog/202402_02/
    * https://software.fujitsu.com/jp/manual/manualfiles/m230004/b1ws1414/03z200/b1414-00-08-03-01.html
    * https://software.fujitsu.com/jp/manual/manualfiles/m230004/b1ws1414/03z200/b1414-00-08-03-02.html

## 参考： JFRによるSpringBootでのアプリケーションのスタートアップの追跡
* Spring Boot APのMainクラス（このサンプルではSampleBffApplication.java）に、以下のようにFlightRecorderApplicationStartupを設定することで、Spring Boot APのスタートアップの追跡が可能になる。

```java
@SpringBootApplication
public class SampleBffApplication {
    public static void main(String[] args) {
        …
        // コメントアウト
        //SpringApplication.run(SampleBffApplication.class, args);
        
        // FlightRecorderApplicationStartupを使った、JFRのアプリケーションのスタートアップの追跡する場合のAP起動例
        SpringApplication app = new SpringApplication(SampleBffApplication.class);
        app.setApplicationStartup(new FlightRecorderApplicationStartup());
        app.run(args);
    }
}
```

* Javaコマンド実行時、以下のJFRのオプションをつけて、SpringBootのJava APを起動
```
-XX:StartFlightRecording:filename=recording.jfr
```

* APを動作させて、JFRを記録する

* AP実行が終了したら、出力されたrecording.jfrファイルを、JMCで開く
 
* JMC画面のアウトラインから、「イベント・ブラウザ」を選択すると、「Spring Application」という項目が追加で表示されているのが分かる。

* 「Startup Step」イベントで、どのBeanのインスタンス化に時間がかかっているか等分かる。

* 参考 
    * https://hirakida29.hatenablog.com/entry/2021/01/23/213825
    * https://spring.pleiades.io/spring-boot/docs/current/reference/html/features.html#features.spring-application.startup-tracking


## ソフトウェアフレームワーク
* 本サンプルアプリケーションでは、ソフトウェアフレームワーク実装例も同梱している。簡単のため、アプリケーションと同じプロジェクトでソース管理している。
* ソースコードはcom.example.fwパッケージ配下に格納されている。    
    * 本格的な開発を実施する場合には、業務アプリケーションと別のGitリポジトリとして管理し、CodeArtifactやSonatype NEXUSといったライブラリリポジトリサーバでjarを管理し、pom.xmlから参照するようにすべきであるし、テストやCI/CD等もちゃんとすべきであるが、ここでは、あえて同じプロジェクトに格納してノウハウを簡単に参考にしてもらいやすいようにしている。
* 各機能と実現方式は、以下の通り。

| 分類 | 機能 | 機能概要と実現方式 | 拡張実装 | 拡張実装の格納パッケージ |
| ---- | ---- | ---- | ---- | ---- |
| オンライン | オンラインAP制御 | SpringMVCの機能を利用し、ユーザからの要求受信、ビジネスロジック実行、応答返却まで一連の定型的な処理を実行を制御する。 | - | - |
| | ファイルダウンロード | SpringMVCの機能を利用し、CSVファイルをダウンロードする機能を提供する。 | ○ | com.example.fw.web.view |
| | 共通画面レイアウト| Thymeleafの機能を利用し、ヘッダ、フッタ等の全画面共通のHTMLのレイアウトを一元管理する。 | - | - |
| | ページネーション | Thymeleafの機能を利用し、一覧表示する際のページネーションの画面部品を提供する。 | ○ | com.example.fw.web.page |
|  | 認証・認可| Spring Securityを利用し、DBで管理するユーザ情報をもとに認証、認可を行う。 | - | - |
| | セッション管理 | 通常、SpringMVCのセッション管理機能で管理するが、オートスケーリング等の対応のため、APサーバ上で保持していたセッション情報をRedisサーバ（AWSの場合、ElastiCache for Redis）に外部化するためにSpring Session Data Redisを利用する。 | - | - |
| | 集約例外ハンドリング | SpringMVCのControllerAdviceやAOPを利用し、エラー（例外）発生時、エラーログの出力、DBのロールバック、エラー画面やエラー電文の返却といった共通的なエラーハンドリングを実施する。 | ○ | com.example.fw.web.advice、com.example.fw.web.aspect |
| | トランザクション管理機能 | Spring Frameworkのトランザクション管理機能を利用して、@Transactionalアノテーションによる宣言的トランザクションを実現する機能を提供する。 | - | - |
| | 分散トレーシング（X-Ray） | AWS X-Rayを利用して、サービス間の分散トレーシング・可視化を実現する。 | ○ | com.example.fw.web.aspect<br/>com.example.fw.servlet<br/>com.example.fw.common.async<br/>com.example.fw.common.dynamodb<br/>com.example.fw.common.httpclient<br/>com.example.fw.common.objectstorage |
| | ヘルスチェック | Spring Boot Actuatorを利用して、ヘルスチェックエンドポイントを提供する。その他、Micrometerメトリックの情報提供も行う。 | - | - |
| | グレースフルシャットダウン | SpringBootの機能で、Webサーバ（組み込みTomcat）のグレースフルシャットダウン機能を提供する 。 | - | - |
| | トランサクショントークンチェック | TERASOLUNA Server Frameworkの共通ライブラリの機能を利用して、不正な画面遷移を防止するトランザクションチェック機能を提供する 。 | - | com.example.fw.web.token |
| オン・バッチ共通 | RDBアクセス | MyBatisやSpringとの統合機能を利用し、DBコネクション取得、SQLの実行等のRDBへのアクセスのため定型的な処理を実施し、ORマッピングやSQLマッピングと呼ばれるドメイン層とインフラ層のインピーダンスミスマッチを吸収する機能を提供する。 | - | - |
| | オブジェクトストレージ（S3）アクセス | AWS SDK for Java 2.xのS3クライアント（S3Client)を使って、S3のアクセス機能を提供する。開発時にS3アクセスできない場合を考慮して通常のファイルシステムへのFakeに切り替える。 | ○ | com.example.fw.common.objectstorage |
| | HTTPクライアント | WebClientやRestTemplateを利用してREST APIの呼び出しやサーバエラー時の例外の取り扱いを制御する。 | ○ | com.example.fw.common.httpclient |
| | リトライ・サーキットブレーカ | Spring Cloud Circuit Breaker（Resillience4j）を利用し、REST APIの呼び出しでの一時的な障害に対する遮断やフォールバック処理等を制御する。また、WebClientのリトライ機能でエクスポネンシャルバックオフによりリトライを実現する。なお、AWSリソースのAPI呼び出しは、AWS SDKにてエクスポネンシャルバックオフによりリトライ処理を提供。 | - | - |
| | 分散トレーシング | Micrometer Tracingを利用して、トレースIDやスパンIDをAP間でのREST API呼び出しで引継ぎログに記録することで、分散トレーシングを実現する。 | - | - |
| | 非同期実行依頼 | Spring JMS、Amazon SQS Java Messaging Libraryを利用し、SQSの標準キューを介した非同期実行依頼のメッセージを送信する。 | ○ | com.example.fw.common.async |
| | 入力チェック| Java BeanValidationとSpringのValidation機能を利用し、単項目チェックや相関項目チェックといった画面の入力項目に対する形式的なチェックを実施する。 | ○ | com.example.fw.common.validation<br/>com.example.fw.web.validation |
| | メッセージ管理 | MessageResourceで画面やログに出力するメッセージを管理する。 | ○ | com.example.fw.common.message |
| | 例外 | RuntimeExceptionを継承し、エラーコード（メッセージID）やメッセージを管理可能な共通的なビジネス例外、システム例外を提供する。 | ○ | com.example.fw.common.exception |
| | ロギング | Slf4jとLogback、SpringBootのLogback拡張の機能を利用し、プロファイルによって動作環境に応じたログレベルや出力先（ファイルや標準出力）、出力形式（タブ区切りやJSON）に切替可能とする。またメッセージIDをもとにログ出力可能な汎用的なAPIを提供する。 | ○ | com.example.fw.common.logging |
| | プロパティ管理 | SpringBootのプロパティ管理を使用して、APから環境依存のパラメータを切り出し、プロファイルによって動作環境に応じたパラメータ値に置き換え可能とする。 | - | - |
| | オブジェクトマッピング | MapStructを利用し、類似のプロパティを持つリソースオブジェクトやDTOとドメインオブジェクト間で、値のコピーやデータ変換処理を簡単にかつ高速に行えるようにする。 | - | - |
| | DI | Springを利用し、DI（依存性の注入）機能を提供する。 | - | - |
| | AOP | SpringとAspectJAOPを利用し、AOP機能を提供する。 | - | - |
| | ボイラープレートコード排除 | Lombokを利用し、オブジェクトのコンストラクタやGetter/Setter等のソースコードを自動生成し、ボイラープレートコードを排除する。 | - | - |
| | S3 Local起動 | 開発端末での動作確認のため、APをローカル起動可能とするようファイルシステムアクセスに差し替えたFakeやS3互換のFakeのサーバ（MinIO、s3ver）に接続する機能を提供する。 | ○ | com.example.fw.common.objectstorage |

* 以下は、今後追加適用を検討中。

| 分類 | 機能 | 機能概要と実現方式 | 拡張実装 | 拡張実装の格納パッケージ |
| ---- | ---- | ---- | ---- | ---- |
| オンライン | OIDC認証 | Spring Securityの機能でOIDCの認証を行う。 | - | - |
| オン・バッチ共通 | プロパティ管理（SSM） | Spring Cloud for AWS機能により、APから環境依存のパラメータをAWSのSSMパラメータストアに切り出し、プロファイルによって動作環境に応じたパラメータ値に置き換え可能とする。 | - | - |
| | テストコード作成支援 | JUnit、Mockito、Springのテスト機能を利用して、単体テストコードや結合テストコードの実装を支援する機能を提供する。 | - | - |