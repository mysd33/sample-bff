package com.example.fw.common.digitalsignature.pades;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.example.fw.common.exception.SystemException;
import com.example.fw.common.keymanagement.Certificate;
import com.example.fw.common.message.CommonFrameworkMessageIds;

import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.spi.x509.CertificateReorderer;

/**
 * 証明書関連のパッケージ内ユーティリティクラス
 */
final class CertificateUtils {
    private CertificateUtils() {
    }

    /**
     * 証明書チェーンを証明書パス順に並び替え、CertificateTokenのリストとして返す
     * 
     * @param certificates 対象の証明書チェーン
     * @return 証明書パス順に並び替えられたCertificateTokenのリスト
     * @throws SystemException 証明書の取得や有効性確認に失敗した場合にスローされる例外
     */
    static List<CertificateToken> exchageOrderdCertifcateTokens(List<Certificate> certificates) {
        if (certificates == null || certificates.isEmpty()) {
            return List.of();
        }
        // 証明書をX.509証明書形式で取得
        List<CertificateToken> tempCertificateTokens = new ArrayList<>();
        for (Certificate certificate : certificates) {
            X509Certificate x509Certificate;
            try {
                x509Certificate = certificate.getX509Certificate();
            } catch (CertificateException | IOException e) {
                throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9007);
            }
            // 証明書の有効性を確認
            CertificateUtils.validateCertificate(x509Certificate);
            // CertificateTokenに変換してリストに追加
            tempCertificateTokens.add(new CertificateToken(x509Certificate));
        }
        // エンドエンティティを最初とする証明書パス順に証明書チェーンを並び替え
        CertificateReorderer reorderer = new CertificateReorderer(tempCertificateTokens);
        return reorderer.getOrderedCertificates();
    }

    /**
     * 証明書の有効性を検証する
     * 
     * @param certificate 検証対象の証明書
     * @throws SystemException 有効期限外の場合にスローされる例外
     */
    static void validateCertificate(X509Certificate certificate) {
        Assert.notNull(certificate, "Certificate must not be null");
        try {
            // 証明書の有効期限を確認
            certificate.checkValidity();
        } catch (CertificateNotYetValidException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9004);
        } catch (CertificateExpiredException e) {
            throw new SystemException(e, CommonFrameworkMessageIds.E_FW_PDFSGN_9005);
        }
    }

}
