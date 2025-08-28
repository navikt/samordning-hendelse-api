package no.nav.samordning.hendelser.common.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjonsamhandling.maskinporten.validation.orgno.RequestAwareOrganisationValidator
import no.nav.samordning.hendelser.common.consumer.TpConfigConsumer
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TpConfigOrgNoValidator(
    private val tpConfigConsumer: TpConfigConsumer,
    @Value("\${maskinporten.validation.required:#{null}}") private val requiredOrgNo: String?
) : RequestAwareOrganisationValidator {

    override fun invoke(orgno: String, o: HttpServletRequest) = isPermitted(orgno) && tpConfigConsumer.validateOrganisation(
        orgno, o.getParameter("tpnr")
    )!!

    fun isPermitted(orgNo: String)= requiredOrgNo == null || requiredOrgNo == orgNo
}