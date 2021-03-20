package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class BreathingSpaceEnteredCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);
    private static final List<CaseEvent> EVENTS = Arrays.asList(CaseEvent.BREATHING_SPACE_ENTERED);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationsProperties notificationsProperties;
    private final String breathingSpaceEnteredTemplateID;
    private final BreathingSpaceLetterService breathingSpaceLetterService;
    private final BreathingSpaceEmailService breathingSpaceEmailService;

    @Autowired
    public BreathingSpaceEnteredCallbackHandler(CaseDetailsConverter caseDetailsConverter,
        NotificationsProperties notificationsProperties,
        @Value("${doc_assembly.breathingSpaceEnteredTemplateID}") String breathingSpaceEnteredTemplateID,
        BreathingSpaceLetterService breathingSpaceLetterService,
        BreathingSpaceEmailService breathingSpaceEmailService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationsProperties = notificationsProperties;
        this.breathingSpaceEnteredTemplateID = breathingSpaceEnteredTemplateID;
        this.breathingSpaceLetterService = breathingSpaceLetterService;
        this.breathingSpaceEmailService = breathingSpaceEmailService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.MID, this::breathingSpaceEnteredMidCallBack,
            CallbackType.ABOUT_TO_SUBMIT, this::breathingSpaceEnteredAboutToSubmitCallBack,
            CallbackType.SUBMITTED, this::breathingSpaceEnteredPostOperations
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private CallbackResponse breathingSpaceEnteredMidCallBack(CallbackParams callbackParams) {

        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();

        return responseBuilder
            .data(Map.of("breathingSpaceSummary", ccdBreathingSpace))
            .build();
    }

    private CallbackResponse breathingSpaceEnteredAboutToSubmitCallBack(CallbackParams callbackParams) {

        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        CCDCase updatedCase = ccdCase;

        breathingSpaceEmailService.sendNotificationToClaimant(claim,
            notificationsProperties.getTemplates().getEmail().getBreathingSpaceEmailToClaimant());
        if (isDefendentLinked(claim)) {
            breathingSpaceEmailService.sendEmailNotificationToDefendant(claim,
                notificationsProperties.getTemplates().getEmail().getBreathingSpaceEmailToDefendant());
        } else {
            updatedCase = breathingSpaceLetterService.sendLetterToDefendant(ccdCase, claim, authorisation,
                breathingSpaceEnteredTemplateID);
        }

        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
    }

    private CallbackResponse breathingSpaceEnteredPostOperations(CallbackParams callbackParams) {
        Claim claim = caseDetailsConverter.extractClaim(callbackParams.getRequest().getCaseDetails())
            .toBuilder().lastEventTriggeredForHwfCase(callbackParams.getRequest().getEventId()).build();
        logger.info("Created citizen case for callback of type {}, claim with external id {}",
            callbackParams.getType(),
            claim.getExternalId());

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isDefendentLinked(Claim claim) {
        return !StringUtils.isBlank(claim.getDefendantId());
    }
}