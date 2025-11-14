package com.example.fw.common.digitalsignature;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 署名時のオプション設定を行うクラス
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignOptions {
    // 署名の理由
    @Builder.Default
    private String reason = "";
    // 署名の場所
    @Builder.Default
    private String location = "";
    // 署名の可視性（true: 可視署名、false: 非可視署名）
    @Builder.Default
    private boolean visible = false;
    // 可視署名のテキスト
    @Builder.Default
    private String visibleSignText = "";
    // 可視署名の画像のパス
    private String visibleSignImagePath;
    // 可視署名の位置を表すRectangle（lower_x, lower_y, upper_x, upper_y）
    private float[] visibleSignRect;
    // 可視署名を表示するページ番号（1から始まる）
    @Builder.Default
    private int visibleSignPage = 1;
    // パスワード保護されたPDFの場合のパスワード
    private String userPassword;
    // セキュリティ設定したPDFの場合のオーナーパスワード（PKCS12BasiceReportSignerのみ利用）
    private String ownerPassword;
}