package io.provenance.onboarding.domain.provenance

import cosmos.base.abci.v1beta1.Abci
import cosmos.tx.v1beta1.TxOuterClass
import io.provenance.client.grpc.Signer
import io.provenance.hdwallet.wallet.Account
import io.provenance.onboarding.domain.usecase.common.model.ProvenanceConfig
import io.provenance.onboarding.domain.usecase.common.model.TxBody
import io.provenance.onboarding.domain.usecase.common.model.TxResponse
import io.provenance.onboarding.frameworks.provenance.ProvenanceTx
import io.provenance.scope.sdk.Session

interface Provenance {
    fun onboard(chainId: String, nodeEndpoint: String, account: Account, storeTxBody: TxBody): TxResponse
    fun executeTransaction(config: ProvenanceConfig, tx: TxOuterClass.TxBody, signer: Signer): Abci.TxResponse
    fun buildContractTx(config: ProvenanceConfig, tx: ProvenanceTx): TxOuterClass.TxBody?
}
