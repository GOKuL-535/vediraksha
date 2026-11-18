package com.example.util

import java.security.MessageDigest

object CryptoUtils {
    fun generateWageHash(
        id: Long,
        workerName: String,
        date: String,
        workType: String,
        quantity: Int,
        amount: Double,
        timestamp: Long
    ): String {
        val input = "VEDI_WAGE_${id}_${workerName}_${date}_${workType}_${quantity}_${amount}_${timestamp}_TAMPER_PROOF_SEAL"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateChemicalSeal(
        entryId: Long,
        roomId: Long,
        chemicalName: String,
        batchCode: String,
        quantityKg: Double,
        timestamp: Long
    ): String {
        val input = "CHEM_LOCK_${entryId}_R${roomId}_${chemicalName}_${batchCode}_${quantityKg}_${timestamp}"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    fun generateBiometricShiftHash(
        workerId: String,
        action: String,
        timestamp: Long,
        room: String
    ): String {
        val input = "BIOMETRIC_${action}_${workerId}_${timestamp}_${room}_SIVAKASI_SAFETY_AUTH"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }.take(20).uppercase()
    }
}
