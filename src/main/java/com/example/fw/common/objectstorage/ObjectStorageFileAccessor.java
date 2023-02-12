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
     * @param prefix プレフィックス（バケット名を除く抽象的な-ターゲットファイルパス）
     * @return ダウンロードしたオブジェクト
     */
    DownloadObject download(String prefix);
}
