package com.example.fw.common.keymanagement;

import java.security.PublicKey;

/**
 * キーを管理するためのインターフェース。
 */
public interface KeyManager {

    /**
     * 新しい暗号鍵を作成します。
     *
     * @param keyAlias 作成する暗号鍵のエイリアス
     * @return 作成された暗号鍵の情報
     */
    KeyInfo createKey();

    /**
     * 新しい暗号鍵を作成します。
     *
     * @param keyAlias 作成する暗号鍵のエイリアス
     * @return 作成された暗号鍵の情報
     */
    KeyInfo createKey(String keyAlias);

    /**
     * 指定された暗号鍵にエイリアスを追加します。
     * 
     * @param keyInfo エイリアスを追加する暗号鍵の情報
     * @param alias   追加するエイリアス
     */
    void addKeyAlias(KeyInfo keyInfo, String alias);

    /**
     * エイリアスを削除します。
     * 
     * @param alias 削除するエイリアス
     */
    void deleteKeyAlias(String alias);

    /**
     * エイリアスから暗号鍵の情報を取得します。
     */
    KeyInfo findKeyByAlias(String alias);

    /**
     * 指定された暗号鍵の公開鍵を取得します。
     *
     * @param keyInfo 暗号鍵の情報
     * @return 公開鍵
     */
    PublicKey getPublicKey(KeyInfo keyInfo);

    /**
     * 指定されたキーIDの暗号鍵を削除します。
     *
     * @param keyInfo 削除する暗号鍵の情報
     * @return 削除された暗号鍵の情報
     */
    KeyInfo deleteKey(KeyInfo keyInfo);

    /**
     * CSR（証明書署名要求）を作成します。
     * 
     * @param keyInfo 証明書作成のための暗号鍵の情報
     * @param subject CSRに指定する項目
     * @return 証明書署名要求
     */
    CertificateSigningRequest createCsr(KeyInfo keyInfo, String subject);

    /**
     * CSR（証明書署名要求）を使ってテスト用に自己署名証明書を作成します。
     * 
     * @param keyInfo 証明書に自己署名をするための暗号鍵の情報
     * @param csr     証明書署名要求
     * @return 作成された自己署名証明書
     */
    Certificate createSelfSignedCertificate(CertificateSigningRequest csr, KeyInfo keyInfo);

    /**
     * CSR（証明書署名要求）をオブジェクトストレージに保存します。
     * 
     * @param csr
     * @param keyInfo
     */
    void saveCsrToObjectStorage(CertificateSigningRequest csr, KeyInfo keyInfo);

    /**
     * オブジェクトストレージからCSR（証明書署名要求）を取得します。
     * 
     * @param keyInfo CSRを取得するための暗号鍵の情報
     * @return 取得した証明書署名要求
     */
    CertificateSigningRequest getCsrFromObjectStorage(KeyInfo keyInfo);

    /**
     * オブジェクトストレージに自己署名証明書を保存します。
     * 
     * @param certificate 証明書
     * @param keyInfo     証明書を保存するための暗号鍵の情報
     */
    void saveSelfSignedCertificateToObjectStorage(Certificate certificate, KeyInfo keyInfo);

    /**
     * オブジェクトストレージに保存された自己署名証明書を取得します。
     * 
     * @param keyInfo 証明書を取得するための暗号鍵の情報
     * @return 取得した自己署名証明書
     */
    Certificate getSelfSignedCertificateFromObjectStorage(KeyInfo keyInfo);

    /**
     * オブジェクトストレージに保存された証明書を取得します。
     * 
     * @param keyInfo 証明書を取得するための暗号鍵の情報
     * @return 取得した証明書
     */
    Certificate getCertificateFromObjectStorage(KeyInfo keyInfo);

    /**
     * メッセージダイジェスト（ハッシュ）をもとに電子署名を生成します。
     * 
     * @param digestData 署名対象のダイジェスト（ハッシュ）データ
     * @param keyInfo    署名に使用する暗号鍵の情報
     * @return 生成された電子署名
     */
    Signature createSignatureFromDigest(byte[] digestData, KeyInfo keyInfo);

    /**
     * 生データをもとに電子署名を生成します。
     * 
     * @param rawData 署名対象のメッセージデータ
     * @param keyInfo 署名に使用する暗号鍵の情報
     * @return 生成された電子署名
     */
    Signature createSignatureFromRawData(byte[] rawData, KeyInfo keyInfo);

}