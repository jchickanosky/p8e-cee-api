package io.provenance.onboarding.frameworks.objectStore

import com.google.protobuf.Message
import io.provenance.objectstore.proto.Objects
import io.provenance.onboarding.domain.objectStore.ObjectStore
import io.provenance.onboarding.domain.usecase.objectStore.model.StoreAssetResponse
import io.provenance.onboarding.domain.usecase.objectStore.model.toModel
import io.provenance.onboarding.frameworks.config.ObjectStoreConfig
import io.provenance.onboarding.frameworks.provenance.extensions.getEncryptedPayload
import io.provenance.scope.encryption.crypto.Pen
import io.provenance.scope.encryption.domain.inputstream.DIMEInputStream
import io.provenance.scope.encryption.ecies.ProvenanceKeyGenerator
import io.provenance.scope.encryption.model.DirectKeyRef
import io.provenance.scope.encryption.proto.Encryption
import io.provenance.scope.objectstore.client.OsClient
import org.springframework.stereotype.Component
import tech.figure.asset.v1beta1.Asset
import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.TimeUnit

@Component
class ObjectStoreService(
    private val osConfig: ObjectStoreConfig
) : ObjectStore {

    // Encrypt and store a protobuf message using a random keypair for the signer
    private fun encryptAndStore(
        client: OsClient,
        message: Message,
        encryptPublicKey: PublicKey,
        additionalAudiences: Set<PublicKey>
    ): Objects.ObjectResponse {
        val future = client.put(
            message,
            encryptPublicKey,
            Pen(ProvenanceKeyGenerator.generateKeyPair(encryptPublicKey)),
            additionalAudiences
        )
        return future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
    }

    // Get a DIME by its hash and public key
    override fun getDIME(
        client: OsClient,
        hash: ByteArray,
        publicKey: PublicKey
    ): Encryption.DIME {
        val future = client.get(hash, publicKey)
        val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
        return res.dime
    }

    // Retrieve an encrypted asset as a byte array by its hash and public key
    override fun retrieve(
        client: OsClient,
        hash: ByteArray,
        publicKey: PublicKey
    ): ByteArray {
        val future = client.get(hash, publicKey)
        val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
        return res.getEncryptedPayload()
    }

    // Retrieve an encrypted asset as a byte array with its DIME by its hash and public key
    override fun retrieveWithDIME(
        client: OsClient,
        hash: ByteArray,
        publicKey: PublicKey
    ): Pair<Encryption.DIME, ByteArray> {
        val future = client.get(hash, publicKey)
        val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
        return Pair(res.dime, res.getEncryptedPayload())
    }

    // Retrieve the asset as a byte array and decrypt using the provided keypair
    override fun retrieveAndDecrypt(
        client: OsClient,
        hash: ByteArray,
        publicKey: PublicKey,
        privateKey: PrivateKey,
    ): ByteArray {
        val future = client.get(hash, publicKey)
        val res: DIMEInputStream = future.get(osConfig.timeoutMs, TimeUnit.MILLISECONDS)
        return res.getDecryptedPayload(DirectKeyRef(publicKey, privateKey)).readAllBytes()
    }

    override fun storeAsset(
        client: OsClient,
        asset: Asset,
        publicKey: PublicKey,
        additionalAudiences: Set<PublicKey>
    ): StoreAssetResponse = encryptAndStore(client, asset, publicKey, additionalAudiences).toModel()
}
