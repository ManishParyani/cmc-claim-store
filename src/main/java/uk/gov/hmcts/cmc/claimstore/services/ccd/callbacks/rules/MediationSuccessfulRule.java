package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.rules;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MediationSuccessfulRule {
    public static final String STAFF_UPLOAD_MEDIATION_AGREEMENT =
        "Upload Mediation Agreement";
    public static final String STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT =
            "Document needs to be a Mediation Agreement";
    public static final String STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT =
            "Mediation agreement needs to be of type PDF";

    public List<String> validateMediationAgreementUploadedByCaseworker(CCDCase ccdCase) {
        Objects.requireNonNull(ccdCase, "CCD case object can not be null");
        List<CCDCollectionElement<CCDClaimDocument>> staffDocuments = ccdCase.getStaffUploadedDocuments();


        List<String> validationErrors = new ArrayList<>();

        if ((staffDocuments == null)
        ) {
            validationErrors.add(STAFF_UPLOAD_MEDIATION_AGREEMENT);
        } else {
            for (CCDCollectionElement<CCDClaimDocument> document : staffDocuments)
            {
                if ((document.getValue().getDocumentType() == CCDClaimDocumentType.MEDIATION_AGREEMENT) && (FilenameUtils.getExtension(document.getValue().getDocumentLink().getDocumentFileName()).contains("pdf"))
                ) {
                    validationErrors.clear();
                    return validationErrors;
                }
                if ((document.getValue().getDocumentType() == CCDClaimDocumentType.MEDIATION_AGREEMENT) && !(FilenameUtils.getExtension(document.getValue().getDocumentLink().getDocumentFileName()).contains("pdf"))
                ) {
                    if (!validationErrors.contains(STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT)){
                        validationErrors.add(STAFF_UPLOAD_PDF_MEDIATION_AGREEMENT);
                    }
                }
                if ((document.getValue().getDocumentType() != CCDClaimDocumentType.MEDIATION_AGREEMENT)
                ) {
                    if (!validationErrors.contains(STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT)){
                        validationErrors.add(STAFF_UPLOAD_TYPE_MEDIATION_AGREEMENT);
                    }
                }
            }
        }
        return validationErrors;
    }
}
