package com.example.fw.common.objectstorage;

/**
 * 
 * オブジェクトストレージアクセスインタフェース
 * 
 *
 */
public interface ObjectStorageFileAccessor {

    /**
     * オブジェクト（ファイル）をアップロードする
     * @param uploadObject アップロード対象のオブジェクト
     */
    void upload(UploadObject uploadObject);
    
    /**
     * オブジェクト（ファイル）をダウンロードする。
     * @param targetFilePath ターゲットファイルパス（バケット名を除く抽象的なパス）
     * @return ダウンロードしたオブジェクト
     */
    DownloadObject download(String targetFilePath);
}
