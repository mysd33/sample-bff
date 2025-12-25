package com.example.fw.common.file;

import java.io.File;

/**
 * ローカルファイルシステムでの一時ファイル操作を抽象化するインターフェース。
 */
public interface TempFileCreator {
    /**
     * 一時ファイルを作成する。 <br>
     * 
     * 作成したファイルは、一定時間後に自動的に削除されることが期待される。
     *
     * @param prefix 一時ファイル名の接頭辞
     * @param suffix 一時ファイル名の接尾辞
     * @return 作成された一時ファイルのFileオブジェクト
     */
    File createTempFile(String prefix, String suffix);

    // TODO: 一時ディレクトリの作成メソッドを追加する
    /**
     * 一時ディレクトリを作成する。 <br>
     * 
     * 作成したディレクトリは、ディレクトリ内のファイルごと、一定時間後に自動的に削除されることが期待される。
     *
     * @param prefix 一時ディレクトリ名の接頭辞
     * @return 作成された一時ディレクトリのFileオブジェクト
     */
    // File createTempDirectory(String prefix);

}
