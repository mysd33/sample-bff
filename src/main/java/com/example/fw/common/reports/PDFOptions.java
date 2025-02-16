package com.example.fw.common.reports;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jasperreports.pdf.type.PdfPermissionsEnum;

/**
 * PDF出力時のオプション設定を行うクラス
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PDFOptions {
    // 読み取りパスワード
    private String userPassword;
    // 権限パスワード
    private String ownerPassword;
    // 128Bit暗号化を使用するか
    private Boolean is128bitKey;
    // 暗号化設定されている場合の権限拒否設定
    private List<PdfPermissionsEnum> permissionsDenied;

    /**
     * PDFの暗号化設定が必要かどうかを返却します パスワード設定されている場合は暗号化設定が必要と判断します
     * 
     * @return trueの場合は暗号化設定が必要
     */
    public boolean isEncrypted() {
        return (userPassword != null && !userPassword.isEmpty()) || (ownerPassword != null && !ownerPassword.isEmpty());
    }

    /**
     * 128Bit暗号化設定が指定されているかどうかを返却します
     * 
     * @return
     */
    public boolean has128bitKey() {
        return is128bitKey != null;
    }

    /**
     * 権限拒否設定が指定されているかどうかを返却します
     * 
     * @return
     */
    public boolean hasPermissionsDenied() {
        return permissionsDenied != null && !permissionsDenied.isEmpty();
    }

}
