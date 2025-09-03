package com.example.fw.common.digitalsignature.pades;

import java.util.List;

import com.example.fw.common.keymanagement.KeyInfo;
import com.example.fw.common.keymanagement.KeyManager;
import com.example.fw.common.keymanagement.Signature;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.Digest;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AWSKmsSignatureToken implements SignatureTokenConnection {
    private final KeyManager keyManager;
    private final String keyId;

    @Override
    public void close() {
        // 何もしない
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, DigestAlgorithm digestAlgorithm, DSSPrivateKeyEntry keyEntry)
            throws DSSException {
        throw new UnsupportedOperationException(
                "sign(ToBeSigned, DigestAlgorithm, DSSPrivateKeyEntry) is not supported in AWSKmsSignatureTokenConnection");
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, SignatureAlgorithm signatureAlgorithm,
            DSSPrivateKeyEntry keyEntry) throws DSSException {
        return doSign(toBeSigned.getBytes(), signatureAlgorithm);
    }

    @Override
    public SignatureValue signDigest(Digest digest, DSSPrivateKeyEntry keyEntry) throws DSSException {
        throw new UnsupportedOperationException(
                "signDigest(Digest, DSSPrivateKeyEntry) is not supported in AWSKmsSignatureTokenConnection");
    }

    @Override
    public SignatureValue signDigest(Digest digest, SignatureAlgorithm signatureAlgorithm, DSSPrivateKeyEntry keyEntry)
            throws DSSException {
        throw new UnsupportedOperationException(
                "signDigest(Digest, SignatureAlgorithm, DSSPrivateKeyEntry) is not supported in AWSKmsSignatureTokenConnection");
    }

    private SignatureValue doSign(byte[] data, SignatureAlgorithm signatureAlgorithm) throws DSSException {
        // ダイジェストから署名を生成
        Signature signature = keyManager.createSignatureFromRawData(data, // ,
                KeyInfo.builder().keyId(keyId).build());
        return new SignatureValue(signatureAlgorithm, signature.getValue());
    }

    @Override
    public List<DSSPrivateKeyEntry> getKeys() throws DSSException {
        throw new UnsupportedOperationException("getKeys() is not supported in AWSKmsSignatureTokenConnection");
    }

}
