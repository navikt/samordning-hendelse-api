package no.nav.samordning.hendelser.security

import jakarta.servlet.http.HttpServletRequest
import no.nav.pensjonsamhandling.maskinporten.validation.orgno.RequestAwareOrganisationValidator
import no.nav.samordning.hendelser.consumer.TpConfigConsumer
import org.springframework.stereotype.Service

@Service
class TpConfigOrgNoValidator(private val tpConfigConsumer: TpConfigConsumer): RequestAwareOrganisationValidator {
    override fun invoke(orgno: String, o: HttpServletRequest) = tpConfigConsumer.validateOrganisation(
        orgno, o.getParameter("tpnr")
    )!!
}